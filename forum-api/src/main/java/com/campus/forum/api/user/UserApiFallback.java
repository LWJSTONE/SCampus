package com.campus.forum.api.user;

import com.campus.forum.common.core.result.R;
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
    public R<UserDTO> getUserById(Long id) {
        log.error("调用用户服务失败，获取用户信息，用户ID: {}", id);
        return R.fail("用户服务不可用，请稍后重试");
    }

    @Override
    public R<UserDTO> getUserByUsername(String username) {
        log.error("调用用户服务失败，获取用户信息，用户名: {}", username);
        return R.fail("用户服务不可用，请稍后重试");
    }

    @Override
    public R<Boolean> updateUserStatus(Long id, Integer status) {
        log.error("调用用户服务失败，更新用户状态，用户ID: {}, 状态: {}", id, status);
        return R.fail("用户服务不可用，请稍后重试");
    }
}
