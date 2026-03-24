package com.campus.forum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.dto.CommentCreateDTO;
import com.campus.forum.dto.CommentQueryDTO;
import com.campus.forum.entity.Comment;
import com.campus.forum.vo.CommentVO;

import java.util.List;

/**
 * 评论服务接口
 * 
 * 提供评论相关的业务操作
 * 
 * @author campus
 * @since 2024-01-01
 */
public interface CommentService {

    /**
     * 获取帖子的评论列表
     * 
     * @param postId 帖子ID
     * @param queryDTO 查询参数
     * @param currentUserId 当前用户ID（可为空）
     * @return 评论分页列表
     */
    IPage<CommentVO> getCommentsByPostId(Long postId, CommentQueryDTO queryDTO, Long currentUserId);

    /**
     * 发布评论
     * 
     * @param createDTO 评论创建DTO
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @return 评论ID
     */
    Long publishComment(CommentCreateDTO createDTO, Long userId, String ipAddress);

    /**
     * 删除评论
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteComment(Long commentId, Long userId);

    /**
     * 点赞评论
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否成功（true-点赞成功，false-取消点赞）
     */
    boolean likeComment(Long commentId, Long userId);

    /**
     * 获取评论的回复列表
     * 
     * @param commentId 评论ID
     * @param page 页码
     * @param size 每页大小
     * @param currentUserId 当前用户ID
     * @return 回复分页列表
     */
    IPage<CommentVO> getReplies(Long commentId, Integer page, Integer size, Long currentUserId);

    /**
     * 获取评论详情
     * 
     * @param commentId 评论ID
     * @param currentUserId 当前用户ID
     * @return 评论详情
     */
    CommentVO getCommentDetail(Long commentId, Long currentUserId);

    /**
     * 统计帖子评论数
     * 
     * @param postId 帖子ID
     * @return 评论数
     */
    int countByPostId(Long postId);

    /**
     * 批量获取帖子的评论数
     * 
     * @param postIds 帖子ID列表
     * @return 帖子ID与评论数的映射
     */
    java.util.Map<Long, Integer> countByPostIds(List<Long> postIds);

    /**
     * 检查用户是否已点赞评论
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    boolean isLiked(Long commentId, Long userId);

    /**
     * 获取用户的评论列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @param currentUserId 当前用户ID
     * @return 评论分页列表
     */
    IPage<CommentVO> getCommentsByUserId(Long userId, Integer page, Integer size, Long currentUserId);

    /**
     * 审核评论
     * 
     * @param commentId 评论ID
     * @param status 审核状态（1-通过，2-驳回）
     * @param remark 审核备注
     * @param auditorId 审核人ID
     * @return 是否成功
     */
    boolean auditComment(Long commentId, Integer status, String remark, Long auditorId);
}
