package com.campus.forum.api.file;

import com.campus.forum.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * File Service Feign Client
 *
 * @author campus
 */
@FeignClient(name = "forum-file", contextId = "fileApi", url = "${feign.file.url:http://localhost:9010}", fallback = FileApiFallback.class)
public interface FileApi {

    /**
     * Get file URL
     *
     * @param fileId File ID
     * @return File URL
     */
    @GetMapping("/api/internal/file/{fileId}/url")
    Result<String> getFileUrl(@PathVariable("fileId") Long fileId);

    /**
     * Delete file
     *
     * @param fileId File ID
     * @return Result
     */
    @DeleteMapping("/api/internal/file/{fileId}")
    Result<Boolean> deleteFile(@PathVariable("fileId") Long fileId);
}
