package com.campus.forum.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类树形VO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "分类树形VO")
public class CategoryTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类图标")
    private String icon;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "父分类ID")
    private Long parentId;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "子分类列表")
    private List<CategoryTreeVO> children;

    @Schema(description = "版块列表")
    private List<ForumVO> forums;
}
