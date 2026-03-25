package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 发布帖子DTO
 * 用于接收发布帖子的请求参数
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "发布帖子DTO")
public class PostCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 所属板块ID
     */
    @Schema(description = "所属板块ID", required = true)
    @NotNull(message = "板块ID不能为空")
    private Long forumId;

    /**
     * 帖子标题
     */
    @Schema(description = "帖子标题", required = true)
    @NotBlank(message = "帖子标题不能为空")
    @Size(min = 2, max = 100, message = "标题长度必须在2-100个字符之间")
    private String title;

    /**
     * 帖子内容
     */
    @Schema(description = "帖子内容", required = true)
    @NotBlank(message = "帖子内容不能为空")
    @Size(min = 10, max = 50000, message = "内容长度必须在10-50000个字符之间")
    private String content;

    /**
     * 帖子摘要（可选，不填则自动生成）
     */
    @Schema(description = "帖子摘要")
    @Size(max = 200, message = "摘要长度不能超过200个字符")
    private String summary;

    /**
     * 帖子类型（0-普通帖子 1-精华帖 2-置顶帖 3-公告）
     */
    @Schema(description = "帖子类型（0-普通帖子 1-精华帖 2-置顶帖 3-公告）", defaultValue = "0")
    @Min(value = 0, message = "帖子类型无效，有效范围：0-3")
    @Max(value = 3, message = "帖子类型无效，有效范围：0-3")
    private Integer type = 0;

    /**
     * 标签ID列表
     */
    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

    /**
     * 附件URL列表
     */
    @Schema(description = "附件URL列表")
    private List<AttachmentDTO> attachments;

    /**
     * 封面图片URL
     */
    @Schema(description = "封面图片URL")
    private String coverImage;

    /**
     * 是否允许评论
     */
    @Schema(description = "是否允许评论", defaultValue = "true")
    private Boolean allowComment = true;

    /**
     * 来源类型（0-PC端 1-APP端 2-小程序）
     */
    @Schema(description = "来源类型（0-PC端 1-APP端 2-小程序）", defaultValue = "0")
    @Min(value = 0, message = "来源类型无效，有效范围：0-2")
    @Max(value = 2, message = "来源类型无效，有效范围：0-2")
    private Integer sourceType = 0;

    /**
     * 附件DTO
     */
    @Data
    @Schema(description = "附件信息")
    public static class AttachmentDTO implements Serializable {

        private static final long serialVersionUID = 1L;

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
         * 宽度（图片/视频）
         */
        @Schema(description = "宽度")
        private Integer width;

        /**
         * 高度（图片/视频）
         */
        @Schema(description = "高度")
        private Integer height;

        /**
         * 时长（音视频）
         */
        @Schema(description = "时长（秒）")
        private Integer duration;
    }
}
