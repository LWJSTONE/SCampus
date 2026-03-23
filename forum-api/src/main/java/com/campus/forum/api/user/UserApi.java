package com.campus.forum.api.user;

import com.campus.forum.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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
    @GetMapping("/api/internal/user/{id}")
    Result<UserDTO> getUserById(@PathVariable("id") Long id);

    /**
     * Get user by username
     *
     * @param username Username
     * @return User info
     */
    @GetMapping("/api/internal/user/username/{username}")
    Result<UserDTO> getUserByUsername(@PathVariable("username") String username);

    /**
     * Update user status
     *
     * @param id     User ID
     * @param status User status
     * @return Result
     */
    @PutMapping("/api/internal/user/{id}/status")
    Result<Boolean> updateUserStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status);

    /**
     * Increment user post count
     *
     * @param id User ID
     * @return Result
     */
    @PutMapping("/api/internal/user/{id}/post-count/increment")
    Result<Boolean> incrementPostCount(@PathVariable("id") Long id);

    /**
     * Decrement user post count
     *
     * @param id User ID
     * @return Result
     */
    @PutMapping("/api/internal/user/{id}/post-count/decrement")
    Result<Boolean> decrementPostCount(@PathVariable("id") Long id);

    /**
     * Increment user comment count
     *
     * @param id User ID
     * @return Result
     */
    @PutMapping("/api/internal/user/{id}/comment-count/increment")
    Result<Boolean> incrementCommentCount(@PathVariable("id") Long id);

    /**
     * Decrement user comment count
     *
     * @param id User ID
     * @return Result
     */
    @PutMapping("/api/internal/user/{id}/comment-count/decrement")
    Result<Boolean> decrementCommentCount(@PathVariable("id") Long id);

    /**
     * Ban user account
     *
     * @param id User ID
     * @return Result
     */
    @PutMapping("/api/internal/user/{id}/ban")
    Result<Boolean> banUser(@PathVariable("id") Long id);

    /**
     * Unban user account
     *
     * @param id User ID
     * @return Result
     */
    @PutMapping("/api/internal/user/{id}/unban")
    Result<Boolean> unbanUser(@PathVariable("id") Long id);
}
