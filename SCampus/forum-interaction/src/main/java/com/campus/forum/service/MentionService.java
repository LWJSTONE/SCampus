package com.campus.forum.service;

import com.campus.forum.entity.Mention;

import java.util.List;

/**
 * @提及服务接口
 * 
 * @author campus
 * @since 2024-01-01
 */
public interface MentionService {

    /**
     * 创建提及
     * 
     * @param sourceType 来源类型（1-帖子 2-评论）
     * @param sourceId 来源ID
     * @param userId 被提及用户ID
     * @param fromUserId 发起提及用户ID
     * @param content 提及内容
     * @return 提及ID
     */
    Long createMention(Integer sourceType, Long sourceId, Long userId, Long fromUserId, String content);

    /**
     * 获取用户的未读提及数
     * 
     * @param userId 用户ID
     * @return 未读数
     */
    int getUnreadCount(Long userId);

    /**
     * 获取用户的提及列表
     * 
     * @param userId 用户ID
     * @return 提及列表
     */
    List<Mention> getMentionList(Long userId);

    /**
     * 标记为已读
     * 
     * @param id 提及ID
     * @return 是否成功
     */
    boolean markAsRead(Long id);

    /**
     * 标记所有为已读
     * 
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllAsRead(Long userId);
}
