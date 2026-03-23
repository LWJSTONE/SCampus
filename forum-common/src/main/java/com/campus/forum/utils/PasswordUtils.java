package com.campus.forum.utils;

import cn.hutool.crypto.digest.BCrypt;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * 密码加密工具类
 * 提供密码加密、验证等功能
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
public class PasswordUtils {

    /**
     * BCrypt加密成本因子
     */
    private static final int BCRYPT_COST = 12;

    /**
     * 安全随机数生成器（用于生成随机密码）
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordUtils() {
        // 私有构造方法，防止实例化
    }

    /**
     * 使用BCrypt加密密码
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            return null;
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_COST));
    }

    /**
     * 使用BCrypt验证密码
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }

    /**
     * 生成随机盐值
     *
     * @return 盐值
     */
    public static String generateSalt() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * MD5加密（不推荐用于密码存储，仅用于数据签名等场景）
     *
     * @param input 输入字符串
     * @return MD5加密后的字符串
     */
    public static String md5(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5加密失败", e);
            return null;
        }
    }

    /**
     * MD5加盐加密
     *
     * @param input 输入字符串
     * @param salt  盐值
     * @return MD5加盐加密后的字符串
     */
    public static String md5WithSalt(String input, String salt) {
        if (input == null || salt == null) {
            return null;
        }
        return md5(input + salt);
    }

    /**
     * SHA256加密
     *
     * @param input 输入字符串
     * @return SHA256加密后的字符串
     */
    public static String sha256(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA256加密失败", e);
            return null;
        }
    }

    /**
     * 验证密码强度
     *
     * @param password 密码
     * @return 是否为强密码（至少包含大小写字母、数字，长度至少8位）
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasUpper && hasLower && hasDigit;
    }

    /**
     * 生成随机密码（使用安全随机数生成器）
     *
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateRandomPassword(int length) {
        if (length < 6) {
            length = 6;
        }
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        // 使用SecureRandom替代Random，确保生成的密码不可预测
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        return password.toString();
    }
}
