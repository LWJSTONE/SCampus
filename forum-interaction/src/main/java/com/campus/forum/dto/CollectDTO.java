package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 收藏DTO
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "收藏DTO")
public class CollectDTO {

    @NotNull(message = "帖子ID不能为空")
    @Schema(description = "帖子ID", example = "1")
    private Long postId;

    @Schema(description = "收藏夹ID（可选）", example = "1")
    private Long folderId;
}
