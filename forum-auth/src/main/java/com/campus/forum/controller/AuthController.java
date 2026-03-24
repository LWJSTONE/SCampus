package com.campus.forum.controller;

import com.campus.forum.dto.EmailCodeDTO;
import com.campus.forum.dto.LoginDTO;
import com.campus.forum.dto.RefreshTokenDTO;
import com.campus.forum.dto.RegisterDTO;
import com.campus.forum.dto.ResetPasswordDTO;
import com.campus.forum.entity.Result;
import com.campus.forum.service.AuthService;
import com.campus.forum.vo.CaptchaVO;
import com.campus.forum.vo.LoginVO;
import com.campus.forum.vo.TokenVO;
import com.campus.forum.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证控制器
 * 
 * <p>提供用户认证相关的REST API接口，包括：</p>
 * <ul>
 *   <li>用户登录 - POST /login</li>
 *   <li>用户登出 - POST /logout</li>
 *   <li>用户注册 - POST /register</li>
 *   <li>刷新Token - POST /refresh</li>
 *   <li>获取验证码 - GET /captcha</li>
 *   <li>重置密码 - POST /password/reset</li>
 *   <li>获取用户信息 - GET /info</li>
 * </ul>
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口，包括登录、注册、登出等")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * 
     * <p>使用用户名和密码进行登录，返回JWT Token</p>
     * <p>支持验证码校验和记住我功能</p>
     *
     * @param loginDTO 登录请求DTO
     * @param request HTTP请求
     * @return 登录响应（包含Token和用户基本信息）
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码进行登录，返回JWT Token")
    public Result<LoginVO> login(
            @Parameter(description = "登录请求DTO", required = true) 
            @Validated @RequestBody LoginDTO loginDTO,
            HttpServletRequest request) {
        log.info("登录请求：username={}", loginDTO.getUsername());
        return authService.login(loginDTO, request);
    }

    /**
     * 用户登出
     * 
     * <p>退出登录，使Token失效</p>
     *
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出登录，使Token失效")
    public Result<Void> logout(HttpServletRequest request) {
        log.info("登出请求");
        return authService.logout(request);
    }

    /**
     * 用户注册
     * 
     * <p>注册新用户账户</p>
     *
     * @param registerDTO 注册请求DTO
     * @return 操作结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户账户")
    public Result<Void> register(
            @Parameter(description = "注册请求DTO", required = true) 
            @Validated @RequestBody RegisterDTO registerDTO) {
        log.info("注册请求：username={}", registerDTO.getUsername());
        return authService.register(registerDTO);
    }

    /**
     * 刷新Token
     * 
     * <p>使用刷新令牌获取新的访问令牌</p>
     * <p>安全修复：刷新Token时将旧Access Token加入黑名单，防止Token被重复使用</p>
     *
     * @param refreshTokenDTO 刷新Token请求DTO
     * @param request HTTP请求（用于获取当前旧的Access Token）
     * @return 新的Token信息
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用刷新令牌获取新的访问令牌")
    public Result<TokenVO> refreshToken(
            @Parameter(description = "刷新Token请求DTO", required = true) 
            @Validated @RequestBody RefreshTokenDTO refreshTokenDTO,
            HttpServletRequest request) {
        log.info("刷新Token请求");
        // 【安全修复】从请求头获取当前旧的Access Token，用于加入黑名单
        String oldAccessToken = getTokenFromRequest(request);
        return authService.refreshToken(refreshTokenDTO, oldAccessToken);
    }

    /**
     * 从请求头获取Token
     *
     * @param request HTTP请求
     * @return Token字符串
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isEmpty()) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }

    /**
     * 获取验证码
     * 
     * <p>生成图形验证码，返回Base64编码的图片</p>
     *
     * @return 验证码信息（包含验证码Key和图片）
     */
    @GetMapping("/captcha")
    @Operation(summary = "获取验证码", description = "生成图形验证码，返回Base64编码的图片")
    public Result<CaptchaVO> getCaptcha() {
        log.info("获取验证码请求");
        return authService.getCaptcha();
    }

    /**
     * 重置密码
     * 
     * <p>重置用户密码，需要验证码验证</p>
     *
     * @param resetPasswordDTO 重置密码请求DTO
     * @return 操作结果
     */
    @PostMapping("/password/reset")
    @Operation(summary = "重置密码", description = "重置用户密码，需要验证码验证")
    public Result<Void> resetPassword(
            @Parameter(description = "重置密码请求DTO", required = true) 
            @Validated @RequestBody ResetPasswordDTO resetPasswordDTO) {
        log.info("重置密码请求：username={}", resetPasswordDTO.getUsername());
        return authService.resetPassword(resetPasswordDTO);
    }

    /**
     * 获取当前用户信息
     * 
     * <p>根据Token获取当前登录用户的详细信息</p>
     *
     * @param request HTTP请求
     * @return 用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "根据Token获取当前登录用户的详细信息")
    public Result<UserInfoVO> getUserInfo(HttpServletRequest request) {
        log.info("获取用户信息请求");
        return authService.getUserInfo(request);
    }

    /**
     * 发送邮箱验证码
     * 
     * <p>发送邮箱验证码，用于注册或重置密码</p>
     *
     * @param emailCodeDTO 包含邮箱地址的请求DTO
     * @return 操作结果
     */
    @PostMapping("/email/code")
    @Operation(summary = "发送邮箱验证码", description = "发送邮箱验证码，用于注册或重置密码验证")
    public Result<Void> sendEmailCode(
            @Parameter(description = "邮箱验证码请求DTO", required = true) 
            @Validated @RequestBody EmailCodeDTO emailCodeDTO) {
        String email = emailCodeDTO.getEmail();
        log.info("发送邮箱验证码请求：email={}", maskEmail(email));
        return authService.sendEmailCode(email);
    }

    /**
     * 邮箱脱敏处理
     * 保留前缀的前几个字符，中间用*替代，保留@后的域名
     *
     * @param email 原始邮箱
     * @return 脱敏后的邮箱
     */
    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }
        String prefix = email.substring(0, atIndex);
        String suffix = email.substring(atIndex);
        if (prefix.length() <= 2) {
            return prefix.charAt(0) + "***" + suffix;
        }
        return prefix.substring(0, 2) + "***" + suffix;
    }

    /**
     * 健康检查
     * 
     * <p>用于服务健康状态检查</p>
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "服务健康状态检查")
    public Result<String> health() {
        return Result.success("认证服务运行正常");
    }
}
