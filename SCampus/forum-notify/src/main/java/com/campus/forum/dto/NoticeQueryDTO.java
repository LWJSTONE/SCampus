package com.campus.forum.dto;

import com.campus.forum.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知查询DTO
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeQueryDTO extends PageQueryDTO {

    /**
     * 通知类型
     */
    private Integer type;

    /**
     * 通知级别
     */
    private Integer level;

    /**
     * 发布状态
     */
    private Integer status;

    /**
     * 是否置顶
     */
    private Integer isTop;

    /**
     * 关键词（标题或内容）
     */
    private String keyword;
}
