package com.campus.forum.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

/**
 * IP工具类
 * 用于获取和处理IP地址信息
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

    private IpUtils() {
        // 私有构造方法，防止实例化
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HttpServletRequest
     * @return IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        String ip = request.getHeader("x-forwarded-for");
        if (isEmptyIp(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isEmptyIp(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (isEmptyIp(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isEmptyIp(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isEmptyIp(ip)) {
            ip = request.getRemoteAddr();
            // 对于通过多个代理的情况，第一个IP才是客户端真实IP
            if (LOCALHOST_IP.equals(ip) || LOCALHOST_IPV6.equals(ip)) {
                // 根据网卡取本机配置的IP
                try {
                    ip = java.net.InetAddress.getLocalHost().getHostAddress();
                } catch (Exception e) {
                    log.error("获取本机IP失败", e);
                }
            }
        }

        // 对于通过多个代理的情况，第一个IP才是客户端真实IP
        if (ip != null && ip.length() > IP_MAX_LENGTH && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }

        return ip;
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
