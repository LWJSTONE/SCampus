package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.dto.UserQueryDTO;
import com.campus.forum.dto.UserUpdateDTO;
import com.campus.forum.dto.PasswordUpdateDTO;
import com.campus.forum.entity.PageResult;
import com.campus.forum.entity.User;
import com.campus.forum.vo.UserDetailVO;
import com.campus.forum.vo.UserListVO;

/**
 * 用户服务接口
 * 
 * 提供用户相关的业务功能：
 * - 用户信息查询（列表、详情）
 * - 用户信息更新（基本信息、头像、密码）
 * - 用户统计信息管理
 *
 * @author campus
 * @since 2024-01-01
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件
     * @return 用户列表分页结果
     */
    PageResult<UserListVO> getUserList(UserQueryDTO queryDTO);

    /**
     * 获取用户详情
     *
     * @param id           用户ID
     * @param currentUserId 当前登录用户ID（用于判断是否已关注）
     * @return 用户详情
     */
    UserDetailVO getUserDetail(Long id, Long currentUserId);

    /**
     * 更新用户信息
     *
     * @param id        用户ID
     * @param updateDTO 更新信息
     * @return 是否成功
     */
    boolean updateUserInfo(Long id, UserUpdateDTO updateDTO);

    /**
     * 更新用户头像
     *
     * @param id     用户ID
     * @param avatar 头像URL
     * @return 是否成功
     */
    boolean updateAvatar(Long id, String avatar);

    /**
     * 修改用户密码
     *
     * @param id           用户ID
     * @param passwordDTO 密码更新信息
     * @return 是否成功
     */
    boolean updatePassword(Long id, PasswordUpdateDTO passwordDTO);

    /**
     * 更新用户状态
     *
     * @param id     用户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    User getByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户实体
     */
    User getByEmail(String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户实体
     */
    User getByPhone(String phone);

    /**
     * 增加帖子数量
     *
     * @param userId 用户ID
     */
    void incrementPostCount(Long userId);

    /**
     * 减少帖子数量
     *
     * @param userId 用户ID
     */
    void decrementPostCount(Long userId);

    /**
     * 增加评论数量
     *
     * @param userId 用户ID
     */
    void incrementCommentCount(Long userId);

    /**
     * 减少评论数量
     *
     * @param userId 用户ID
     */
    void decrementCommentCount(Long userId);

    /**
     * 更新最后登录信息
     *
     * @param userId  用户ID
     * @param loginIp 登录IP
     */
    void updateLoginInfo(Long userId, String loginIp);

    /**
     * 获取当前登录用户信息
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    UserDetailVO getCurrentUser(Long userId);

    /**
     * 验证用户是否为管理员
     * 用于跨服务的管理员权限二次验证
     *
     * @param userId 用户ID
     * @return 是否为管理员
     */
    boolean verifyAdmin(Long userId);

    /**
     * 获取用户角色编码
     * 用于跨服务的角色查询
     *
     * @param userId 用户ID
     * @return 角色编码（如ADMIN、USER等）
     */
    String getUserRole(Long userId);
}
