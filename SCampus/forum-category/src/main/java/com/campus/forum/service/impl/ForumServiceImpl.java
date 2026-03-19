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
import java.util.stream.Collectors;

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
        return forums.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ForumVO> getForumsByCategory(Long categoryId) {
        log.info("获取分类下的版块列表：{}", categoryId);
        List<Forum> forums = forumMapper.selectByCategoryId(categoryId);
        return forums.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
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

        // 删除版块的所有版主
        moderatorMapper.deleteByForumId(id);

        return removeById(id);
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
     * 转换为VO
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
}
