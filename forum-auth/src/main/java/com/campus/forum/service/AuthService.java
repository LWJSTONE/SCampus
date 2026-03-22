package com.campus.forum.service;

import com.campus.forum.dto.LoginDTO;
import com.campus.forum.dto.RefreshTokenDTO;
import com.campus.forum.dto.RegisterDTO;
import com.campus.forum.dto.ResetPasswordDTO;
import com.campus.forum.entity.Result;
import com.campus.forum.vo.CaptchaVO;
import com.campus.forum.vo.LoginVO;
import com.campus.forum.vo.TokenVO;
import com.campus.forum.vo.UserInfoVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证服务接口
 * 
 * <p>提供用户认证相关的业务操作接口，包括：</p>
 * <ul>
 *   <li>用户登录</li>
 *   <li>用户登出</li>
 *   <li>用户注册</li>
 *   <li>Token刷新</li>
 *   <li>验证码获取</li>
 *   <li>密码重置</li>
 *   <li>用户信息查询</li>
 * </ul>
 *
 * @author campus
 * @since 2024-01-01
 */
public interface AuthService {

    /**
     * 用户登录
     * 
     * <p>登录流程：</p>
     * <ol>
     *   <li>验证码校验</li>
     *   <li>账户状态检查（是否锁定、禁用）</li>
     *   <li>密码校验</li>
     *   <li>生成JWT Token</li>
     *   <li>记录登录日志</li>
     * </ol>
     *
     * @param loginDTO 登录请求DTO
     * @param request HTTP请求
     * @return 登录响应（包含Token和用户信息）
     */
    Result<LoginVO> login(LoginDTO loginDTO, HttpServletRequest request);

    /**
     * 用户登出
     * 
     * <p>登出流程：</p>
     * <ol>
     *   <li>从请求头获取Token</li>
     *   <li>将Token加入黑名单</li>
     *   <li>清除用户在线状态</li>
     * </ol>
     *
     * @param request HTTP请求
     * @return 操作结果
     */
    Result<Void> logout(HttpServletRequest request);

    /**
     * 用户注册
     * 
     * <p>注册流程：</p>
     * <ol>
     *   <li>验证码校验</li>
     *   <li>用户名唯一性检查</li>
     *   <li>邮箱唯一性检查</li>
     *   <li>密码加密</li>
     *   <li>创建用户记录</li>
     *   <li>分配默认角色</li>
     * </ol>
     *
     * @param registerDTO 注册请求DTO
     * @return 操作结果
     */
    Result<Void> register(RegisterDTO registerDTO);

    /**
     * 刷新Token
     * 
     * <p>使用刷新令牌获取新的访问令牌</p>
     *
     * @param refreshTokenDTO 刷新Token请求DTO
     * @return 新的Token信息
     */
    Result<TokenVO> refreshToken(RefreshTokenDTO refreshTokenDTO);

    /**
     * 获取验证码
     * 
     * <p>生成图形验证码，返回Base64编码的图片和验证码Key</p>
     *
     * @return 验证码信息
     */
    Result<CaptchaVO> getCaptcha();

    /**
     * 重置密码
     * 
     * <p>重置密码流程：</p>
     * <ol>
     *   <li>验证码校验</li>
     *   <li>用户存在性检查</li>
     *   <li>新密码加密</li>
     *   <li>更新密码</li>
     *   <li>使旧Token失效</li>
     * </ol>
     *
     * @param resetPasswordDTO 重置密码请求DTO
     * @return 操作结果
     */
    Result<Void> resetPassword(ResetPasswordDTO resetPasswordDTO);

    /**
     * 获取当前登录用户信息
     * 
     * <p>从Token中解析用户信息，并查询完整的用户详情</p>
     *
     * @param request HTTP请求
     * @return 用户信息
     */
    Result<UserInfoVO> getUserInfo(HttpServletRequest request);

    /**
     * 校验验证码
     *
     * @param captchaKey 验证码Key
     * @param captcha 验证码
     * @return 校验结果
     */
    boolean validateCaptcha(String captchaKey, String captcha);

    /**
     * 检查用户是否被锁定
     *
     * @param userId 用户ID
     * @return 是否被锁定
     */
    boolean isUserLocked(Long userId);

    /**
     * 检查登录失败次数是否超限
     *
     * @param userId 用户ID
     * @return 是否超限
     */
    boolean isLoginFailCountExceeded(Long userId);

    /**
     * 发送邮箱验证码
     * 
     * <p>发送流程：</p>
     * <ol>
     *   <li>验证邮箱格式</li>
     *   <li>检查发送频率限制</li>
     *   <li>生成6位随机验证码</li>
     *   <li>发送邮件</li>
     *   <li>将验证码存入Redis（有效期5分钟）</li>
     * </ol>
     *
     * @param email 邮箱地址
     * @return 操作结果
     */
    Result<Void> sendEmailCode(String email);
}
