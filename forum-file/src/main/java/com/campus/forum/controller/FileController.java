package com.campus.forum.controller;

import com.campus.forum.dto.FileQueryDTO;
import com.campus.forum.entity.File;
import com.campus.forum.entity.PageResult;
import com.campus.forum.entity.Result;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
            throw new RuntimeException("请先登录后再上传文件");
        }
        FileVO result = fileService.upload(file, uploaderId, bizType, bizId);
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
            throw new RuntimeException("请先登录后再上传文件");
        }
        FileVO result = fileService.uploadImage(file, uploaderId, bizType, bizId);
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
            throw new RuntimeException("请先登录后再上传文件");
        }
        List<FileVO> result = fileService.uploadBatch(files, uploaderId, bizType, bizId);
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
            throw new RuntimeException("请先登录后再下载文件");
        }
        return fileService.download(id, response);
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
            throw new RuntimeException("请先登录后再预览文件");
        }
        return fileService.preview(id);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除文件", description = "根据ID删除文件")
    public Result<Void> delete(
            @Parameter(description = "文件ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        fileService.deleteFile(id, userId);
        return Result.success();
    }

    /**
     * 批量删除文件
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除文件", description = "批量删除多个文件")
    public Result<Void> deleteBatch(
            @Parameter(description = "文件ID列表") @RequestBody List<Long> ids,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        fileService.deleteBatch(ids, userId);
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
            throw new RuntimeException("无权限执行此操作，需要管理员权限");
        }
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
}
