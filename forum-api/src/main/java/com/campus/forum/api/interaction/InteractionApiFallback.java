package com.campus.forum.api.interaction;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Interaction Service Feign Client Fallback
 * 交互服务Feign客户端回退处理类
 *
 * @author campus
 */
@Slf4j
@Component
public class InteractionApiFallback implements InteractionApi {

    @Override
    public Result<IPage<?>> getCollectList(Integer current, Integer size, Long userId, String serviceKey) {
        log.error("调用交互服务失败，获取用户收藏列表，用户ID: {}, 页码: {}, 每页大小: {}", userId, current, size);
        return Result.fail("交互服务不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> checkCollected(Long postId, Long userId, String serviceKey) {
        log.error("调用交互服务失败，检查收藏状态，帖子ID: {}, 用户ID: {}", postId, userId);
        return Result.fail("交互服务不可用，请稍后重试");
    }
}
