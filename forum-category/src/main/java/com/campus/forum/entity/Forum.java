package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.forum.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 版块实体
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("forum_forum")
public class Forum extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 版块名称
     */
    private String name;

    /**
     * 版块图标
     */
    private String icon;

    /**
     * 版块描述
     */
    private String description;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 版主用户ID（主要版主）
     */
    private Long moderatorId;

    /**
     * 帖子数量
     */
    private Integer postCount;

    /**
     * 今日帖子数
     */
    private Integer todayPostCount;

    /**
     * 最后发帖时间
     */
    private java.time.LocalDateTime lastPostTime;

    /**
     * 最后发帖用户ID
     */
    private Long lastPostUserId;

    /**
     * 最后发帖标题
     */
    private String lastPostTitle;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 是否允许发帖
     */
    private Boolean allowPost;

    /**
     * 是否允许回复
     */
    private Boolean allowReply;

    /**
     * 版块规则
     */
    private String rules;
}
