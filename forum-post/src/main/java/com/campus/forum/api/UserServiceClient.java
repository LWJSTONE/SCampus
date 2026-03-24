package com.campus.forum.api;

import com.campus.forum.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 用户服务Feign客户端
 * 用于跨服务调用用户相关接口
 *
 * 【安全说明】
 * 此客户端用于在关键操作时进行二次验证，确保用户角色的真实性。
 * 所有内部服务调用需要传递 X-Internal-Service-Key 进行身份验证。
 *
 * @author campus
 * @since 2024-01-01
 */
@FeignClient(name = "forum-user", contextId = "userServiceClient")
public interface UserServiceClient {

    /**
     * 验证用户是否为管理员
     * 用于关键操作的二次验证，防止HTTP Header伪造攻击
     *
     * @param userId 用户ID
     * @param serviceKey 内部服务密钥
     * @return 验证结果，包含是否为管理员的信息
     */
    @GetMapping("/api/v1/users/internal/{userId}/verify-admin")
    Result<Boolean> verifyAdmin(
            @PathVariable("userId") Long userId,
            @RequestHeader("X-Internal-Service-Key") String serviceKey
    );

    /**
     * 获取用户角色
     * 用于二次验证用户角色
     *
     * @param userId 用户ID
     * @param serviceKey 内部服务密钥
     * @return 用户角色信息
     */
    @GetMapping("/api/v1/users/internal/{userId}/role")
    Result<String> getUserRole(
            @PathVariable("userId") Long userId,
            @RequestHeader("X-Internal-Service-Key") String serviceKey
    );
}
