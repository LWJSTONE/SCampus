package com.campus.forum.controller;

import com.campus.forum.config.FileConfigProperties;
import com.campus.forum.constant.ResultCode;
import com.campus.forum.dto.FileQueryDTO;
import com.campus.forum.entity.PageResult;
import com.campus.forum.entity.Result;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.service.FileService;
import com.campus.forum.vo.FileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 文件控制器
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传、下载、查询等接口")
public class FileController {

    private final FileService fileService;
    private final FileConfigProperties fileConfig;

    /**
     * 上传文件
     * 需要用户登录
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传单个文件（需要登录）")
    public Result<FileVO> upload(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务类型") @RequestParam(value = "bizType", required = false) String bizType,
            @Parameter(description = "业务ID") @RequestParam(value = "bizId", required = false) Long bizId,
            HttpServletRequest request) {
        Long uploaderId = getUserIdFromRequest(request);
        // 安全检查：未登录用户不能上传文件
        if (uploaderId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录后再上传文件");
        }
        // 参数校验：文件大小限制
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件不能为空");
        }
        if (file.getSize() > fileConfig.getMaxSize()) {
            log.warn("文件大小超过限制，文件名: {}, 大小: {} bytes, 限制: {} bytes", 
                    file.getOriginalFilename(), file.getSize(), fileConfig.getMaxSize());
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED, 
                    "文件大小超过限制，最大允许 " + formatFileSize(fileConfig.getMaxSize()));
        }
        log.info("用户 {} 开始上传文件: {}, 大小: {} bytes", uploaderId, file.getOriginalFilename(), file.getSize());
        FileVO result = fileService.upload(file, uploaderId, bizType, bizId);
        log.info("文件上传成功，文件ID: {}, 文件名: {}", result.getId(), result.getOriginalName());
        return Result.success(result);
    }

    /**
     * 上传图片
     * 需要用户登录
     */
    @PostMapping("/upload/image")
    @Operation(summary = "上传图片", description = "上传图片文件，仅支持图片类型（需要登录）")
    public Result<FileVO> uploadImage(
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务类型") @RequestParam(value = "bizType", required = false) String bizType,
            @Parameter(description = "业务ID") @RequestParam(value = "bizId", required = false) Long bizId,
            HttpServletRequest request) {
        Long uploaderId = getUserIdFromRequest(request);
        // 安全检查：未登录用户不能上传文件
        if (uploaderId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录后再上传文件");
        }
        // 参数校验：文件大小限制
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "图片文件不能为空");
        }
        if (file.getSize() > fileConfig.getMaxSize()) {
            log.warn("图片大小超过限制，文件名: {}, 大小: {} bytes, 限制: {} bytes", 
                    file.getOriginalFilename(), file.getSize(), fileConfig.getMaxSize());
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED, 
                    "图片大小超过限制，最大允许 " + formatFileSize(fileConfig.getMaxSize()));
        }
        log.info("用户 {} 开始上传图片: {}, 大小: {} bytes", uploaderId, file.getOriginalFilename(), file.getSize());
        FileVO result = fileService.uploadImage(file, uploaderId, bizType, bizId);
        log.info("图片上传成功，文件ID: {}, 文件名: {}", result.getId(), result.getOriginalName());
        return Result.success(result);
    }

    /**
     * 批量上传文件
     * 需要用户登录
     */
    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传文件", description = "批量上传多个文件（需要登录）")
    public Result<List<FileVO>> uploadBatch(
            @Parameter(description = "文件列表") @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "业务类型") @RequestParam(value = "bizType", required = false) String bizType,
            @Parameter(description = "业务ID") @RequestParam(value = "bizId", required = false) Long bizId,
            HttpServletRequest request) {
        Long uploaderId = getUserIdFromRequest(request);
        // 安全检查：未登录用户不能上传文件
        if (uploaderId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录后再上传文件");
        }
        // 参数校验：批量上传数量限制
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件列表不能为空");
        }
        int maxBatchCount = fileConfig.getBatch().getMaxCount();
        if (files.size() > maxBatchCount) {
            log.warn("批量上传数量超过限制，请求数量: {}, 限制: {}", files.size(), maxBatchCount);
            throw new BusinessException(ResultCode.PARAM_ERROR, 
                    "批量上传数量超过限制，最多允许上传 " + maxBatchCount + " 个文件");
        }
        // 校验每个文件大小
        for (MultipartFile file : files) {
            if (file.getSize() > fileConfig.getMaxSize()) {
                log.warn("批量上传中存在超大文件，文件名: {}, 大小: {} bytes", 
                        file.getOriginalFilename(), file.getSize());
                throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED, 
                        "文件 " + file.getOriginalFilename() + " 大小超过限制");
            }
        }
        log.info("用户 {} 开始批量上传文件，数量: {}", uploaderId, files.size());
        List<FileVO> result = fileService.uploadBatch(files, uploaderId, bizType, bizId);
        log.info("批量上传成功，共 {} 个文件", result.size());
        return Result.success(result);
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取文件信息", description = "根据ID获取文件详细信息")
    public Result<FileVO> getInfo(
            @Parameter(description = "文件ID") @PathVariable Long id) {
        FileVO result = fileService.getFileInfo(id);
        return Result.success(result);
    }

    /**
     * 下载文件
     * 修复：添加登录验证，确保只有已登录用户才能下载文件
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "下载文件", description = "根据ID下载文件（需要登录）")
    public ResponseEntity<byte[]> download(
            @Parameter(description = "文件ID") @PathVariable Long id,
            HttpServletResponse response,
            HttpServletRequest request) {
        // 修复：验证用户是否已登录
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录后再下载文件");
        }
        log.info("用户 {} 开始下载文件，文件ID: {}", userId, id);
        ResponseEntity<byte[]> result = fileService.download(id, response);
        log.info("文件下载完成，文件ID: {}", id);
        return result;
    }

    /**
     * 预览文件（图片等）
     * 修复：添加登录验证，确保只有已登录用户才能预览文件
     */
    @GetMapping("/{id}/preview")
    @Operation(summary = "预览文件", description = "预览文件（如图片）（需要登录）")
    public ResponseEntity<byte[]> preview(
            @Parameter(description = "文件ID") @PathVariable Long id,
            HttpServletRequest request) {
        // 修复：验证用户是否已登录
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录后再预览文件");
        }
        log.info("用户 {} 预览文件，文件ID: {}", userId, id);
        return fileService.preview(id);
    }

    /**
     * 删除文件
     * 修复：添加权限验证，只有文件所有者或管理员可以删除
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除文件", description = "根据ID删除文件（需要所有者或管理员权限）")
    public Result<Void> delete(
            @Parameter(description = "文件ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        boolean isAdmin = isAdmin(request);
        
        // 权限验证：未登录用户不能删除文件
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录后再删除文件");
        }
        
        // 权限验证：只有文件所有者或管理员可以删除
        FileVO fileInfo = fileService.getFileInfo(id);
        if (fileInfo == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND, "文件不存在");
        }
        if (!isAdmin && !userId.equals(fileInfo.getUploaderId())) {
            log.warn("用户 {} 尝试删除非自己的文件，文件ID: {}, 上传者ID: {}", 
                    userId, id, fileInfo.getUploaderId());
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此文件，只有文件所有者或管理员可以删除");
        }
        
        log.info("用户 {} (管理员: {}) 删除文件，文件ID: {}", userId, isAdmin, id);
        fileService.deleteFile(id, userId);
        log.info("文件删除成功，文件ID: {}", id);
        return Result.success();
    }

    /**
     * 批量删除文件
     * 修复：添加权限验证，只有文件所有者或管理员可以删除
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除文件", description = "批量删除多个文件（需要所有者或管理员权限）")
    public Result<Void> deleteBatch(
            @Parameter(description = "文件ID列表") @RequestBody List<Long> ids,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        boolean isAdmin = isAdmin(request);
        
        // 权限验证：未登录用户不能删除文件
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录后再删除文件");
        }
        
        // 参数校验
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件ID列表不能为空");
        }
        
        // 权限验证：验证每个文件的所有权（管理员可跳过）
        if (!isAdmin) {
            for (Long fileId : ids) {
                FileVO fileInfo = fileService.getFileInfo(fileId);
                if (fileInfo != null && !userId.equals(fileInfo.getUploaderId())) {
                    log.warn("用户 {} 尝试批量删除非自己的文件，文件ID: {}, 上传者ID: {}", 
                            userId, fileId, fileInfo.getUploaderId());
                    throw new BusinessException(ResultCode.FORBIDDEN, 
                            "无权删除文件ID为 " + fileId + " 的文件，只能删除自己上传的文件");
                }
            }
        }
        
        log.info("用户 {} (管理员: {}) 批量删除文件，文件ID列表: {}", userId, isAdmin, ids);
        fileService.deleteBatch(ids, userId);
        log.info("批量删除成功，共删除 {} 个文件", ids.size());
        return Result.success();
    }

    /**
     * 获取文件列表
     * 修复：添加权限控制，非管理员只能查询自己的文件
     */
    @GetMapping("/list")
    @Operation(summary = "获取文件列表", description = "分页获取文件列表（非管理员只能查看自己的文件）")
    public Result<PageResult<FileVO>> list(FileQueryDTO queryDTO, HttpServletRequest request) {
        // 修复：非管理员只能查询自己的文件
        if (!isAdmin(request)) {
            Long userId = getUserIdFromRequest(request);
            if (userId != null) {
                queryDTO.setUploaderId(userId);
            }
        }
        PageResult<FileVO> result = fileService.getFileList(queryDTO);
        return Result.success(result);
    }

    /**
     * 根据业务类型和业务ID获取文件列表
     */
    @GetMapping("/biz")
    @Operation(summary = "根据业务获取文件", description = "根据业务类型和业务ID获取关联文件")
    public Result<List<FileVO>> getByBiz(
            @Parameter(description = "业务类型") @RequestParam String bizType,
            @Parameter(description = "业务ID") @RequestParam Long bizId) {
        List<FileVO> result = fileService.getByBizType(bizType, bizId);
        return Result.success(result);
    }

    /**
     * 更新文件状态
     * 需要管理员权限
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新文件状态", description = "启用或禁用文件（需要管理员权限）")
    public Result<Void> updateStatus(
            @Parameter(description = "文件ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam Integer status,
            HttpServletRequest request) {
        // 权限验证：只有管理员可以更新文件状态
        if (!isAdmin(request)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限执行此操作，需要管理员权限");
        }
        log.info("管理员更新文件状态，文件ID: {}, 新状态: {}", id, status);
        fileService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 从请求中获取用户ID
     * 修复：同时检查Header中的X-User-Id（网关传递）和request.getAttribute
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // 优先从Header获取（网关传递）
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                log.warn("解析X-User-Id Header失败: {}", userIdHeader);
            }
        }
        
        // 回退到request.getAttribute获取
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            try {
                return Long.parseLong(userId.toString());
            } catch (NumberFormatException e) {
                log.warn("解析userId Attribute失败: {}", userId);
            }
        }
        
        return null;
    }

    /**
     * 检查用户是否为管理员
     * 修复：同时检查Header中的X-User-Role（网关传递）和request.getAttribute
     */
    private boolean isAdmin(HttpServletRequest request) {
        // 优先从Header获取（网关传递）
        String roleHeader = request.getHeader("X-User-Role");
        if (roleHeader != null && !roleHeader.isEmpty()) {
            return "ADMIN".equalsIgnoreCase(roleHeader) || "ROLE_ADMIN".equalsIgnoreCase(roleHeader);
        }
        
        // 回退到request.getAttribute获取
        Object userRole = request.getAttribute("userRole");
        if (userRole != null) {
            String role = userRole.toString();
            return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
        }
        
        return false;
    }

    /**
     * 格式化文件大小
     * 将字节数转换为易读的格式（如：10.5 MB）
     *
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小字符串
     */
    private String formatFileSize(Long size) {
        if (size == null || size == 0) {
            return "0 B";
        }
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double fileSize = size.doubleValue();
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }
}
