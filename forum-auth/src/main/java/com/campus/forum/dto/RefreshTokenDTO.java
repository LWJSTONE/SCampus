package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 刷新Token请求DTO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "刷新Token请求DTO")
public class RefreshTokenDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 刷新令牌
     */
    @Schema(description = "刷新令牌", required = true)
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
