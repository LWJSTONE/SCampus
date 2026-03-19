package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 注册请求DTO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "注册请求DTO")
public class RegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @Schema(description = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "用户名必须以字母开头，只能包含字母、数字和下划线")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /**
     * 确认密码
     */
    @Schema(description = "确认密码", required = true)
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    @Size(max = 30, message = "昵称长度不能超过30个字符")
    private String nickname;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", required = true)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

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
     * 验证码
     */
    @Schema(description = "验证码", required = true)
    @NotBlank(message = "验证码不能为空")
    private String captcha;

    /**
     * 验证码Key
     */
    @Schema(description = "验证码Key", required = true)
    @NotBlank(message = "验证码Key不能为空")
    private String captchaKey;
}
