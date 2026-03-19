package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.forum.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 版块分类实体
 * 支持多级分类（树形结构）
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("forum_category")
public class Category extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 父分类ID（0表示顶级分类）
     */
    private Long parentId;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 帖子数量
     */
    private Integer postCount;

    /**
     * 子分类列表
     */
    @TableField(exist = false)
    private List<Category> children;
}
