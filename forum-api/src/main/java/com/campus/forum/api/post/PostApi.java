package com.campus.forum.api.post;

import com.campus.forum.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 帖子服务Feign客户端接口
 *
 * @author campus
 */
@FeignClient(name = "forum-post", fallback = PostApiFallback.class)
public interface PostApi {

    /**
     * 根据ID获取帖子
     *
     * @param id 帖子ID
     * @return 帖子信息
     */
    @GetMapping("/api/post/{id}")
    Result<PostDTO> getPostById(@PathVariable("id") Long id);

    /**
     * 更新帖子统计信息
     *
     * @param id    帖子ID
     * @param field 统计字段（viewCount, likeCount, commentCount, collectCount）
     * @param delta 增量（正数增加，负数减少）
     * @return 操作结果
     */
    @PostMapping("/api/post/{id}/stats")
    Result<Boolean> updatePostStats(@PathVariable("id") Long id,
                               @RequestParam("field") String field,
                               @RequestParam("delta") int delta);

    /**
     * 获取用户帖子列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 帖子列表
     */
    @GetMapping("/api/post/user/{userId}")
    Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<PostDTO>> getPostsByUserId(
            @PathVariable("userId") Long userId,
            @RequestParam("page") int page,
            @RequestParam("size") int size);
}
