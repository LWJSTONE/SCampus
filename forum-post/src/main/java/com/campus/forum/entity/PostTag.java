package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 帖子标签关联实体类
 * 用于存储帖子与标签的关联关系
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("post_tag")
@Schema(description = "帖子标签关联实体")
public class PostTag extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 帖子ID
     */
    @Schema(description = "帖子ID")
    @TableField("post_id")
    private Long postId;

    /**
     * 标签ID
     */
    @Schema(description = "标签ID")
    @TableField("tag_id")
    private Long tagId;

    /**
     * 标签名称（冗余字段，方便查询）
     */
    @Schema(description = "标签名称")
    @TableField("tag_name")
    private String tagName;

    /**
     * 排序号
     */
    @Schema(description = "排序号")
    @TableField("sort_order")
    private Integer sortOrder;
}
