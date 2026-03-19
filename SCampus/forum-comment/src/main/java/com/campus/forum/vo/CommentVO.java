package com.campus.forum.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论VO
 * 
 * 用于返回评论详情信息，包含用户信息和回复列表
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "评论VO")
public class CommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @Schema(description = "评论ID")
    private Long id;

    /**
     * 帖子ID
     */
    @Schema(description = "帖子ID")
    private Long postId;

    /**
     * 父评论ID
     */
    @Schema(description = "父评论ID")
    private Long parentId;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String content;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数")
    private Integer likeCount;

    /**
     * 回复数
     */
    @Schema(description = "回复数")
    private Integer replyCount;

    /**
     * 是否为热门评论
     */
    @Schema(description = "是否为热门评论")
    private Boolean isHot;

    /**
     * IP归属地
     */
    @Schema(description = "IP归属地")
    private String ipLocation;

    /**
     * 评论状态
     */
    @Schema(description = "评论状态（0-正常，1-已删除，2-被屏蔽）")
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // ==================== 用户信息 ====================

    /**
     * 评论用户ID
     */
    @Schema(description = "评论用户ID")
    private Long userId;

    /**
     * 评论用户昵称
     */
    @Schema(description = "评论用户昵称")
    private String userName;

    /**
     * 评论用户头像
     */
    @Schema(description = "评论用户头像")
    private String userAvatar;

    /**
     * 用户等级
     */
    @Schema(description = "用户等级")
    private Integer userLevel;

    // ==================== 回复目标用户信息 ====================

    /**
     * 回复目标用户ID
     */
    @Schema(description = "回复目标用户ID")
    private Long replyToUserId;

    /**
     * 回复目标用户昵称
     */
    @Schema(description = "回复目标用户昵称")
    private String replyToUserName;

    // ==================== 交互状态 ====================

    /**
     * 当前用户是否已点赞
     */
    @Schema(description = "当前用户是否已点赞")
    private Boolean isLiked;

    /**
     * 当前用户是否为评论作者
     */
    @Schema(description = "当前用户是否为评论作者")
    private Boolean isAuthor;

    // ==================== 回复列表 ====================

    /**
     * 子评论列表（回复）
     * 只在查询一级评论时加载
     */
    @Schema(description = "子评论列表")
    private List<CommentVO> replies;

    /**
     * 是否还有更多回复
     */
    @Schema(description = "是否还有更多回复")
    private Boolean hasMoreReplies;

    // ==================== 时间显示 ====================

    /**
     * 友好的时间显示
     * 如：刚刚、1分钟前、1小时前等
     */
    @Schema(description = "友好的时间显示")
    private String timeAgo;
}
