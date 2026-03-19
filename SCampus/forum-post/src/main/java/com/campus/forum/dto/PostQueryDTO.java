package com.campus.forum.dto;

import com.campus.forum.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 帖子查询DTO
 * 用于接收帖子列表查询的请求参数
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "帖子查询DTO")
public class PostQueryDTO extends PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 板块ID
     */
    @Schema(description = "板块ID")
    private Long forumId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 帖子类型（0-普通帖子 1-精华帖 2-置顶帖 3-公告）
     */
    @Schema(description = "帖子类型（0-普通帖子 1-精华帖 2-置顶帖 3-公告）")
    private Integer type;

    /**
     * 帖子状态（0-待审核 1-已发布 2-已关闭 3-已删除）
     */
    @Schema(description = "帖子状态（0-待审核 1-已发布 2-已关闭 3-已删除）")
    private Integer status;

    /**
     * 是否置顶
     */
    @Schema(description = "是否置顶")
    private Integer isTop;

    /**
     * 是否精华
     */
    @Schema(description = "是否精华")
    private Integer isEssence;

    /**
     * 标签ID
     */
    @Schema(description = "标签ID")
    private Long tagId;

    /**
     * 标签名称
     */
    @Schema(description = "标签名称")
    private String tagName;

    /**
     * 时间范围类型（1-今天 2-本周 3-本月 4-全部）
     */
    @Schema(description = "时间范围类型（1-今天 2-本周 3-本月 4-全部）")
    private Integer timeRange;

    /**
     * 排序类型（1-最新发布 2-最新回复 3-热度排序 4-评论最多 5-点赞最多）
     */
    @Schema(description = "排序类型（1-最新发布 2-最新回复 3-热度排序 4-评论最多 5-点赞最多）")
    private Integer sortType;

    /**
     * 审核状态（0-待审核 1-审核通过 2-审核拒绝）
     */
    @Schema(description = "审核状态（0-待审核 1-审核通过 2-审核拒绝）")
    private Integer auditStatus;

    /**
     * 是否只看我的帖子
     */
    @Schema(description = "是否只看我的帖子")
    private Boolean onlyMe;

    /**
     * 是否包含草稿
     */
    @Schema(description = "是否包含草稿")
    private Boolean includeDraft;
}
