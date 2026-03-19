package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.dto.UserBanDTO;
import com.campus.forum.entity.UserBan;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.UserBanMapper;
import com.campus.forum.service.UserBanService;
import com.campus.forum.vo.UserBanVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 用户禁言服务实现类
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserBanServiceImpl extends ServiceImpl<UserBanMapper, UserBan> implements UserBanService {

    private final UserBanMapper userBanMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long banUser(UserBanDTO banDTO, Long operatorId) {
        Long userId = banDTO.getUserId();
        
        // 检查是否已被禁言
        UserBanVO activeBan = userBanMapper.selectActiveBan(userId);
        if (activeBan != null) {
            throw new BusinessException("该用户已被禁言");
        }

        UserBan userBan = new UserBan();
        userBan.setUserId(userId);
        userBan.setBanType(banDTO.getBanType() != null ? banDTO.getBanType() : 1); // 默认全站禁言
        userBan.setForumId(banDTO.getForumId());
        userBan.setReason(banDTO.getReason());
        userBan.setReportId(banDTO.getReportId());
        userBan.setOperatorId(operatorId);
        userBan.setStartTime(LocalDateTime.now());
        userBan.setEndTime(LocalDateTime.now().plusDays(banDTO.getBanDays()));
        userBan.setStatus(1); // 禁言中
        userBan.setDeleteFlag(0);
        
        save(userBan);
        
        log.info("用户 {} 被禁言 {} 天，禁言ID: {}", userId, banDTO.getBanDays(), userBan.getId());
        return userBan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unbanUser(Long userId, Long operatorId, String reason) {
        UserBanVO activeBan = userBanMapper.selectActiveBan(userId);
        if (activeBan == null) {
            throw new BusinessException("该用户未被禁言");
        }

        int result = userBanMapper.releaseBan(activeBan.getId(), operatorId, reason);
        
        if (result > 0) {
            log.info("用户 {} 解除禁言，操作人: {}", userId, operatorId);
            return true;
        }
        
        return false;
    }

    @Override
    public IPage<UserBanVO> getBanPage(Long userId, Integer status, Integer current, Integer size) {
        Page<UserBanVO> page = new Page<>(current, size);
        
        IPage<UserBanVO> result = userBanMapper.selectBanPage(page, userId, status, null);
        
        // 填充名称和计算剩余时间
        result.getRecords().forEach(this::fillNames);
        
        return result;
    }

    @Override
    public UserBanVO getActiveBan(Long userId) {
        // 先更新过期的禁言记录
        userBanMapper.expireBans();
        
        UserBanVO banVO = userBanMapper.selectActiveBan(userId);
        if (banVO != null) {
            fillNames(banVO);
            
            // 计算剩余禁言时间
            if (banVO.getEndTime() != null) {
                long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), banVO.getEndTime());
                banVO.setRemainingHours(Math.max(0, hours));
            }
        }
        
        return banVO;
    }

    @Override
    public boolean isBanned(Long userId) {
        UserBanVO activeBan = getActiveBan(userId);
        return activeBan != null;
    }

    @Override
    public List<UserBanVO> getBanHistory(Long userId) {
        List<UserBanVO> history = userBanMapper.selectBanHistory(userId);
        history.forEach(this::fillNames);
        return history;
    }

    /**
     * 填充名称
     */
    private void fillNames(UserBanVO vo) {
        // 禁言类型名称
        if (vo.getBanType() != null) {
            String[] typeNames = {"", "全站禁言", "板块禁言"};
            vo.setBanTypeName(typeNames[vo.getBanType()]);
        }
        
        // 禁言状态名称
        if (vo.getStatus() != null) {
            String[] statusNames = {"已解除", "禁言中", "已过期"};
            vo.setStatusName(statusNames[vo.getStatus()]);
        }
    }
}
