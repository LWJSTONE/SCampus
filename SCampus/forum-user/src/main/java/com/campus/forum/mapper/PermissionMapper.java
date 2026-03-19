package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper接口
 * 
 * 提供权限数据的持久化操作，包括：
 * - 基础CRUD操作（继承自BaseMapper）
 * - 根据用户ID查询权限
 * - 根据角色ID查询权限
 *
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据用户ID查询权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.delete_flag = 0 AND p.status = 1 " +
            "ORDER BY p.sort ASC")
    List<Permission> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.delete_flag = 0 AND p.status = 1 " +
            "ORDER BY p.sort ASC")
    List<Permission> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限编码查询权限
     *
     * @param permissionCode 权限编码
     * @return 权限实体
     */
    @Select("SELECT * FROM sys_permission WHERE permission_code = #{permissionCode} AND delete_flag = 0")
    Permission selectByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 查询用户的所有权限编码
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    @Select("SELECT DISTINCT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.delete_flag = 0 AND p.status = 1")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    /**
     * 查询所有启用的权限
     *
     * @return 权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE delete_flag = 0 AND status = 1 ORDER BY sort ASC")
    List<Permission> selectAllEnabled();
}
