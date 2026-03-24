package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 更新帖子DTO
 * 用于接收编辑帖子的请求参数
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "更新帖子DTO")
public class PostUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 帖子ID
     */
    @Schema(description = "帖子ID", required = true)
    @NotNull(message = "帖子ID不能为空")
    private Long id;

    /**
     * 所属板块ID
     */
    @Schema(description = "所属板块ID")
    private Long forumId;

    /**
     * 帖子标题
     */
    @Schema(description = "帖子标题")
    @Size(min = 2, max = 100, message = "标题长度必须在2-100个字符之间")
    private String title;

    /**
     * 帖子内容
     */
    @Schema(description = "帖子内容")
    @Size(min = 10, max = 50000, message = "内容长度必须在10-50000个字符之间")
    private String content;

    /**
     * 帖子摘要
     */
    @Schema(description = "帖子摘要")
    @Size(max = 200, message = "摘要长度不能超过200个字符")
    private String summary;

    /**
     * 帖子类型
     */
    @Schema(description = "帖子类型（0-普通帖子 1-精华帖 2-置顶帖 3-公告）")
    @Min(value = 0, message = "帖子类型无效，有效范围：0-3")
    @Max(value = 3, message = "帖子类型无效，有效范围：0-3")
    private Integer type;

    /**
     * 标签ID列表
     */
    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

    /**
     * 附件列表
     */
    @Schema(description = "附件列表")
    private List<PostCreateDTO.AttachmentDTO> attachments;

    /**
     * 封面图片URL
     */
    @Schema(description = "封面图片URL")
    private String coverImage;

    /**
     * 是否允许评论
     */
    @Schema(description = "是否允许评论")
    private Boolean allowComment;

    /**
     * 编辑原因
     */
    @Schema(description = "编辑原因")
    @Size(max = 200, message = "编辑原因长度不能超过200个字符")
    private String editReason;
}
