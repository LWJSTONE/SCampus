package com.campus.forum.api.user;

import com.campus.forum.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务Feign客户端接口
 *
 * @author campus
 */
@FeignClient(name = "forum-user", fallback = UserApiFallback.class)
public interface UserApi {

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/api/user/{id}")
    Result<UserDTO> getUserById(@PathVariable("id") Long id);

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/api/user/username/{username}")
    Result<UserDTO> getUserByUsername(@PathVariable("username") String username);

    /**
     * 更新用户状态
     *
     * @param id     用户ID
     * @param status 用户状态
     * @return 操作结果
     */
    @PutMapping("/api/user/{id}/status")
    Result<Boolean> updateUserStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status);
}
