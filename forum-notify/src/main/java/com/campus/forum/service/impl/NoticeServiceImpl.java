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
import java.util.List;
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
        List<NoticeVO> voList = noticePage.getRecords().stream()
                .map(notice -> convertToVO(notice, userId))
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public NoticeVO getNoticeDetail(Long noticeId, Long userId) {
        log.info("获取通知详情, noticeId: {}, userId: {}", noticeId, userId);
        
        // 查询通知
        Notice notice = noticeMapper.selectById(noticeId);
        if (notice == null || notice.getDeleteFlag() == 1) {
            throw new BusinessException(404, "通知不存在");
        }
        
        // 增加阅读数
        noticeMapper.incrementReadCount(noticeId);
        
        // 转换为VO
        NoticeVO vo = convertToVO(notice, userId);
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishNotice(NoticeCreateDTO createDTO, Long userId, String userName) {
        log.info("发布通知, title: {}, userId: {}", createDTO.getTitle(), userId);
        
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
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Integer.parseInt(cached);
        }
        
        // 查询已发布的通知总数
        Long totalNoticeCount = noticeMapper.selectCount(
                new LambdaQueryWrapper<Notice>()
                        .eq(Notice::getStatus, 1)
                        .eq(Notice::getDeleteFlag, 0)
        );
        
        // 查询用户已读通知数
        Long readCount = userNoticeMapper.selectCount(
                new LambdaQueryWrapper<UserNotice>()
                        .eq(UserNotice::getUserId, userId)
                        .eq(UserNotice::getIsRead, 1)
                        .eq(UserNotice::getDeleteFlag, 0)
        );
        
        int unreadCount = (int) (totalNoticeCount - readCount);
        unreadCount = Math.max(0, unreadCount);
        
        // 写入缓存
        redisTemplate.opsForValue().set(cacheKey, String.valueOf(unreadCount), Duration.ofHours(1));
        
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
        String cacheKey = REDIS_KEY_UNREAD_COUNT + userId;
        redisTemplate.delete(cacheKey);
        
        log.info("通知已标记为已读, noticeId: {}, userId: {}", noticeId, userId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAllAsRead(Long userId) {
        log.info("全部标记已读, userId: {}", userId);
        
        // 获取所有已发布的通知
        List<Notice> notices = noticeMapper.selectList(
                new LambdaQueryWrapper<Notice>()
                        .eq(Notice::getStatus, 1)
                        .eq(Notice::getDeleteFlag, 0)
                        .select(Notice::getId)
        );
        
        // 批量标记已读
        for (Notice notice : notices) {
            // 查询是否已有记录
            UserNotice userNotice = userNoticeMapper.selectOne(
                    new LambdaQueryWrapper<UserNotice>()
                            .eq(UserNotice::getUserId, userId)
                            .eq(UserNotice::getNoticeId, notice.getId())
            );
            
            if (userNotice != null) {
                if (userNotice.getIsRead() != 1) {
                    userNotice.setIsRead(1);
                    userNotice.setReadTime(LocalDateTime.now());
                    userNoticeMapper.updateById(userNotice);
                }
            } else {
                userNotice = new UserNotice();
                userNotice.setUserId(userId);
                userNotice.setNoticeId(notice.getId());
                userNotice.setIsRead(1);
                userNotice.setReadTime(LocalDateTime.now());
                userNotice.setIsDeleted(0);
                userNoticeMapper.insert(userNotice);
            }
        }
        
        // 清除未读数缓存，设置为0
        String cacheKey = REDIS_KEY_UNREAD_COUNT + userId;
        redisTemplate.opsForValue().set(cacheKey, "0", Duration.ofHours(1));
        
        log.info("全部通知已标记为已读, userId: {}", userId);
        return true;
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为VO
     */
    private NoticeVO convertToVO(Notice notice, Long userId) {
        NoticeVO vo = new NoticeVO();
        BeanUtils.copyProperties(notice, vo);
        vo.setIsTop(notice.getIsTop() != null && notice.getIsTop() == 1);
        
        // 查询用户是否已读
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
}
