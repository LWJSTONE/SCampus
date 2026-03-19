package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.forum.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 举报实体类
 *
 * 用于存储用户举报信息
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_report")
public class Report extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 举报人ID
     */
    @TableField("reporter_id")
    private Long reporterId;

    /**
     * 被举报人ID
     */
    @TableField("reported_user_id")
    private Long reportedUserId;

    /**
     * 举报类型
     * 1-帖子 2-评论 3-用户
     */
    @TableField("report_type")
    private Integer reportType;

    /**
     * 被举报内容ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 举报原因类型
     * 1-垃圾广告 2-色情低俗 3-违法违规 4-人身攻击 5-恶意灌水 6-其他
     */
    @TableField("reason_type")
    private Integer reasonType;

    /**
     * 举报原因详情
     */
    @TableField("reason")
    private String reason;

    /**
     * 举报截图（JSON数组）
     */
    @TableField("images")
    private String images;

    /**
     * 处理状态
     * 0-待处理 1-处理中 2-已处理 3-已驳回
     */
    @TableField("status")
    private Integer status;

    /**
     * 处理人ID
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 处理结果
     * 0-无违规 1-警告 2-删除内容 3-禁言 4-封号
     */
    @TableField("result")
    private Integer result;

    /**
     * 处理备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 处理时间
     */
    @TableField("handle_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;
}
