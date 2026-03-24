package com.campus.forum.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.campus.forum.config.PostConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.constant.ResultCode;
import com.campus.forum.dto.PostCreateDTO;
import com.campus.forum.dto.PostQueryDTO;
import com.campus.forum.dto.PostUpdateDTO;
import com.campus.forum.entity.PageResult;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.PostAttachment;
import com.campus.forum.entity.PostTag;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.PostAttachmentMapper;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.mapper.PostTagMapper;
import com.campus.forum.service.PostService;
import com.campus.forum.vo.PostDetailVO;
import com.campus.forum.vo.PostListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子服务实现类
 * 实现帖子相关的业务逻辑
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final PostTagMapper postTagMapper;
    private final PostAttachmentMapper postAttachmentMapper;
    private final StringRedisTemplate redisTemplate;
    private final PostConfig postConfig;

    // Redis Key前缀
    private static final String REDIS_KEY_POST_VIEW = "post:view:";
    private static final String REDIS_KEY_POST_LIKE = "post:like:";
    private static final String REDIS_KEY_POST_COLLECT = "post:collect:";
    private static final String REDIS_KEY_HOT_POSTS = "post:hot:list";
    private static final String REDIS_KEY_POST_COUNT = "post:count:user:";
    private static final String REDIS_KEY_POST_LIKE_LOCK = "post:like:lock:";
    private static final String REDIS_KEY_POST_COLLECT_LOCK = "post:collect:lock:";
    // 【修复】浏览量防刷相关key
    private static final String REDIS_KEY_POST_VIEWED_USER = "post:viewed:user:";  // 已浏览用户记录
    private static final String REDIS_KEY_POST_VIEWED_IP = "post:viewed:ip:";      // 已浏览IP记录
    private static final long LOCK_EXPIRE_TIME = 3; // 锁过期时间（秒）
    // 【修复】防刷时间窗口：同一用户/IP在此时间内重复浏览不计入
    private static final long VIEW_ANTI_SPAM_HOURS = 24;

    // 【修复】敏感词列表改为从PostConfig配置类获取，支持配置文件动态配置
    // 不再使用硬编码，便于运维人员调整敏感词列表

    @Override
    public PageResult<PostListVO> getPostList(PostQueryDTO queryDTO, Long currentUserId) {
        log.info("获取帖子列表, queryDTO: {}, currentUserId: {}", queryDTO, currentUserId);

        // 构建分页参数
        Page<PostListVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        // 查询帖子列表
        IPage<PostListVO> postPage = postMapper.selectPostPage(page, queryDTO);

        // 处理每条帖子
        for (PostListVO post : postPage.getRecords()) {
            processPost(post, currentUserId);
        }

        return PageResult.of(postPage);
    }

    @Override
    public PostDetailVO getPostDetail(Long id, Long currentUserId, String ipAddress) {
        log.info("获取帖子详情, id: {}, currentUserId: {}", id, currentUserId);

        // 查询帖子
        Post post = postMapper.selectPostDetailById(id);
        if (post == null || post.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        // 检查帖子状态
        if (post.getStatus() != 1) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND, "帖子不存在或已被删除");
        }

        // 转换为VO
        PostDetailVO detailVO = new PostDetailVO();
        BeanUtils.copyProperties(post, detailVO);

        // 设置用户信息
        detailVO.setUserName(post.getUserName());
        detailVO.setUserAvatar(post.getUserAvatar());
        detailVO.setForumName(post.getForumName());

        // 设置标签列表
        List<PostTag> tags = postTagMapper.selectByPostId(id);
        if (tags != null && !tags.isEmpty()) {
            List<PostDetailVO.TagVO> tagVOList = tags.stream().map(tag -> {
                PostDetailVO.TagVO tagVO = new PostDetailVO.TagVO();
                tagVO.setId(tag.getTagId());
                tagVO.setName(tag.getTagName());
                return tagVO;
            }).collect(Collectors.toList());
            detailVO.setTags(tagVOList);
        }

        // 设置附件列表
        List<PostAttachment> attachments = postAttachmentMapper.selectByPostId(id);
        if (attachments != null && !attachments.isEmpty()) {
            List<PostDetailVO.AttachmentVO> attachmentVOList = attachments.stream().map(att -> {
                PostDetailVO.AttachmentVO attVO = new PostDetailVO.AttachmentVO();
                attVO.setId(att.getId());
                attVO.setType(att.getType());
                attVO.setName(att.getName());
                attVO.setUrl(att.getUrl());
                attVO.setThumbnailUrl(att.getThumbnailUrl());
                attVO.setSize(att.getSize());
                attVO.setMimeType(att.getMimeType());
                attVO.setWidth(att.getWidth());
                attVO.setHeight(att.getHeight());
                attVO.setDuration(att.getDuration());
                return attVO;
            }).collect(Collectors.toList());
            detailVO.setAttachments(attachmentVOList);
        }

        // 设置用户状态
        if (currentUserId != null) {
            detailVO.setIsLiked(isLiked(id, currentUserId));
            detailVO.setIsCollected(isCollected(id, currentUserId));
            detailVO.setIsAuthor(post.getUserId().equals(currentUserId));
        }

        // 【修复】增加浏览量（带防刷机制）
        // 同一用户/IP在24小时内只计一次浏览
        incrementViewCountWithAntiSpam(id, currentUserId, ipAddress);

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishPost(PostCreateDTO createDTO, Long userId, String ipAddress) {
        log.info("发布帖子, userId: {}, title: {}", userId, createDTO.getTitle());

        // 1. 参数校验
        validatePost(createDTO);

        // 2. 验证板块是否存在（简化处理，实际应调用forum服务）
        // Forum forum = forumMapper.selectById(createDTO.getForumId());
        // if (forum == null || forum.getStatus() != 1) {
        //     throw new BusinessException(ResultCode.FORUM_NOT_FOUND, "板块不存在或已关闭");
        // }

        // 3. 敏感词过滤
        String content = filterSensitiveWords(createDTO.getContent());
        String title = filterSensitiveWords(createDTO.getTitle());

        // 4. 构建帖子实体
        Post post = new Post();
        post.setForumId(createDTO.getForumId());
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(content);
        post.setSummary(generateSummary(createDTO.getSummary(), content));
        post.setType(createDTO.getType());
        post.setStatus(1); // 已发布状态
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);
        post.setShareCount(0);
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setAllowComment(createDTO.getAllowComment() ? 1 : 0);
        post.setCoverImage(createDTO.getCoverImage());
        post.setSourceType(createDTO.getSourceType());
        post.setIpAddress(ipAddress);
        post.setAuditStatus(1); // 审核通过（实际项目可能需要审核流程）

        // 5. 保存帖子
        postMapper.insert(post);

        Long postId = post.getId();

        // 6. 保存标签关联
        if (createDTO.getTagIds() != null && !createDTO.getTagIds().isEmpty()) {
            savePostTags(postId, createDTO.getTagIds());
        }

        // 7. 保存附件
        if (createDTO.getAttachments() != null && !createDTO.getAttachments().isEmpty()) {
            savePostAttachments(postId, createDTO.getAttachments());
        }

        // 8. 更新用户帖子数缓存
        // 【修复】为Redis key设置过期时间（24小时），避免缓存永久存在
        String countKey = REDIS_KEY_POST_COUNT + userId;
        Long newCount = redisTemplate.opsForValue().increment(countKey);
        if (newCount != null && newCount == 1) {
            // 如果是新建的key，设置过期时间
            redisTemplate.expire(countKey, Duration.ofHours(24));
        }

        log.info("帖子发布成功, postId: {}", postId);
        return postId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePost(PostUpdateDTO updateDTO, Long userId) {
        log.info("编辑帖子, postId: {}, userId: {}", updateDTO.getId(), userId);

        // 1. 查询帖子
        Post post = postMapper.selectById(updateDTO.getId());
        if (post == null || post.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        // 2. 权限校验（只能编辑自己的帖子）
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.POST_NO_PERMISSION);
        }

        // 3. 检查帖子状态
        if (post.getStatus() == 3) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND, "帖子已被删除，无法编辑");
        }
        if (post.getStatus() == 2) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "帖子已关闭，无法编辑");
        }

        // 4. 敏感词过滤
        String content = updateDTO.getContent() != null ? filterSensitiveWords(updateDTO.getContent()) : null;
        String title = updateDTO.getTitle() != null ? filterSensitiveWords(updateDTO.getTitle()) : null;

        // 【安全修复】5. 校验帖子类型范围（0-普通帖子 1-精华帖 2-置顶帖 3-公告）
        // 防止恶意用户传入非法类型值
        if (updateDTO.getType() != null && (updateDTO.getType() < 0 || updateDTO.getType() > 3)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "帖子类型无效，有效范围：0-3");
        }

        // 6. 更新帖子
        if (updateDTO.getForumId() != null) {
            post.setForumId(updateDTO.getForumId());
        }
        if (title != null) {
            post.setTitle(title);
        }
        if (content != null) {
            post.setContent(content);
            post.setSummary(generateSummary(updateDTO.getSummary(), content));
        }
        if (updateDTO.getType() != null) {
            post.setType(updateDTO.getType());
        }
        if (updateDTO.getCoverImage() != null) {
            post.setCoverImage(updateDTO.getCoverImage());
        }
        if (updateDTO.getAllowComment() != null) {
            post.setAllowComment(updateDTO.getAllowComment() ? 1 : 0);
        }

        postMapper.updateById(post);

        // 7. 更新标签
        if (updateDTO.getTagIds() != null) {
            postTagMapper.deleteByPostId(updateDTO.getId());
            if (!updateDTO.getTagIds().isEmpty()) {
                savePostTags(updateDTO.getId(), updateDTO.getTagIds());
            }
        }

        // 8. 更新附件
        if (updateDTO.getAttachments() != null) {
            postAttachmentMapper.deleteByPostId(updateDTO.getId());
            if (!updateDTO.getAttachments().isEmpty()) {
                savePostAttachments(updateDTO.getId(), updateDTO.getAttachments());
            }
        }

        log.info("帖子编辑成功, postId: {}", updateDTO.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePost(Long id, Long userId, boolean isAdmin) {
        log.info("删除帖子, postId: {}, userId: {}, isAdmin: {}", id, userId, isAdmin);

        // 1. 查询帖子
        Post post = postMapper.selectById(id);
        if (post == null || post.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        // 2. 权限校验（管理员或帖子作者可以删除）
        if (!isAdmin && !post.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.POST_NO_PERMISSION);
        }

        // 3. 逻辑删除
        post.setDeleteFlag(1);
        post.setStatus(3); // 已删除状态
        postMapper.updateById(post);

        // 4. 删除标签关联
        postTagMapper.deleteByPostId(id);

        // 5. 删除附件关联
        postAttachmentMapper.deleteByPostId(id);

        // 6. 清理Redis中的点赞、收藏等交互数据
        cleanupPostInteractionData(id);

        // 7. 更新用户帖子数缓存
        // 【修复】添加更健壮的计数同步逻辑
        String countKey = REDIS_KEY_POST_COUNT + post.getUserId();
        String count = redisTemplate.opsForValue().get(countKey);
        if (count != null) {
            // 缓存存在，直接递减
            long currentCount = Long.parseLong(count);
            if (currentCount > 0) {
                redisTemplate.opsForValue().decrement(countKey);
            }
        } else {
            // 【修复】缓存不存在时，从数据库重新获取并设置缓存
            // 这确保了即使缓存过期，计数也能正确同步
            int actualCount = postMapper.countByUserId(post.getUserId());
            redisTemplate.opsForValue().set(countKey, String.valueOf(actualCount), Duration.ofHours(24));
            log.info("Redis缓存不存在，从数据库重新同步帖子计数, userId: {}, count: {}", post.getUserId(), actualCount);
        }

        log.info("帖子删除成功, postId: {}", id);
        return true;
    }
    
    /**
     * 清理帖子的交互数据（点赞、收藏等）
     * 帖子删除时调用，清理Redis中的孤立数据
     *
     * @param postId 帖子ID
     */
    private void cleanupPostInteractionData(Long postId) {
        try {
            // 清理点赞数据
            String likeKey = REDIS_KEY_POST_LIKE + postId;
            redisTemplate.delete(likeKey);
            
            // 清理收藏数据（需要遍历所有用户的收藏集合）
            // 由于收藏是按用户存储的，这里只记录日志
            // 实际项目中可以通过定时任务清理，或者在用户查询收藏列表时过滤已删除的帖子
            log.debug("帖子删除，需要清理收藏数据, postId: {}", postId);
            
            // 清理浏览量数据（可选，浏览量数据可以保留用于统计分析）
            String viewKey = REDIS_KEY_POST_VIEW + postId;
            // redisTemplate.delete(viewKey); // 浏览量数据可以保留
            
            log.info("清理帖子交互数据完成, postId: {}", postId);
        } catch (Exception e) {
            log.warn("清理帖子交互数据失败, postId: {}", postId, e);
            // 清理失败不影响帖子删除的主流程
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setTop(Long id, Integer isTop, Long operatorId, boolean isAdmin) {
        log.info("置顶帖子, postId: {}, isTop: {}, operatorId: {}, isAdmin: {}", id, isTop, operatorId, isAdmin);

        // 【安全修复】Service层权限验证：只有管理员才能执行置顶操作
        // 防止Controller层权限绕过或Header伪造攻击
        if (!isAdmin) {
            log.warn("置顶操作权限校验失败: 非管理员尝试置顶帖子, operatorId: {}, postId: {}", operatorId, id);
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限执行置顶操作，需要管理员权限");
        }

        // 1. 查询帖子
        Post post = postMapper.selectById(id);
        if (post == null || post.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        // 2. 更新置顶状态（SQL中已经处理了top_time的设置）
        int rows = postMapper.updateTopStatus(id, isTop);

        log.info("帖子置顶状态更新成功, postId: {}, isTop: {}", id, isTop);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setEssence(Long id, Integer isEssence, Long operatorId, boolean isAdmin) {
        log.info("加精帖子, postId: {}, isEssence: {}, operatorId: {}, isAdmin: {}", id, isEssence, operatorId, isAdmin);

        // 【安全修复】Service层权限验证：只有管理员才能执行加精操作
        // 防止Controller层权限绕过或Header伪造攻击
        if (!isAdmin) {
            log.warn("加精操作权限校验失败: 非管理员尝试加精帖子, operatorId: {}, postId: {}", operatorId, id);
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限执行加精操作，需要管理员权限");
        }

        // 1. 查询帖子
        Post post = postMapper.selectById(id);
        if (post == null || post.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        // 2. 更新精华状态（SQL中已经处理了essence_time的设置）
        int rows = postMapper.updateEssenceStatus(id, isEssence);

        log.info("帖子精华状态更新成功, postId: {}, isEssence: {}", id, isEssence);
        return rows > 0;
    }

    @Override
    public List<PostListVO> getHotPosts(Integer limit, Long currentUserId) {
        log.info("获取热门帖子, limit: {}", limit);

        // 先从缓存获取
        String cachedKey = REDIS_KEY_HOT_POSTS;
        // 这里简化处理，实际可以从缓存读取

        // 从数据库查询
        List<PostListVO> hotPosts = postMapper.selectHotPosts(limit);

        // 处理每条帖子
        for (PostListVO post : hotPosts) {
            processPost(post, currentUserId);
            // 计算热度分数
            post.setHotScore(calculateHotScore(post));
        }

        // 按热度排序
        hotPosts.sort((a, b) -> b.getHotScore().compareTo(a.getHotScore()));

        return hotPosts;
    }

    @Override
    public PageResult<PostListVO> searchPosts(String keyword, PostQueryDTO queryDTO, Long currentUserId) {
        log.info("搜索帖子, keyword: {}", keyword);

        if (StrUtil.isBlank(keyword)) {
            return getPostList(queryDTO, currentUserId);
        }

        // 构建分页参数
        Page<PostListVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        // 搜索帖子
        IPage<PostListVO> postPage = postMapper.searchPosts(page, keyword, queryDTO);

        // 处理每条帖子
        for (PostListVO post : postPage.getRecords()) {
            processPost(post, currentUserId);
        }

        return PageResult.of(postPage);
    }

    @Override
    public void incrementViewCount(Long id) {
        // 使用Redis计数，避免频繁更新数据库
        try {
            String viewKey = REDIS_KEY_POST_VIEW + id;
            Long newCount = redisTemplate.opsForValue().increment(viewKey);
            
            // 设置缓存过期时间（如果key是新建的）
            if (newCount != null && newCount == 1) {
                redisTemplate.expire(viewKey, Duration.ofHours(24));
            }
            
            // 每100次浏览同步一次到数据库，减少数据库压力
            // 使用取模运算判断是否需要同步
            if (newCount != null && newCount % 100 == 0) {
                // 异步更新数据库，避免阻塞主线程
                try {
                    postMapper.incrementViewCount(id);
                    log.debug("同步帖子浏览量到数据库, postId: {}, viewCount: {}", id, newCount);
                } catch (Exception e) {
                    log.warn("同步浏览量到数据库失败, postId: {}", id, e);
                }
            }
        } catch (Exception e) {
            log.warn("Redis浏览量计数失败, postId: {}", id, e);
        }
    }

    /**
     * 【修复】增加浏览量（带防刷机制）
     * 同一用户/IP在指定时间窗口内（默认24小时）只计一次浏览
     *
     * 防刷策略：
     * 1. 已登录用户：使用用户ID作为唯一标识
     * 2. 未登录用户：使用IP地址作为唯一标识
     * 3. 使用Redis Set记录已浏览的用户/IP，设置过期时间
     *
     * @param id 帖子ID
     * @param userId 用户ID（可为null，未登录用户使用IP标识）
     * @param ipAddress 用户IP地址（用于未登录用户的防刷）
     * @return 是否成功计入浏览量（true-新浏览，false-重复浏览被过滤）
     */
    @Override
    public boolean incrementViewCountWithAntiSpam(Long id, Long userId, String ipAddress) {
        try {
            // 构建浏览记录的key
            String viewedKey;
            String identifier;

            if (userId != null) {
                // 已登录用户：使用用户ID作为标识
                viewedKey = REDIS_KEY_POST_VIEWED_USER + id;
                identifier = userId.toString();
            } else if (ipAddress != null && !ipAddress.isEmpty()) {
                // 未登录用户：使用IP地址作为标识
                viewedKey = REDIS_KEY_POST_VIEWED_IP + id;
                identifier = ipAddress;
            } else {
                // 既没有用户ID也没有IP，无法防刷，直接计数
                log.warn("无法获取用户标识，跳过防刷检查, postId: {}", id);
                incrementViewCount(id);
                return true;
            }

            // 检查是否已在防刷时间窗口内浏览过
            Boolean hasViewed = redisTemplate.opsForSet().isMember(viewedKey, identifier);
            if (Boolean.TRUE.equals(hasViewed)) {
                // 已浏览过，不计入新浏览
                log.debug("重复浏览被过滤, postId: {}, identifier: {}", id, identifier);
                return false;
            }

            // 添加到已浏览记录
            redisTemplate.opsForSet().add(viewedKey, identifier);
            // 设置整个Set的过期时间（如果key是新建的）
            Long size = redisTemplate.opsForSet().size(viewedKey);
            if (size != null && size == 1) {
                redisTemplate.expire(viewedKey, Duration.ofHours(VIEW_ANTI_SPAM_HOURS));
            }

            // 增加浏览量
            incrementViewCount(id);
            log.debug("浏览量增加成功, postId: {}, identifier: {}", id, identifier);
            return true;

        } catch (Exception e) {
            log.warn("浏览量防刷检查失败，降级为直接计数, postId: {}", id, e);
            // 防刷检查失败时，降级为直接计数（保证功能可用）
            incrementViewCount(id);
            return true;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likePost(Long id, Long userId) {
        log.info("点赞帖子, postId: {}, userId: {}", id, userId);

        // 1. 验证帖子是否存在
        Post post = postMapper.selectById(id);
        if (post == null || post.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        // 2. 使用分布式锁防止并发问题
        String lockKey = REDIS_KEY_POST_LIKE_LOCK + id + ":" + userId;
        String lockValue = String.valueOf(System.currentTimeMillis());
        
        try {
            // 尝试获取锁
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, LOCK_EXPIRE_TIME, java.util.concurrent.TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(locked)) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "操作过于频繁，请稍后再试");
            }
            
            // 3. 检查是否已点赞（使用Redis Set）
            String likeKey = REDIS_KEY_POST_LIKE + id;
            Boolean isMember = redisTemplate.opsForSet().isMember(likeKey, userId.toString());

            boolean isLike;
            if (Boolean.TRUE.equals(isMember)) {
                // 已点赞，取消点赞
                redisTemplate.opsForSet().remove(likeKey, userId.toString());
                postMapper.incrementLikeCount(id, -1);
                isLike = false;
            } else {
                // 未点赞，添加点赞
                redisTemplate.opsForSet().add(likeKey, userId.toString());
                postMapper.incrementLikeCount(id, 1);
                isLike = true;
            }

            log.info("帖子{}成功, postId: {}, userId: {}", isLike ? "点赞" : "取消点赞", id, userId);
            return isLike;
        } finally {
            // 【安全修复】使用Lua脚本原子性地验证并释放锁
            // 防止误删其他线程的锁：只有锁的持有者才能释放锁
            try {
                String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                redisTemplate.execute(
                    new org.springframework.data.redis.core.script.DefaultRedisScript<>(luaScript, Long.class),
                    java.util.Collections.singletonList(lockKey),
                    lockValue
                );
            } catch (Exception e) {
                log.warn("释放锁失败: {}", lockKey, e);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean collectPost(Long id, Long userId) {
        log.info("收藏帖子, postId: {}, userId: {}", id, userId);

        // 1. 验证帖子是否存在
        Post post = postMapper.selectById(id);
        if (post == null || post.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        // 2. 使用分布式锁防止并发问题
        String lockKey = REDIS_KEY_POST_COLLECT_LOCK + id + ":" + userId;
        String lockValue = String.valueOf(System.currentTimeMillis());
        
        try {
            // 尝试获取锁
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, LOCK_EXPIRE_TIME, java.util.concurrent.TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(locked)) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "操作过于频繁，请稍后再试");
            }
            
            // 3. 检查是否已收藏（使用Redis Set）
            String collectKey = REDIS_KEY_POST_COLLECT + userId;
            Boolean isMember = redisTemplate.opsForSet().isMember(collectKey, id.toString());

            boolean isCollect;
            if (Boolean.TRUE.equals(isMember)) {
                // 已收藏，取消收藏
                redisTemplate.opsForSet().remove(collectKey, id.toString());
                postMapper.incrementCollectCount(id, -1);
                isCollect = false;
            } else {
                // 未收藏，添加收藏
                redisTemplate.opsForSet().add(collectKey, id.toString());
                postMapper.incrementCollectCount(id, 1);
                isCollect = true;
            }

            log.info("帖子{}成功, postId: {}, userId: {}", isCollect ? "收藏" : "取消收藏", id, userId);
            return isCollect;
        } finally {
            // 【安全修复】使用Lua脚本原子性地验证并释放锁
            // 防止误删其他线程的锁：只有锁的持有者才能释放锁
            try {
                String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                redisTemplate.execute(
                    new org.springframework.data.redis.core.script.DefaultRedisScript<>(luaScript, Long.class),
                    java.util.Collections.singletonList(lockKey),
                    lockValue
                );
            } catch (Exception e) {
                log.warn("释放锁失败: {}", lockKey, e);
            }
        }
    }

    @Override
    public PageResult<PostListVO> getUserPosts(Long userId, PostQueryDTO queryDTO, Long currentUserId) {
        log.info("获取用户帖子列表, userId: {}", userId);

        Page<PostListVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<PostListVO> postPage = postMapper.selectUserPosts(page, userId);

        for (PostListVO post : postPage.getRecords()) {
            processPost(post, currentUserId);
        }

        return PageResult.of(postPage);
    }

    @Override
    public int countByUserId(Long userId) {
        String countKey = REDIS_KEY_POST_COUNT + userId;
        String count = redisTemplate.opsForValue().get(countKey);

        if (count != null) {
            return Integer.parseInt(count);
        }

        int postCount = postMapper.countByUserId(userId);
        redisTemplate.opsForValue().set(countKey, String.valueOf(postCount), Duration.ofHours(1));

        return postCount;
    }

    @Override
    public void updateCommentCount(Long id, Integer delta) {
        postMapper.incrementCommentCount(id, delta);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean movePost(Long id, Long forumId, Long operatorId) {
        log.info("移动帖子, postId: {}, forumId: {}, operatorId: {}", id, forumId, operatorId);

        // 1. 查询帖子
        Post post = postMapper.selectById(id);
        if (post == null || post.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        // 2. 更新版块ID
        post.setForumId(forumId);
        postMapper.updateById(post);

        log.info("帖子移动成功, postId: {}, newForumId: {}", id, forumId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePostStatus(Long id, Integer status, Long operatorId) {
        log.info("更新帖子状态, postId: {}, status: {}, operatorId: {}", id, status, operatorId);

        // 1. 查询帖子
        Post post = postMapper.selectById(id);
        if (post == null || post.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        // 2. 验证状态值 (0-关闭 1-正常)
        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "状态值无效，只能为0（关闭）或1（正常）");
        }

        // 3. 更新状态
        post.setStatus(status);
        postMapper.updateById(post);

        log.info("帖子状态更新成功, postId: {}, status: {}", id, status);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditPost(Long id, Integer status, String reason, Long operatorId) {
        log.info("审核帖子, postId: {}, status: {}, reason: {}, operatorId: {}", id, status, reason, operatorId);

        // 1. 查询帖子
        Post post = postMapper.selectById(id);
        if (post == null || post.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        // 2. 验证状态值 (2-审核通过 3-审核拒绝)
        if (status != 2 && status != 3) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "审核状态无效，只能为2（通过）或3（拒绝）");
        }

        // 3. 更新审核状态
        post.setAuditStatus(status);
        // 如果审核通过，更新帖子状态为已发布
        if (status == 2) {
            post.setStatus(1);
        }
        postMapper.updateById(post);

        log.info("帖子审核完成, postId: {}, status: {}", id, status);
        return true;
    }

    // ==================== 私有方法 ====================

    /**
     * 校验帖子参数
     */
    private void validatePost(PostCreateDTO createDTO) {
        if (createDTO.getForumId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "板块ID不能为空");
        }
        if (StrUtil.isBlank(createDTO.getTitle())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "帖子标题不能为空");
        }
        if (StrUtil.isBlank(createDTO.getContent())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "帖子内容不能为空");
        }
        // 校验帖子类型范围（0-普通帖子 1-精华帖 2-置顶帖 3-公告）
        if (createDTO.getType() != null && (createDTO.getType() < 0 || createDTO.getType() > 3)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "帖子类型无效，有效范围：0-3");
        }
    }

    /**
     * 敏感词过滤与XSS防护
     *
     * 【安全说明】
     * 本方法对用户输入的内容进行两个层面的安全处理：
     *
     * 1. 敏感词过滤：将配置文件中的敏感词替换为指定字符
     * 2. XSS防护：采用更全面的过滤策略，防止多种XSS攻击向量
     *
     * 【XSS防护策略】
     * 采用输入过滤 + 输出转义的双重防护策略：
     * - 输入时：本方法过滤危险字符，减少存储内容中的安全风险
     * - 输出时：前端渲染时需要进行HTML转义，防止存储型XSS攻击
     *
     * 【修复说明】
     * 1. 敏感词列表从配置文件读取，便于运维动态调整
     * 2. XSS过滤增强：
     *    - 处理更多危险字符和攻击向量
     *    - 过滤javascript/vbscript等协议
     *    - 过滤事件处理器属性（onclick、onerror等）
     *    - 过滤危险CSS属性（expression、behavior等）
     *
     * @param content 待过滤的内容
     * @return 过滤后的安全内容
     */
    private String filterSensitiveWords(String content) {
        if (content == null) {
            return null;
        }

        String result = content;

        // 1. 敏感词过滤 - 从配置类获取敏感词列表
        if (postConfig.isSensitiveFilterEnabled()) {
            Set<String> sensitiveWords = postConfig.getSensitiveWords();
            String replacement = postConfig.getSensitiveReplacement();
            for (String word : sensitiveWords) {
                if (result.contains(word)) {
                    result = result.replace(word, replacement);
                }
            }
        }

        // 2. XSS过滤（如果启用）
        if (postConfig.isXssFilterEnabled()) {
            result = filterXSS(result);
        }

        return result;
    }

    /**
     * 专业XSS过滤方法
     * 
     * 对用户输入进行全面的XSS防护，包括：
     * - HTML实体编码
     * - 危险协议过滤
     * - 事件处理器过滤
     * - 危险CSS属性过滤
     *
     * @param content 待过滤的内容
     * @return 过滤后的安全内容
     */
    private String filterXSS(String content) {
        if (content == null) {
            return null;
        }

        String result = content;

        // 2.1 过滤危险协议（防止javascript:、vbscript:、data:等协议注入）
        result = result.replaceAll("(?i)javascript\\s*:", "");
        result = result.replaceAll("(?i)vbscript\\s*:", "");
        result = result.replaceAll("(?i)data\\s*:", "");
        result = result.replaceAll("(?i)expression\\s*\\(", "");

        // 2.2 过滤事件处理器属性（防止onclick、onerror等事件注入）
        result = result.replaceAll("(?i)on\\w+\\s*=", "");
        
        // 2.3 过滤危险的HTML标签
        result = result.replaceAll("(?i)<\\s*script[^>]*>.*?<\\s*/\\s*script\\s*>", "");
        result = result.replaceAll("(?i)<\\s*iframe[^>]*>.*?<\\s*/\\s*iframe\\s*>", "");
        result = result.replaceAll("(?i)<\\s*object[^>]*>.*?<\\s*/\\s*object\\s*>", "");
        result = result.replaceAll("(?i)<\\s*embed[^>]*>.*?<\\s*/\\s*embed\\s*>", "");
        result = result.replaceAll("(?i)<\\s*form[^>]*>", "");
        result = result.replaceAll("(?i)<\\s*input[^>]*>", "");
        result = result.replaceAll("(?i)<\\s*button[^>]*>", "");

        // 2.4 HTML实体编码（基础防护）
        result = result
                .replace("&", "&amp;")       // & 符号最先处理，避免重复编码
                .replace("<", "&lt;")        // < 符号
                .replace(">", "&gt;")        // > 符号
                .replace("\"", "&quot;")     // 双引号
                .replace("'", "&#x27;")      // 单引号
                .replace("/", "&#x2F;");     // 斜杠（防止闭合标签）

        return result;
    }

    /**
     * 生成帖子摘要
     */
    private String generateSummary(String summary, String content) {
        if (StrUtil.isNotBlank(summary)) {
            return summary.length() > 200 ? summary.substring(0, 200) : summary;
        }
        // 从内容中提取摘要
        String plainText = content.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ");
        return plainText.length() > 200 ? plainText.substring(0, 200) + "..." : plainText;
    }

    /**
     * 保存帖子标签关联
     */
    private void savePostTags(Long postId, List<Long> tagIds) {
        List<PostTag> postTags = new ArrayList<>();
        int order = 0;
        for (Long tagId : tagIds) {
            PostTag postTag = new PostTag();
            postTag.setPostId(postId);
            postTag.setTagId(tagId);
            postTag.setSortOrder(order++);
            postTags.add(postTag);
        }
        postTagMapper.batchInsert(postId, postTags);
    }

    /**
     * 保存帖子附件
     */
    private void savePostAttachments(Long postId, List<PostCreateDTO.AttachmentDTO> attachments) {
        List<PostAttachment> postAttachments = new ArrayList<>();
        int order = 0;
        for (PostCreateDTO.AttachmentDTO att : attachments) {
            PostAttachment attachment = new PostAttachment();
            attachment.setPostId(postId);
            attachment.setType(att.getType());
            attachment.setName(att.getName());
            attachment.setUrl(att.getUrl());
            attachment.setThumbnailUrl(att.getThumbnailUrl());
            attachment.setSize(att.getSize());
            attachment.setMimeType(att.getMimeType());
            attachment.setWidth(att.getWidth());
            attachment.setHeight(att.getHeight());
            attachment.setDuration(att.getDuration());
            attachment.setSortOrder(order++);
            attachment.setStatus(1);
            postAttachments.add(attachment);
        }
        postAttachmentMapper.batchInsert(postAttachments);
    }

    /**
     * 处理帖子VO
     */
    private void processPost(PostListVO post, Long currentUserId) {
        // 设置是否为作者
        if (currentUserId != null && post.getUserId() != null) {
            post.setIsAuthor(post.getUserId().equals(currentUserId));
        }

        // 判断是否已点赞
        if (currentUserId != null) {
            post.setIsLiked(isLiked(post.getId(), currentUserId));
            post.setIsCollected(isCollected(post.getId(), currentUserId));
        }

        // 查询图片列表
        List<String> images = postAttachmentMapper.selectImageUrlsByPostId(post.getId(), 3);
        post.setImages(images);
    }

    /**
     * 检查是否已点赞
     * 
     * 优先从Redis缓存获取点赞状态，缓存不存在时不返回默认值
     * 因为帖子服务和交互服务是独立微服务，这里仅依赖Redis缓存
     * 实际点赞状态由forum-interaction服务维护并同步到Redis
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否已点赞，缓存不存在时返回false（保守策略）
     */
    private boolean isLiked(Long postId, Long userId) {
        if (postId == null || userId == null) {
            return false;
        }
        try {
            String likeKey = REDIS_KEY_POST_LIKE + postId;
            Boolean isMember = redisTemplate.opsForSet().isMember(likeKey, userId.toString());
            if (Boolean.TRUE.equals(isMember)) {
                return true;
            }
            // 缓存中不存在，可能是：
            // 1. 用户确实没有点赞
            // 2. 缓存过期或尚未初始化
            // 由于是独立微服务架构，这里采用保守策略返回false
            // 实际应用中可以考虑通过Feign调用interaction服务获取真实状态
            return false;
        } catch (Exception e) {
            log.warn("检查点赞状态失败, postId: {}, userId: {}", postId, userId, e);
            return false;
        }
    }

    /**
     * 检查是否已收藏
     * 
     * 优先从Redis缓存获取收藏状态，缓存不存在时不返回默认值
     * 因为帖子服务和交互服务是独立微服务，这里仅依赖Redis缓存
     * 实际收藏状态由forum-interaction服务维护并同步到Redis
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否已收藏，缓存不存在时返回false（保守策略）
     */
    private boolean isCollected(Long postId, Long userId) {
        if (postId == null || userId == null) {
            return false;
        }
        try {
            String collectKey = REDIS_KEY_POST_COLLECT + userId;
            Boolean isMember = redisTemplate.opsForSet().isMember(collectKey, postId.toString());
            if (Boolean.TRUE.equals(isMember)) {
                return true;
            }
            // 缓存中不存在，可能是：
            // 1. 用户确实没有收藏
            // 2. 缓存过期或尚未初始化
            // 由于是独立微服务架构，这里采用保守策略返回false
            // 实际应用中可以考虑通过Feign调用interaction服务获取真实状态
            return false;
        } catch (Exception e) {
            log.warn("检查收藏状态失败, postId: {}, userId: {}", postId, userId, e);
            return false;
        }
    }

    /**
     * 计算热度分数
     */
    private Integer calculateHotScore(PostListVO post) {
        // 简单的热度计算公式：浏览量 + 点赞数*5 + 评论数*10 + 收藏数*8
        int score = (post.getViewCount() != null ? post.getViewCount() : 0)
                + (post.getLikeCount() != null ? post.getLikeCount() * 5 : 0)
                + (post.getCommentCount() != null ? post.getCommentCount() * 10 : 0)
                + (post.getCollectCount() != null ? post.getCollectCount() * 8 : 0);
        return score;
    }
}
