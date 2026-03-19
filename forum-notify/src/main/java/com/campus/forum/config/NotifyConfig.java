package com.campus.forum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 通知配置类
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "notify")
public class NotifyConfig {

    /**
     * 每页最大通知数量
     */
    private int maxPageSize = 50;

    /**
     * 通知标题最大长度
     */
    private int maxTitleLength = 100;

    /**
     * 通知内容最大长度
     */
    private int maxContentLength = 2000;

    /**
     * 未读消息缓存过期时间（小时）
     */
    private int unreadCacheExpire = 24;
}
