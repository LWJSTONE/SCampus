package com.campus.forum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 评论服务业务配置类
 * 
 * 从配置文件读取业务相关参数
 * 
 * @author campus
 * @since 2024-01-01
 */
@Configuration
public class CommentConfig {

    /**
     * 评论内容最大长度
     */
    @Value("${comment.max-content-length:500}")
    private int maxContentLength;

    /**
     * 每次获取回复数量限制
     */
    @Value("${comment.max-replies-size:20}")
    private int maxRepliesSize;

    /**
     * 敏感词替换字符
     */
    @Value("${comment.sensitive-replacement:***}")
    private String sensitiveReplacement;

    /**
     * 热门评论点赞阈值
     */
    @Value("${comment.hot-comment-threshold:10}")
    private int hotCommentThreshold;

    /**
     * 【修复】敏感词列表配置（逗号分隔）
     */
    @Value("${comment.sensitive.words:色情,暴力,赌博,毒品,诈骗,广告,违禁词}")
    private String sensitiveWordsConfig;

    /**
     * 【修复】是否启用敏感词过滤
     */
    @Value("${comment.sensitive.enabled:true}")
    private boolean sensitiveFilterEnabled;

    /**
     * 【修复】分布式锁过期时间（秒）
     * 高并发场景下需要足够长的时间，防止业务执行超过锁过期时间
     */
    @Value("${comment.lock.expire-time:30}")
    private long lockExpireTime;

    /**
     * 【修复】获取敏感词列表
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

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    public int getMaxRepliesSize() {
        return maxRepliesSize;
    }

    public void setMaxRepliesSize(int maxRepliesSize) {
        this.maxRepliesSize = maxRepliesSize;
    }

    public String getSensitiveReplacement() {
        return sensitiveReplacement;
    }

    public void setSensitiveReplacement(String sensitiveReplacement) {
        this.sensitiveReplacement = sensitiveReplacement;
    }

    public int getHotCommentThreshold() {
        return hotCommentThreshold;
    }

    public void setHotCommentThreshold(int hotCommentThreshold) {
        this.hotCommentThreshold = hotCommentThreshold;
    }

    public String getSensitiveWordsConfig() {
        return sensitiveWordsConfig;
    }

    public void setSensitiveWordsConfig(String sensitiveWordsConfig) {
        this.sensitiveWordsConfig = sensitiveWordsConfig;
    }

    public boolean isSensitiveFilterEnabled() {
        return sensitiveFilterEnabled;
    }

    public void setSensitiveFilterEnabled(boolean sensitiveFilterEnabled) {
        this.sensitiveFilterEnabled = sensitiveFilterEnabled;
    }

    public long getLockExpireTime() {
        return lockExpireTime;
    }

    public void setLockExpireTime(long lockExpireTime) {
        this.lockExpireTime = lockExpireTime;
    }
}
