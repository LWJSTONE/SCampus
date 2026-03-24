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
     * MD5加密
     *
     * ⚠️⚠️⚠️ 【严重安全警告】⚠️⚠️⚠️
     * 
     * 此方法【严格禁止】用于密码存储！
     *
     * MD5 已被证明不安全，存在以下漏洞：
     * 1. 碰撞攻击：可找到不同输入产生相同输出
     * 2. 彩虹表攻击：常见密码的MD5已被大量收集
     * 3. 快速计算：现代硬件可每秒计算数十亿次MD5
     * 4. 无盐值保护：相同的输入产生相同的输出
     *
     * 【严格禁止的使用场景】：
     * - 密码存储
     * - 密码验证
     * - 敏感数据加密
     * - 身份验证令牌生成
     * 
     * 允许的使用场景（仅限非安全敏感的数据签名）：
     * - 文件完整性校验（非安全关键）
     * - 非敏感数据去重
     * - 缓存键生成（非密码相关）
     * - 数据指纹（非安全关键）
     *
     * 如需密码加密，请使用：
     * - encode() 方法（BCrypt，推荐）
     * - 或其他安全的密码哈希算法（Argon2, scrypt, PBKDF2）
     *
     * @param input 输入字符串
     * @return MD5加密后的字符串
     * @deprecated 请勿用于任何安全敏感场景，特别是密码存储。请使用 {@link #encode(String)} 方法。
     *             此方法将在未来版本中移除，请尽快迁移到安全的哈希算法。
     */
    @Deprecated(since = "2024", forRemoval = true)
    public static String md5(String input) {
        if (input == null) {
            return null;
        }
        // 【安全修复】增强警告日志，记录完整调用堆栈以便追踪不当使用
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder stackInfo = new StringBuilder();
        for (int i = 2; i < Math.min(5, stackTrace.length); i++) {
            stackInfo.append("\n    at ").append(stackTrace[i]);
        }
        log.warn("【严重安全警告】MD5方法被调用！请确保此方法不用于密码存储！\n调用堆栈:{}", stackInfo);
        
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
     * ⚠️⚠️⚠️ 【严重安全警告】⚠⚠️⚠️
     * 
     * 此方法【严格禁止】用于密码存储！
     * 
     * 即使加盐，MD5 仍然不安全：
     * 1. MD5算法本身存在碰撞漏洞
     * 2. 缺少迭代计算，易受GPU/ASIC攻击
     * 3. 简单加盐方式不够安全
     * 4. 相同输入+相同盐值仍产生相同输出
     *
     * 【严格禁止的使用场景】：
     * - 密码存储
     * - 密码验证
     * - 敏感数据加密
     * - 身份验证令牌生成
     *
     * 如需密码加密，请使用 encode() 方法（BCrypt），它内置了安全的盐值处理和迭代计算。

     * @param input 输入字符串
     * @param salt  盐值
     * @return MD5加盐加密后的字符串
     * @deprecated 请勿用于密码存储。请使用 {@link #encode(String)} 方法。
     *             此方法将在未来版本中移除，请尽快迁移到安全的哈希算法。
     */
    @Deprecated(since = "2024", forRemoval = true)
    public static String md5WithSalt(String input, String salt) {
        if (input == null || salt == null) {
            return null;
        }
        // 【安全修复】增强警告日志，记录完整调用堆栈
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder stackInfo = new StringBuilder();
        for (int i = 2; i < Math.min(5, stackTrace.length); i++) {
            stackInfo.append("\n    at ").append(stackTrace[i]);
        }
        log.warn("【严重安全警告】MD5加盐方法被调用！请确保此方法不用于密码存储！\n调用堆栈:{}", stackInfo);
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
