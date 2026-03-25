package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 版块创建/更新DTO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "版块创建/更新DTO")
public class ForumDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "版块名称", required = true)
    @NotBlank(message = "版块名称不能为空")
    private String name;

    @Schema(description = "版块图标")
    private String icon;

    @Schema(description = "版块描述")
    private String description;

    @Schema(description = "所属分类ID", required = true)
    @NotNull(message = "所属分类不能为空")
    private Long categoryId;

    @Schema(description = "版主用户ID")
    private Long moderatorId;

    @Schema(description = "排序号")
    private Integer sort = 0;

    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status = 1;

    @Schema(description = "是否允许发帖")
    private Boolean allowPost = true;

    @Schema(description = "是否允许回复")
    private Boolean allowReply = true;

    @Schema(description = "版块规则")
    private String rules;
}
