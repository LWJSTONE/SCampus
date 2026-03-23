package com.campus.forum.api.user;

import com.campus.forum.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户服务Feign客户端回退处理类
 *
 * @author campus
 */
@Slf4j
@Component
public class UserApiFallback implements UserApi {

    @Override
    public Result<UserDTO> getUserById(Long id) {
        log.error("调用用户服务失败，获取用户信息，用户ID: {}", id);
        return Result.fail("用户服务不可用，请稍后重试");
    }

    @Override
    public Result<UserDTO> getUserByUsername(String username) {
        log.error("调用用户服务失败，获取用户信息，用户名: {}", username);
        return Result.fail("用户服务不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> updateUserStatus(Long id, Integer status) {
        log.error("调用用户服务失败，更新用户状态，用户ID: {}, 状态: {}", id, status);
        return Result.fail("用户服务不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> incrementPostCount(Long id) {
        log.error("调用用户服务失败，增加帖子数，用户ID: {}", id);
        return Result.fail("用户服务不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> decrementPostCount(Long id) {
        log.error("调用用户服务失败，减少帖子数，用户ID: {}", id);
        return Result.fail("用户服务不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> incrementCommentCount(Long id) {
        log.error("调用用户服务失败，增加评论数，用户ID: {}", id);
        return Result.fail("用户服务不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> decrementCommentCount(Long id) {
        log.error("调用用户服务失败，减少评论数，用户ID: {}", id);
        return Result.fail("用户服务不可用，请稍后重试");
    }
}
