package com.campus.forum.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果类
 * 用于封装分页查询结果
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 数据列表
     */
    private List<T> records;

    public PageResult() {
    }

    public PageResult(Long current, Long size, Long total, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.records = records;
        this.pages = (total + size - 1) / size;
    }

    /**
     * 构建分页结果
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param total   总记录数
     * @param records 数据列表
     * @param <T>     数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> build(Long current, Long size, Long total, List<T> records) {
        return new PageResult<>(current, size, total, records);
    }

    /**
     * 从MyBatis Plus的Page对象转换
     *
     * @param page MyBatis Plus分页对象
     * @param <T>  数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }
}
