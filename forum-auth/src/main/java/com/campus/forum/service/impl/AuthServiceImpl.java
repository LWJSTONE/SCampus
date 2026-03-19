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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Base64;

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
     * 账户锁定时间（分钟）
     */
    private static final int LOCK_TIME_MINUTES = 30;

    /**
     * JWT密钥
     */
    private static final String JWT_SECRET = "campus-forum-jwt-secret-key-2024";

    @Override
    public Result<LoginVO> login(LoginDTO loginDTO, HttpServletRequest request) {
        log.info("用户登录请求：username={}", loginDTO.getUsername());

        // 1. 验证码校验（如果提供了验证码）
        if (StrUtil.isNotBlank(loginDTO.getCaptchaKey()) && StrUtil.isNotBlank(loginDTO.getCaptcha())) {
            if (!validateCaptcha(loginDTO.getCaptchaKey(), loginDTO.getCaptcha())) {
                return Result.fail(400, "验证码错误或已过期");
            }
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

        // 6. 生成Token
        String accessToken = JwtUtils.generateToken(user.getId(), user.getUsername(), JWT_SECRET, 
                Constants.TOKEN_EXPIRE_TIME * 1000);
        String refreshToken = JwtUtils.generateRefreshToken(user.getId(), user.getUsername(), JWT_SECRET);

        // 7. 将Token存入Redis
        String tokenKey = Constants.TOKEN_PREFIX + user.getId();
        long expireSeconds = loginDTO.getRememberMe() != null && loginDTO.getRememberMe() 
                ? Constants.REFRESH_TOKEN_EXPIRE_TIME 
                : Constants.TOKEN_EXPIRE_TIME;
        redisUtils.set(tokenKey, accessToken, expireSeconds);

        // 8. 更新登录信息
        String loginIp = IpUtils.getIpAddr(request);
        authUserMapper.updateLoginInfo(user.getId(), LocalDateTime.now(), loginIp);

        // 9. 构建返回结果
        LoginVO loginVO = buildLoginVO(user, accessToken, refreshToken);

        log.info("用户登录成功：userId={}, username={}", user.getId(), user.getUsername());
        return Result.success("登录成功", loginVO);
    }

    @Override
    public Result<Void> logout(HttpServletRequest request) {
        // 从请求头获取Token
        String token = getTokenFromRequest(request);
        if (StrUtil.isBlank(token)) {
            return Result.success("登出成功");
        }

        // 解析用户ID
        Long userId = JwtUtils.getUserId(token);
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

        return Result.success("登出成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> register(RegisterDTO registerDTO) {
        log.info("用户注册请求：username={}", registerDTO.getUsername());

        // 1. 验证码校验
        if (!validateCaptcha(registerDTO.getCaptchaKey(), registerDTO.getCaptcha())) {
            return Result.fail(400, "验证码错误或已过期");
        }

        // 2. 密码确认校验
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            return Result.fail(400, "两次输入的密码不一致");
        }

        // 3. 检查用户名是否已存在
        AuthUser existUser = authUserMapper.selectByUsername(registerDTO.getUsername());
        if (existUser != null) {
            return Result.fail(400, "用户名已被注册");
        }

        // 4. 检查邮箱是否已存在
        AuthUser existEmail = authUserMapper.selectByEmail(registerDTO.getEmail());
        if (existEmail != null) {
            return Result.fail(400, "邮箱已被注册");
        }

        // 5. 创建用户
        AuthUser user = new AuthUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(PasswordUtils.encode(registerDTO.getPassword()));
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
        user.setEmailVerified(0);
        user.setPhoneVerified(0);
        user.setAvatar(Constants.DEFAULT_AVATAR);

        int result = authUserMapper.insert(user);
        if (result <= 0) {
            log.error("用户注册失败：username={}", registerDTO.getUsername());
            return Result.fail("注册失败，请稍后再试");
        }

        log.info("用户注册成功：userId={}, username={}", user.getId(), user.getUsername());
        return Result.success("注册成功");
    }

    @Override
    public Result<TokenVO> refreshToken(RefreshTokenDTO refreshTokenDTO) {
        log.info("刷新Token请求");

        String refreshToken = refreshTokenDTO.getRefreshToken();
        if (StrUtil.isBlank(refreshToken)) {
            return Result.fail(400, "刷新令牌不能为空");
        }

        // 1. 验证刷新令牌
        if (!JwtUtils.verifyToken(refreshToken, JWT_SECRET)) {
            return Result.fail(401, "刷新令牌无效或已过期");
        }

        // 2. 检查是否为刷新令牌
        String tokenType = JwtUtils.decodeToken(refreshToken).getClaim("type").asString();
        if (!"refresh".equals(tokenType)) {
            return Result.fail(400, "无效的刷新令牌");
        }

        // 3. 获取用户信息
        Long userId = JwtUtils.getUserId(refreshToken);
        String username = JwtUtils.getUsername(refreshToken);

        if (userId == null || StrUtil.isBlank(username)) {
            return Result.fail(401, "刷新令牌无效");
        }

        // 4. 查询用户状态
        AuthUser user = authUserMapper.selectById(userId);
        if (user == null || user.getStatus() != Constants.STATUS_ENABLE) {
            return Result.fail(401, "用户状态异常");
        }

        // 5. 生成新的访问令牌
        String newAccessToken = JwtUtils.generateToken(userId, username, JWT_SECRET, 
                Constants.TOKEN_EXPIRE_TIME * 1000);

        // 6. 更新Redis中的Token
        String tokenKey = Constants.TOKEN_PREFIX + userId;
        redisUtils.set(tokenKey, newAccessToken, Constants.TOKEN_EXPIRE_TIME);

        // 7. 构建返回结果
        TokenVO tokenVO = new TokenVO();
        tokenVO.setAccessToken(newAccessToken);
        tokenVO.setRefreshToken(refreshToken);
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

        log.debug("生成验证码：captchaKey={}, code={}", captchaKey, captchaCode);
        return Result.success(captchaVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> resetPassword(ResetPasswordDTO resetPasswordDTO) {
        log.info("重置密码请求：username={}", resetPasswordDTO.getUsername());

        // 1. 验证码校验
        if (!validateCaptcha(resetPasswordDTO.getCaptchaKey(), resetPasswordDTO.getCaptcha())) {
            return Result.fail(400, "验证码错误或已过期");
        }

        // 2. 密码确认校验
        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            return Result.fail(400, "两次输入的密码不一致");
        }

        // 3. 查询用户
        AuthUser user = authUserMapper.selectByUsername(resetPasswordDTO.getUsername());
        if (user == null) {
            return Result.fail(400, "用户不存在");
        }

        // 4. 更新密码
        String encodedPassword = PasswordUtils.encode(resetPasswordDTO.getNewPassword());
        int result = authUserMapper.updatePassword(user.getId(), encodedPassword);
        if (result <= 0) {
            log.error("密码重置失败：userId={}", user.getId());
            return Result.fail("密码重置失败，请稍后再试");
        }

        // 5. 使旧Token失效
        String tokenKey = Constants.TOKEN_PREFIX + user.getId();
        redisUtils.del(tokenKey);

        log.info("密码重置成功：userId={}", user.getId());
        return Result.success("密码重置成功");
    }

    @Override
    public Result<UserInfoVO> getUserInfo(HttpServletRequest request) {
        // 从请求头获取Token
        String token = getTokenFromRequest(request);
        if (StrUtil.isBlank(token)) {
            return Result.fail(401, "未登录");
        }

        // 解析用户ID
        Long userId = JwtUtils.getUserId(token);
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

        boolean result = cachedCaptcha.toString().equalsIgnoreCase(captcha.trim());
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
     * 
     * @param userId 用户ID
     */
    private void handleLoginFail(Long userId) {
        // 增加失败次数
        authUserMapper.incrementLoginFailCount(userId);

        // 检查是否需要锁定
        AuthUser user = authUserMapper.selectById(userId);
        if (user != null && user.getLoginFailCount() != null 
                && user.getLoginFailCount() >= MAX_LOGIN_FAIL_COUNT) {
            authUserMapper.lockUser(userId, LocalDateTime.now());
            log.warn("账户已被锁定：userId={}, failCount={}", userId, user.getLoginFailCount());
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
        // 默认角色
        loginVO.setRoles(java.util.Collections.singletonList("USER"));
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
        // 默认角色
        userInfoVO.setRoles(java.util.Collections.emptyList());
        userInfoVO.setPermissions(java.util.Collections.singletonList("user:view"));
        return userInfoVO;
    }
}
