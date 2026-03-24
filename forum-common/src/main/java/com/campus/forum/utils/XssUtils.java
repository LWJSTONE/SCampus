package com.campus.forum.utils;

import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

/**
 * XSS防护工具类
 * 用于过滤和转义用户输入中的恶意脚本
 *
 * @author campus
 * @since 2024-01-01
 */
public class XssUtils {

    /**
     * 危险的HTML标签模式
     */
    private static final Pattern[] DANGEROUS_PATTERNS = {
            // 脚本标签
            Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            // 事件处理器属性
            Pattern.compile("on\\s*=", Pattern.CASE_INSENSITIVE),
            // javascript: 协议
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            // vbscript: 协议
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            // data: 协议（可能包含恶意代码）
            Pattern.compile("data\\s*:", Pattern.CASE_INSENSITIVE),
            // iframe 标签
            Pattern.compile("<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            // object 标签
            Pattern.compile("<object[^>]*>.*?</object>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            // embed 标签
            Pattern.compile("<embed[^>]*>", Pattern.CASE_INSENSITIVE),
            // expression CSS 表达式
            Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),
            // 行为属性
            Pattern.compile("behavior\\s*:", Pattern.CASE_INSENSITIVE),
            // style 属性中的脚本
            Pattern.compile("style\\s*=\\s*[\"'][^\"']*expression", Pattern.CASE_INSENSITIVE)
    };

    /**
     * 私有构造方法，防止实例化
     */
    private XssUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 对字符串进行XSS过滤（HTML转义）
     * 适用于一般文本内容
     *
     * @param input 输入字符串
     * @return 转义后的安全字符串
     */
    public static String escape(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return HtmlUtils.htmlEscape(input, "UTF-8");
    }

    /**
     * 对字符串进行XSS清理（移除危险标签，保留安全HTML）
     * 适用于允许部分HTML标签的场景
     *
     * @param input 输入字符串
     * @return 清理后的安全字符串
     */
    public static String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input;

        // 移除危险模式
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            result = pattern.matcher(result).replaceAll("");
        }

        return result;
    }

    /**
     * 对字符串进行严格的XSS过滤
     * 先清理危险标签，再进行HTML转义
     * 适用于标题等关键字段
     *
     * @param input 输入字符串
     * @return 过滤后的安全字符串
     */
    public static String strictFilter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 先清理危险标签
        String result = sanitize(input);
        // 再进行HTML转义
        result = escape(result);

        return result;
    }

    /**
     * 检查字符串是否包含潜在的XSS攻击内容
     *
     * @param input 输入字符串
     * @return 是否包含危险内容
     */
    public static boolean containsXss(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 过滤通知标题（严格模式）
     *
     * @param title 标题
     * @return 过滤后的标题
     */
    public static String filterTitle(String title) {
        return strictFilter(title);
    }

    /**
     * 过滤通知内容（允许部分安全HTML，但转义特殊字符）
     *
     * @param content 内容
     * @return 过滤后的内容
     */
    public static String filterContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // 先清理危险标签
        String result = sanitize(content);

        return result;
    }

    /**
     * 过滤备注信息（严格模式）
     *
     * @param remark 备注
     * @return 过滤后的备注
     */
    public static String filterRemark(String remark) {
        return strictFilter(remark);
    }
}
