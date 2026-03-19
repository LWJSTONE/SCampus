package com.campus.forum.api.file;

import com.campus.forum.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 文件服务Feign客户端接口
 *
 * @author campus
 */
@FeignClient(name = "forum-file", fallback = FileApiFallback.class)
public interface FileApi {

    /**
     * 获取文件URL
     *
     * @param fileId 文件ID
     * @return 文件URL
     */
    @GetMapping("/api/file/{fileId}/url")
    Result<String> getFileUrl(@PathVariable("fileId") Long fileId);

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @return 操作结果
     */
    @DeleteMapping("/api/file/{fileId}")
    Result<Boolean> deleteFile(@PathVariable("fileId") Long fileId);
}
