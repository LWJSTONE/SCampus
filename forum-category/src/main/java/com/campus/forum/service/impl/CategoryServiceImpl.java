package com.campus.forum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.dto.CategoryDTO;
import com.campus.forum.entity.Category;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.CategoryMapper;
import com.campus.forum.mapper.ForumMapper;
import com.campus.forum.service.CategoryService;
import com.campus.forum.vo.CategoryTreeVO;
import com.campus.forum.vo.ForumVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ForumMapper forumMapper;

    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        log.info("获取分类树形结构");

        // 查询所有分类
        List<Category> allCategories = categoryMapper.selectAllCategories();

        // 构建树形结构
        Map<Long, List<Category>> categoryMap = allCategories.stream()
                .collect(Collectors.groupingBy(Category::getParentId));

        // 获取顶级分类
        List<Category> topCategories = categoryMap.getOrDefault(0L, new ArrayList<>());

        // 递归构建树
        return topCategories.stream()
                .map(category -> buildCategoryTree(category, categoryMap))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category createCategory(CategoryDTO dto) {
        log.info("创建分类：{}", dto.getName());

        // 检查分类名称是否已存在
        int count = categoryMapper.countByName(dto.getName(), dto.getParentId());
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }

        // 检查父分类是否存在
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            Category parent = getById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
        }

        Category category = new Category();
        BeanUtil.copyProperties(dto, category);
        category.setPostCount(0);

        save(category);
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCategory(Long id, CategoryDTO dto) {
        log.info("更新分类：{}", id);

        Category category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查分类名称是否重复
        if (!category.getName().equals(dto.getName())) {
            int count = categoryMapper.countByName(dto.getName(), dto.getParentId() != null ? dto.getParentId() : category.getParentId());
            if (count > 0) {
                throw new BusinessException("分类名称已存在");
            }
        }

        // 检查是否将分类设置为自己的子分类
        if (dto.getParentId() != null && dto.getParentId().equals(id)) {
            throw new BusinessException("不能将分类设置为自己的子分类");
        }

        BeanUtil.copyProperties(dto, category, "id");
        return updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(Long id) {
        log.info("删除分类：{}", id);

        Category category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查是否有子分类
        int childCount = categoryMapper.countChildren(id);
        if (childCount > 0) {
            throw new BusinessException("该分类下还有子分类，无法删除");
        }

        // 检查是否有版块
        int forumCount = categoryMapper.countForums(id);
        if (forumCount > 0) {
            throw new BusinessException("该分类下还有版块，无法删除");
        }

        // 使用逻辑删除而非物理删除，保持与其他模块一致
        category.setDeleteFlag(1);
        return updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        log.info("更新分类状态：{}，状态：{}", id, status);
        return categoryMapper.updateStatus(id, status) > 0;
    }

    @Override
    public List<Category> getChildren(Long parentId) {
        return categoryMapper.selectByParentId(parentId);
    }

    /**
     * 递归构建分类树
     */
    private CategoryTreeVO buildCategoryTree(Category category, Map<Long, List<Category>> categoryMap) {
        CategoryTreeVO vo = new CategoryTreeVO();
        BeanUtil.copyProperties(category, vo);

        // 获取子分类
        List<Category> children = categoryMap.getOrDefault(category.getId(), new ArrayList<>());
        if (!children.isEmpty()) {
            vo.setChildren(children.stream()
                    .map(child -> buildCategoryTree(child, categoryMap))
                    .collect(Collectors.toList()));
        }

        return vo;
    }
}
