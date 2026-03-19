package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.forum.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限实体类
 * 
 * 用于定义系统权限，包括：
 * - 菜单权限（permission_type=1）
 * - 按钮权限（permission_type=2）
 * - 接口权限（permission_type=3）
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class Permission extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 父权限ID（用于构建权限树）
     */
    private Long parentId;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 权限编码（用于权限判断）
     */
    private String permissionCode;

    /**
     * 权限类型（1-菜单，2-按钮，3-接口）
     */
    private Integer permissionType;

    /**
     * 资源路径
     */
    private String path;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态（0-禁用，1-正常）
     */
    private Integer status;
}
