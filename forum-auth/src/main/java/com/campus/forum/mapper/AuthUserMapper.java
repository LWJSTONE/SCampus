package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.AuthUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证用户Mapper接口
 * 
 * <p>提供用户认证相关的数据库操作</p>
 *
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface AuthUserMapper extends BaseMapper<AuthUser> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    AuthUser selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户实体
     */
    AuthUser selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户实体
     */
    AuthUser selectByPhone(@Param("phone") String phone);

    /**
     * 更新最后登录信息
     *
     * @param userId 用户ID
     * @param loginTime 登录时间
     * @param loginIp 登录IP
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET last_login_time = #{loginTime}, last_login_ip = #{loginIp}, " +
            "login_fail_count = 0, lock_time = NULL WHERE id = #{userId}")
    int updateLoginInfo(@Param("userId") Long userId, 
                        @Param("loginTime") LocalDateTime loginTime, 
                        @Param("loginIp") String loginIp);

    /**
     * 增加登录失败次数
     *
     * @param userId 用户ID
     * @return 当前失败次数
     */
    @Update("UPDATE sys_user SET login_fail_count = login_fail_count + 1 WHERE id = #{userId}")
    int incrementLoginFailCount(@Param("userId") Long userId);

    /**
     * 锁定用户账户
     *
     * @param userId 用户ID
     * @param lockTime 锁定时间
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET lock_time = #{lockTime}, status = 0 WHERE id = #{userId}")
    int lockUser(@Param("userId") Long userId, @Param("lockTime") LocalDateTime lockTime);

    /**
     * 解锁用户账户
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET lock_time = NULL, login_fail_count = 0, status = 1 WHERE id = #{userId}")
    int unlockUser(@Param("userId") Long userId);

    /**
     * 更新密码
     *
     * @param userId 用户ID
     * @param password 新密码（加密后）
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET password = #{password}, password_update_time = NOW() WHERE id = #{userId}")
    int updatePassword(@Param("userId") Long userId, @Param("password") String password);

    /**
     * 查询用户角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 原子操作：增加登录失败次数，如果达到最大次数则锁定账户
     * 此方法用于解决并发情况下的竞态条件问题
     *
     * @param userId 用户ID
     * @param maxFailCount 最大失败次数
     * @param lockTime 锁定时间
     * @return 如果触发了锁定返回大于0的值，否则返回0
     */
    @Update("UPDATE sys_user SET login_fail_count = login_fail_count + 1, " +
            "lock_time = CASE WHEN login_fail_count + 1 >= #{maxFailCount} THEN #{lockTime} ELSE lock_time END, " +
            "status = CASE WHEN login_fail_count + 1 >= #{maxFailCount} THEN 0 ELSE status END " +
            "WHERE id = #{userId} AND (lock_time IS NULL OR lock_time < DATE_SUB(NOW(), INTERVAL 30 MINUTE))")
    int incrementLoginFailCountAndLockIfNeeded(@Param("userId") Long userId, 
                                                @Param("maxFailCount") int maxFailCount, 
                                                @Param("lockTime") LocalDateTime lockTime);
}
