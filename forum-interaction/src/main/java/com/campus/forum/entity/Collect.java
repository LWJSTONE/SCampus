package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏实体类
 * 
 * 记录用户对帖子的收藏信息
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@TableName("forum_collect")
public class Collect implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 帖子ID
     */
    @TableField("post_id")
    private Long postId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 收藏夹ID（可选）
     */
    @TableField("folder_id")
    private Long folderId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 删除标志（0-未取消，1-已取消）
     * 用于实现收藏/取消收藏的状态切换，避免频繁删除和插入记录
     */
    @TableField("delete_flag")
    private Integer deleteFlag;
}
