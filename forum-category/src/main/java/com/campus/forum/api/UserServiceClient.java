package com.campus.forum.api;

import com.campus.forum.entity.Result;
import com.campus.forum.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 用户服务Feign客户端
 * 用于跨服务调用用户相关接口
 *
 * 【安全说明】
 * 此客户端用于获取用户真实信息，防止前端数据篡改。
 * 所有内部服务调用需要传递 X-Internal-Service-Key 进行身份验证。
 *
 * @author campus
 * @since 2024-01-01
 */
@FeignClient(name = "forum-user", contextId = "userServiceClient-forum-category")
public interface UserServiceClient {

    /**
     * 获取用户基本信息
     * 用于获取用户的真实信息，防止前端传入虚假数据
     *
     * @param userId 用户ID
     * @param serviceKey 内部服务密钥
     * @return 用户基本信息
     */
    @GetMapping("/api/v1/users/internal/{userId}/info")
    Result<UserVO> getUserInfo(
            @PathVariable("userId") Long userId,
            @RequestHeader("X-Internal-Service-Key") String serviceKey
    );

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
}
