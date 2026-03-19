package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 验证码DTO
 * 用于验证码校验请求
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "验证码DTO")
public class CaptchaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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
