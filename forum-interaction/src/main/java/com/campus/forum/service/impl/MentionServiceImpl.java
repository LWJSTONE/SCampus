package com.campus.forum.service.impl;

import com.campus.forum.entity.Mention;
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
    public Long createMention(Integer sourceType, Long sourceId, Long userId, Long fromUserId, String content) {
        Mention mention = new Mention();
        mention.setSourceType(sourceType);
        mention.setSourceId(sourceId);
        mention.setUserId(userId);
        mention.setFromUserId(fromUserId);
        mention.setContent(content);
        mention.setIsRead(0);
        
        mentionMapper.insert(mention);
        
        log.info("创建@提及: sourceType={}, sourceId={}, userId={}, fromUserId={}", 
                sourceType, sourceId, userId, fromUserId);
        
        return mention.getId();
    }

    @Override
    public int getUnreadCount(Long userId) {
        return mentionMapper.countUnread(userId);
    }

    @Override
    public List<Mention> getMentionList(Long userId) {
        return mentionMapper.selectByUserId(userId);
    }

    @Override
    public boolean markAsRead(Long id, Long userId) {
        // 通过在SQL中添加userId条件实现归属权验证
        // 如果userId不匹配，更新将影响0行，返回false
        return mentionMapper.markAsRead(id, userId) > 0;
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        return mentionMapper.markAllAsRead(userId) > 0;
    }
}
