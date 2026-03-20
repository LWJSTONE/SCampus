package com.campus.forum.api.notify;

import com.campus.forum.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Notify Service Feign Client
 *
 * @author campus
 */
@FeignClient(name = "forum-notify", contextId = "notifyApi", url = "${feign.notify.url:http://localhost:9009}", fallback = NotifyApiFallback.class)
public interface NotifyApi {

    /**
     * Send notification
     *
     * @param userId  User ID
     * @param title   Notification title
     * @param content Notification content
     * @return Result
     */
    @PostMapping("/api/internal/notify/send")
    Result<Boolean> sendNotice(@RequestParam("userId") Long userId,
                               @RequestParam("title") String title,
                               @RequestParam("content") String content);

    /**
     * Batch send notifications
     *
     * @param userIds User ID list
     * @param title   Notification title
     * @param content Notification content
     * @return Result
     */
    @PostMapping("/api/internal/notify/send/batch")
    Result<Boolean> sendBatchNotice(@RequestParam("userIds") List<Long> userIds,
                                    @RequestParam("title") String title,
                                    @RequestParam("content") String content);
}
