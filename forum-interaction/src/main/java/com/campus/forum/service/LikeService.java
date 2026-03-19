package com.campus.forum.service;

/**
 * 点赞服务接口
 * 
 * @author campus
 * @since 2024-01-01
 */
public interface LikeService {

    /**
     * 点赞/取消点赞
     * 
     * @param targetType 目标类型（1-帖子 2-评论）
     * @param targetId 目标ID
     * @param userId 用户ID
     * @return true-点赞成功，false-取消点赞
     */
    boolean like(Integer targetType, Long targetId, Long userId);

    /**
     * 检查是否已点赞
     * 
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    boolean isLiked(Integer targetType, Long targetId, Long userId);

    /**
     * 获取点赞数量
     * 
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 点赞数
     */
    int getLikeCount(Integer targetType, Long targetId);
}
