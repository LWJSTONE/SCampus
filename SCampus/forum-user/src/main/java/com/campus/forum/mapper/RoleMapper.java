package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper接口
 * 
 * 提供角色数据的持久化操作，包括：
 * - 基础CRUD操作（继承自BaseMapper）
 * - 根据用户ID查询角色
 * - 根据角色编码查询角色
 *
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.delete_flag = 0 AND r.status = 1")
    List<Role> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色编码查询角色
     *
     * @param roleCode 角色编码
     * @return 角色实体
     */
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode} AND delete_flag = 0")
    Role selectByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 查询所有启用的角色
     *
     * @return 角色列表
     */
    @Select("SELECT * FROM sys_role WHERE delete_flag = 0 AND status = 1 ORDER BY sort ASC")
    List<Role> selectAllEnabled();
}
