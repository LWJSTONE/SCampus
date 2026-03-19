package com.campus.forum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.dto.ModeratorDTO;
import com.campus.forum.entity.Forum;
import com.campus.forum.entity.Moderator;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.ForumMapper;
import com.campus.forum.mapper.ModeratorMapper;
import com.campus.forum.service.ModeratorService;
import com.campus.forum.vo.ModeratorVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        moderator.setUsername(dto.getUsername());
        moderator.setNickname(dto.getNickname());
        moderator.setAvatar(dto.getAvatar());
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
