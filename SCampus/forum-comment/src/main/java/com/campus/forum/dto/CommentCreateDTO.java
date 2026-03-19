package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 评论创建DTO
 * 
 * 用于接收用户发布评论的请求数据
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "评论创建DTO")
public class CommentCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 帖子ID
     * 必填，指定评论所属的帖子
     */
    @Schema(description = "帖子ID", required = true)
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    /**
     * 父评论ID
     * 非必填，0或不传表示一级评论
     * 非0表示回复指定的评论
     */
    @Schema(description = "父评论ID（0或不传表示一级评论）")
    private Long parentId;

    /**
     * 回复目标用户ID
     * 非必填，用于楼中楼回复时标识被回复的用户
     */
    @Schema(description = "回复目标用户ID")
    private Long replyToUserId;

    /**
     * 评论内容
     * 必填，最大长度500字
     */
    @Schema(description = "评论内容", required = true, maxLength = 500)
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字")
    private String content;

    /**
     * 回复用户名
     * 前端传递，用于展示@用户名
     */
    @Schema(description = "回复用户名（用于@用户）")
    private String replyUserName;
}
