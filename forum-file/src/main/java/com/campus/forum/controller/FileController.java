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

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传单个文件")
    public Result<FileVO> upload(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务类型") @RequestParam(value = "bizType", required = false) String bizType,
            @Parameter(description = "业务ID") @RequestParam(value = "bizId", required = false) Long bizId,
            HttpServletRequest request) {
        Long uploaderId = getUserIdFromRequest(request);
        FileVO result = fileService.upload(file, uploaderId, bizType, bizId);
        return Result.success(result);
    }

    /**
     * 上传图片
     */
    @PostMapping("/upload/image")
    @Operation(summary = "上传图片", description = "上传图片文件，仅支持图片类型")
    public Result<FileVO> uploadImage(
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务类型") @RequestParam(value = "bizType", required = false) String bizType,
            @Parameter(description = "业务ID") @RequestParam(value = "bizId", required = false) Long bizId,
            HttpServletRequest request) {
        Long uploaderId = getUserIdFromRequest(request);
        FileVO result = fileService.uploadImage(file, uploaderId, bizType, bizId);
        return Result.success(result);
    }

    /**
     * 批量上传文件
     */
    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传文件", description = "批量上传多个文件")
    public Result<List<FileVO>> uploadBatch(
            @Parameter(description = "文件列表") @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "业务类型") @RequestParam(value = "bizType", required = false) String bizType,
            @Parameter(description = "业务ID") @RequestParam(value = "bizId", required = false) Long bizId,
            HttpServletRequest request) {
        Long uploaderId = getUserIdFromRequest(request);
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
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "下载文件", description = "根据ID下载文件")
    public ResponseEntity<byte[]> download(
            @Parameter(description = "文件ID") @PathVariable Long id,
            HttpServletResponse response) {
        return fileService.download(id, response);
    }

    /**
     * 预览文件（图片等）
     */
    @GetMapping("/{id}/preview")
    @Operation(summary = "预览文件", description = "预览文件（如图片）")
    public ResponseEntity<byte[]> preview(
            @Parameter(description = "文件ID") @PathVariable Long id) {
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
     */
    @GetMapping("/list")
    @Operation(summary = "获取文件列表", description = "分页获取文件列表")
    public Result<PageResult<FileVO>> list(FileQueryDTO queryDTO) {
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
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新文件状态", description = "启用或禁用文件")
    public Result<Void> updateStatus(
            @Parameter(description = "文件ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam Integer status) {
        fileService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? Long.parseLong(userId.toString()) : null;
    }
}
