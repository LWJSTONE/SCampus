package com.campus.forum.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 验证码响应VO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "验证码响应VO")
public class CaptchaVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 验证码Key（用于后续校验）
     */
    @Schema(description = "验证码Key")
    private String captchaKey;

    /**
     * 验证码图片（Base64编码）
     */
    @Schema(description = "验证码图片（Base64编码）")
    private String captchaImage;

    /**
     * 验证码过期时间（秒）
     */
    @Schema(description = "验证码过期时间（秒）")
    private Long expireTime;
}
