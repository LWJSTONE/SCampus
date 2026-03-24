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

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private final StringRedisTemplate stringRedisTemplate;
    
    /**
     * 禁言操作分布式锁key前缀
     */
    private static final String BAN_LOCK_KEY_PREFIX = "ban:lock:user:";
    /**
     * 分布式锁超时时间（秒）
     */
    private static final long LOCK_TIMEOUT_SECONDS = 10;

    // 禁言类型映射
    private static final Map<Integer, String> BAN_TYPE_NAMES = new HashMap<>();
    static {
        BAN_TYPE_NAMES.put(1, "全站禁言");
        BAN_TYPE_NAMES.put(2, "板块禁言");
    }

    // 禁言状态映射
    private static final Map<Integer, String> BAN_STATUS_NAMES = new HashMap<>();
    static {
        BAN_STATUS_NAMES.put(0, "已解除");
        BAN_STATUS_NAMES.put(1, "禁言中");
        BAN_STATUS_NAMES.put(2, "已过期");
    }

    // 最小禁言天数
    private static final int MIN_BAN_DAYS = 1;
    // 最大禁言天数（365天）
    private static final int MAX_BAN_DAYS = 365;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long banUser(UserBanDTO banDTO, Long operatorId) {
        Long userId = banDTO.getUserId();

        // 参数验证
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 验证禁言天数
        Integer banDays = banDTO.getBanDays();
        if (banDays == null || banDays < MIN_BAN_DAYS) {
            throw new BusinessException("禁言天数不能小于" + MIN_BAN_DAYS + "天");
        }
        if (banDays > MAX_BAN_DAYS) {
            throw new BusinessException("禁言天数不能超过" + MAX_BAN_DAYS + "天");
        }

        // 验证禁言类型
        Integer banType = banDTO.getBanType();
        if (banType == null) {
            banType = 1; // 默认全站禁言
        }
        if (!BAN_TYPE_NAMES.containsKey(banType)) {
            throw new BusinessException("无效的禁言类型");
        }

        // 板块禁言时必须指定板块ID
        if (banType == 2 && banDTO.getForumId() == null) {
            throw new BusinessException("板块禁言必须指定板块ID");
        }
        
        // 【并发安全修复】使用分布式锁防止并发创建多条禁言记录
        String lockKey = BAN_LOCK_KEY_PREFIX + userId;
        boolean locked = false;
        try {
            // 尝试获取分布式锁
            locked = Boolean.TRUE.equals(stringRedisTemplate.opsForValue()
                    .setIfAbsent(lockKey, String.valueOf(operatorId), LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS));
            
            if (!locked) {
                log.warn("获取禁言锁失败，用户 {} 可能正在被其他管理员处理", userId);
                throw new BusinessException("该用户正在被其他管理员处理，请稍后再试");
            }
            
            // 检查是否已被禁言（在锁保护下进行）
            UserBanVO activeBan = userBanMapper.selectActiveBan(userId);
            if (activeBan != null) {
                throw new BusinessException("该用户已被禁言");
            }

            UserBan userBan = new UserBan();
            userBan.setUserId(userId);
            userBan.setBanType(banType);
            userBan.setForumId(banDTO.getForumId());
            userBan.setReason(banDTO.getReason());
            userBan.setReportId(banDTO.getReportId());
            userBan.setOperatorId(operatorId);
            userBan.setStartTime(LocalDateTime.now());
            userBan.setEndTime(LocalDateTime.now().plusDays(banDays));
            userBan.setStatus(1); // 禁言中
            userBan.setDeleteFlag(0);
            
            save(userBan);
            
            log.info("用户 {} 被禁言 {} 天，禁言ID: {}", userId, banDays, userBan.getId());
            return userBan.getId();
        } finally {
            // 释放锁（只释放自己持有的锁）
            if (locked) {
                String lockValue = stringRedisTemplate.opsForValue().get(lockKey);
                if (String.valueOf(operatorId).equals(lockValue)) {
                    stringRedisTemplate.delete(lockKey);
                }
            }
        }
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
     * 填充名称（使用Map避免数组越界）
     */
    private void fillNames(UserBanVO vo) {
        // 禁言类型名称
        if (vo.getBanType() != null) {
            vo.setBanTypeName(BAN_TYPE_NAMES.getOrDefault(vo.getBanType(), "未知类型"));
        }
        
        // 禁言状态名称
        if (vo.getStatus() != null) {
            vo.setStatusName(BAN_STATUS_NAMES.getOrDefault(vo.getStatus(), "未知状态"));
        }
    }
}
