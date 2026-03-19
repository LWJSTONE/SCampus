package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.forum.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户禁言实体类
 *
 * 用于存储用户禁言记录
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_ban")
public class UserBan extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 禁言类型
     * 1-全站禁言 2-板块禁言
     */
    @TableField("ban_type")
    private Integer banType;

    /**
     * 板块ID（板块禁言时使用）
     */
    @TableField("forum_id")
    private Long forumId;

    /**
     * 禁言原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 关联举报ID
     */
    @TableField("report_id")
    private Long reportId;

    /**
     * 操作人ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 开始时间
     */
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 禁言状态
     * 0-已解除 1-禁言中 2-已过期
     */
    @TableField("status")
    private Integer status;

    /**
     * 解除时间
     */
    @TableField("release_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime releaseTime;

    /**
     * 解除操作人ID
     */
    @TableField("release_operator_id")
    private Long releaseOperatorId;

    /**
     * 解除原因
     */
    @TableField("release_reason")
    private String releaseReason;
}
