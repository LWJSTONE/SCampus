package com.campus.forum.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子详情VO
 * 用于返回帖子详情信息
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "帖子详情VO")
public class PostDetailVO implements Serializable {

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
     * 帖子内容
     */
    @Schema(description = "帖子内容")
    private String content;

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
     * 分享数
     */
    @Schema(description = "分享数")
    private Integer shareCount;

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
     * 是否允许评论
     */
    @Schema(description = "是否允许评论")
    private Integer allowComment;

    /**
     * 置顶时间
     */
    @Schema(description = "置顶时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime topTime;

    /**
     * 精华时间
     */
    @Schema(description = "精华时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime essenceTime;

    /**
     * 最后回复时间
     */
    @Schema(description = "最后回复时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastReplyTime;

    /**
     * 最后回复用户ID
     */
    @Schema(description = "最后回复用户ID")
    private Long lastReplyUserId;

    /**
     * 最后回复用户昵称
     */
    @Schema(description = "最后回复用户昵称")
    private String lastReplyUserName;

    /**
     * 封面图片URL
     */
    @Schema(description = "封面图片URL")
    private String coverImage;

    /**
     * 标签列表
     */
    @Schema(description = "标签列表")
    private List<TagVO> tags;

    /**
     * 附件列表
     */
    @Schema(description = "附件列表")
    private List<AttachmentVO> attachments;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

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
     * 当前用户是否为作者
     */
    @Schema(description = "当前用户是否为作者")
    private Boolean isAuthor;

    /**
     * 标签VO
     */
    @Data
    @Schema(description = "标签信息")
    public static class TagVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 标签ID
         */
        @Schema(description = "标签ID")
        private Long id;

        /**
         * 标签名称
         */
        @Schema(description = "标签名称")
        private String name;

        /**
         * 标签颜色
         */
        @Schema(description = "标签颜色")
        private String color;
    }

    /**
     * 附件VO
     */
    @Data
    @Schema(description = "附件信息")
    public static class AttachmentVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 附件ID
         */
        @Schema(description = "附件ID")
        private Long id;

        /**
         * 附件类型
         */
        @Schema(description = "附件类型（1-图片 2-视频 3-音频 4-文档 5-其他）")
        private Integer type;

        /**
         * 附件名称
         */
        @Schema(description = "附件名称")
        private String name;

        /**
         * 附件URL
         */
        @Schema(description = "附件URL")
        private String url;

        /**
         * 缩略图URL
         */
        @Schema(description = "缩略图URL")
        private String thumbnailUrl;

        /**
         * 文件大小
         */
        @Schema(description = "文件大小（字节）")
        private Long size;

        /**
         * MIME类型
         */
        @Schema(description = "MIME类型")
        private String mimeType;

        /**
         * 宽度
         */
        @Schema(description = "宽度")
        private Integer width;

        /**
         * 高度
         */
        @Schema(description = "高度")
        private Integer height;

        /**
         * 时长
         */
        @Schema(description = "时长（秒）")
        private Integer duration;
    }
}
