package com.campus.forum.dto;

import com.campus.forum.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询DTO
 * 
 * 用于用户列表查询，支持以下筛选条件：
 * - 关键词搜索（用户名、昵称、邮箱、手机号）
 * - 状态筛选
 * - 性别筛选
 * - 学校筛选
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户查询DTO")
public class UserQueryDTO extends PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态（0-禁用，1-正常）
     */
    @Schema(description = "状态（0-禁用，1-正常）")
    private Integer status;

    /**
     * 性别（0-未知，1-男，2-女）
     */
    @Schema(description = "性别（0-未知，1-男，2-女）")
    private Integer gender;

    /**
     * 学校ID
     */
    @Schema(description = "学校ID")
    private Long schoolId;

    /**
     * 专业
     */
    @Schema(description = "专业")
    private String major;

    /**
     * 年级
     */
    @Schema(description = "年级")
    private String grade;
}
