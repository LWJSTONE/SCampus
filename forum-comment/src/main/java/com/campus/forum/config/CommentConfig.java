package com.campus.forum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

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
}
