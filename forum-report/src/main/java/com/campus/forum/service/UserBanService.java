package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.dto.UserBanDTO;
import com.campus.forum.entity.UserBan;
import com.campus.forum.vo.UserBanVO;

import java.util.List;

/**
 * 用户禁言服务接口
 *
 * @author campus
 * @since 2024-01-01
 */
public interface UserBanService extends IService<UserBan> {

    /**
     * 禁言用户
     *
     * @param banDTO 禁言DTO
     * @param operatorId 操作人ID
     * @return 禁言ID
     */
    Long banUser(UserBanDTO banDTO, Long operatorId);

    /**
     * 解除禁言
     *
     * @param userId 用户ID
     * @param operatorId 操作人ID
     * @param reason 解除原因
     * @return 是否成功
     */
    boolean unbanUser(Long userId, Long operatorId, String reason);

    /**
     * 分页查询禁言列表
     *
     * @param userId 用户ID（可选）
     * @param status 状态（可选）
     * @param current 当前页
     * @param size 每页大小
     * @return 禁言分页列表
     */
    Page<UserBanVO> getBanPage(Long userId, Integer status, Integer current, Integer size);

    /**
     * 查询用户当前禁言状态
     *
     * @param userId 用户ID
     * @return 禁言信息，无禁言返回null
     */
    UserBanVO getActiveBan(Long userId);

    /**
     * 检查用户是否被禁言
     *
     * @param userId 用户ID
     * @return 是否被禁言
     */
    boolean isBanned(Long userId);

    /**
     * 查询用户禁言历史
     *
     * @param userId 用户ID
     * @return 禁言历史列表
     */
    List<UserBanVO> getBanHistory(Long userId);
}
