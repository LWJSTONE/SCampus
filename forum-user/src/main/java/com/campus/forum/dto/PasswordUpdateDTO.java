package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 密码更新DTO
 * 
 * 用于用户修改密码，包含：
 * - 原密码（用于验证）
 * - 新密码
 * - 确认密码
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "密码更新DTO")
public class PasswordUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 原密码
     */
    @Schema(description = "原密码", required = true)
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     */
    @Schema(description = "新密码", required = true)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String newPassword;

    /**
     * 确认密码
     */
    @Schema(description = "确认密码", required = true)
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
