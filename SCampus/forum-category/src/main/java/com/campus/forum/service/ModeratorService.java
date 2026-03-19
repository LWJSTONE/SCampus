package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.dto.ModeratorDTO;
import com.campus.forum.entity.Moderator;
import com.campus.forum.vo.ModeratorVO;

import java.util.List;

/**
 * 版主服务接口
 *
 * @author campus
 * @since 2024-01-01
 */
public interface ModeratorService extends IService<Moderator> {

    /**
     * 获取版块的版主列表
     *
     * @param forumId 版块ID
     * @return 版主列表
     */
    List<ModeratorVO> getModerators(Long forumId);

    /**
     * 添加版主
     *
     * @param forumId 版块ID
     * @param dto     版主DTO
     * @return 版主实体
     */
    Moderator addModerator(Long forumId, ModeratorDTO dto);

    /**
     * 移除版主
     *
     * @param forumId 版块ID
     * @param userId  用户ID
     * @return 是否成功
     */
    boolean removeModerator(Long forumId, Long userId);

    /**
     * 设置主版主
     *
     * @param forumId 版块ID
     * @param userId  用户ID
     * @return 是否成功
     */
    boolean setPrimaryModerator(Long forumId, Long userId);

    /**
     * 检查用户是否为版主
     *
     * @param forumId 版块ID
     * @param userId  用户ID
     * @return 是否为版主
     */
    boolean isModerator(Long forumId, Long userId);

    /**
     * 获取用户管理的版块ID列表
     *
     * @param userId 用户ID
     * @return 版块ID列表
     */
    List<Long> getManagedForumIds(Long userId);
}
