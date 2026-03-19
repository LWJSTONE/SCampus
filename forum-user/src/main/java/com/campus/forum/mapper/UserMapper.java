package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户Mapper接口
 * 
 * 提供用户数据的持久化操作，包括：
 * - 基础CRUD操作（继承自BaseMapper）
 * - 自定义查询方法
 *
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND delete_flag = 0")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户实体
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email} AND delete_flag = 0")
    User selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户实体
     */
    @Select("SELECT * FROM sys_user WHERE phone = #{phone} AND delete_flag = 0")
    User selectByPhone(@Param("phone") String phone);

    /**
     * 更新用户最后登录信息
     *
     * @param id       用户ID
     * @param loginIp  登录IP
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET last_login_time = NOW(), last_login_ip = #{loginIp}, update_time = NOW() WHERE id = #{id}")
    int updateLoginInfo(@Param("id") Long id, @Param("loginIp") String loginIp);

    /**
     * 更新用户头像
     *
     * @param id      用户ID
     * @param avatar  头像URL
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET avatar = #{avatar}, update_time = NOW() WHERE id = #{id}")
    int updateAvatar(@Param("id") Long id, @Param("avatar") String avatar);

    /**
     * 更新用户密码
     *
     * @param id       用户ID
     * @param password 新密码
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET password = #{password}, update_time = NOW() WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 更新用户状态
     *
     * @param id     用户ID
     * @param status 状态
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 增加帖子数量
     *
     * @param id 用户ID
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET post_count = post_count + 1 WHERE id = #{id}")
    int incrementPostCount(@Param("id") Long id);

    /**
     * 减少帖子数量
     *
     * @param id 用户ID
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET post_count = GREATEST(post_count - 1, 0) WHERE id = #{id}")
    int decrementPostCount(@Param("id") Long id);

    /**
     * 增加评论数量
     *
     * @param id 用户ID
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET comment_count = comment_count + 1 WHERE id = #{id}")
    int incrementCommentCount(@Param("id") Long id);

    /**
     * 减少评论数量
     *
     * @param id 用户ID
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET comment_count = GREATEST(comment_count - 1, 0) WHERE id = #{id}")
    int decrementCommentCount(@Param("id") Long id);

    /**
     * 增加粉丝数量
     *
     * @param id 用户ID
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET follower_count = follower_count + 1 WHERE id = #{id}")
    int incrementFollowerCount(@Param("id") Long id);

    /**
     * 减少粉丝数量
     *
     * @param id 用户ID
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET follower_count = GREATEST(follower_count - 1, 0) WHERE id = #{id}")
    int decrementFollowerCount(@Param("id") Long id);

    /**
     * 增加关注数量
     *
     * @param id 用户ID
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET following_count = following_count + 1 WHERE id = #{id}")
    int incrementFollowingCount(@Param("id") Long id);

    /**
     * 减少关注数量
     *
     * @param id 用户ID
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET following_count = GREATEST(following_count - 1, 0) WHERE id = #{id}")
    int decrementFollowingCount(@Param("id") Long id);

    /**
     * 分页查询用户列表（支持关键词搜索）
     *
     * @param page    分页对象
     * @param keyword 关键词
     * @param status  状态
     * @return 用户分页列表
     */
    @Select("<script>" +
            "SELECT * FROM sys_user WHERE delete_flag = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR nickname LIKE CONCAT('%', #{keyword}, '%') " +
            "OR email LIKE CONCAT('%', #{keyword}, '%') " +
            "OR phone LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    IPage<User> selectUserPage(Page<User> page, @Param("keyword") String keyword, @Param("status") Integer status);

    /**
     * 根据学校ID查询用户列表
     *
     * @param schoolId 学校ID
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user WHERE school_id = #{schoolId} AND delete_flag = 0 ORDER BY create_time DESC")
    List<User> selectBySchoolId(@Param("schoolId") Long schoolId);
}
