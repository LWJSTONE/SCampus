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
@FeignClient(name = "forum-user", url = "${feign.user.url:http://localhost:9002}", fallback = UserApiFallback.class)
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
}
