package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.dto.PostCreateDTO;
import com.campus.forum.dto.PostQueryDTO;
import com.campus.forum.dto.PostUpdateDTO;
import com.campus.forum.entity.PageResult;
import com.campus.forum.entity.Post;
import com.campus.forum.vo.PostDetailVO;
import com.campus.forum.vo.PostListVO;

import java.util.List;

/**
 * 帖子服务接口
 * 提供帖子相关的业务操作
 *
 * @author campus
 * @since 2024-01-01
 */
public interface PostService {

    /**
     * 分页查询帖子列表
     *
     * @param queryDTO 查询参数
     * @param currentUserId 当前用户ID（可为空）
     * @return 帖子分页列表
     */
    PageResult<PostListVO> getPostList(PostQueryDTO queryDTO, Long currentUserId);

    /**
     * 获取帖子详情
     *
     * @param id 帖子ID
     * @param currentUserId 当前用户ID（可为空）
     * @param ipAddress 用户IP地址（用于浏览量防刷，可为空）
     * @return 帖子详情
     */
    PostDetailVO getPostDetail(Long id, Long currentUserId, String ipAddress);

    /**
     * 发布帖子
     *
     * @param createDTO 帖子创建DTO
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @return 帖子ID
     */
    Long publishPost(PostCreateDTO createDTO, Long userId, String ipAddress);

    /**
     * 编辑帖子
     *
     * @param updateDTO 帖子更新DTO
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean updatePost(PostUpdateDTO updateDTO, Long userId);

    /**
     * 删除帖子
     *
     * @param id 帖子ID
     * @param userId 用户ID
     * @param isAdmin 是否为管理员
     * @return 是否成功
     */
    boolean deletePost(Long id, Long userId, boolean isAdmin);

    /**
     * 置顶/取消置顶帖子
     *
     * 【安全修复】添加管理员权限验证参数，Service层进行权限二次校验
     * 防止Controller层权限绕过或Header伪造攻击
     *
     * @param id 帖子ID
     * @param isTop 是否置顶
     * @param operatorId 操作人ID
     * @param isAdmin 是否为管理员（由Controller传入，需要经过二次验证）
     * @return 是否成功
     */
    boolean setTop(Long id, Integer isTop, Long operatorId, boolean isAdmin);

    /**
     * 加精/取消加精帖子
     *
     * 【安全修复】添加管理员权限验证参数，Service层进行权限二次校验
     * 防止Controller层权限绕过或Header伪造攻击
     *
     * @param id 帖子ID
     * @param isEssence 是否精华
     * @param operatorId 操作人ID
     * @param isAdmin 是否为管理员（由Controller传入，需要经过二次验证）
     * @return 是否成功
     */
    boolean setEssence(Long id, Integer isEssence, Long operatorId, boolean isAdmin);

    /**
     * 获取热门帖子列表
     *
     * @param limit 数量限制
     * @param currentUserId 当前用户ID
     * @return 热门帖子列表
     */
    List<PostListVO> getHotPosts(Integer limit, Long currentUserId);

    /**
     * 搜索帖子
     *
     * @param keyword 关键词
     * @param queryDTO 查询参数
     * @param currentUserId 当前用户ID
     * @return 搜索结果
     */
    PageResult<PostListVO> searchPosts(String keyword, PostQueryDTO queryDTO, Long currentUserId);

    /**
     * 增加浏览量（内部API使用，无防刷机制）
     *
     * @param id 帖子ID
     */
    void incrementViewCount(Long id);

    /**
     * 增加浏览量（带防刷机制）
     * 同一用户/IP在指定时间窗口内只计一次浏览
     *
     * @param id 帖子ID
     * @param userId 用户ID（可为null，未登录用户使用IP标识）
     * @param ipAddress 用户IP地址（用于未登录用户的防刷）
     * @return 是否成功计入浏览量（true-新浏览，false-重复浏览被过滤）
     */
    boolean incrementViewCountWithAntiSpam(Long id, Long userId, String ipAddress);

    /**
     * 点赞/取消点赞帖子
     *
     * @param id 帖子ID
     * @param userId 用户ID
     * @return 是否点赞（true-点赞成功，false-取消点赞）
     */
    boolean likePost(Long id, Long userId);

    /**
     * 收藏/取消收藏帖子
     *
     * @param id 帖子ID
     * @param userId 用户ID
     * @return 是否收藏（true-收藏成功，false-取消收藏）
     */
    boolean collectPost(Long id, Long userId);

    /**
     * 获取用户发布的帖子
     *
     * @param userId 用户ID
     * @param queryDTO 查询参数
     * @param currentUserId 当前用户ID
     * @return 帖子列表
     */
    PageResult<PostListVO> getUserPosts(Long userId, PostQueryDTO queryDTO, Long currentUserId);

    /**
     * 统计用户帖子数量
     *
     * @param userId 用户ID
     * @return 帖子数量
     */
    int countByUserId(Long userId);

    /**
     * 更新帖子评论数
     *
     * @param id 帖子ID
     * @param delta 变化量
     */
    void updateCommentCount(Long id, Integer delta);

    /**
     * 移动帖子到其他版块
     *
     * @param id 帖子ID
     * @param forumId 目标版块ID
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean movePost(Long id, Long forumId, Long operatorId);

    /**
     * 更新帖子状态（关闭/打开）
     *
     * @param id 帖子ID
     * @param status 帖子状态
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean updatePostStatus(Long id, Integer status, Long operatorId);

    /**
     * 审核帖子
     *
     * @param id 帖子ID
     * @param status 审核状态(2-通过 3-拒绝)
     * @param reason 审核备注
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean auditPost(Long id, Integer status, String reason, Long operatorId);
}
