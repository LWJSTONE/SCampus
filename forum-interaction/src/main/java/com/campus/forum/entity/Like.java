package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点赞实体类
 * 
 * 记录用户对帖子的点赞信息
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@TableName("forum_like")
public class Like implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 目标类型（1-帖子 2-评论）
     */
    @TableField("target_type")
    private Integer targetType;

    /**
     * 目标ID（帖子ID或评论ID）
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 删除标志（0-未取消，1-已取消）
     * 用于实现点赞/取消点赞的状态切换，避免频繁删除和插入记录
     */
    @TableField("delete_flag")
    private Integer deleteFlag;
}
