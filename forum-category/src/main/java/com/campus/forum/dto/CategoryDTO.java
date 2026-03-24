package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类创建/更新DTO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "分类创建/更新DTO")
public class CategoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分类名称", required = true)
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @Schema(description = "分类图标")
    private String icon;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "父分类ID（0表示顶级分类）")
    private Long parentId = 0L;

    @Schema(description = "排序号")
    private Integer sort = 0;

    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status = 1;
}
