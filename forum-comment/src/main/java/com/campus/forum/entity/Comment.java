package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体类
 * 
 * 用于存储帖子的评论信息，支持楼中楼回复
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("forum_comment")
public class Comment extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 帖子ID
     * 关联帖子表的主键
     */
    @TableField("post_id")
    private Long postId;

    /**
     * 父评论ID
     * 0表示一级评论（直接评论帖子）
     * 非0表示子评论（回复其他评论）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 回复目标用户ID
     * 用于楼中楼回复时，标识被回复的用户
     */
    @TableField("reply_to_user_id")
    private Long replyToUserId;

    /**
     * 评论用户ID
     * 发表该评论的用户
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 评论内容
     * 支持文本内容，最大长度500字
     */
    @TableField("content")
    private String content;

    /**
     * 点赞数量
     * 记录该评论被点赞的次数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 回复数量
     * 记录该评论下的子评论数量
     */
    @TableField("reply_count")
    private Integer replyCount;

    /**
     * 评论状态
     * 0-已删除
     * 1-正常
     * 2-被系统屏蔽
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否为热门评论
     * 0-否
     * 1-是（点赞数超过阈值）
     */
    @TableField("is_hot")
    private Integer isHot;

    /**
     * IP地址
     * 评论发表时的IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * IP归属地
     * 根据IP解析出的地理位置
     */
    @TableField("ip_location")
    private String ipLocation;

    /**
     * 审核状态
     * 0-待审核
     * 1-审核通过
     * 2-审核拒绝
     */
    @TableField("audit_status")
    private Integer auditStatus;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /**
     * 审核人ID
     */
    @TableField("auditor_id")
    private Long auditorId;

    /**
     * 审核备注
     */
    @TableField("audit_remark")
    private String auditRemark;
}
