package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 点赞DTO
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "点赞DTO")
public class LikeDTO {

    @NotNull(message = "目标类型不能为空")
    @Schema(description = "目标类型（1-帖子 2-评论）", example = "1")
    private Integer targetType;

    @NotNull(message = "目标ID不能为空")
    @Schema(description = "目标ID（帖子ID或评论ID）", example = "1")
    private Long targetId;
}
