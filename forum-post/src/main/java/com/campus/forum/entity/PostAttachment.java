package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 帖子附件实体类
 * 用于存储帖子关联的附件信息（图片、文件等）
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("post_attachment")
@Schema(description = "帖子附件实体")
public class PostAttachment extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 帖子ID
     */
    @Schema(description = "帖子ID")
    @TableField("post_id")
    private Long postId;

    /**
     * 附件类型（1-图片 2-视频 3-音频 4-文档 5-其他）
     */
    @Schema(description = "附件类型（1-图片 2-视频 3-音频 4-文档 5-其他）")
    @TableField("type")
    private Integer type;

    /**
     * 附件名称
     */
    @Schema(description = "附件名称")
    @TableField("name")
    private String name;

    /**
     * 附件原始名称
     */
    @Schema(description = "附件原始名称")
    @TableField("original_name")
    private String originalName;

    /**
     * 附件路径/URL
     */
    @Schema(description = "附件路径/URL")
    @TableField("url")
    private String url;

    /**
     * 缩略图路径/URL
     */
    @Schema(description = "缩略图路径/URL")
    @TableField("thumbnail_url")
    private String thumbnailUrl;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小（字节）")
    @TableField("size")
    private Long size;

    /**
     * 文件MIME类型
     */
    @Schema(description = "文件MIME类型")
    @TableField("mime_type")
    private String mimeType;

    /**
     * 图片宽度（图片类型时有效）
     */
    @Schema(description = "图片宽度")
    @TableField("width")
    private Integer width;

    /**
     * 图片高度（图片类型时有效）
     */
    @Schema(description = "图片高度")
    @TableField("height")
    private Integer height;

    /**
     * 时长（音视频类型时有效，单位：秒）
     */
    @Schema(description = "时长（秒）")
    @TableField("duration")
    private Integer duration;

    /**
     * 排序号
     */
    @Schema(description = "排序号")
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态（0-禁用 1-启用）
     */
    @Schema(description = "状态（0-禁用 1-启用）")
    @TableField("status")
    private Integer status;
}
