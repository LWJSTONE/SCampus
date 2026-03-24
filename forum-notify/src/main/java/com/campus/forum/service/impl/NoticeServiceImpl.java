package com.campus.forum.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.dto.NoticeCreateDTO;
import com.campus.forum.dto.NoticeQueryDTO;
import com.campus.forum.dto.NoticeUpdateDTO;
import com.campus.forum.entity.Notice;
import com.campus.forum.entity.UserNotice;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.NoticeMapper;
import com.campus.forum.mapper.UserNoticeMapper;
import com.campus.forum.service.NoticeService;
import com.campus.forum.vo.NoticeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 * 
 * 实现通知相关的业务逻辑
 * 
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;
    private final UserNoticeMapper userNoticeMapper;
    private final StringRedisTemplate redisTemplate;

    // Redis Key前缀
    private static final String REDIS_KEY_UNREAD_COUNT = "notify:unread:";
    private static final String REDIS_KEY_NOTICE_READ = "notify:read:";

    @Override
    public Page<NoticeVO> getNoticeList(NoticeQueryDTO queryDTO, Long userId) {
        log.info("获取通知列表, queryDTO: {}, userId: {}", queryDTO, userId);
        
        // 构建查询条件
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getDeleteFlag, 0);
        
        // 只查询已发布的通知
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Notice::getStatus, queryDTO.getStatus());
        } else {
            wrapper.eq(Notice::getStatus, 1);
        }
        
        // 类型筛选
        if (queryDTO.getType() != null) {
            wrapper.eq(Notice::getType, queryDTO.getType());
        }
        
        // 级别筛选
        if (queryDTO.getLevel() != null) {
            wrapper.eq(Notice::getLevel, queryDTO.getLevel());
        }
        
        // 置顶筛选
        if (queryDTO.getIsTop() != null) {
            wrapper.eq(Notice::getIsTop, queryDTO.getIsTop());
        }
        
        // 关键词搜索
        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            wrapper.and(w -> w
                    .like(Notice::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(Notice::getContent, queryDTO.getKeyword())
            );
        }
        
        // 排序：置顶优先，然后按发布时间倒序
        wrapper.orderByDesc(Notice::getIsTop)
               .orderByDesc(Notice::getPublishTime);
        
        // 分页查询
        Page<Notice> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Notice> noticePage = noticeMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<NoticeVO> voPage = new Page<>(noticePage.getCurrent(), noticePage.getSize(), noticePage.getTotal());
        
        // 优化：批量查询已读状态，避免 N+1 问题
        Map<Long, UserNotice> userNoticeMap = batchGetUserNoticeStatus(userId, noticePage.getRecords());
        
        List<NoticeVO> voList = noticePage.getRecords().stream()
                .map(notice -> convertToVO(notice, userId, userNoticeMap))
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    /**
     * 批量获取用户通知已读状态
     * 优化 N+1 查询问题
     *
     * @param userId 用户ID
     * @param notices 通知列表
     * @return 通知ID -> 用户通知记录映射
     */
    private Map<Long, UserNotice> batchGetUserNoticeStatus(Long userId, List<Notice> notices) {
        if (userId == null || notices == null || notices.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // 提取通知ID列表
        List<Long> noticeIds = notices.stream()
                .map(Notice::getId)
                .collect(Collectors.toList());
        
        // 批量查询用户通知记录
        List<UserNotice> userNotices = userNoticeMapper.selectList(
                new LambdaQueryWrapper<UserNotice>()
                        .eq(UserNotice::getUserId, userId)
                        .in(UserNotice::getNoticeId, noticeIds)
        );
        
        // 转换为Map
        return userNotices.stream()
                .collect(Collectors.toMap(UserNotice::getNoticeId, un -> un, (a, b) -> a));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoticeVO getNoticeDetail(Long noticeId, Long userId) {
        log.info("获取通知详情, noticeId: {}, userId: {}", noticeId, userId);
        
        // 查询通知
        Notice notice = noticeMapper.selectById(noticeId);
        if (notice == null || notice.getDeleteFlag() == 1) {
            throw new BusinessException(404, "通知不存在");
        }
        
        // 增加阅读数
        noticeMapper.incrementReadCount(noticeId);
        
        // 自动标记已读
        if (userId != null) {
            markAsReadInternal(noticeId, userId);
        }
        
        // 转换为VO
        NoticeVO vo = convertToVO(notice, userId);
        vo.setIsRead(true); // 已标记已读
        
        return vo;
    }
    
    /**
     * 内部标记已读方法，不包含事务注解，供其他方法内部调用
     *
     * @param noticeId 通知ID
     * @param userId 用户ID
     */
    private void markAsReadInternal(Long noticeId, Long userId) {
        // 查询是否已有记录
        UserNotice userNotice = userNoticeMapper.selectOne(
                new LambdaQueryWrapper<UserNotice>()
                        .eq(UserNotice::getUserId, userId)
                        .eq(UserNotice::getNoticeId, noticeId)
        );
        
        if (userNotice != null) {
            // 更新已读状态
            if (userNotice.getIsRead() != 1) {
                userNotice.setIsRead(1);
                userNotice.setReadTime(LocalDateTime.now());
                userNoticeMapper.updateById(userNotice);
            }
        } else {
            // 创建新记录
            userNotice = new UserNotice();
            userNotice.setUserId(userId);
            userNotice.setNoticeId(noticeId);
            userNotice.setIsRead(1);
            userNotice.setReadTime(LocalDateTime.now());
            userNotice.setIsDeleted(0);
            userNoticeMapper.insert(userNotice);
        }
        
        // 清除未读数缓存
        clearUnreadCountCache(userId);
        
        log.info("通知已自动标记为已读, noticeId: {}, userId: {}", noticeId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishNotice(NoticeCreateDTO createDTO, Long userId, String userName) {
        log.info("发布通知, title: {}, userId: {}", createDTO.getTitle(), userId);
        
        // 【修复】参数前置校验
        if (StrUtil.isBlank(createDTO.getTitle())) {
            throw new BusinessException(400, "通知标题不能为空");
        }
        if (createDTO.getTitle().length() > 100) {
            throw new BusinessException(400, "通知标题不能超过100个字符");
        }
        if (StrUtil.isBlank(createDTO.getContent())) {
            throw new BusinessException(400, "通知内容不能为空");
        }
        // 校验生效时间范围
        if (createDTO.getEffectiveStartTime() != null && createDTO.getEffectiveEndTime() != null) {
            if (createDTO.getEffectiveStartTime().isAfter(createDTO.getEffectiveEndTime())) {
                throw new BusinessException(400, "生效开始时间不能晚于结束时间");
            }
        }
        
        // 构建通知实体
        Notice notice = new Notice();
        BeanUtils.copyProperties(createDTO, notice);
        notice.setStatus(1); // 已发布
        notice.setPublisherId(userId);
        notice.setPublisherName(userName);
        notice.setPublishTime(LocalDateTime.now());
        notice.setReadCount(0);
        
        // 保存通知
        noticeMapper.insert(notice);
        
        // 清除所有用户的未读数缓存
        // 由于无法获取所有用户ID，使用模式匹配清除所有未读数缓存
        clearAllUnreadCountCache();
        
        log.info("通知发布成功, noticeId: {}", notice.getId());
        return notice.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNotice(Long noticeId, NoticeUpdateDTO updateDTO, Long userId) {
        log.info("更新通知, noticeId: {}, userId: {}", noticeId, userId);
        
        // 查询通知
        Notice notice = noticeMapper.selectById(noticeId);
        if (notice == null || notice.getDeleteFlag() == 1) {
            throw new BusinessException(404, "通知不存在");
        }
        
        // 更新字段
        if (StrUtil.isNotBlank(updateDTO.getTitle())) {
            notice.setTitle(updateDTO.getTitle());
        }
        if (StrUtil.isNotBlank(updateDTO.getContent())) {
            notice.setContent(updateDTO.getContent());
        }
        if (updateDTO.getType() != null) {
            notice.setType(updateDTO.getType());
        }
        if (updateDTO.getLevel() != null) {
            notice.setLevel(updateDTO.getLevel());
        }
        if (updateDTO.getIsTop() != null) {
            notice.setIsTop(updateDTO.getIsTop());
        }
        if (updateDTO.getEffectiveStartTime() != null) {
            notice.setEffectiveStartTime(updateDTO.getEffectiveStartTime());
        }
        if (updateDTO.getEffectiveEndTime() != null) {
            notice.setEffectiveEndTime(updateDTO.getEffectiveEndTime());
        }
        if (updateDTO.getAttachments() != null) {
            notice.setAttachments(updateDTO.getAttachments());
        }
        if (updateDTO.getRemark() != null) {
            notice.setRemark(updateDTO.getRemark());
        }
        
        // 保存更新
        noticeMapper.updateById(notice);
        
        log.info("通知更新成功, noticeId: {}", noticeId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNotice(Long noticeId, Long userId) {
        log.info("删除通知, noticeId: {}, userId: {}", noticeId, userId);
        
        // 查询通知
        Notice notice = noticeMapper.selectById(noticeId);
        if (notice == null || notice.getDeleteFlag() == 1) {
            throw new BusinessException(404, "通知不存在");
        }
        
        // 逻辑删除
        notice.setDeleteFlag(1);
        noticeMapper.updateById(notice);
        
        log.info("通知删除成功, noticeId: {}", noticeId);
        return true;
    }

    @Override
    public int getUnreadCount(Long userId) {
        log.info("获取未读消息数, userId: {}", userId);
        
        // 先从缓存获取
        String cacheKey = REDIS_KEY_UNREAD_COUNT + userId;
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return Integer.parseInt(cached);
            }
        } catch (Exception e) {
            log.warn("Redis读取未读数缓存失败, userId: {}, 将直接查询数据库", userId, e);
            // 降级：直接查询数据库
        }
        
        // 查询已发布但用户未读的通知数量
        // 正确的未读数计算：查询已发布通知中用户未读的数量
        int unreadCount = noticeMapper.countUnreadByUserId(userId);
        unreadCount = Math.max(0, unreadCount);
        
        // 写入缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, String.valueOf(unreadCount), Duration.ofHours(1));
        } catch (Exception e) {
            log.warn("Redis写入未读数缓存失败, userId: {}", userId, e);
            // 缓存写入失败不影响业务，忽略异常
        }
        
        return unreadCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long noticeId, Long userId) {
        log.info("标记通知已读, noticeId: {}, userId: {}", noticeId, userId);
        
        // 检查通知是否存在
        Notice notice = noticeMapper.selectById(noticeId);
        if (notice == null || notice.getDeleteFlag() == 1) {
            throw new BusinessException(404, "通知不存在");
        }
        
        // 查询是否已有记录
        UserNotice userNotice = userNoticeMapper.selectOne(
                new LambdaQueryWrapper<UserNotice>()
                        .eq(UserNotice::getUserId, userId)
                        .eq(UserNotice::getNoticeId, noticeId)
        );
        
        if (userNotice != null) {
            // 更新已读状态
            if (userNotice.getIsRead() != 1) {
                userNotice.setIsRead(1);
                userNotice.setReadTime(LocalDateTime.now());
                userNoticeMapper.updateById(userNotice);
            }
        } else {
            // 创建新记录
            userNotice = new UserNotice();
            userNotice.setUserId(userId);
            userNotice.setNoticeId(noticeId);
            userNotice.setIsRead(1);
            userNotice.setReadTime(LocalDateTime.now());
            userNotice.setIsDeleted(0);
            userNoticeMapper.insert(userNotice);
        }
        
        // 清除未读数缓存
        clearUnreadCountCache(userId);
        
        log.info("通知已标记为已读, noticeId: {}, userId: {}", noticeId, userId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAllAsRead(Long userId) {
        log.info("全部标记已读, userId: {}", userId);
        
        // 使用批量更新SQL语句，避免循环处理每条通知
        // 1. 先批量插入未读通知的记录（对于还没有user_notice记录的通知）
        int insertedCount = userNoticeMapper.batchInsertUnreadNotices(userId);
        log.debug("批量插入未读通知记录数: {}", insertedCount);
        
        // 2. 批量更新所有未读通知为已读状态
        int updatedCount = userNoticeMapper.batchMarkAsRead(userId, LocalDateTime.now());
        log.debug("批量更新已读状态记录数: {}", updatedCount);
        
        // 清除未读数缓存，设置为0
        String cacheKey = REDIS_KEY_UNREAD_COUNT + userId;
        try {
            redisTemplate.opsForValue().set(cacheKey, "0", Duration.ofHours(1));
        } catch (Exception e) {
            log.warn("Redis写入未读数缓存失败, userId: {}", userId, e);
            // 缓存写入失败不影响业务，忽略异常
        }
        
        log.info("全部通知已标记为已读, userId: {}, 更新记录数: {}", userId, updatedCount);
        return true;
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为VO（使用预加载的用户通知状态，避免 N+1 问题）
     *
     * @param notice 通知实体
     * @param userId 用户ID
     * @param userNoticeMap 用户通知状态映射（由批量查询预加载）
     * @return 通知VO
     */
    private NoticeVO convertToVO(Notice notice, Long userId, Map<Long, UserNotice> userNoticeMap) {
        NoticeVO vo = new NoticeVO();
        BeanUtils.copyProperties(notice, vo);
        vo.setIsTop(notice.getIsTop() != null && notice.getIsTop() == 1);
        
        // 使用预加载的用户通知状态
        if (userId != null && userNoticeMap != null) {
            UserNotice userNotice = userNoticeMap.get(notice.getId());
            if (userNotice != null && userNotice.getIsRead() == 1) {
                vo.setIsRead(true);
                vo.setReadTime(userNotice.getReadTime());
            } else {
                vo.setIsRead(false);
            }
        } else {
            vo.setIsRead(false);
        }
        
        return vo;
    }
    
    /**
     * 转换为VO（单条查询使用，用于详情等场景）
     *
     * @param notice 通知实体
     * @param userId 用户ID
     * @return 通知VO
     */
    private NoticeVO convertToVO(Notice notice, Long userId) {
        NoticeVO vo = new NoticeVO();
        BeanUtils.copyProperties(notice, vo);
        vo.setIsTop(notice.getIsTop() != null && notice.getIsTop() == 1);
        
        // 查询用户是否已读（单条查询场景）
        if (userId != null) {
            UserNotice userNotice = userNoticeMapper.selectOne(
                    new LambdaQueryWrapper<UserNotice>()
                            .eq(UserNotice::getUserId, userId)
                            .eq(UserNotice::getNoticeId, notice.getId())
            );
            
            if (userNotice != null && userNotice.getIsRead() == 1) {
                vo.setIsRead(true);
                vo.setReadTime(userNotice.getReadTime());
            } else {
                vo.setIsRead(false);
            }
        } else {
            vo.setIsRead(false);
        }
        
        return vo;
    }
    
    // ==================== Redis缓存辅助方法 ====================
    
    /**
     * 清除单个用户的未读数缓存
     *
     * @param userId 用户ID
     */
    private void clearUnreadCountCache(Long userId) {
        try {
            String cacheKey = REDIS_KEY_UNREAD_COUNT + userId;
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.warn("Redis清除未读数缓存失败, userId: {}", userId, e);
            // 缓存操作失败不影响业务，忽略异常
        }
    }
    
    /**
     * 清除所有用户的未读数缓存
     * 用于发布新通知后使所有缓存失效
     */
    private void clearAllUnreadCountCache() {
        try {
            // 使用scan命令遍历并删除所有匹配的key，避免使用keys命令阻塞Redis
            String pattern = REDIS_KEY_UNREAD_COUNT + "*";
            java.util.Set<String> keys = redisTemplate.execute(
                    (org.springframework.data.redis.connection.RedisCallback<java.util.Set<String>>) connection -> {
                        java.util.Set<String> matchedKeys = new java.util.HashSet<>();
                        org.springframework.data.redis.core.Cursor<byte[]> cursor = connection.scan(
                                org.springframework.data.redis.core.ScanOptions.scanOptions()
                                        .match(pattern)
                                        .count(1000)
                                        .build()
                        );
                        while (cursor.hasNext()) {
                            matchedKeys.add(new String(cursor.next()));
                        }
                        return matchedKeys;
                    }
            );
            
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已清除所有用户未读数缓存, 数量: {}", keys.size());
            }
        } catch (Exception e) {
            log.warn("Redis清除所有未读数缓存失败", e);
            // 缓存操作失败不影响业务，忽略异常
        }
    }
}
