package com.campus.forum.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * IP工具类
 * 用于获取和处理IP地址信息
 *
 * 【安全修复】增强IP获取安全性
 * 原问题：直接信任 X-Forwarded-For 等HTTP头，未验证IP格式，存在IP伪造风险
 * 修复方案：
 * 1. 添加IP格式验证，防止注入恶意IP
 * 2. 添加可信代理IP配置，只信任来自可信代理的IP头
 * 3. 增加安全日志记录
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
public class IpUtils {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final int IP_MAX_LENGTH = 15;

    /**
     * 可信代理IP集合（需要根据实际部署环境配置）
     * 默认包含常见的内网代理和负载均衡器IP
     * 生产环境应通过配置文件设置
     */
    private static volatile Set<String> trustedProxies = new HashSet<>(Arrays.asList(
            "127.0.0.1",
            "0:0:0:0:0:0:0:1",
            "10.0.0.0/8",      // 10.0.0.0 - 10.255.255.255
            "172.16.0.0/12",   // 172.16.0.0 - 172.31.255.255
            "192.168.0.0/16"   // 192.168.0.0 - 192.168.255.255
    ));

    /**
     * 是否启用可信代理验证
     * 默认为 true，生产环境强烈建议开启
     */
    private static volatile boolean enableTrustedProxyCheck = true;

    private IpUtils() {
        // 私有构造方法，防止实例化
    }

    /**
     * 设置可信代理IP列表
     *
     * @param proxies 可信代理IP集合
     */
    public static void setTrustedProxies(Set<String> proxies) {
        if (proxies != null) {
            trustedProxies = new HashSet<>(proxies);
            log.info("已更新可信代理IP列表，共 {} 个条目", trustedProxies.size());
        }
    }

    /**
 * 设置是否启用可信代理验证
     *
     * @param enable 是否启用
     */
    public static void setEnableTrustedProxyCheck(boolean enable) {
        enableTrustedProxyCheck = enable;
        log.info("可信代理验证已{}", enable ? "启用" : "禁用");
    }

    /**
     * 检查IP是否在可信代理列表中
     *
     * @param ip IP地址
     * @return 是否为可信代理
     */
    public static boolean isTrustedProxy(String ip) {
        if (StrUtil.isEmpty(ip)) {
            return false;
        }
        // 直接匹配
        if (trustedProxies.contains(ip)) {
            return true;
        }
        // CIDR匹配（简化版，仅支持常见内网段）
        if (isInternalIp(ip)) {
            return true;
        }
        return false;
    }

    /**
     * 获取客户端IP地址（增强安全版）
     *
     * 安全措施：
     * 1. 只在请求来自可信代理时才读取代理头
     * 2. 对获取的IP进行格式验证，防止注入
     * 3. 记录异常IP获取情况
     *
     * @param request HttpServletRequest
     * @return IP地址（已验证格式）
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        // 获取请求来源IP（直接连接的IP）
        String remoteAddr = request.getRemoteAddr();

        // 检查是否来自可信代理
        boolean fromTrustedProxy = !enableTrustedProxyCheck || isTrustedProxy(remoteAddr);

        String ip = null;

        if (fromTrustedProxy) {
            // 只有来自可信代理时才读取代理头
            ip = getIpFromHeaders(request);
        }

        // 如果没有从代理头获取到有效IP，使用直接连接IP
        if (isEmptyIp(ip)) {
            ip = remoteAddr;
            // 对于本地访问，获取本机配置的IP
            if (LOCALHOST_IP.equals(ip) || LOCALHOST_IPV6.equals(ip)) {
                try {
                    ip = java.net.InetAddress.getLocalHost().getHostAddress();
                } catch (Exception e) {
                    log.error("获取本机IP失败", e);
                }
            }
        }

        // 处理多IP情况（取第一个有效IP）
        ip = extractFirstValidIp(ip);

        // 验证IP格式，防止注入攻击
        if (!isValidIpFormat(ip)) {
            log.warn("检测到无效IP格式: {}，可能存在IP伪造攻击！", ip);
            return UNKNOWN;
        }

        return ip;
    }

    /**
     * 从HTTP头获取IP（仅限可信代理场景使用）
     *
     * @param request HttpServletRequest
     * @return IP地址
     */
    private static String getIpFromHeaders(HttpServletRequest request) {
        String ip = null;

        // 按优先级检查各个代理头
        // X-Forwarded-For 是标准头，优先级最高
        ip = request.getHeader("X-Forwarded-For");
        if (isEmptyIp(ip)) {
            ip = request.getHeader("x-forwarded-for");
        }
        if (isEmptyIp(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isEmptyIp(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isEmptyIp(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        return ip;
    }

    /**
     * 从多IP字符串中提取第一个有效IP
     *
     * @param ipStr IP字符串（可能包含多个IP，逗号分隔）
     * @return 第一个有效IP
     */
    private static String extractFirstValidIp(String ipStr) {
        if (StrUtil.isEmpty(ipStr)) {
            return ipStr;
        }

        // 处理多IP情况（X-Forwarded-For格式：client, proxy1, proxy2）
        if (ipStr.contains(",")) {
            String[] ips = ipStr.split(",");
            for (String ip : ips) {
                ip = ip.trim();
                if (isValidIpFormat(ip)) {
                    return ip;
                }
            }
            // 如果没有有效IP，取第一个并清理
            return ips[0].trim();
        }

        return ipStr.trim();
    }

    /**
     * 验证IP格式是否有效（支持IPv4和IPv6）
     *
     * @param ip IP地址
     * @return 是否为有效IP格式
     */
    private static boolean isValidIpFormat(String ip) {
        if (StrUtil.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            return false;
        }
        // 检查是否包含非法字符（防止注入攻击）
        if (ip.contains("\n") || ip.contains("\r") || ip.contains("\t")
                || ip.contains("\"") || ip.contains("'") || ip.contains("<") || ip.contains(">")) {
            log.warn("IP地址包含非法字符: {}", ip);
            return false;
        }
        // 验证IPv4或IPv6格式
        return isValidIp(ip) || isValidIpv6(ip);
    }

    /**
     * 判断IP是否为空
     *
     * @param ip IP地址
     * @return 是否为空
     */
    private static boolean isEmptyIp(String ip) {
        return ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip);
    }

    /**
     * 是否为内网IP
     *
     * @param ip IP地址
     * @return 是否为内网IP
     */
    public static boolean isInternalIp(String ip) {
        if (StrUtil.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            return false;
        }
        if (LOCALHOST_IP.equals(ip) || LOCALHOST_IPV6.equals(ip)) {
            return true;
        }
        // 私有IP地址段
        return ip.startsWith("10.") ||
                ip.startsWith("192.168.") ||
                is172Range(ip);
    }

    /**
     * 判断是否为172.16.0.0 - 172.31.255.255范围
     *
     * @param ip IP地址
     * @return 是否在172范围
     */
    private static boolean is172Range(String ip) {
        if (!ip.startsWith("172.")) {
            return false;
        }
        String[] parts = ip.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        try {
            int second = Integer.parseInt(parts[1]);
            return second >= 16 && second <= 31;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * IP地址转换为长整型
     *
     * @param ip IP地址
     * @return 长整型
     */
    public static long ipToLong(String ip) {
        if (StrUtil.isEmpty(ip)) {
            return 0;
        }
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return 0;
        }
        try {
            return (Long.parseLong(parts[0]) << 24) +
                    (Long.parseLong(parts[1]) << 16) +
                    (Long.parseLong(parts[2]) << 8) +
                    Long.parseLong(parts[3]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 长整型转换为IP地址
     *
     * @param ipLong 长整型
     * @return IP地址
     */
    public static String longToIp(long ipLong) {
        return ((ipLong >> 24) & 0xFF) + "." +
                ((ipLong >> 16) & 0xFF) + "." +
                ((ipLong >> 8) & 0xFF) + "." +
                (ipLong & 0xFF);
    }

    /**
     * 获取IP地址归属地（调用外部API）
     *
     * @param ip IP地址
     * @return 归属地信息
     */
    public static String getIpLocation(String ip) {
        if (isInternalIp(ip)) {
            return "内网IP";
        }
        try {
            // 使用免费的IP地址查询API
            String url = "http://whois.pconline.com.cn/ipJson.jsp?ip=" + ip + "&json=true";
            String response = HttpUtil.get(url, 3000);
            // 简单解析返回的JSON，实际项目中应该使用JSON工具解析
            if (response != null && response.contains("addr")) {
                // 提取addr字段
                int start = response.indexOf("\"addr\":\"") + 8;
                int end = response.indexOf("\"", start);
                if (start > 7 && end > start) {
                    return response.substring(start, end);
                }
            }
        } catch (Exception e) {
            log.warn("获取IP归属地失败: {}", ip, e);
        }
        return "未知";
    }

    /**
     * 验证IP地址格式
     *
     * @param ip IP地址
     * @return 是否为有效IP
     */
    public static boolean isValidIp(String ip) {
        if (StrUtil.isEmpty(ip)) {
            return false;
        }
        String regex = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
        return ip.matches(regex);
    }

    /**
     * 验证IPv6地址格式
     *
     * @param ip IPv6地址
     * @return 是否为有效IPv6地址
     */
    public static boolean isValidIpv6(String ip) {
        if (StrUtil.isEmpty(ip)) {
            return false;
        }
        String regex = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";
        return ip.matches(regex);
    }
}
