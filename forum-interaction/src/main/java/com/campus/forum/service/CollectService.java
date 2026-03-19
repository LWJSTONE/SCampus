package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.vo.CollectVO;

/**
 * 收藏服务接口
 * 
 * @author campus
 * @since 2024-01-01
 */
public interface CollectService {

    /**
     * 收藏/取消收藏
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     * @param folderId 收藏夹ID（可选）
     * @return true-收藏成功，false-取消收藏
     */
    boolean collect(Long postId, Long userId, Long folderId);

    /**
     * 检查是否已收藏
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否已收藏
     */
    boolean isCollected(Long postId, Long userId);

    /**
     * 获取收藏数量
     * 
     * @param postId 帖子ID
     * @return 收藏数
     */
    int getCollectCount(Long postId);

    /**
     * 获取用户收藏列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 收藏列表
     */
    Page<CollectVO> getCollectList(Long userId, Integer page, Integer size);
}
