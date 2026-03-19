package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子实体类
 * 用于存储论坛帖子的基本信息
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("post")
@Schema(description = "帖子实体")
public class Post extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 所属板块ID
     */
    @Schema(description = "所属板块ID")
    @TableField("forum_id")
    private Long forumId;

    /**
     * 发帖用户ID
     */
    @Schema(description = "发帖用户ID")
    @TableField("user_id")
    private Long userId;

    /**
     * 帖子标题
     */
    @Schema(description = "帖子标题")
    @TableField("title")
    private String title;

    /**
     * 帖子内容（富文本）
     */
    @Schema(description = "帖子内容")
    @TableField("content")
    private String content;

    /**
     * 帖子摘要
     */
    @Schema(description = "帖子摘要")
    @TableField("summary")
    private String summary;

    /**
     * 帖子类型（0-普通帖子 1-精华帖 2-置顶帖 3-公告）
     */
    @Schema(description = "帖子类型（0-普通帖子 1-精华帖 2-置顶帖 3-公告）")
    @TableField("type")
    private Integer type;

    /**
     * 帖子状态（0-待审核 1-已发布 2-已关闭 3-已删除）
     */
    @Schema(description = "帖子状态（0-待审核 1-已发布 2-已关闭 3-已删除）")
    @TableField("status")
    private Integer status;

    /**
     * 浏览量
     */
    @Schema(description = "浏览量")
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数")
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 评论数
     */
    @Schema(description = "评论数")
    @TableField("comment_count")
    private Integer commentCount;

    /**
     * 收藏数
     */
    @Schema(description = "收藏数")
    @TableField("collect_count")
    private Integer collectCount;

    /**
     * 分享数
     */
    @Schema(description = "分享数")
    @TableField("share_count")
    private Integer shareCount;

    /**
     * 是否置顶（0-否 1-是）
     */
    @Schema(description = "是否置顶（0-否 1-是）")
    @TableField("is_top")
    private Integer isTop;

    /**
     * 是否精华（0-否 1-是）
     */
    @Schema(description = "是否精华（0-否 1-是）")
    @TableField("is_essence")
    private Integer isEssence;

    /**
     * 是否允许评论（0-否 1-是）
     */
    @Schema(description = "是否允许评论（0-否 1-是）")
    @TableField("allow_comment")
    private Integer allowComment;

    /**
     * 置顶时间
     */
    @Schema(description = "置顶时间")
    @TableField("top_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime topTime;

    /**
     * 精华时间
     */
    @Schema(description = "精华时间")
    @TableField("essence_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime essenceTime;

    /**
     * 最后回复时间
     */
    @Schema(description = "最后回复时间")
    @TableField("last_reply_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastReplyTime;

    /**
     * 最后回复用户ID
     */
    @Schema(description = "最后回复用户ID")
    @TableField("last_reply_user_id")
    private Long lastReplyUserId;

    /**
     * 封面图片URL
     */
    @Schema(description = "封面图片URL")
    @TableField("cover_image")
    private String coverImage;

    /**
     * 来源类型（0-PC端 1-APP端 2-小程序）
     */
    @Schema(description = "来源类型（0-PC端 1-APP端 2-小程序）")
    @TableField("source_type")
    private Integer sourceType;

    /**
     * IP地址
     */
    @Schema(description = "IP地址")
    @TableField("ip_address")
    private String ipAddress;

    /**
     * IP归属地
     */
    @Schema(description = "IP归属地")
    @TableField("ip_location")
    private String ipLocation;

    /**
     * 审核状态（0-待审核 1-审核通过 2-审核拒绝）
     */
    @Schema(description = "审核状态（0-待审核 1-审核通过 2-审核拒绝）")
    @TableField("audit_status")
    private Integer auditStatus;

    /**
     * 审核人ID
     */
    @Schema(description = "审核人ID")
    @TableField("audit_user_id")
    private Long auditUserId;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    @TableField("audit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /**
     * 审核备注
     */
    @Schema(description = "审核备注")
    @TableField("audit_remark")
    private String auditRemark;

    /**
     * 拒绝原因
     */
    @Schema(description = "拒绝原因")
    @TableField("reject_reason")
    private String rejectReason;

    // ==================== 非数据库字段 ====================

    /**
     * 发布用户昵称（关联查询）
     */
    @Schema(description = "发布用户昵称")
    @TableField(exist = false)
    private String userName;

    /**
     * 发布用户头像（关联查询）
     */
    @Schema(description = "发布用户头像")
    @TableField(exist = false)
    private String userAvatar;

    /**
     * 板块名称（关联查询）
     */
    @Schema(description = "板块名称")
    @TableField(exist = false)
    private String forumName;

    /**
     * 标签列表（关联查询）
     */
    @Schema(description = "标签列表")
    @TableField(exist = false)
    private java.util.List<String> tags;

    /**
     * 附件列表（关联查询）
     */
    @Schema(description = "附件列表")
    @TableField(exist = false)
    private java.util.List<PostAttachment> attachments;
}
