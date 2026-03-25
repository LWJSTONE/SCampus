package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 分页查询DTO
 * 通用分页查询参数
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "分页查询DTO")
public class PageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", defaultValue = "1")
    @Min(value = 1, message = "页码最小值为1")
    private Integer current = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", defaultValue = "10")
    @Min(value = 1, message = "每页大小最小值为1")
    @Max(value = 100, message = "每页大小最大值为100")
    private Integer size = 10;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段")
    private String sortField;

    /**
     * 排序方式（asc/desc）
     */
    @Schema(description = "排序方式（asc/desc）")
    private String sortOrder;

    /**
     * 关键词
     */
    @Schema(description = "关键词")
    private String keyword;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private String startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private String endTime;

    /**
     * 获取分页偏移量
     *
     * @return 偏移量
     */
    public Integer getOffset() {
        return (current - 1) * size;
    }

    /**
     * 获取MyBatis Plus的Page对象
     *
     * @param <T> 数据类型
     * @return Page对象
     */
    public <T> com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> toPage() {
        return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
    }
}
