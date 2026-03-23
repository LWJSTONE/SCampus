package com.campus.forum.service.impl;

import com.campus.forum.entity.Mention;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.MentionMapper;
import com.campus.forum.service.MentionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @提及服务实现类
 * 
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MentionServiceImpl implements MentionService {

    private final MentionMapper mentionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMention(Integer targetType, Long targetId, Long toUserId, Long fromUserId, String content) {
        // 参数验证
        if (targetType == null || (targetType != 1 && targetType != 2)) {
            throw new BusinessException("目标类型无效，必须为1(帖子)或2(评论)");
        }
        if (targetId == null || targetId <= 0) {
            throw new BusinessException("目标ID无效");
        }
        if (toUserId == null || toUserId <= 0) {
            throw new BusinessException("被提及用户ID无效");
        }
        if (fromUserId == null || fromUserId <= 0) {
            throw new BusinessException("发起用户ID无效");
        }
        
        // 防止自己@自己
        if (toUserId.equals(fromUserId)) {
            log.debug("用户不能@自己，跳过创建提及: userId={}", fromUserId);
            return null;
        }
        
        // 防止重复提及（同一用户在同一目标中被重复@）
        int existingCount = mentionMapper.countExistingMention(fromUserId, toUserId, targetType, targetId);
        if (existingCount > 0) {
            log.debug("已存在相同的提及记录，跳过创建: fromUserId={}, toUserId={}, targetType={}, targetId={}", 
                    fromUserId, toUserId, targetType, targetId);
            return null;
        }
        
        Mention mention = new Mention();
        mention.setTargetType(targetType);
        mention.setTargetId(targetId);
        mention.setToUserId(toUserId);
        mention.setFromUserId(fromUserId);
        mention.setIsRead(0);
        
        mentionMapper.insert(mention);
        
        log.info("创建@提及: targetType={}, targetId={}, toUserId={}, fromUserId={}", 
                targetType, targetId, toUserId, fromUserId);
        
        return mention.getId();
    }

    @Override
    public int getUnreadCount(Long userId) {
        if (userId == null || userId <= 0) {
            return 0;
        }
        return mentionMapper.countUnread(userId);
    }

    @Override
    public List<Mention> getMentionList(Long userId) {
        if (userId == null || userId <= 0) {
            return List.of();
        }
        return mentionMapper.selectByUserId(userId);
    }

    @Override
    public boolean markAsRead(Long id, Long userId) {
        if (id == null || userId == null) {
            return false;
        }
        // 通过在SQL中添加userId条件实现归属权验证
        // 如果userId不匹配，更新将影响0行，返回false
        return mentionMapper.markAsRead(id, userId) > 0;
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        if (userId == null || userId <= 0) {
            return false;
        }
        return mentionMapper.markAllAsRead(userId) > 0;
    }
}
