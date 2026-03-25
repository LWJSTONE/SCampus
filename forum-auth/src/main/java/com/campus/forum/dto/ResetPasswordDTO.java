package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 重置密码请求DTO
 *
 * 安全修复：只支持邮箱验证码方式，必须提供邮箱+用户名+邮箱验证码三者匹配
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "重置密码请求DTO")
public class ResetPasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名（必填）
     */
    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 邮箱（必填）
     */
    @Schema(description = "邮箱")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    /**
     * 邮箱验证码（必填）
     */
    @Schema(description = "邮箱验证码")
    @NotBlank(message = "验证码不能为空")
    private String code;

    /**
     * 新密码（必填）
     */
    @Schema(description = "新密码")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "密码必须包含字母和数字")
    private String password;
}
