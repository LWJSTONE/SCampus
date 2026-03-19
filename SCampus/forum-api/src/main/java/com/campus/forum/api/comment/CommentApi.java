package com.campus.forum.api.comment;

import com.campus.forum.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 评论服务Feign客户端接口
 *
 * @author campus
 */
@FeignClient(name = "forum-comment", fallback = CommentApiFallback.class)
public interface CommentApi {

    /**
     * 获取帖子评论列表
     *
     * @param postId 帖子ID
     * @param page   页码
     * @param size   每页大小
     * @return 评论列表
     */
    @GetMapping("/api/comment/post/{postId}")
    R<com.baomidou.mybatisplus.extension.plugins.pagination.Page<CommentDTO>> getCommentsByPostId(
            @PathVariable("postId") Long postId,
            @RequestParam("page") int page,
            @RequestParam("size") int size);

    /**
     * 获取帖子评论数
     *
     * @param postId 帖子ID
     * @return 评论数
     */
    @GetMapping("/api/comment/post/{postId}/count")
    R<Integer> getCommentCountByPostId(@PathVariable("postId") Long postId);
}
