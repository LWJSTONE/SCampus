package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 发送邮箱验证码请求DTO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "发送邮箱验证码请求DTO")
public class EmailCodeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 邮箱地址
     */
    @Schema(description = "邮箱地址", required = true)
    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 验证码类型（可选）
     * register: 注册验证码
     * reset: 重置密码验证码
     */
    @Schema(description = "验证码类型：register-注册，reset-重置密码")
    @Pattern(regexp = "^(register|reset)?$", message = "验证码类型无效")
    private String type;
}
