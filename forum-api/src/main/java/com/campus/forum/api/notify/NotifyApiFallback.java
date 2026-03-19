package com.campus.forum.api.notify;

import com.campus.forum.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 通知服务Feign客户端回退处理类
 *
 * @author campus
 */
@Slf4j
@Component
public class NotifyApiFallback implements NotifyApi {

    @Override
    public Result<Boolean> sendNotice(Long userId, String title, String content) {
        log.error("调用通知服务失败，发送通知，用户ID: {}, 标题: {}", userId, title);
        return Result.fail("通知服务不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> sendBatchNotice(List<Long> userIds, String title, String content) {
        log.error("调用通知服务失败，批量发送通知，用户ID数量: {}, 标题: {}", userIds.size(), title);
        return Result.fail("通知服务不可用，请稍后重试");
    }
}
