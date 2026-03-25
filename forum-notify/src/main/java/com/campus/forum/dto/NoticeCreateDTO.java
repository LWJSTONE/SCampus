package com.campus.forum.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 通知创建DTO
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
public class NoticeCreateDTO {

    /**
     * 通知标题
     */
    @NotBlank(message = "通知标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100字符")
    private String title;

    /**
     * 通知内容
     */
    @NotBlank(message = "通知内容不能为空")
    @Size(max = 2000, message = "内容长度不能超过2000字符")
    private String content;

    /**
     * 通知类型
     * 1-系统公告
     * 2-活动通知
     * 3-版本更新
     * 4-其他
     */
    @NotNull(message = "通知类型不能为空")
    private Integer type;

    /**
     * 通知级别
     * 1-普通
     * 2-重要
     * 3-紧急
     */
    private Integer level = 1;

    /**
     * 是否置顶
     */
    private Integer isTop = 0;

    /**
     * 生效开始时间
     */
    private LocalDateTime effectiveStartTime;

    /**
     * 生效结束时间
     */
    private LocalDateTime effectiveEndTime;

    /**
     * 附件URL（JSON格式）
     */
    private String attachments;

    /**
     * 备注
     */
    private String remark;
}
