package com.campus.forum.api.comment;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论数据传输对象
 *
 * @author campus
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论者ID
     */
    private Long userId;

    /**
     * 评论者昵称
     */
    private String authorName;

    /**
     * 评论者头像
     */
    private String authorAvatar;

    /**
     * 父评论ID（0表示一级评论）
     */
    private Long parentId;

    /**
     * 回复目标用户ID
     */
    private Long replyUserId;

    /**
     * 回复目标用户昵称
     */
    private String replyUserName;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论状态：0-正常，1-已删除
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
