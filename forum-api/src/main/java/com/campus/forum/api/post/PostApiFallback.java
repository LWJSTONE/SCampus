package com.campus.forum.api.post;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 帖子服务Feign客户端回退处理类
 *
 * @author campus
 */
@Slf4j
@Component
public class PostApiFallback implements PostApi {

    @Override
    public Result<PostDTO> getPostById(Long id, String serviceKey) {
        log.error("调用帖子服务失败，获取帖子信息，帖子ID: {}", id);
        return Result.fail("帖子服务不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> deletePost(Long id, String serviceKey) {
        log.error("调用帖子服务失败，删除帖子，帖子ID: {}", id);
        return Result.fail("帖子服务不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> updatePostStats(Long id, String field, int delta, String serviceKey) {
        log.error("调用帖子服务失败，更新帖子统计，帖子ID: {}, 字段: {}, 增量: {}", id, field, delta);
        return Result.fail("帖子服务不可用，请稍后重试");
    }

    @Override
    public Result<Page<PostDTO>> getPostsByUserId(Long userId, int page, int size, String serviceKey) {
        log.error("调用帖子服务失败，获取用户帖子列表，用户ID: {}, 页码: {}, 每页大小: {}", userId, page, size);
        return Result.fail("帖子服务不可用，请稍后重试");
    }
}
