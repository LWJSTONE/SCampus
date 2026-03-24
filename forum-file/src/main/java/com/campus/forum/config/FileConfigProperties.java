package com.campus.forum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 文件服务配置属性类
 * 
 * 修复问题：使用 @ConfigurationProperties 正确读取 YAML 列表格式配置
 * 原 @Value 注解无法正确读取 YAML 列表格式（使用 - 前缀的列表），
 * 导致 file.allowed-types 和 file.image-types 配置无法生效
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileConfigProperties {

    /**
     * 存储配置
     */
    private StorageConfig storage = new StorageConfig();

    /**
     * 允许上传的文件类型列表
     * 修复：正确读取 YAML 列表格式配置
     */
    private List<String> allowedTypes = Collections.emptyList();

    /**
     * 允许上传的图片类型列表
     * 修复：正确读取 YAML 列表格式配置
     */
    private List<String> imageTypes = Collections.emptyList();

    /**
     * 最大文件大小（字节）
     */
    private Long maxSize = 52428800L; // 默认 50MB

    /**
     * 批量上传配置
     */
    private BatchConfig batch = new BatchConfig();

    @Data
    public static class StorageConfig {
        /**
         * 存储类型: LOCAL-本地存储, MINIO-MinIO存储
         */
        private String type = "LOCAL";

        /**
         * 本地存储路径
         */
        private String localPath = "/data/files";

        /**
         * 访问URL前缀
         */
        private String urlPrefix = "http://localhost:9010/files";
    }

    @Data
    public static class BatchConfig {
        /**
         * 批量上传最大文件数量
         * 修复：添加批量上传数量限制，防止恶意大量文件上传
         */
        private int maxCount = 10;
    }
}
