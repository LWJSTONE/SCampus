package com.campus.forum.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子列表VO
 * 用于返回帖子列表信息
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "帖子列表VO")
public class PostListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 帖子ID
     */
    @Schema(description = "帖子ID")
    private Long id;

    /**
     * 所属板块ID
     */
    @Schema(description = "所属板块ID")
    private Long forumId;

    /**
     * 板块名称
     */
    @Schema(description = "板块名称")
    private String forumName;

    /**
     * 发帖用户ID
     */
    @Schema(description = "发帖用户ID")
    private Long userId;

    /**
     * 发布用户昵称
     */
    @Schema(description = "发布用户昵称")
    private String userName;

    /**
     * 发布用户头像
     */
    @Schema(description = "发布用户头像")
    private String userAvatar;

    /**
     * 帖子标题
     */
    @Schema(description = "帖子标题")
    private String title;

    /**
     * 帖子摘要
     */
    @Schema(description = "帖子摘要")
    private String summary;

    /**
     * 帖子类型
     */
    @Schema(description = "帖子类型（0-普通帖子 1-精华帖 2-置顶帖 3-公告）")
    private Integer type;

    /**
     * 帖子状态
     */
    @Schema(description = "帖子状态（0-待审核 1-已发布 2-已关闭 3-已删除）")
    private Integer status;

    /**
     * 浏览量
     */
    @Schema(description = "浏览量")
    private Integer viewCount;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数")
    private Integer likeCount;

    /**
     * 评论数
     */
    @Schema(description = "评论数")
    private Integer commentCount;

    /**
     * 收藏数
     */
    @Schema(description = "收藏数")
    private Integer collectCount;

    /**
     * 是否置顶
     */
    @Schema(description = "是否置顶")
    private Integer isTop;

    /**
     * 是否精华
     */
    @Schema(description = "是否精华")
    private Integer isEssence;

    /**
     * 最后回复时间
     */
    @Schema(description = "最后回复时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastReplyTime;

    /**
     * 封面图片URL
     */
    @Schema(description = "封面图片URL")
    private String coverImage;

    /**
     * 图片列表（前3张）
     */
    @Schema(description = "图片列表（前3张）")
    private List<String> images;

    /**
     * 标签列表
     */
    @Schema(description = "标签列表")
    private List<String> tags;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * IP归属地
     */
    @Schema(description = "IP归属地")
    private String ipLocation;

    /**
     * 当前用户是否已点赞
     */
    @Schema(description = "当前用户是否已点赞")
    private Boolean isLiked;

    /**
     * 当前用户是否已收藏
     */
    @Schema(description = "当前用户是否已收藏")
    private Boolean isCollected;

    /**
     * 热度值（用于排序）
     */
    @Schema(description = "热度值")
    private Integer hotScore;
}
