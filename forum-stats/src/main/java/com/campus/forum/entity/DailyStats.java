package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日统计实体类
 * 
 * 用于记录每日的系统统计数据，包括用户增长、帖子发布、互动数据等
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@TableName("daily_stats")
public class DailyStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 统计日期
     */
    private LocalDate statsDate;

    /**
     * 新增用户数
     */
    private Integer newUsers;

    /**
     * 活跃用户数
     */
    private Integer activeUsers;

    /**
     * 新增帖子数
     */
    private Integer newPosts;

    /**
     * 新增评论数
     */
    private Integer newComments;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 关注数
     */
    private Integer followCount;

    /**
     * 浏览量
     */
    private Long viewCount;

    /**
     * 总用户数（累计）
     */
    private Long totalUsers;

    /**
     * 总帖子数（累计）
     */
    private Long totalPosts;

    /**
     * 总评论数（累计）
     */
    private Long totalComments;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleteFlag;
}
