package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.dto.ForumDTO;
import com.campus.forum.entity.Forum;
import com.campus.forum.vo.ForumVO;

import java.util.List;

/**
 * 版块服务接口
 *
 * @author campus
 * @since 2024-01-01
 */
public interface ForumService extends IService<Forum> {

    /**
     * 获取所有版块列表
     *
     * @return 版块列表
     */
    List<ForumVO> getAllForums();

    /**
     * 根据分类获取版块列表
     *
     * @param categoryId 分类ID
     * @return 版块列表
     */
    List<ForumVO> getForumsByCategory(Long categoryId);

    /**
     * 获取版块详情
     *
     * @param id 版块ID
     * @return 版块详情
     */
    ForumVO getForumDetail(Long id);

    /**
     * 创建版块
     *
     * @param dto 版块DTO
     * @return 版块实体
     */
    Forum createForum(ForumDTO dto);

    /**
     * 更新版块
     *
     * @param id  版块ID
     * @param dto 版块DTO
     * @return 是否成功
     */
    boolean updateForum(Long id, ForumDTO dto);

    /**
     * 删除版块
     *
     * @param id 版块ID
     * @return 是否成功
     */
    boolean deleteForum(Long id);

    /**
     * 更新版块状态
     *
     * @param id     版块ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 增加帖子数量
     *
     * @param forumId 版块ID
     */
    void incrementPostCount(Long forumId);

    /**
     * 减少帖子数量
     *
     * @param forumId 版块ID
     */
    void decrementPostCount(Long forumId);

    /**
     * 更新最后发帖信息
     *
     * @param forumId  版块ID
     * @param userId   用户ID
     * @param postTitle 帖子标题
     */
    void updateLastPost(Long forumId, Long userId, String postTitle);

    /**
     * 检查用户是否为版主
     *
     * @param forumId 版块ID
     * @param userId  用户ID
     * @return 是否为版主
     */
    boolean isModerator(Long forumId, Long userId);
}
