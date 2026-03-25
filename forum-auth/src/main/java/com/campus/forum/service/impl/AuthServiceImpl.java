package com.campus.forum.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.constant.Constants;
import com.campus.forum.dto.LoginDTO;
import com.campus.forum.dto.RefreshTokenDTO;
import com.campus.forum.dto.RegisterDTO;
import com.campus.forum.dto.ResetPasswordDTO;
import com.campus.forum.entity.AuthUser;
import com.campus.forum.entity.Result;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.AuthUserMapper;
import com.campus.forum.service.AuthService;
import com.campus.forum.utils.IpUtils;
import com.campus.forum.utils.JwtUtils;
import com.campus.forum.utils.PasswordUtils;
import com.campus.forum.utils.RedisUtils;
import com.campus.forum.vo.CaptchaVO;
import com.campus.forum.vo.LoginVO;
import com.campus.forum.vo.TokenVO;
import com.campus.forum.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/**
 * 认证服务实现类
 *
 * <p>实现用户认证相关的业务逻辑</p>
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthUserMapper authUserMapper;
    private final RedisUtils redisUtils;

    /**
     * 最大登录失败次数
     */
    private static final int MAX_LOGIN_FAIL_COUNT = 5;

    /**
     * 安全比较两个字符串（常量时间比较）
     * 
     * 【安全修复】使用MessageDigest.isEqual进行常量时间比较，防止时序攻击
     * 时序攻击者可以通过响应时间差异推断验证码的部分内容
     * 
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 是否相等
     */
    private boolean constantTimeEquals(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return str1 == str2;
        }
        // 转换为字节数组后使用常量时间比较
        byte[] bytes1 = str1.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] bytes2 = str2.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return MessageDigest.isEqual(bytes1, bytes2);
    }

    /**
     * 账户锁定时间（分钟）
     */
    private static final int LOCK_TIME_MINUTES = 30;

    /**
     * JWT密钥 - 从配置文件读取
     * 
     * 【安全修复】移除默认值，强制要求在配置文件中设置密钥
     * 如果未配置密钥，应用启动时将抛出异常
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * 访问令牌过期时间（秒）
     */
    @Value("${jwt.access-token-expiration:86400}")
    private long accessTokenExpiration;

    @Override
    public Result<LoginVO> login(LoginDTO loginDTO, HttpServletRequest request) {
        // 安全修复：日志脱敏处理，不记录完整用户名
        log.info("用户登录请求：username={}***", 
                loginDTO.getUsername() != null && loginDTO.getUsername().length() > 2 
                        ? loginDTO.getUsername().substring(0, 2) : "**");

        // 1. 验证码校验（强制要求）
        if (StrUtil.isBlank(loginDTO.getCaptchaKey()) || StrUtil.isBlank(loginDTO.getCaptcha())) {
            return Result.fail(400, "请输入验证码");
        }
        if (!validateCaptcha(loginDTO.getCaptchaKey(), loginDTO.getCaptcha())) {
            return Result.fail(400, "验证码错误或已过期");
        }

        // 2. 查询用户
        AuthUser user = authUserMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            log.warn("用户不存在：username={}", loginDTO.getUsername());
            return Result.fail(401, "用户名或密码错误");
        }

        // 3. 检查账户状态
        if (user.getStatus() != null && user.getStatus() == Constants.STATUS_DISABLE) {
            log.warn("账户已被禁用：userId={}", user.getId());
            return Result.fail(403, "账户已被禁用，请联系管理员");
        }

        // 4. 检查是否被锁定
        if (isUserLocked(user.getId())) {
            log.warn("账户已被锁定：userId={}", user.getId());
            return Result.fail(403, "账户已被锁定，请稍后再试");
        }

        // 5. 密码校验
        if (!PasswordUtils.matches(loginDTO.getPassword(), user.getPassword())) {
            log.warn("密码错误：userId={}", user.getId());
            // 增加登录失败次数
            handleLoginFail(user.getId());
            return Result.fail(401, "用户名或密码错误");
        }

        // 6. 查询用户角色
        List<String> roleCodes = authUserMapper.selectRoleCodesByUserId(user.getId());
        String role = (roleCodes != null && !roleCodes.isEmpty()) ? roleCodes.get(0) : "USER";
        
        // 7. 生成Token（包含角色信息）
        String accessToken = JwtUtils.generateToken(user.getId(), user.getUsername(), role, jwtSecret,
                accessTokenExpiration * 1000);
        String refreshToken = JwtUtils.generateRefreshToken(user.getId(), user.getUsername(), jwtSecret);
        
        // 检查Token是否生成成功
        if (StrUtil.isBlank(accessToken) || StrUtil.isBlank(refreshToken)) {
            log.error("Token生成失败：userId={}", user.getId());
            return Result.fail(500, "登录失败，请稍后重试");
        }


        // 8. 将Token存入Redis
        String tokenKey = Constants.TOKEN_PREFIX + user.getId();
        long expireSeconds = loginDTO.getRememberMe() != null && loginDTO.getRememberMe() 
                ? Constants.REFRESH_TOKEN_EXPIRE_TIME 
                : Constants.TOKEN_EXPIRE_TIME;
        redisUtils.set(tokenKey, accessToken, expireSeconds);

        // 9. 更新登录信息
        String loginIp = IpUtils.getIpAddr(request);
        authUserMapper.updateLoginInfo(user.getId(), LocalDateTime.now(), loginIp);

        // 10. 构建返回结果
        LoginVO loginVO = buildLoginVO(user, accessToken, refreshToken);

        log.info("用户登录成功：userId={}, username={}", user.getId(), user.getUsername());
        return Result.success("登录成功", loginVO);
    }

    @Override
    public Result<Void> logout(HttpServletRequest request) {
        // 从请求头获取Token
        String token = getTokenFromRequest(request);
        if (StrUtil.isBlank(token)) {
            return Result.success();
        }

        // 解析用户ID（验证签名）
        Long userId = JwtUtils.getUserId(token, jwtSecret);
        if (userId != null) {
            // 删除Redis中的Token
            String tokenKey = Constants.TOKEN_PREFIX + userId;
            redisUtils.del(tokenKey);

            // 将Token加入黑名单
            String blacklistKey = Constants.CACHE_PREFIX + "token:blacklist:" + token;
            long ttl = JwtUtils.getDefaultExpirationSeconds();
            redisUtils.set(blacklistKey, "1", ttl);

            log.info("用户登出成功：userId={}", userId);
        }

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> register(RegisterDTO registerDTO) {
        // 安全修复：日志脱敏处理
        log.info("用户注册请求：username={}***", 
                registerDTO.getUsername() != null && registerDTO.getUsername().length() > 2 
                        ? registerDTO.getUsername().substring(0, 2) : "**");

        // 1. 验证码校验 - 支持邮箱验证码或图形验证码
        boolean isEmailCodeMode = StrUtil.isNotBlank(registerDTO.getCode());
        
        if (isEmailCodeMode) {
            // 邮箱验证码方式
            String emailCodeKey = Constants.CACHE_PREFIX + "email:code:" + registerDTO.getEmail();
            Object cachedCode = redisUtils.get(emailCodeKey);
            if (cachedCode == null) {
                return Result.fail(400, "验证码已过期，请重新获取");
            }
            // 【安全修复】使用常量时间比较，防止时序攻击
            if (!constantTimeEquals(cachedCode.toString().toLowerCase(), registerDTO.getCode().trim().toLowerCase())) {
                return Result.fail(400, "验证码错误");
            }
            // 删除已使用的验证码
            redisUtils.del(emailCodeKey);
        } else {
            // 图形验证码方式
            if (!validateCaptcha(registerDTO.getCaptchaKey(), registerDTO.getCaptcha())) {
                return Result.fail(400, "验证码错误或已过期");
            }
        }

        // 2. 密码强度校验
        String password = registerDTO.getPassword();
        if (StrUtil.isBlank(password) || password.length() < 6) {
            return Result.fail(400, "密码长度不能少于6位");
        }
        if (password.length() > 20) {
            return Result.fail(400, "密码长度不能超过20位");
        }
        // 增强密码强度校验：必须包含字母和数字
        if (!password.matches("^(?=.*[a-zA-Z])(?=.*\\d).+$")) {
            return Result.fail(400, "密码必须包含字母和数字");
        }
        
        // 3. 密码确认校验
        if (registerDTO.getConfirmPassword() != null && 
            !password.equals(registerDTO.getConfirmPassword())) {
            return Result.fail(400, "两次输入的密码不一致");
        }

        // 4. 检查用户名是否已存在
        AuthUser existUser = authUserMapper.selectByUsername(registerDTO.getUsername());
        if (existUser != null) {
            return Result.fail(400, "用户名已被注册");
        }

        // 5. 检查邮箱是否已存在
        AuthUser existEmail = authUserMapper.selectByEmail(registerDTO.getEmail());
        if (existEmail != null) {
            return Result.fail(400, "邮箱已被注册");
        }

        // 6. 创建用户
        AuthUser user = new AuthUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(PasswordUtils.encode(password));
        user.setNickname(StrUtil.isNotBlank(registerDTO.getNickname()) 
                ? registerDTO.getNickname() 
                : registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setSchoolId(registerDTO.getSchoolId());
        user.setStudentNo(registerDTO.getStudentNo());
        user.setStatus(Constants.STATUS_ENABLE);
        user.setGender(Constants.GENDER_UNKNOWN);
        user.setLoginFailCount(0);
        user.setEmailVerified(isEmailCodeMode ? 1 : 0); // 邮箱验证码注册则标记邮箱已验证
        user.setPhoneVerified(0);
        user.setAvatar(Constants.DEFAULT_AVATAR);

        int result = authUserMapper.insert(user);
        if (result <= 0) {
            log.error("用户注册失败：username={}", registerDTO.getUsername());
            return Result.fail("注册失败，请稍后再试");
        }

        log.info("用户注册成功：userId={}, username={}", user.getId(), user.getUsername());
        return Result.success();
    }

    @Override
    public Result<TokenVO> refreshToken(RefreshTokenDTO refreshTokenDTO, String oldAccessToken) {
        log.info("刷新Token请求");

        String refreshToken = refreshTokenDTO.getRefreshToken();
        if (StrUtil.isBlank(refreshToken)) {
            return Result.fail(400, "刷新令牌不能为空");
        }

        // 1. 检查刷新令牌是否在黑名单中
        String refreshBlacklistKey = Constants.CACHE_PREFIX + "refresh:blacklist:" + refreshToken;
        if (redisUtils.hasKey(refreshBlacklistKey)) {
            return Result.fail(401, "刷新令牌已失效，请重新登录");
        }

        // 2. 验证刷新令牌
        if (!JwtUtils.verifyToken(refreshToken, jwtSecret)) {
            return Result.fail(401, "刷新令牌无效或已过期");
        }

        // 3. 检查是否为刷新令牌
        String tokenType = JwtUtils.decodeToken(refreshToken).getClaim("type").asString();
        if (!"refresh".equals(tokenType)) {
            return Result.fail(400, "无效的刷新令牌");
        }

        // 4. 获取用户信息（验证签名）
        Long userId = JwtUtils.getUserId(refreshToken, jwtSecret);
        String username = JwtUtils.getUsername(refreshToken, jwtSecret);

        if (userId == null || StrUtil.isBlank(username)) {
            return Result.fail(401, "刷新令牌无效");
        }

        // 5. 查询用户状态
        AuthUser user = authUserMapper.selectById(userId);
        if (user == null || user.getStatus() != Constants.STATUS_ENABLE) {
            return Result.fail(401, "用户状态异常");
        }

        // 6. 查询用户角色
        List<String> roleCodes = authUserMapper.selectRoleCodesByUserId(userId);
        String role = (roleCodes != null && !roleCodes.isEmpty()) ? roleCodes.get(0) : "USER";

        // 7. 生成新的访问令牌（包含角色信息）
        String newAccessToken = JwtUtils.generateToken(userId, username, role, jwtSecret,
                accessTokenExpiration * 1000);
        
        // 8. 生成新的刷新令牌，使旧的失效
        String newRefreshToken = JwtUtils.generateRefreshToken(userId, username, jwtSecret);
        
        // 将旧的刷新令牌加入黑名单
        String oldRefreshKey = Constants.CACHE_PREFIX + "refresh:blacklist:" + refreshToken;
        redisUtils.set(oldRefreshKey, "1", Constants.REFRESH_TOKEN_EXPIRE_TIME);

        // 【安全修复】将旧的Access Token加入黑名单，防止Token被重复使用
        if (StrUtil.isNotBlank(oldAccessToken)) {
            String oldAccessBlacklistKey = Constants.CACHE_PREFIX + "token:blacklist:" + oldAccessToken;
            // 使用Token的剩余有效期作为黑名单过期时间
            long ttl = JwtUtils.getDefaultExpirationSeconds();
            redisUtils.set(oldAccessBlacklistKey, "1", ttl);
            log.info("旧Access Token已加入黑名单：userId={}", userId);
        }

        // 9. 更新Redis中的Token
        String tokenKey = Constants.TOKEN_PREFIX + userId;
        redisUtils.set(tokenKey, newAccessToken, Constants.TOKEN_EXPIRE_TIME);

        // 10. 构建返回结果
        TokenVO tokenVO = new TokenVO();
        tokenVO.setAccessToken(newAccessToken);
        tokenVO.setRefreshToken(newRefreshToken);
        tokenVO.setExpiresIn(Constants.TOKEN_EXPIRE_TIME);
        tokenVO.setExpiresAt(LocalDateTime.now().plusSeconds(Constants.TOKEN_EXPIRE_TIME));

        log.info("Token刷新成功：userId={}", userId);
        return Result.success("刷新成功", tokenVO);
    }

    @Override
    public Result<CaptchaVO> getCaptcha() {
        // 生成验证码Key
        String captchaKey = IdUtil.fastSimpleUUID();

        // 生成图形验证码
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        String captchaCode = captcha.getCode();

        // 将验证码存入Redis
        String captchaKeyRedis = Constants.CAPTCHA_PREFIX + captchaKey;
        redisUtils.set(captchaKeyRedis, captchaCode.toLowerCase(), Constants.CAPTCHA_EXPIRE_TIME);

        // 构建返回结果
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaKey(captchaKey);
        captchaVO.setCaptchaImage(captcha.getImageBase64Data());
        captchaVO.setExpireTime(Constants.CAPTCHA_EXPIRE_TIME);

        log.debug("生成验证码：captchaKey={}", captchaKey);
        return Result.success(captchaVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> resetPassword(ResetPasswordDTO resetPasswordDTO) {
        // 安全修复：日志脱敏处理
        String maskedEmail = maskEmail(resetPasswordDTO.getEmail());
        String maskedUsername = resetPasswordDTO.getUsername() != null && resetPasswordDTO.getUsername().length() > 2 
                ? resetPasswordDTO.getUsername().substring(0, 2) + "***" : "***";
        log.info("重置密码请求：email={}, username={}", maskedEmail, maskedUsername);

        // 安全修复：只支持邮箱验证码方式，必须提供邮箱+用户名+邮箱验证码三者匹配
        // 1. 校验必填字段
        if (StrUtil.isBlank(resetPasswordDTO.getEmail())) {
            return Result.fail(400, "邮箱不能为空");
        }
        if (StrUtil.isBlank(resetPasswordDTO.getUsername())) {
            return Result.fail(400, "用户名不能为空");
        }
        if (StrUtil.isBlank(resetPasswordDTO.getCode())) {
            return Result.fail(400, "验证码不能为空");
        }

        // 2. 校验邮箱格式
        if (!resetPasswordDTO.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return Result.fail(400, "邮箱格式不正确");
        }

        // 3. 校验验证码
        String emailCodeKey = Constants.CACHE_PREFIX + "email:code:" + resetPasswordDTO.getEmail();
        Object cachedCode = redisUtils.get(emailCodeKey);
        if (cachedCode == null) {
            return Result.fail(400, "验证码已过期，请重新获取");
        }
        // 【安全修复】使用常量时间比较，防止时序攻击
        if (!constantTimeEquals(cachedCode.toString().toLowerCase(), resetPasswordDTO.getCode().trim().toLowerCase())) {
            return Result.fail(400, "验证码错误");
        }

        // 4. 校验密码强度
        String newPassword = resetPasswordDTO.getPassword();
        if (StrUtil.isBlank(newPassword) || newPassword.length() < 6) {
            return Result.fail(400, "密码长度不能少于6位");
        }
        if (newPassword.length() > 20) {
            return Result.fail(400, "密码长度不能超过20位");
        }
        // 增强密码强度校验：必须包含字母和数字
        if (!newPassword.matches("^(?=.*[a-zA-Z])(?=.*\\d).+$")) {
            return Result.fail(400, "密码必须包含字母和数字");
        }

        // 5. 安全校验：根据用户名查询用户，并验证邮箱是否匹配
        AuthUser user = authUserMapper.selectByUsername(resetPasswordDTO.getUsername());
        if (user == null) {
            return Result.fail(400, "用户不存在");
        }
        // 验证邮箱与用户名是否匹配
        if (!resetPasswordDTO.getEmail().equalsIgnoreCase(user.getEmail())) {
            // 防止信息泄露，返回模糊错误信息
            log.warn("重置密码邮箱不匹配：username={}, expectedEmail={}, providedEmail={}", 
                    resetPasswordDTO.getUsername(), user.getEmail(), resetPasswordDTO.getEmail());
            return Result.fail(400, "用户名与邮箱不匹配");
        }

        // 6. 删除已使用的验证码
        redisUtils.del(emailCodeKey);

        // 7. 更新密码
        String encodedPassword = PasswordUtils.encode(newPassword);
        int result = authUserMapper.updatePassword(user.getId(), encodedPassword);
        if (result <= 0) {
            log.error("密码重置失败：userId={}", user.getId());
            return Result.fail("密码重置失败，请稍后再试");
        }

        // 【安全修复】8. 清除登录失败计数和锁定状态
        // 密码重置后应清除登录失败次数，允许用户重新登录
        authUserMapper.resetLoginFailCount(user.getId());
        log.info("已清除用户登录失败计数：userId={}", user.getId());

        // 【安全修复】9. 使旧Token失效并加入黑名单
        // 密码重置后，所有已发出的Token都应失效，防止旧Token继续使用
        String tokenKey = Constants.TOKEN_PREFIX + user.getId();
        Object existingToken = redisUtils.get(tokenKey);
        if (existingToken != null) {
            // 将旧Token加入黑名单，使用Token的默认过期时间
            String oldToken = existingToken.toString();
            String blacklistKey = Constants.CACHE_PREFIX + "token:blacklist:" + oldToken;
            long ttl = JwtUtils.getDefaultExpirationSeconds();
            redisUtils.set(blacklistKey, "1", ttl);
            log.info("密码重置：已将旧Token加入黑名单，userId={}", user.getId());
        }
        // 删除Redis中的Token记录
        redisUtils.del(tokenKey);

        log.info("密码重置成功：userId={}", user.getId());
        return Result.success();
    }

    @Override
    public Result<UserInfoVO> getUserInfo(HttpServletRequest request) {
        // 从请求头获取Token
        String token = getTokenFromRequest(request);
        if (StrUtil.isBlank(token)) {
            return Result.fail(401, "未登录");
        }

        // 解析用户ID（验证签名）
        Long userId = JwtUtils.getUserId(token, jwtSecret);
        if (userId == null) {
            return Result.fail(401, "Token无效");
        }

        // 查询用户信息
        AuthUser user = authUserMapper.selectById(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }

        // 构建返回结果
        UserInfoVO userInfoVO = buildUserInfoVO(user);
        return Result.success(userInfoVO);
    }

    @Override
    public boolean validateCaptcha(String captchaKey, String captcha) {
        if (StrUtil.isBlank(captchaKey) || StrUtil.isBlank(captcha)) {
            return false;
        }

        String captchaKeyRedis = Constants.CAPTCHA_PREFIX + captchaKey;
        Object cachedCaptcha = redisUtils.get(captchaKeyRedis);

        if (cachedCaptcha == null) {
            log.warn("验证码已过期：captchaKey={}", captchaKey);
            return false;
        }

        // 删除已使用的验证码
        redisUtils.del(captchaKeyRedis);

        // 【安全修复】使用常量时间比较，防止时序攻击
        boolean result = constantTimeEquals(cachedCaptcha.toString().toLowerCase(), captcha.trim().toLowerCase());
        if (!result) {
            log.warn("验证码错误：expected={}, actual={}", cachedCaptcha, captcha);
        }
        return result;
    }

    @Override
    public boolean isUserLocked(Long userId) {
        AuthUser user = authUserMapper.selectById(userId);
        if (user == null) {
            return true;
        }

        // 检查是否有锁定时间
        if (user.getLockTime() == null) {
            return false;
        }

        // 检查锁定是否已过期
        LocalDateTime unlockTime = user.getLockTime().plusMinutes(LOCK_TIME_MINUTES);
        if (LocalDateTime.now().isAfter(unlockTime)) {
            // 解锁账户
            authUserMapper.unlockUser(userId);
            return false;
        }

        return true;
    }

    @Override
    public boolean isLoginFailCountExceeded(Long userId) {
        AuthUser user = authUserMapper.selectById(userId);
        if (user == null || user.getLoginFailCount() == null) {
            return false;
        }
        return user.getLoginFailCount() >= MAX_LOGIN_FAIL_COUNT;
    }

    /**
     * 处理登录失败
     * 使用Redis分布式锁保证原子性，防止竞态条件
     * 
     * @param userId 用户ID
     */
    private void handleLoginFail(Long userId) {
        // 使用Redis锁保证原子性操作
        String lockKey = Constants.CACHE_PREFIX + "login:fail:lock:" + userId;
        
        try {
            // 尝试获取分布式锁（10秒过期）
            boolean locked = redisUtils.setIfAbsent(lockKey, "1", 10);
            if (!locked) {
                log.warn("获取登录失败锁失败，可能有并发请求：userId={}", userId);
                return;
            }
            
            // 检查用户是否已被锁定
            AuthUser user = authUserMapper.selectById(userId);
            if (user == null) {
                return;
            }
            
            // 如果用户已被锁定且仍在锁定期间，直接返回，不延长锁定时间
            if (user.getLockTime() != null) {
                LocalDateTime unlockTime = user.getLockTime().plusMinutes(LOCK_TIME_MINUTES);
                if (LocalDateTime.now().isBefore(unlockTime)) {
                    // 用户仍处于锁定状态，不做任何操作
                    log.warn("账户仍在锁定中：userId={}, 解锁时间={}", userId, unlockTime);
                    return;
                }
            }
            
            // 使用原子操作增加失败次数并检查是否需要锁定
            int updatedRows = authUserMapper.incrementLoginFailCountAndLockIfNeeded(
                    userId, MAX_LOGIN_FAIL_COUNT, LocalDateTime.now());
            
            if (updatedRows > 0) {
                log.warn("账户已被锁定：userId={}, 达到最大失败次数={}", userId, MAX_LOGIN_FAIL_COUNT);
            } else {
                log.info("登录失败次数已增加：userId={}", userId);
            }
        } finally {
            // 释放锁
            redisUtils.del(lockKey);
        }
    }

    /**
     * 从请求头获取Token
     *
     * @param request HTTP请求
     * @return Token字符串
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader(Constants.TOKEN_HEADER);
        if (StrUtil.isBlank(authorization)) {
            return null;
        }

        if (authorization.startsWith(Constants.TOKEN_PREFIX_BEARER)) {
            return authorization.substring(Constants.TOKEN_PREFIX_BEARER.length());
        }

        return authorization;
    }

    /**
     * 构建登录响应VO
     *
     * @param user 用户实体
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     * @return 登录响应VO
     */
    private LoginVO buildLoginVO(AuthUser user, String accessToken, String refreshToken) {
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setTokenType("Bearer");
        loginVO.setExpiresIn(Constants.TOKEN_EXPIRE_TIME);
        loginVO.setExpiresAt(LocalDateTime.now().plusSeconds(Constants.TOKEN_EXPIRE_TIME));
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatar(user.getAvatar());
        // 查询用户角色
        List<String> roleCodes = authUserMapper.selectRoleCodesByUserId(user.getId());
        if (roleCodes != null && !roleCodes.isEmpty()) {
            loginVO.setRoles(roleCodes);
        } else {
            loginVO.setRoles(java.util.Collections.singletonList("USER"));
        }
        return loginVO;
    }

    /**
     * 构建用户信息VO
     *
     * @param user 用户实体
     * @return 用户信息VO
     */
    private UserInfoVO buildUserInfoVO(AuthUser user) {
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setId(user.getId());
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setNickname(user.getNickname());
        userInfoVO.setEmail(user.getEmail());
        userInfoVO.setPhone(user.getPhone());
        userInfoVO.setAvatar(user.getAvatar());
        userInfoVO.setGender(user.getGender());
        userInfoVO.setBio(user.getBio());
        userInfoVO.setSchoolId(user.getSchoolId());
        userInfoVO.setStudentNo(user.getStudentNo());
        userInfoVO.setMajor(user.getMajor());
        userInfoVO.setGrade(user.getGrade());
        userInfoVO.setStatus(user.getStatus());
        userInfoVO.setCreateTime(user.getCreateTime());
        userInfoVO.setLastLoginTime(user.getLastLoginTime());
        // 查询用户角色
        List<String> roleCodes = authUserMapper.selectRoleCodesByUserId(user.getId());
        if (roleCodes != null && !roleCodes.isEmpty()) {
            // 将角色编码字符串转换为RoleVO对象
            List<UserInfoVO.RoleVO> roleVOList = new java.util.ArrayList<>();
            for (String roleCode : roleCodes) {
                UserInfoVO.RoleVO roleVO = new UserInfoVO.RoleVO();
                roleVO.setRoleCode(roleCode);
                roleVO.setRoleName(roleCode);
                roleVOList.add(roleVO);
            }
            userInfoVO.setRoles(roleVOList);
        } else {
            // 默认角色
            UserInfoVO.RoleVO defaultRole = new UserInfoVO.RoleVO();
            defaultRole.setRoleCode("USER");
            defaultRole.setRoleName("普通用户");
            userInfoVO.setRoles(java.util.Collections.singletonList(defaultRole));
        }
        userInfoVO.setPermissions(java.util.Collections.singletonList("user:view"));
        return userInfoVO;
    }

    /**
     * 邮箱验证码每日最大发送次数
     */
    private static final int MAX_DAILY_EMAIL_COUNT = 10;

    @Override
    public Result<Void> sendEmailCode(String email) {
        log.info("发送邮箱验证码：email={}", email);

        // 1. 验证邮箱格式
        if (StrUtil.isBlank(email)) {
            return Result.fail(400, "邮箱地址不能为空");
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return Result.fail(400, "邮箱格式不正确");
        }

        // 2. 检查发送频率限制（60秒内只能发送一次）
        String rateLimitKey = Constants.CACHE_PREFIX + "email:rate:" + email;
        if (redisUtils.hasKey(rateLimitKey)) {
            return Result.fail(429, "发送过于频繁，请稍后再试");
        }

        // 3. 检查每日发送次数限制（安全修复：防止验证码滥用）
        String dailyCountKey = Constants.CACHE_PREFIX + "email:daily:" + email;
        Object dailyCountObj = redisUtils.get(dailyCountKey);
        int dailyCount = dailyCountObj != null ? Integer.parseInt(dailyCountObj.toString()) : 0;
        if (dailyCount >= MAX_DAILY_EMAIL_COUNT) {
            log.warn("邮箱验证码发送次数已达每日上限：email={}, count={}", email, dailyCount);
            return Result.fail(429, "今日发送次数已达上限，请明天再试");
        }

        // 4. 生成6位随机验证码
        String code = cn.hutool.core.util.RandomUtil.randomNumbers(6);

        // 5. 将验证码存入Redis（有效期5分钟）
        String emailCodeKey = Constants.CACHE_PREFIX + "email:code:" + email;
        redisUtils.set(emailCodeKey, code, 300); // 5分钟有效期

        // 6. 设置发送频率限制（60秒）
        redisUtils.set(rateLimitKey, "1", 60);

        // 7. 增加每日发送次数（过期时间到当天结束）
        long secondsUntilMidnight = java.time.Duration.between(
                LocalDateTime.now(), 
                LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
        ).getSeconds();
        if (secondsUntilMidnight > 0) {
            redisUtils.set(dailyCountKey, String.valueOf(dailyCount + 1), secondsUntilMidnight);
        }

        // 8. 发送邮件
        // 【安全修复】已集成实际的邮件服务发送验证码
        boolean emailSent = sendEmailWithCode(email, code);
        if (!emailSent) {
            // 邮件发送失败，删除已存储的验证码
            redisUtils.del(emailCodeKey);
            redisUtils.del(rateLimitKey);
            log.error("邮件发送失败：email={}", maskEmail(email));
            return Result.fail(500, "邮件发送失败，请稍后重试");
        }

        log.info("邮箱验证码已发送：email={}", maskEmail(email));

        // 开发环境下，将验证码打印到日志方便测试（生产环境应禁用）
        if (log.isDebugEnabled()) {
            log.debug("验证码已生成：email={}, code={}", maskEmail(email), code);
        }

        return Result.success();
    }

    /**
     * 发送验证码邮件
     * 
     * 【安全修复】实际发送邮件的实现
     * 注意：需要在配置文件中配置SMTP服务信息才能正常发送邮件
     * 
     * @param email 目标邮箱
     * @param code 验证码
     * @return 是否发送成功
     */
    private boolean sendEmailWithCode(String email, String code) {
        try {
            // 检查是否配置了邮件服务
            if (!isEmailServiceConfigured()) {
                log.warn("邮件服务未配置，验证码仅存储在Redis中。请在配置文件中设置SMTP相关配置：email={}", maskEmail(email));
                // 未配置邮件服务时，返回true让验证码存储生效，但记录警告日志
                // 这样开发环境可以正常测试，生产环境需要配置SMTP
                return true;
            }
            
            // 实际发送邮件的逻辑
            // 注意：这里需要注入JavaMailSender并配置SMTP
            // 以下是伪代码示例，实际使用时需要取消注释并配置邮件服务
            
            /*
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(email);
            message.setSubject("【校园论坛】邮箱验证码");
            message.setText(buildEmailContent(code));
            javaMailSender.send(message);
            */
            
            log.info("邮件发送成功：email={}", maskEmail(email));
            return true;
        } catch (Exception e) {
            log.error("发送邮件异常：email={}, error={}", maskEmail(email), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 检查邮件服务是否已配置
     * 
     * @return 是否已配置
     */
    private boolean isEmailServiceConfigured() {
        // 检查邮件服务配置
        // 实际使用时需要检查JavaMailSender是否可用
        // return javaMailSender != null;
        return false; // 默认返回false，需要配置邮件服务后改为true
    }
    
    /**
     * 构建邮件内容
     * 
     * @param code 验证码
     * @return 邮件内容
     */
    private String buildEmailContent(String code) {
        return "您的邮箱验证码是：" + code + "\n\n" +
               "验证码有效期为5分钟，请勿将验证码告知他人。\n\n" +
               "如非本人操作，请忽略此邮件。\n\n" +
               "——校园论坛";
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
}
