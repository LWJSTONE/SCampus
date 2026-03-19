package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.forum.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户角色关联实体类
 * 
 * 用于建立用户与角色的多对多关系
 * - 一个用户可以拥有多个角色
 * - 一个角色可以分配给多个用户
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_role")
public class UserRole extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;
}
