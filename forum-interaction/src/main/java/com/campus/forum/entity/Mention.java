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
 * 数据库表: forum_mention
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@TableName("forum_mention")
public class Mention implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发起提及的用户ID（@别人的人）
     */
    @TableField("from_user_id")
    private Long fromUserId;

    /**
     * 被提及的用户ID（被@的人）
     */
    @TableField("to_user_id")
    private Long toUserId;

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
}
