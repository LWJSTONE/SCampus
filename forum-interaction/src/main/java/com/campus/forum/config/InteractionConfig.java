package com.campus.forum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 互动服务业务配置类
 * 
 * 从配置文件读取业务相关参数
 * 
 * @author campus
 * @since 2024-01-01
 */
@Configuration
public class InteractionConfig {

    /**
     * 点赞缓存过期时间（秒）
     */
    @Value("${interaction.like.cache-expire:86400}")
    private long likeCacheExpire;

    /**
     * 收藏缓存过期时间（秒）
     */
    @Value("${interaction.collect.cache-expire:86400}")
    private long collectCacheExpire;

    /**
     * 收藏每页最大数量
     */
    @Value("${interaction.collect.max-page-size:50}")
    private int collectMaxPageSize;

    /**
     * 【修复】缓存补偿任务是否启用
     */
    @Value("${interaction.compensation.enabled:true}")
    private boolean compensationEnabled;

    /**
     * 【修复】缓存补偿任务执行间隔（毫秒）
     * 默认每5分钟执行一次
     */
    @Value("${interaction.compensation.interval:300000}")
    private long compensationInterval;

    /**
     * 【修复】缓存补偿批次大小
     */
    @Value("${interaction.compensation.batch-size:100}")
    private int compensationBatchSize;

    /**
     * 【修复】分布式锁过期时间（秒）
     */
    @Value("${interaction.lock.expire-time:10}")
    private long lockExpireTime;

    public long getLikeCacheExpire() {
        return likeCacheExpire;
    }

    public void setLikeCacheExpire(long likeCacheExpire) {
        this.likeCacheExpire = likeCacheExpire;
    }

    public long getCollectCacheExpire() {
        return collectCacheExpire;
    }

    public void setCollectCacheExpire(long collectCacheExpire) {
        this.collectCacheExpire = collectCacheExpire;
    }

    public int getCollectMaxPageSize() {
        return collectMaxPageSize;
    }

    public void setCollectMaxPageSize(int collectMaxPageSize) {
        this.collectMaxPageSize = collectMaxPageSize;
    }

    public boolean isCompensationEnabled() {
        return compensationEnabled;
    }

    public void setCompensationEnabled(boolean compensationEnabled) {
        this.compensationEnabled = compensationEnabled;
    }

    public long getCompensationInterval() {
        return compensationInterval;
    }

    public void setCompensationInterval(long compensationInterval) {
        this.compensationInterval = compensationInterval;
    }

    public int getCompensationBatchSize() {
        return compensationBatchSize;
    }

    public void setCompensationBatchSize(int compensationBatchSize) {
        this.compensationBatchSize = compensationBatchSize;
    }

    public long getLockExpireTime() {
        return lockExpireTime;
    }

    public void setLockExpireTime(long lockExpireTime) {
        this.lockExpireTime = lockExpireTime;
    }
}
