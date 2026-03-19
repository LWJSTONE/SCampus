package com.campus.forum.api.notify;

import com.campus.forum.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 通知服务Feign客户端接口
 *
 * @author campus
 */
@FeignClient(name = "forum-notify", fallback = NotifyApiFallback.class)
public interface NotifyApi {

    /**
     * 发送通知
     *
     * @param userId  用户ID
     * @param title   通知标题
     * @param content 通知内容
     * @return 操作结果
     */
    @PostMapping("/api/notify/send")
    Result<Boolean> sendNotice(@RequestParam("userId") Long userId,
                          @RequestParam("title") String title,
                          @RequestParam("content") String content);

    /**
     * 批量发送通知
     *
     * @param userIds 用户ID列表
     * @param title   通知标题
     * @param content 通知内容
     * @return 操作结果
     */
    @PostMapping("/api/notify/send/batch")
    Result<Boolean> sendBatchNotice(@RequestParam("userIds") List<Long> userIds,
                               @RequestParam("title") String title,
                               @RequestParam("content") String content);
}
