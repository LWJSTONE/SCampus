package com.campus.forum.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知VO
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
public class NoticeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型
     * 1-系统公告
     * 2-活动通知
     * 3-版本更新
     * 4-其他
     */
    private Integer type;

    /**
     * 通知类型名称
     */
    private String typeName;

    /**
     * 通知级别
     * 1-普通
     * 2-重要
     * 3-紧急
     */
    private Integer level;

    /**
     * 通知级别名称
     */
    private String levelName;

    /**
     * 发布状态
     * 0-草稿
     * 1-已发布
     * 2-已撤回
     */
    private Integer status;

    /**
     * 发布状态名称
     */
    private String statusName;

    /**
     * 发布人ID
     */
    private Long publisherId;

    /**
     * 发布人名称
     */
    private String publisherName;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * 阅读数量
     */
    private Integer readCount;

    /**
     * 是否已读（当前用户）
     */
    private Boolean isRead;

    /**
     * 阅读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    /**
     * 生效开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveStartTime;

    /**
     * 生效结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveEndTime;

    /**
     * 附件URL
     */
    private String attachments;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 获取类型名称
     */
    public String getTypeName() {
        if (type == null) return "";
        switch (type) {
            case 1: return "系统公告";
            case 2: return "活动通知";
            case 3: return "版本更新";
            case 4: return "其他";
            default: return "未知";
        }
    }

    /**
     * 获取级别名称
     */
    public String getLevelName() {
        if (level == null) return "";
        switch (level) {
            case 1: return "普通";
            case 2: return "重要";
            case 3: return "紧急";
            default: return "未知";
        }
    }

    /**
     * 获取状态名称
     */
    public String getStatusName() {
        if (status == null) return "";
        switch (status) {
            case 0: return "草稿";
            case 1: return "已发布";
            case 2: return "已撤回";
            default: return "未知";
        }
    }

    /**
     * 获取是否置顶
     */
    public Boolean getIsTop() {
        return isTop != null && isTop;
    }
}
