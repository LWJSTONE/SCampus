package com.campus.forum.api.interaction;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.forum.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Interaction Service Feign Client
 * 交互服务Feign客户端接口
 *
 * @author campus
 */
@FeignClient(name = "forum-interaction", contextId = "interactionApi", url = "${feign.interaction.url:http://localhost:9007}", fallback = InteractionApiFallback.class)
public interface InteractionApi {

    /**
     * Get user's collection list
     * 获取用户收藏列表
     *
     * @param current    当前页
     * @param size       每页大小
     * @param userId     用户ID（通过Header传递）
     * @param serviceKey 内部服务密钥
     * @return 收藏列表
     */
    @GetMapping("/api/v1/interactions/collect/list/internal")
    Result<IPage<?>> getCollectList(
            @RequestParam("current") Integer current,
            @RequestParam("size") Integer size,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Internal-Service-Key") String serviceKey);

    /**
     * Check if user has collected a post
     * 检查用户是否已收藏帖子
     *
     * @param postId     帖子ID
     * @param userId     用户ID（通过Header传递）
     * @param serviceKey 内部服务密钥
     * @return 是否已收藏
     */
    @GetMapping("/api/v1/interactions/collect/check/internal")
    Result<Boolean> checkCollected(
            @RequestParam("postId") Long postId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Internal-Service-Key") String serviceKey);
}
