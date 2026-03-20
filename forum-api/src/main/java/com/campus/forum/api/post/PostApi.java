package com.campus.forum.api.post;

import com.campus.forum.entity.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Post Service Feign Client
 *
 * @author campus
 */
@FeignClient(name = "forum-post", contextId = "postApi", url = "${feign.post.url:http://localhost:9004}")
public interface PostApi {

    /**
     * Get post by ID
     *
     * @param id Post ID
     * @return Post info
     */
    @GetMapping("/api/internal/post/{id}")
    Result<PostDTO> getPostById(@PathVariable("id") Long id);

    /**
     * Update post stats
     *
     * @param id    Post ID
     * @param field Stats field (viewCount, likeCount, commentCount, collectCount)
     * @param delta Delta (positive to increase, negative to decrease)
     * @return Result
     */
    @PostMapping("/api/internal/post/{id}/stats")
    Result<Boolean> updatePostStats(@PathVariable("id") Long id,
                                    @RequestParam("field") String field,
                                    @RequestParam("delta") int delta);

    /**
     * Get user posts
     *
     * @param userId User ID
     * @param page   Page number
     * @param size   Page size
     * @return Post list
     */
    @GetMapping("/api/internal/post/user/{userId}")
    Result<Page<PostDTO>> getPostsByUserId(
            @PathVariable("userId") Long userId,
            @RequestParam("page") int page,
            @RequestParam("size") int size);
}
