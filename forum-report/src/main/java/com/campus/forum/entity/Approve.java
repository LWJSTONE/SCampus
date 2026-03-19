package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.forum.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审核记录实体类
 *
 * 用于存储内容审核记录
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_approve")
public class Approve extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 内容类型
     * 1-帖子 2-评论 3-头像 4-昵称
     */
    @TableField("content_type")
    private Integer contentType;

    /**
     * 内容ID
     */
    @TableField("content_id")
    private Long contentId;

    /**
     * 内容标题
     */
    @TableField("title")
    private String title;

    /**
     * 内容摘要
     */
    @TableField("content")
    private String content;

    /**
     * 审核状态
     * 0-待审核 1-审核通过 2-审核拒绝
     */
    @TableField("status")
    private Integer status;

    /**
     * 审核人ID
     */
    @TableField("auditor_id")
    private Long auditorId;

    /**
     * 审核意见
     */
    @TableField("audit_remark")
    private String auditRemark;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /**
     * 敏感词列表
     */
    @TableField("sensitive_words")
    private String sensitiveWords;

    /**
     * 优先级
     * 0-普通 1-重要 2-紧急
     */
    @TableField("priority")
    private Integer priority;
}
