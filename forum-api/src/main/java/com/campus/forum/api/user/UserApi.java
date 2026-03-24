package com.campus.forum.api.user;

import com.campus.forum.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * User Service Feign Client
 *
 * @author campus
 */
@FeignClient(name = "forum-user", contextId = "userApi", url = "${feign.user.url:http://localhost:9002}", fallback = UserApiFallback.class)
public interface UserApi {

    /**
     * Get user by ID
     *
     * @param id User ID
     * @return User info
     */
    @GetMapping("/api/v1/users/internal/{id}")
    Result<UserDTO> getUserById(@PathVariable("id") Long id, @RequestHeader("X-Internal-Service-Key") String serviceKey);

    /**
     * Get user by username
     *
     * @param username Username
     * @return User info
     */
    @GetMapping("/api/v1/users/internal/username/{username}")
    Result<UserDTO> getUserByUsername(@PathVariable("username") String username, @RequestHeader("X-Internal-Service-Key") String serviceKey);

    /**
     * Update user status
     *
     * @param id     User ID
     * @param status User status
     * @return Result
     */
    @PutMapping("/api/v1/users/internal/{id}/status")
    Result<Boolean> updateUserStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status, @RequestHeader("X-Internal-Service-Key") String serviceKey);

    /**
     * Increment user post count
     *
     * @param id User ID
     * @return Result
     */
    @PutMapping("/api/v1/users/internal/{id}/post-count/increment")
    Result<Boolean> incrementPostCount(@PathVariable("id") Long id, @RequestHeader("X-Internal-Service-Key") String serviceKey);

    /**
     * Decrement user post count
     *
     * @param id User ID
     * @return Result
     */
    @PutMapping("/api/v1/users/internal/{id}/post-count/decrement")
    Result<Boolean> decrementPostCount(@PathVariable("id") Long id, @RequestHeader("X-Internal-Service-Key") String serviceKey);

    /**
     * Increment user comment count
     *
     * @param id User ID
     * @return Result
     */
    @PutMapping("/api/v1/users/internal/{id}/comment-count/increment")
    Result<Boolean> incrementCommentCount(@PathVariable("id") Long id, @RequestHeader("X-Internal-Service-Key") String serviceKey);

    /**
     * Decrement user comment count
     *
     * @param id User ID
     * @return Result
     */
    @PutMapping("/api/v1/users/internal/{id}/comment-count/decrement")
    Result<Boolean> decrementCommentCount(@PathVariable("id") Long id, @RequestHeader("X-Internal-Service-Key") String serviceKey);

    /**
     * Ban user account
     *
     * @param id User ID
     * @return Result
     */
    @PutMapping("/api/v1/users/internal/{id}/ban")
    Result<Boolean> banUser(@PathVariable("id") Long id, @RequestHeader("X-Internal-Service-Key") String serviceKey);

    /**
     * Unban user account
     *
     * @param id User ID
     * @return Result
     */
    @PutMapping("/api/v1/users/internal/{id}/unban")
    Result<Boolean> unbanUser(@PathVariable("id") Long id, @RequestHeader("X-Internal-Service-Key") String serviceKey);
}
