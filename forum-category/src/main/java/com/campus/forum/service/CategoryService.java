package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.dto.CategoryDTO;
import com.campus.forum.entity.Category;
import com.campus.forum.vo.CategoryTreeVO;

import java.util.List;

/**
 * 分类服务接口
 *
 * @author campus
 * @since 2024-01-01
 */
public interface CategoryService extends IService<Category> {

    /**
     * 获取分类树形结构
     *
     * @return 分类树形列表
     */
    List<CategoryTreeVO> getCategoryTree();

    /**
     * 创建分类
     *
     * @param dto 分类DTO
     * @return 分类实体
     */
    Category createCategory(CategoryDTO dto);

    /**
     * 更新分类
     *
     * @param id  分类ID
     * @param dto 分类DTO
     * @return 是否成功
     */
    boolean updateCategory(Long id, CategoryDTO dto);

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(Long id);

    /**
     * 更新分类状态
     *
     * @param id     分类ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 获取子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<Category> getChildren(Long parentId);
}
