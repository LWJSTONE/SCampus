package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知公告实体类
 * 
 * 用于存储系统通知和公告信息
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_notice")
public class Notice extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知标题
     */
    @TableField("title")
    private String title;

    /**
     * 通知内容
     */
    @TableField("content")
    private String content;

    /**
     * 通知类型
     * 1-系统公告
     * 2-活动通知
     * 3-版本更新
     * 4-其他
     */
    @TableField("type")
    private Integer type;

    /**
     * 通知级别
     * 1-普通
     * 2-重要
     * 3-紧急
     */
    @TableField("level")
    private Integer level;

    /**
     * 发布状态
     * 0-草稿
     * 1-已发布
     * 2-已撤回
     */
    @TableField("status")
    private Integer status;

    /**
     * 发布人ID
     */
    @TableField("publisher_id")
    private Long publisherId;

    /**
     * 发布人名称
     */
    @TableField("publisher_name")
    private String publisherName;

    /**
     * 发布时间
     */
    @TableField("publish_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    /**
     * 生效开始时间
     */
    @TableField("effective_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveStartTime;

    /**
     * 生效结束时间
     */
    @TableField("effective_end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveEndTime;

    /**
     * 是否置顶
     * 0-否
     * 1-是
     */
    @TableField("is_top")
    private Integer isTop;

    /**
     * 阅读数量
     */
    @TableField("read_count")
    private Integer readCount;

    /**
     * 附件URL（JSON格式）
     */
    @TableField("attachments")
    private String attachments;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
