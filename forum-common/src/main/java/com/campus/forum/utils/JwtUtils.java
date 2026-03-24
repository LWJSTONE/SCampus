package com.campus.forum.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和验证JWT令牌
 *
 * 安全警告：
 * 1. 生产环境必须配置 jwt.secret 密钥，不能使用默认密钥
 * 2. 调用需要密钥的方法时必须传入配置的密钥
 * 3. 仅解码不验证签名的方法仅用于获取Token信息展示，不能用于身份验证
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
public class JwtUtils {

    /**
     * 【安全修复】移除硬编码默认密钥
     * 
     * 原安全问题：硬编码的默认密钥可被攻击者利用伪造Token
     * 修复方案：强制要求从配置文件加载密钥，不提供默认值
     * 
     * 必须通过 setConfiguredSecret() 方法设置密钥后才能使用
     * 如果未配置密钥，将抛出 SecurityException 异常
     */
    // 已移除 DEFAULT_SECRET 常量 - 强制配置密钥

    /**
     * 未配置密钥时的错误消息
     */
    private static final String NO_SECRET_ERROR = "JWT密钥未配置！请通过配置文件设置 jwt.secret 属性后启动应用。";

    /**
     * 是否已配置密钥
     */
    private static volatile boolean secretConfigured = false;

    /**
     * 配置的密钥
     */
    private static volatile String configuredSecret = null;

    /**
     * 设置配置的密钥（应用启动时调用）
     *
     * @param secret 配置的密钥（必须是非空且长度至少32字符的强密钥）
     * @throws IllegalArgumentException 如果密钥不符合安全要求
     */
    public static void setConfiguredSecret(String secret) {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("JWT密钥不能为空！请在配置文件中设置 jwt.secret 属性。");
        }
        if (secret.length() < 32) {
            throw new IllegalArgumentException("JWT密钥长度不足！密钥长度至少需要32字符，当前长度: " + secret.length());
        }
        // 检查是否为常见弱密钥
        if (isWeakSecret(secret)) {
            throw new IllegalArgumentException("JWT密钥强度不足！请使用包含大小写字母、数字和特殊字符的随机密钥。");
        }
        configuredSecret = secret;
        secretConfigured = true;
        log.info("JWT密钥已从配置文件加载，密钥长度: {}", secret.length());
    }

    /**
     * 检查是否为弱密钥
     *
     * @param secret 密钥
     * @return 是否为弱密钥
     */
    private static boolean isWeakSecret(String secret) {
        // 检查常见的弱密钥模式
        String lowerSecret = secret.toLowerCase();
        String[] weakPatterns = {
            "secret", "password", "key", "default", "admin", "test",
            "123456", "qwerty", "abc", "xyz", "campus", "forum"
        };
        for (String pattern : weakPatterns) {
            if (lowerSecret.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
 * 获取当前使用的密钥
     *
     * @return 密钥
     * @throws SecurityException 如果密钥未配置
     */
    public static String getCurrentSecret() {
        if (configuredSecret == null || !secretConfigured) {
            throw new SecurityException(NO_SECRET_ERROR);
        }
        return configuredSecret;
    }

    /**
     * 检查是否已配置密钥
     *
     * @return 是否已配置密钥
     */
    public static boolean isSecretConfigured() {
        return secretConfigured;
    }

    /**
     * 默认过期时间（24小时）
     */
    private static final long DEFAULT_EXPIRATION = 24 * 60 * 60 * 1000L;

    /**
     * 刷新令牌过期时间（7天）
     */
    private static final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    /**
     * JWT发行者
     */
    private static final String ISSUER = "campus-forum";

    /**
     * 生成访问令牌（使用配置的密钥）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT令牌
     * @throws SecurityException 如果密钥未配置
     */
    public static String generateToken(Long userId, String username) {
        ensureSecretConfigured();
        return generateToken(userId, username, configuredSecret, DEFAULT_EXPIRATION);
    }

    /**
     * 确保密钥已配置
     *
     * @throws SecurityException 如果密钥未配置
     */
    private static void ensureSecretConfigured() {
        if (!secretConfigured || configuredSecret == null) {
            throw new SecurityException(NO_SECRET_ERROR);
        }
    }

    /**
     * 生成访问令牌（自定义密钥和过期时间）
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param secret     密钥
     * @param expiration 过期时间（毫秒）
     * @return JWT令牌
     */
    public static String generateToken(Long userId, String username, String secret, long expiration) {
        return generateToken(userId, username, null, secret, expiration);
    }

    /**
     * 生成访问令牌（包含角色信息）
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param role       用户角色
     * @param secret     密钥
     * @param expiration 过期时间（毫秒）
     * @return JWT令牌
     */
    public static String generateToken(Long userId, String username, String role, String secret, long expiration) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            com.auth0.jwt.JWTCreator.Builder builder = JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(username)
                    .withClaim("userId", userId)
                    .withClaim("username", username)
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(System.currentTimeMillis() + expiration));
            
            // 添加角色信息（如果提供）
            if (role != null && !role.isEmpty()) {
                builder.withClaim("role", role);
            } else {
                // 默认角色为普通用户
                builder.withClaim("role", "USER");
            }
            
            return builder.sign(algorithm);
        } catch (Exception e) {
            log.error("生成JWT令牌失败", e);
            return null;
        }
    }

    /**
     * 从令牌中获取角色（验证签名后获取）
     *
     * @param token JWT令牌
     * @return 角色
     */
    public static String getRole(String token) {
        return getRole(token, getCurrentSecret());
    }

    /**
     * 从令牌中获取角色（自定义密钥验证）
     *
     * @param token JWT令牌
     * @param secret 密钥
     * @return 角色
     */
    public static String getRole(String token, String secret) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("role").asString();
        } catch (JWTVerificationException e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 生成刷新令牌（使用配置的密钥）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return 刷新令牌
     * @throws SecurityException 如果密钥未配置
     */
    public static String generateRefreshToken(Long userId, String username) {
        ensureSecretConfigured();
        return generateRefreshToken(userId, username, configuredSecret);
    }

    /**
     * 生成刷新令牌（自定义密钥）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param secret   密钥
     * @return 刷新令牌
     */
    public static String generateRefreshToken(Long userId, String username, String secret) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(username)
                    .withClaim("userId", userId)
                    .withClaim("type", "refresh")
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                    .sign(algorithm);
        } catch (Exception e) {
            log.error("生成刷新令牌失败", e);
            return null;
        }
    }

    /**
     * 验证令牌（使用配置的密钥）
     *
     * @param token JWT令牌
     * @return 验证结果
     * @throws SecurityException 如果密钥未配置
     */
    public static boolean verifyToken(String token) {
        ensureSecretConfigured();
        return verifyToken(token, configuredSecret);
    }

    /**
     * 验证令牌（自定义密钥）
     *
     * @param token  JWT令牌
     * @param secret 密钥
     * @return 验证结果
     */
    public static boolean verifyToken(String token, String secret) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 解析令牌
     *
     * @param token JWT令牌
     * @return 解析后的JWT
     */
    public static DecodedJWT decodeToken(String token) {
        try {
            return JWT.decode(token);
        } catch (Exception e) {
            log.error("JWT令牌解析失败", e);
            return null;
        }
    }

    /**
     * 从令牌中获取用户ID（仅解码，不验证签名）
     * 
     * ⚠️ 安全警告：此方法仅解码Token不验证签名，存在安全风险！
     * 用于身份验证时请使用 getUserId(String token, String secret) 方法
     * 此方法仅适用于：日志记录、前端展示等非安全敏感场景
     *
     * @param token JWT令牌
     * @return 用户ID
     * @deprecated 请使用 {@link #getUserId(String, String)} 方法进行安全的Token验证
     */
    @Deprecated
    public static Long getUserId(String token) {
        DecodedJWT jwt = decodeToken(token);
        if (jwt != null) {
            return jwt.getClaim("userId").asLong();
        }
        return null;
    }

    /**
     * 从令牌中获取用户ID（验证签名后获取）
     * 推荐使用此方法进行身份验证
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public static Long getUserIdSecure(String token) {
        return getUserId(token, getCurrentSecret());
    }

    /**
     * 从令牌中获取用户ID（需验证签名）
     *
     * @param token JWT令牌
     * @param secret 密钥
     * @return 用户ID
     */
    public static Long getUserId(String token, String secret) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("userId").asLong();
        } catch (JWTVerificationException e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从令牌中获取用户名（仅解码，不验证签名）
     * 
     * ⚠️ 安全警告：此方法仅解码Token不验证签名，存在安全风险！
     * 用于身份验证时请使用 getUsername(String token, String secret) 方法
     * 此方法仅适用于：日志记录、前端展示等非安全敏感场景
     *
     * @param token JWT令牌
     * @return 用户名
     * @deprecated 请使用 {@link #getUsername(String, String)} 方法进行安全的Token验证
     */
    @Deprecated
    public static String getUsername(String token) {
        DecodedJWT jwt = decodeToken(token);
        if (jwt != null) {
            return jwt.getSubject();
        }
        return null;
    }

    /**
     * 从令牌中获取用户名（验证签名后获取）
     * 推荐使用此方法进行身份验证
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public static String getUsernameSecure(String token) {
        return getUsername(token, getCurrentSecret());
    }

    /**
     * 从令牌中获取用户名（需验证签名）
     *
     * @param token JWT令牌
     * @param secret 密钥
     * @return 用户名
     */
    public static String getUsername(String token, String secret) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getSubject();
        } catch (JWTVerificationException e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从令牌中获取过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public static Date getExpiration(String token) {
        DecodedJWT jwt = decodeToken(token);
        if (jwt != null) {
            return jwt.getExpiresAt();
        }
        return null;
    }

    /**
     * 判断令牌是否过期
     *
     * @param token JWT令牌
     * @return 是否过期
     */
    public static boolean isExpired(String token) {
        Date expiration = getExpiration(token);
        if (expiration == null) {
            return true;
        }
        return expiration.before(new Date());
    }

    /**
     * 获取默认过期时间（秒）
     *
     * @return 过期时间（秒）
     */
    public static long getDefaultExpirationSeconds() {
        return DEFAULT_EXPIRATION / 1000;
    }

    /**
     * 获取刷新令牌过期时间（秒）
     *
     * @return 过期时间（秒）
     */
    public static long getRefreshExpirationSeconds() {
        return REFRESH_EXPIRATION / 1000;
    }
}
