package com.campus.forum.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Token响应VO
 * 用于Token刷新等场景
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "Token响应VO")
public class TokenVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌")
    private String accessToken;

    /**
     * 刷新令牌
     */
    @Schema(description = "刷新令牌")
    private String refreshToken;

    /**
     * 令牌类型
     */
    @Schema(description = "令牌类型", defaultValue = "Bearer")
    private String tokenType = "Bearer";

    /**
     * 过期时间（秒）
     */
    @Schema(description = "过期时间（秒）")
    private Long expiresIn;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
}
