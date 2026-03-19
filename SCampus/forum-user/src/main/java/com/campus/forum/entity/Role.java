package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.forum.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类
 * 
 * 用于定义系统角色，如：
 * - 管理员（admin）
 * - 普通用户（user）
 * - 版主（moderator）
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class Role extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码（用于权限判断）
     */
    private String roleCode;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态（0-禁用，1-正常）
     */
    private Integer status;
}
