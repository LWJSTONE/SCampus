package com.campus.forum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.dto.ForumDTO;
import com.campus.forum.entity.Category;
import com.campus.forum.entity.Forum;
import com.campus.forum.entity.Moderator;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.CategoryMapper;
import com.campus.forum.mapper.ForumMapper;
import com.campus.forum.mapper.ModeratorMapper;
import com.campus.forum.service.ForumService;
import com.campus.forum.service.ModeratorService;
import com.campus.forum.vo.ForumVO;
import com.campus.forum.vo.ModeratorVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.function.Function;

/**
 * 版块服务实现类
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForumServiceImpl extends ServiceImpl<ForumMapper, Forum> implements ForumService {

    private final ForumMapper forumMapper;
    private final CategoryMapper categoryMapper;
    private final ModeratorMapper moderatorMapper;
    private final ModeratorService moderatorService;

    @Override
    public List<ForumVO> getAllForums() {
        log.info("获取所有版块列表");
        List<Forum> forums = forumMapper.selectAllActive();
        // 【性能优化】使用批量转换解决N+1查询问题
        return convertToVOList(forums);
    }

    @Override
    public List<ForumVO> getForumsByCategory(Long categoryId) {
        log.info("获取分类下的版块列表：{}", categoryId);
        List<Forum> forums = forumMapper.selectByCategoryId(categoryId);
        // 【性能优化】使用批量转换解决N+1查询问题
        return convertToVOList(forums);
    }

    @Override
    public ForumVO getForumDetail(Long id) {
        log.info("获取版块详情：{}", id);
        Forum forum = getById(id);
        if (forum == null) {
            throw new BusinessException("版块不存在");
        }
        return convertToVO(forum);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Forum createForum(ForumDTO dto) {
        log.info("创建版块：{}", dto.getName());

        // 检查分类是否存在
        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException("所属分类不存在");
        }

        // 检查版块名称是否已存在
        int count = forumMapper.countByName(dto.getName(), 0L);
        if (count > 0) {
            throw new BusinessException("版块名称已存在");
        }

        Forum forum = new Forum();
        BeanUtil.copyProperties(dto, forum);
        forum.setPostCount(0);
        forum.setTodayPostCount(0);

        save(forum);
        return forum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateForum(Long id, ForumDTO dto) {
        log.info("更新版块：{}", id);

        Forum forum = getById(id);
        if (forum == null) {
            throw new BusinessException("版块不存在");
        }

        // 检查分类是否存在
        if (dto.getCategoryId() != null) {
            Category category = categoryMapper.selectById(dto.getCategoryId());
            if (category == null) {
                throw new BusinessException("所属分类不存在");
            }
        }

        // 检查版块名称是否重复
        if (!forum.getName().equals(dto.getName())) {
            int count = forumMapper.countByName(dto.getName(), id);
            if (count > 0) {
                throw new BusinessException("版块名称已存在");
            }
        }

        BeanUtil.copyProperties(dto, forum, "id", "postCount", "todayPostCount");
        return updateById(forum);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteForum(Long id) {
        log.info("删除版块：{}", id);

        Forum forum = getById(id);
        if (forum == null) {
            throw new BusinessException("版块不存在");
        }

        // 检查版块下是否存在帖子（需要调用帖子服务或检查post_count）
        // 这里检查post_count字段，如果大于0则不允许删除
        if (forum.getPostCount() != null && forum.getPostCount() > 0) {
            throw new BusinessException("该版块下还有帖子，无法删除。请先迁移或删除帖子。");
        }

        // 逻辑删除版块的所有版主关联
        moderatorMapper.deleteByForumId(id);

        // 逻辑删除版块（与分类删除保持一致）
        forum.setDeleteFlag(1);
        return updateById(forum);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        log.info("更新版块状态：{}，状态：{}", id, status);
        return forumMapper.updateStatus(id, status) > 0;
    }

    @Override
    public void incrementPostCount(Long forumId) {
        forumMapper.incrementPostCount(forumId);
    }

    @Override
    public void decrementPostCount(Long forumId) {
        forumMapper.decrementPostCount(forumId);
    }

    @Override
    public void updateLastPost(Long forumId, Long userId, String postTitle) {
        forumMapper.updateLastPost(forumId, userId, postTitle);
    }

    @Override
    public boolean isModerator(Long forumId, Long userId) {
        return moderatorService.isModerator(forumId, userId);
    }

    /**
     * 转换为VO（单个版块，用于详情查询）
     */
    private ForumVO convertToVO(Forum forum) {
        ForumVO vo = new ForumVO();
        BeanUtil.copyProperties(forum, vo);

        // 获取分类名称
        if (forum.getCategoryId() != null) {
            Category category = categoryMapper.selectById(forum.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        // 获取版主列表
        List<ModeratorVO> moderators = moderatorService.getModerators(forum.getId());
        vo.setModerators(moderators);

        // 设置主版主名称
        moderators.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsPrimary()))
                .findFirst()
                .ifPresent(m -> vo.setModeratorName(m.getNickname() != null ? m.getNickname() : m.getUsername()));

        return vo;
    }

    /**
     * 批量转换为VO列表
     * 【性能优化】解决N+1查询问题，使用批量查询后Map映射
     * 
     * 原实现：每个版块单独查询分类名称和版主列表，N个版块产生2N+1次查询
     * 优化后：批量查询所有分类和版主，通过Map映射组装，仅需3次查询
     *
     * @param forums 版块列表
     * @return VO列表
     */
    private List<ForumVO> convertToVOList(List<Forum> forums) {
        if (forums == null || forums.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 收集所有分类ID
        Set<Long> categoryIds = forums.stream()
                .map(Forum::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 2. 批量查询分类信息并构建Map
        Map<Long, Category> categoryMap = Collections.emptyMap();
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryMapper.selectByIds(new ArrayList<>(categoryIds));
            categoryMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, Function.identity()));
        }

        // 3. 收集所有版块ID
        List<Long> forumIds = forums.stream()
                .map(Forum::getId)
                .collect(Collectors.toList());

        // 4. 批量查询版主信息并按版块ID分组
        Map<Long, List<ModeratorVO>> moderatorsByForumId = Collections.emptyMap();
        if (!forumIds.isEmpty()) {
            List<Moderator> allModerators = moderatorMapper.selectByForumIds(forumIds);
            moderatorsByForumId = allModerators.stream()
                    .map(this::convertModeratorToVO)
                    .collect(Collectors.groupingBy(ModeratorVO::getForumId));
        }

        // 5. 组装VO列表
        final Map<Long, Category> finalCategoryMap = categoryMap;
        final Map<Long, List<ModeratorVO>> finalModeratorsMap = moderatorsByForumId;

        return forums.stream().map(forum -> {
            ForumVO vo = new ForumVO();
            BeanUtil.copyProperties(forum, vo);

            // 设置分类名称
            if (forum.getCategoryId() != null) {
                Category category = finalCategoryMap.get(forum.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                }
            }

            // 设置版主列表
            List<ModeratorVO> moderators = finalModeratorsMap.getOrDefault(forum.getId(), Collections.emptyList());
            vo.setModerators(moderators);

            // 设置主版主名称
            moderators.stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIsPrimary()))
                    .findFirst()
                    .ifPresent(m -> vo.setModeratorName(m.getNickname() != null ? m.getNickname() : m.getUsername()));

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 转换版主实体为VO
     */
    private ModeratorVO convertModeratorToVO(Moderator moderator) {
        ModeratorVO vo = new ModeratorVO();
        BeanUtil.copyProperties(moderator, vo);
        return vo;
    }
}
