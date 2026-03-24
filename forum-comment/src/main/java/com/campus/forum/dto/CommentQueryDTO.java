package com.campus.forum.dto;

import com.campus.forum.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 评论查询DTO
 * 
 * 用于接收查询评论列表的请求参数
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "评论查询DTO")
public class CommentQueryDTO extends PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 帖子ID
     * 必填，查询指定帖子的评论
     */
    @Schema(description = "帖子ID", required = true)
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    /**
     * 父评论ID
     * 非必填，查询指定评论的回复列表
     */
    @Schema(description = "父评论ID（查询回复列表时使用）")
    private Long parentId;

    /**
     * 用户ID
     * 非必填，查询指定用户的评论
     */
    @Schema(description = "用户ID（查询用户评论列表时使用）")
    private Long userId;

    /**
     * 评论状态
     * 非必填，筛选评论状态
     */
    @Schema(description = "评论状态（0-正常，1-已删除，2-被屏蔽）")
    private Integer status;

    /**
     * 是否只查询热门评论
     */
    @Schema(description = "是否只查询热门评论")
    private Boolean onlyHot;

    /**
     * 排序方式
     * time-按时间排序
     * like-按点赞数排序
     */
    @Schema(description = "排序方式（time-时间，like-点赞数）")
    private String sortBy;

    /**
     * 是否包含回复数量
     */
    @Schema(description = "是否包含回复数量")
    private Boolean includeReplyCount;

    /**
     * 当前登录用户ID
     * 用于判断是否已点赞
     */
    @Schema(description = "当前登录用户ID", hidden = true)
    private Long currentUserId;
}
