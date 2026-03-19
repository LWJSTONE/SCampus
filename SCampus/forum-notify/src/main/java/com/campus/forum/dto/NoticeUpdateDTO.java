package com.campus.forum.dto;

import lombok.Data;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 通知更新DTO
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
public class NoticeUpdateDTO {

    /**
     * 通知标题
     */
    @Size(max = 100, message = "标题长度不能超过100字符")
    private String title;

    /**
     * 通知内容
     */
    @Size(max = 2000, message = "内容长度不能超过2000字符")
    private String content;

    /**
     * 通知类型
     */
    private Integer type;

    /**
     * 通知级别
     */
    private Integer level;

    /**
     * 是否置顶
     */
    private Integer isTop;

    /**
     * 生效开始时间
     */
    private LocalDateTime effectiveStartTime;

    /**
     * 生效结束时间
     */
    private LocalDateTime effectiveEndTime;

    /**
     * 附件URL
     */
    private String attachments;

    /**
     * 备注
     */
    private String remark;
}
