package com.campus.forum.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 * 提供日期格式化、解析、计算等功能
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
public class DateUtils {

    /**
     * 日期时间格式
     */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 时间格式
     */
    public static final String TIME_PATTERN = "HH:mm:ss";

    /**
     * 紧凑日期格式
     */
    public static final String COMPACT_DATE_TIME_PATTERN = "yyyyMMddHHmmss";

    /**
     * 日期时间格式化器
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    /**
     * 日期格式化器
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    /**
     * 时间格式化器
     */
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);

    private DateUtils() {
        // 私有构造方法，防止实例化
    }

    // ===================== LocalDateTime 相关 =====================

    /**
     * 获取当前日期时间
     *
     * @return 当前日期时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前日期
     *
     * @return 当前日期
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * 格式化日期时间
     *
     * @param dateTime 日期时间
     * @param pattern  格式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化日期时间（使用默认格式）
     *
     * @param dateTime 日期时间
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * 格式化日期
     *
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * 解析日期时间字符串
     *
     * @param dateTimeStr 日期时间字符串
     * @param pattern     格式
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            log.error("解析日期时间失败: {}", dateTimeStr, e);
            return null;
        }
    }

    /**
     * 解析日期时间字符串（使用默认格式）
     *
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            log.error("解析日期时间失败: {}", dateTimeStr, e);
            return null;
        }
    }

    /**
     * 解析日期字符串
     *
     * @param dateStr 日期字符串
     * @return LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            log.error("解析日期失败: {}", dateStr, e);
            return null;
        }
    }

    /**
     * 获取一天的开始时间
     *
     * @param date 日期
     * @return 开始时间
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * 获取一天的结束时间
     *
     * @param date 日期
     * @return 结束时间
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(LocalTime.MAX);
    }

    /**
     * 获取当天的开始时间
     *
     * @return 开始时间
     */
    public static LocalDateTime getStartOfToday() {
        return getStartOfDay(LocalDate.now());
    }

    /**
     * 获取当天的结束时间
     *
     * @return 结束时间
     */
    public static LocalDateTime getEndOfToday() {
        return getEndOfDay(LocalDate.now());
    }

    /**
     * 获取本周的第一天
     *
     * @return 本周第一天
     */
    public static LocalDate getFirstDayOfWeek() {
        return LocalDate.now().with(DayOfWeek.MONDAY);
    }

    /**
     * 获取本月的第一天
     *
     * @return 本月第一天
     */
    public static LocalDate getFirstDayOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    /**
     * 获取本月的最后一天
     *
     * @return 本月最后一天
     */
    public static LocalDate getLastDayOfMonth() {
        return LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
    }

    /**
     * 计算两个日期之间的天数
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 天数
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 计算两个日期时间之间的小时数
     *
     * @param startDateTime 开始日期时间
     * @param endDateTime   结束日期时间
     * @return 小时数
     */
    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    /**
     * 计算两个日期时间之间的分钟数
     *
     * @param startDateTime 开始日期时间
     * @param endDateTime   结束日期时间
     * @return 分钟数
     */
    public static long minutesBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    }

    /**
     * 判断是否为今天
     *
     * @param date 日期
     * @return 是否为今天
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }

    /**
     * 判断是否为今天
     *
     * @param dateTime 日期时间
     * @return 是否为今天
     */
    public static boolean isToday(LocalDateTime dateTime) {
        return dateTime != null && dateTime.toLocalDate().equals(LocalDate.now());
    }

    /**
     * 判断是否在指定范围内
     *
     * @param dateTime 日期时间
     * @param start    开始时间
     * @param end      结束时间
     * @return 是否在范围内
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        if (dateTime == null || start == null || end == null) {
            return false;
        }
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    // ===================== Date 相关 =====================

    /**
     * Date转LocalDateTime
     *
     * @param date Date
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * LocalDateTime转Date
     *
     * @param localDateTime LocalDateTime
     * @return Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDate转Date
     *
     * @param localDate LocalDate
     * @return Date
     */
    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 格式化Date
     *
     * @param date    Date
     * @param pattern 格式
     * @return 格式化后的字符串
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 格式化Date（使用默认格式）
     *
     * @param date Date
     * @return 格式化后的字符串
     */
    public static String format(Date date) {
        return format(date, DATE_TIME_PATTERN);
    }

    /**
     * 解析日期字符串为Date
     *
     * @param dateStr 日期字符串
     * @param pattern 格式
     * @return Date
     */
    public static Date parse(String dateStr, String pattern) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            log.error("解析日期失败: {}", dateStr, e);
            return null;
        }
    }

    /**
     * 获取时间戳（毫秒）
     *
     * @return 时间戳
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取时间戳（秒）
     *
     * @return 时间戳
     */
    public static long currentSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 时间戳转LocalDateTime
     *
     * @param timestamp 时间戳（毫秒）
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    /**
     * LocalDateTime转时间戳
     *
     * @param localDateTime LocalDateTime
     * @return 时间戳（毫秒）
     */
    public static long toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return 0;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 获取友好的时间显示
     *
     * @param dateTime 日期时间
     * @return 友好的时间显示
     */
    public static String getFriendlyTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        long minutes = minutesBetween(dateTime, LocalDateTime.now());
        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (minutes < 60 * 24) {
            return (minutes / 60) + "小时前";
        } else if (minutes < 60 * 24 * 2) {
            return "昨天";
        } else if (minutes < 60 * 24 * 3) {
            return "前天";
        } else if (minutes < 60 * 24 * 30) {
            return (minutes / (60 * 24)) + "天前";
        } else if (minutes < 60 * 24 * 365) {
            return (minutes / (60 * 24 * 30)) + "个月前";
        } else {
            return (minutes / (60 * 24 * 365)) + "年前";
        }
    }
}
