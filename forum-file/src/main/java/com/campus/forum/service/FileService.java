package com.campus.forum.service;

import com.campus.forum.dto.FileQueryDTO;
import com.campus.forum.entity.File;
import com.campus.forum.entity.PageResult;
import com.campus.forum.vo.FileVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 文件服务接口
 *
 * @author campus
 * @since 2024-01-01
 */
public interface FileService {

    /**
     * 上传文件
     *
     * @param file       文件
     * @param uploaderId 上传者ID
     * @param bizType    业务类型
     * @param bizId      业务ID
     * @return 文件信息
     */
    FileVO upload(MultipartFile file, Long uploaderId, String bizType, Long bizId);

    /**
     * 上传图片
     *
     * @param file       图片文件
     * @param uploaderId 上传者ID
     * @param bizType    业务类型
     * @param bizId      业务ID
     * @return 文件信息
     */
    FileVO uploadImage(MultipartFile file, Long uploaderId, String bizType, Long bizId);

    /**
     * 批量上传文件
     *
     * @param files      文件列表
     * @param uploaderId 上传者ID
     * @param bizType    业务类型
     * @param bizId      业务ID
     * @return 文件信息列表
     */
    List<FileVO> uploadBatch(List<MultipartFile> files, Long uploaderId, String bizType, Long bizId);

    /**
     * 获取文件信息
     *
     * @param id 文件ID
     * @return 文件信息
     */
    FileVO getFileInfo(Long id);

    /**
     * 下载文件
     *
     * @param id       文件ID
     * @param response 响应
     * @return 文件内容
     */
    ResponseEntity<byte[]> download(Long id, HttpServletResponse response);

    /**
     * 预览文件
     *
     * @param id 文件ID
     * @return 文件内容
     */
    ResponseEntity<byte[]> preview(Long id);

    /**
     * 删除文件
     *
     * @param id       文件ID
     * @param userId   操作用户ID
     */
    void deleteFile(Long id, Long userId);

    /**
     * 批量删除文件
     *
     * @param ids    文件ID列表
     * @param userId 操作用户ID
     */
    void deleteBatch(List<Long> ids, Long userId);

    /**
     * 获取文件列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<FileVO> getFileList(FileQueryDTO queryDTO);

    /**
     * 根据业务类型和业务ID获取文件列表
     *
     * @param bizType 业务类型
     * @param bizId   业务ID
     * @return 文件列表
     */
    List<FileVO> getByBizType(String bizType, Long bizId);

    /**
     * 更新文件状态
     *
     * @param id     文件ID
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 根据MD5检查文件是否存在（秒传）
     *
     * @param fileMd5 文件MD5值
     * @return 文件信息，不存在返回null
     */
    FileVO checkByMd5(String fileMd5);
}
