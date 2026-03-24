package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户更新DTO
 * 
 * 用于用户信息更新，包含可更新的字段：
 * - 昵称
 * - 邮箱
 * - 手机号
 * - 性别
 * - 个人简介
 * - 专业
 * - 年级
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "用户更新DTO")
public class UserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    @Size(min = 2, max = 20, message = "昵称长度必须在2-20个字符之间")
    private String nickname;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 性别（0-未知，1-男，2-女）
     */
    @Schema(description = "性别（0-未知，1-男，2-女）")
    private Integer gender;

    /**
     * 个人简介
     */
    @Schema(description = "个人简介")
    @Size(max = 200, message = "个人简介不能超过200个字符")
    private String bio;

    /**
     * 专业
     */
    @Schema(description = "专业")
    @Size(max = 50, message = "专业不能超过50个字符")
    private String major;

    /**
     * 年级
     */
    @Schema(description = "年级")
    @Size(max = 20, message = "年级不能超过20个字符")
    private String grade;
}
