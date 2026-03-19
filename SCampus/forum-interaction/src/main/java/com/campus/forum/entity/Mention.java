package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @提及实体类
 * 
 * 记录帖子或评论中@提及用户的信息
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@TableName("t_mention")
public class Mention implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 来源类型（1-帖子 2-评论）
     */
    @TableField("source_type")
    private Integer sourceType;

    /**
     * 来源ID（帖子ID或评论ID）
     */
    @TableField("source_id")
    private Long sourceId;

    /**
     * 提及的用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 发起提及的用户ID
     */
    @TableField("from_user_id")
    private Long fromUserId;

    /**
     * 提及内容片段
     */
    @TableField("content")
    private String content;

    /**
     * 是否已读（0-未读，1-已读）
     */
    @TableField("is_read")
    private Integer isRead;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 删除标志（0-未删除，1-已删除）
     */
    @TableLogic
    @TableField("delete_flag")
    private Integer deleteFlag;
}
