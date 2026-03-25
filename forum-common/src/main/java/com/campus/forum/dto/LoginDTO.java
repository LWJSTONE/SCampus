package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 登录请求DTO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "登录请求DTO")
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @Schema(description = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /**
     * 验证码
     */
    @Schema(description = "验证码")
    private String captcha;

    /**
     * 验证码Key
     */
    @Schema(description = "验证码Key")
    private String captchaKey;

    /**
     * 记住我
     */
    @Schema(description = "记住我")
    private Boolean rememberMe = false;
}
