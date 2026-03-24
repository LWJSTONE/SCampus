package com.campus.forum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.api.UserServiceClient;
import com.campus.forum.dto.ModeratorDTO;
import com.campus.forum.entity.Forum;
import com.campus.forum.entity.Moderator;
import com.campus.forum.entity.Result;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.ForumMapper;
import com.campus.forum.mapper.ModeratorMapper;
import com.campus.forum.service.ModeratorService;
import com.campus.forum.vo.ModeratorVO;
import com.campus.forum.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 版主服务实现类
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModeratorServiceImpl extends ServiceImpl<ModeratorMapper, Moderator> implements ModeratorService {

    private final ModeratorMapper moderatorMapper;
    private final ForumMapper forumMapper;
    private final UserServiceClient userServiceClient;

    /**
     * 内部服务密钥，用于验证来自网关的内部请求
     * 【安全修复】通过用户服务获取真实用户信息，防止前端数据篡改
     */
    @Value("${service.internal.secret-key:}")
    private String internalSecretKey;

    @Override
    public List<ModeratorVO> getModerators(Long forumId) {
        log.info("获取版块版主列表：{}", forumId);
        List<Moderator> moderators = moderatorMapper.selectByForumId(forumId);
        return moderators.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Moderator addModerator(Long forumId, ModeratorDTO dto) {
        log.info("添加版主：版块={}，用户={}", forumId, dto.getUserId());

        // 检查版块是否存在
        Forum forum = forumMapper.selectById(forumId);
        if (forum == null) {
            throw new BusinessException("版块不存在");
        }

        // 检查是否已是版主
        Moderator existModerator = moderatorMapper.selectByForumIdAndUserId(forumId, dto.getUserId());
        if (existModerator != null) {
            throw new BusinessException("该用户已是版主");
        }

        Moderator moderator = new Moderator();
        moderator.setForumId(forumId);
        moderator.setUserId(dto.getUserId());

        // 【安全修复】从用户服务获取真实用户信息，防止前端数据篡改
        // 不再信任DTO传入的username、nickname、avatar字段
        try {
            Result<UserVO> userResult = userServiceClient.getUserInfo(dto.getUserId(), internalSecretKey);
            if (userResult == null || userResult.getData() == null) {
                throw new BusinessException("用户不存在");
            }
            UserVO user = userResult.getData();
            
            // 验证用户状态
            if (user.getStatus() == null || user.getStatus() != 1) {
                throw new BusinessException("该用户已被禁用，无法设为版主");
            }
            
            // 使用从用户服务获取的真实信息
            moderator.setUsername(user.getUsername());
            moderator.setNickname(user.getNickname());
            moderator.setAvatar(user.getAvatar());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户信息失败，userId: {}", dto.getUserId(), e);
            // 如果用户服务不可用，记录警告但继续处理（兼容性考虑）
            if (!StringUtils.hasText(internalSecretKey)) {
                log.warn("内部服务密钥未配置，使用DTO传入的用户信息（存在安全风险）");
                moderator.setUsername(dto.getUsername());
                moderator.setNickname(dto.getNickname());
                moderator.setAvatar(dto.getAvatar());
            } else {
                throw new BusinessException("获取用户信息失败，请稍后重试");
            }
        }
        
        moderator.setIsPrimary(dto.getIsPrimary() != null ? dto.getIsPrimary() : false);
        moderator.setStatus(1);

        // 如果设置为主版主，先清除其他主版主
        if (Boolean.TRUE.equals(moderator.getIsPrimary())) {
            moderatorMapper.clearPrimaryModerator(forumId);
        }

        save(moderator);

        // 更新版块的主版主ID
        if (Boolean.TRUE.equals(moderator.getIsPrimary())) {
            forum.setModeratorId(dto.getUserId());
            forumMapper.updateById(forum);
        }

        return moderator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeModerator(Long forumId, Long userId) {
        log.info("移除版主：版块={}，用户={}", forumId, userId);

        Moderator moderator = moderatorMapper.selectByForumIdAndUserId(forumId, userId);
        if (moderator == null) {
            throw new BusinessException("该用户不是版主");
        }

        // 如果移除的是主版主，清除版块的主版主ID
        if (Boolean.TRUE.equals(moderator.getIsPrimary())) {
            Forum forum = forumMapper.selectById(forumId);
            if (forum != null) {
                forum.setModeratorId(null);
                forumMapper.updateById(forum);
            }
        }

        return removeById(moderator.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setPrimaryModerator(Long forumId, Long userId) {
        log.info("设置主版主：版块={}，用户={}", forumId, userId);

        Moderator moderator = moderatorMapper.selectByForumIdAndUserId(forumId, userId);
        if (moderator == null) {
            throw new BusinessException("该用户不是版主");
        }

        // 清除原主版主
        moderatorMapper.clearPrimaryModerator(forumId);

        // 设置新主版主
        moderatorMapper.setPrimaryModerator(forumId, userId);

        // 更新版块的主版主ID
        Forum forum = forumMapper.selectById(forumId);
        if (forum != null) {
            forum.setModeratorId(userId);
            forumMapper.updateById(forum);
        }

        return true;
    }

    @Override
    public boolean isModerator(Long forumId, Long userId) {
        if (forumId == null || userId == null) {
            return false;
        }
        Moderator moderator = moderatorMapper.selectByForumIdAndUserId(forumId, userId);
        return moderator != null && moderator.getStatus() == 1;
    }

    @Override
    public List<Long> getManagedForumIds(Long userId) {
        log.info("获取用户管理的版块列表：{}", userId);
        List<Moderator> moderators = moderatorMapper.selectByUserId(userId);
        return moderators.stream()
                .filter(m -> m.getStatus() == 1)
                .map(Moderator::getForumId)
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO
     */
    private ModeratorVO convertToVO(Moderator moderator) {
        ModeratorVO vo = new ModeratorVO();
        BeanUtil.copyProperties(moderator, vo);
        return vo;
    }
}
