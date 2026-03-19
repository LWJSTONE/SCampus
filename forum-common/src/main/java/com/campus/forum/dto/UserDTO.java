package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户DTO
 * 用于用户信息更新等操作
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "用户DTO")
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long id;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    @Size(max = 30, message = "昵称长度不能超过30个字符")
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
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatar;

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
     * 学校ID
     */
    @Schema(description = "学校ID")
    private Long schoolId;

    /**
     * 学号
     */
    @Schema(description = "学号")
    private String studentNo;

    /**
     * 专业
     */
    @Schema(description = "专业")
    @Size(max = 50, message = "专业长度不能超过50个字符")
    private String major;

    /**
     * 年级
     */
    @Schema(description = "年级")
    @Size(max = 20, message = "年级长度不能超过20个字符")
    private String grade;
}
