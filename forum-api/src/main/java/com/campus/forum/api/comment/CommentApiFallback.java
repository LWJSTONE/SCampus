package com.campus.forum.api.comment;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 评论服务Feign客户端回退处理类
 *
 * @author campus
 */
@Slf4j
@Component
public class CommentApiFallback implements CommentApi {

    @Override
    public Result<CommentDTO> getCommentById(Long id, String serviceKey) {
        log.error("调用评论服务失败，获取评论详情，评论ID: {}", id);
        return Result.fail("评论服务不可用，请稍后重试");
    }

    @Override
    public Result<Page<CommentDTO>> getCommentsByPostId(Long postId, int page, int size, String serviceKey) {
        log.error("调用评论服务失败，获取帖子评论列表，帖子ID: {}, 页码: {}, 每页大小: {}", postId, page, size);
        return Result.fail("评论服务不可用，请稍后重试");
    }

    @Override
    public Result<Integer> getCommentCountByPostId(Long postId, String serviceKey) {
        log.error("调用评论服务失败，获取帖子评论数，帖子ID: {}", postId);
        return Result.fail("评论服务不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> deleteComment(Long id, String serviceKey) {
        log.error("调用评论服务失败，删除评论，评论ID: {}", id);
        return Result.fail("评论服务不可用，请稍后重试");
    }

    @Override
    public Result<Page<CommentDTO>> getCommentsByUserId(Long userId, int page, int size, String serviceKey) {
        log.error("调用评论服务失败，获取用户评论列表，用户ID: {}, 页码: {}, 每页大小: {}", userId, page, size);
        return Result.fail("评论服务不可用，请稍后重试");
    }
}
