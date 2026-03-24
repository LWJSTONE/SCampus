package com.campus.forum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

/**
 * 帖子服务业务配置类
 * 
 * 从配置文件读取业务相关参数，包括敏感词列表等
 * 
 * @author campus
 * @since 2024-01-01
 */
@Configuration
public class PostConfig {

    /**
     * 敏感词列表（从配置文件读取，逗号分隔）
     */
    @Value("${post.sensitive.words:色情,暴力,赌博,毒品,诈骗}")
    private String sensitiveWordsConfig;

    /**
     * 敏感词替换字符
     */
    @Value("${post.sensitive.replacement:***}")
    private String sensitiveReplacement;

    /**
     * 是否启用敏感词过滤
     */
    @Value("${post.sensitive.enabled:true}")
    private boolean sensitiveFilterEnabled;

    /**
     * 是否启用XSS过滤
     */
    @Value("${post.xss.enabled:true}")
    private boolean xssFilterEnabled;

    /**
     * 热门帖子缓存过期时间（秒）
     */
    @Value("${post.hot.cache-expire:3600}")
    private int hotPostCacheExpire;

    /**
     * 热门帖子数量限制
     */
    @Value("${post.hot.limit:10}")
    private int hotPostLimit;

    /**
     * 获取敏感词列表
     * 
     * @return 敏感词集合
     */
    public Set<String> getSensitiveWords() {
        if (sensitiveWordsConfig == null || sensitiveWordsConfig.trim().isEmpty()) {
            return Collections.emptySet();
        }
        String[] words = sensitiveWordsConfig.split(",");
        Set<String> result = new HashSet<>();
        for (String word : words) {
            String trimmed = word.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    public String getSensitiveReplacement() {
        return sensitiveReplacement;
    }

    public boolean isSensitiveFilterEnabled() {
        return sensitiveFilterEnabled;
    }

    public boolean isXssFilterEnabled() {
        return xssFilterEnabled;
    }

    public int getHotPostCacheExpire() {
        return hotPostCacheExpire;
    }

    public int getHotPostLimit() {
        return hotPostLimit;
    }

    public void setSensitiveWordsConfig(String sensitiveWordsConfig) {
        this.sensitiveWordsConfig = sensitiveWordsConfig;
    }

    public void setSensitiveReplacement(String sensitiveReplacement) {
        this.sensitiveReplacement = sensitiveReplacement;
    }

    public void setSensitiveFilterEnabled(boolean sensitiveFilterEnabled) {
        this.sensitiveFilterEnabled = sensitiveFilterEnabled;
    }

    public void setXssFilterEnabled(boolean xssFilterEnabled) {
        this.xssFilterEnabled = xssFilterEnabled;
    }

    public void setHotPostCacheExpire(int hotPostCacheExpire) {
        this.hotPostCacheExpire = hotPostCacheExpire;
    }

    public void setHotPostLimit(int hotPostLimit) {
        this.hotPostLimit = hotPostLimit;
    }
}
