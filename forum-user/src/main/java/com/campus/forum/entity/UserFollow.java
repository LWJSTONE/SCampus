package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.forum.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户关注实体类
 * 
 * 用于记录用户之间的关注关系
 * - 一个用户可以关注多个用户
 * - 一个用户可以被多个用户关注
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_follow")
public class UserFollow extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 关注者ID（发起关注的用户）
     */
    private Long followerId;

    /**
     * 被关注者ID（被关注的用户）
     */
    private Long followingId;

    /**
     * 关注状态（0-已取消，1-正常关注）
     */
    private Integer status;
}
