package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户通知阅读实体类
 * 
 * 用于记录用户的通知阅读状态
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_notice")
public class UserNotice extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 通知ID
     */
    @TableField("notice_id")
    private Long noticeId;

    /**
     * 是否已读
     * 0-未读
     * 1-已读
     */
    @TableField("is_read")
    private Integer isRead;

    /**
     * 阅读时间
     */
    @TableField("read_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    /**
     * 是否删除（用户端删除）
     * 0-未删除
     * 1-已删除
     */
    @TableField("is_deleted")
    private Integer isDeleted;
}
