package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @提及DTO
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "@提及DTO")
public class MentionDTO {

    @NotNull(message = "来源类型不能为空")
    @Schema(description = "来源类型（1-帖子 2-评论）", example = "1")
    private Integer sourceType;

    @NotNull(message = "来源ID不能为空")
    @Schema(description = "来源ID（帖子ID或评论ID）", example = "1")
    private Long sourceId;

    @NotNull(message = "被提及用户ID不能为空")
    @Schema(description = "被提及用户ID", example = "2")
    private Long userId;

    @Schema(description = "提及内容片段")
    private String content;
}
