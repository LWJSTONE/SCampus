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
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
public class JwtUtils {

    /**
     * 默认密钥
     */
    private static final String DEFAULT_SECRET = "campus-forum-jwt-secret-key-2024";

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
     * 生成访问令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT令牌
     */
    public static String generateToken(Long userId, String username) {
        return generateToken(userId, username, DEFAULT_SECRET, DEFAULT_EXPIRATION);
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
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(username)
                    .withClaim("userId", userId)
                    .withClaim("username", username)
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                    .sign(algorithm);
        } catch (Exception e) {
            log.error("生成JWT令牌失败", e);
            return null;
        }
    }

    /**
     * 生成刷新令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return 刷新令牌
     */
    public static String generateRefreshToken(Long userId, String username) {
        return generateRefreshToken(userId, username, DEFAULT_SECRET);
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
     * 验证令牌
     *
     * @param token JWT令牌
     * @return 验证结果
     */
    public static boolean verifyToken(String token) {
        return verifyToken(token, DEFAULT_SECRET);
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
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public static Long getUserId(String token) {
        DecodedJWT jwt = decodeToken(token);
        if (jwt != null) {
            return jwt.getClaim("userId").asLong();
        }
        return null;
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public static String getUsername(String token) {
        DecodedJWT jwt = decodeToken(token);
        if (jwt != null) {
            return jwt.getSubject();
        }
        return null;
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
