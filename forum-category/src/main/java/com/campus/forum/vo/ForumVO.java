package com.campus.forum.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 版块VO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "版块VO")
public class ForumVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "版块ID")
    private Long id;

    @Schema(description = "版块名称")
    private String name;

    @Schema(description = "版块图标")
    private String icon;

    @Schema(description = "版块描述")
    private String description;

    @Schema(description = "所属分类ID")
    private Long categoryId;

    @Schema(description = "所属分类名称")
    private String categoryName;

    @Schema(description = "版主用户ID")
    private Long moderatorId;

    @Schema(description = "版主用户名")
    private String moderatorName;

    @Schema(description = "帖子数量")
    private Integer postCount;

    @Schema(description = "今日帖子数")
    private Integer todayPostCount;

    @Schema(description = "最后发帖时间")
    private LocalDateTime lastPostTime;

    @Schema(description = "最后发帖用户ID")
    private Long lastPostUserId;

    @Schema(description = "最后发帖标题")
    private String lastPostTitle;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;

    @Schema(description = "是否允许发帖")
    private Boolean allowPost;

    @Schema(description = "是否允许回复")
    private Boolean allowReply;

    @Schema(description = "版块规则")
    private String rules;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "版主列表")
    private List<ModeratorVO> moderators;
}
