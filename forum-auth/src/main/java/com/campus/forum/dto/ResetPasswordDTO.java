package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 重置密码请求DTO
 *
 * 支持两种重置方式：
 * 1. 通过邮箱验证码重置：需要 email, code, password
 * 2. 通过图形验证码重置：需要 username, captcha, captchaKey, newPassword, confirmPassword
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "重置密码请求DTO")
public class ResetPasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名（图形验证码方式）
     */
    @Schema(description = "用户名（图形验证码方式）")
    private String username;

    /**
     * 邮箱（邮箱验证码方式）
     */
    @Schema(description = "邮箱（邮箱验证码方式）")
    private String email;

    /**
     * 邮箱验证码（邮箱验证码方式）
     */
    @Schema(description = "邮箱验证码")
    private String code;

    /**
     * 新密码（邮箱验证码方式）
     */
    @Schema(description = "新密码")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /**
     * 新密码（图形验证码方式）
     */
    @Schema(description = "新密码（图形验证码方式）")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String newPassword;

    /**
     * 确认新密码（图形验证码方式）
     */
    @Schema(description = "确认新密码")
    private String confirmPassword;

    /**
     * 图形验证码
     */
    @Schema(description = "图形验证码")
    private String captcha;

    /**
     * 图形验证码Key
     */
    @Schema(description = "图形验证码Key")
    private String captchaKey;
}
