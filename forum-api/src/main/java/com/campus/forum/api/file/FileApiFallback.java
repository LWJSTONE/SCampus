package com.campus.forum.api.file;

import com.campus.forum.common.core.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文件服务Feign客户端回退处理类
 *
 * @author campus
 */
@Slf4j
@Component
public class FileApiFallback implements FileApi {

    @Override
    public R<String> getFileUrl(Long fileId) {
        log.error("调用文件服务失败，获取文件URL，文件ID: {}", fileId);
        return R.fail("文件服务不可用，请稍后重试");
    }

    @Override
    public R<Boolean> deleteFile(Long fileId) {
        log.error("调用文件服务失败，删除文件，文件ID: {}", fileId);
        return R.fail("文件服务不可用，请稍后重试");
    }
}
