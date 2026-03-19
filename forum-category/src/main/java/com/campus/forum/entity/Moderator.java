package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.forum.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 版主关联实体
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("forum_moderator")
public class Moderator extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 版块ID
     */
    private Long forumId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名（冗余字段，方便查询）
     */
    private String username;

    /**
     * 用户昵称（冗余字段，方便查询）
     */
    private String nickname;

    /**
     * 用户头像（冗余字段，方便查询）
     */
    private String avatar;

    /**
     * 是否为主版主
     */
    private Boolean isPrimary;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;
}
