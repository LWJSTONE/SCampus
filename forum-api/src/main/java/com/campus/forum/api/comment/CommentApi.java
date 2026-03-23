package com.campus.forum.api.comment;

import com.campus.forum.entity.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Comment Service Feign Client
 *
 * @author campus
 */
@FeignClient(name = "forum-comment", contextId = "commentApi", url = "${feign.comment.url:http://localhost:9005}", fallback = CommentApiFallback.class)
public interface CommentApi {

    /**
     * Get comment by ID
     *
     * @param id Comment ID
     * @return Comment info
     */
    @GetMapping("/api/internal/comment/{id}")
    Result<CommentDTO> getCommentById(@PathVariable("id") Long id);

    /**
     * Delete comment by ID (logical delete)
     * 
     * @param id Comment ID
     * @return Result
     */
    @DeleteMapping("/api/internal/comment/{id}")
    Result<Boolean> deleteComment(@PathVariable("id") Long id);

    /**
     * Get comments by post ID
     *
     * @param postId Post ID
     * @param page   Page number
     * @param size   Page size
     * @return Comment list
     */
    @GetMapping("/api/internal/comment/post/{postId}")
    Result<Page<CommentDTO>> getCommentsByPostId(
            @PathVariable("postId") Long postId,
            @RequestParam("page") int page,
            @RequestParam("size") int size);

    /**
     * Get comment count by post ID
     *
     * @param postId Post ID
     * @return Comment count
     */
    @GetMapping("/api/internal/comment/post/{postId}/count")
    Result<Integer> getCommentCountByPostId(@PathVariable("postId") Long postId);
}
