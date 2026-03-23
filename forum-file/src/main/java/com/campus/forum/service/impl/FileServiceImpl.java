package com.campus.forum.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.dto.FileQueryDTO;
import com.campus.forum.entity.File;
import com.campus.forum.entity.PageResult;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.FileMapper;
import com.campus.forum.service.FileService;
import com.campus.forum.vo.FileVO;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 文件服务实现类
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {

    private final FileMapper fileMapper;

    // MinIO配置
    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.access-key}")
    private String minioAccessKey;

    @Value("${minio.secret-key}")
    private String minioSecretKey;

    @Value("${minio.bucket-name}")
    private String minioBucketName;

    // 本地存储配置
    @Value("${file.storage.type:LOCAL}")
    private String storageType;

    @Value("${file.storage.local-path:/data/files}")
    private String localStoragePath;

    @Value("${file.storage.url-prefix:http://localhost:9010/files}")
    private String urlPrefix;

    @Value("${file.max-size:52428800}")
    private Long maxFileSize;

    @Value("${file.allowed-types:}")
    private String allowedTypes;

    @Value("${file.image-types:}")
    private String imageTypes;

    private MinioClient minioClient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @PostConstruct
    public void init() {
        if ("MINIO".equalsIgnoreCase(storageType)) {
            try {
                minioClient = MinioClient.builder()
                        .endpoint(minioEndpoint)
                        .credentials(minioAccessKey, minioSecretKey)
                        .build();
                
                // 创建bucket如果不存在
                boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                        .bucket(minioBucketName)
                        .build());
                if (!exists) {
                    minioClient.makeBucket(MakeBucketArgs.builder()
                            .bucket(minioBucketName)
                            .build());
                }
                log.info("MinIO客户端初始化成功，bucket: {}", minioBucketName);
            } catch (Exception e) {
                log.error("MinIO客户端初始化失败: {}", e.getMessage());
            }
        } else {
            // 创建本地存储目录
            try {
                Files.createDirectories(Paths.get(localStoragePath));
                log.info("本地存储目录初始化成功: {}", localStoragePath);
            } catch (IOException e) {
                log.error("创建本地存储目录失败: {}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileVO upload(MultipartFile file, Long uploaderId, String bizType, Long bizId) {
        // 验证文件
        validateFile(file);

        try {
            // 计算文件MD5
            String fileMd5 = DigestUtil.md5Hex(file.getInputStream());
            
            // 检查是否已存在（秒传）
            File existFile = fileMapper.selectByMd5(fileMd5);
            if (existFile != null) {
                // 创建新记录，复用已有文件
                File newFile = createFileRecord(file, existFile.getFilePath(), existFile.getFileUrl(),
                        existFile.getStorageType(), fileMd5, uploaderId, bizType, bizId);
                save(newFile);
                return convertToVO(newFile);
            }

            // 生成文件存储信息
            String originalFilename = file.getOriginalFilename();
            String fileExt = FileUtil.extName(originalFilename);
            String fileName = IdUtil.fastSimpleUUID() + "." + fileExt;
            String datePath = LocalDate.now().format(DATE_FORMATTER);
            String filePath = datePath + "/" + fileName;

            String fileUrl;
            if ("MINIO".equalsIgnoreCase(storageType)) {
                fileUrl = uploadToMinio(file, filePath);
            } else {
                fileUrl = uploadToLocal(file, filePath);
            }

            // 保存文件记录
            File fileEntity = createFileRecord(file, filePath, fileUrl, storageType, fileMd5, uploaderId, bizType, bizId);
            save(fileEntity);

            return convertToVO(fileEntity);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileVO uploadImage(MultipartFile file, Long uploaderId, String bizType, Long bizId) {
        // 验证是否为图片
        String contentType = file.getContentType();
        List<String> imageTypeList = Arrays.asList(imageTypes.split(","));
        if (StrUtil.isBlank(contentType) || !imageTypeList.contains(contentType.trim())) {
            throw new BusinessException("只能上传图片文件");
        }
        return upload(file, uploaderId, bizType, bizId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FileVO> uploadBatch(List<MultipartFile> files, Long uploaderId, String bizType, Long bizId) {
        List<FileVO> result = new ArrayList<>();
        for (MultipartFile file : files) {
            result.add(upload(file, uploaderId, bizType, bizId));
        }
        return result;
    }

    @Override
    public FileVO getFileInfo(Long id) {
        File file = getById(id);
        if (file == null) {
            throw new BusinessException("文件不存在");
        }
        return convertToVO(file);
    }

    @Override
    public ResponseEntity<byte[]> download(Long id, HttpServletResponse response) {
        File file = getById(id);
        if (file == null) {
            throw new BusinessException("文件不存在");
        }
        if (file.getStatus() != null && file.getStatus() == 1) {
            throw new BusinessException("文件已被禁用");
        }

        try {
            byte[] content;
            String contentType = file.getFileType();

            if ("MINIO".equalsIgnoreCase(file.getStorageType())) {
                InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                        .bucket(minioBucketName)
                        .object(file.getFilePath())
                        .build());
                content = IoUtil.readBytes(inputStream);
            } else {
                Path path = Paths.get(localStoragePath, file.getFilePath());
                content = Files.readAllBytes(path);
            }

            // 增加下载次数
            fileMapper.incrementDownloadCount(id);

            String encodedFilename = URLEncoder.encode(file.getOriginalName(), "UTF-8")
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content);
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage());
            throw new BusinessException("文件下载失败");
        }
    }

    @Override
    public ResponseEntity<byte[]> preview(Long id) {
        File file = getById(id);
        if (file == null) {
            throw new BusinessException("文件不存在");
        }
        if (file.getStatus() != null && file.getStatus() == 1) {
            throw new BusinessException("文件已被禁用");
        }

        try {
            byte[] content;
            String contentType = file.getFileType();

            if ("MINIO".equalsIgnoreCase(file.getStorageType())) {
                InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                        .bucket(minioBucketName)
                        .object(file.getFilePath())
                        .build());
                content = IoUtil.readBytes(inputStream);
            } else {
                Path path = Paths.get(localStoragePath, file.getFilePath());
                content = Files.readAllBytes(path);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content);
        } catch (Exception e) {
            log.error("文件预览失败: {}", e.getMessage());
            throw new BusinessException("文件预览失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long id, Long userId) {
        File file = getById(id);
        if (file == null) {
            throw new BusinessException("文件不存在");
        }

        // 检查权限（上传者才能删除）
        if (userId != null && !userId.equals(file.getUploaderId())) {
            throw new BusinessException("无权删除此文件");
        }

        try {
            // 删除物理文件
            if ("MINIO".equalsIgnoreCase(file.getStorageType())) {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(minioBucketName)
                        .object(file.getFilePath())
                        .build());
            } else {
                Path path = Paths.get(localStoragePath, file.getFilePath());
                Files.deleteIfExists(path);
            }

            // 删除数据库记录
            removeById(id);
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage());
            throw new BusinessException("文件删除失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids, Long userId) {
        for (Long id : ids) {
            deleteFile(id, userId);
        }
    }

    @Override
    public PageResult<FileVO> getFileList(FileQueryDTO queryDTO) {
        Page<File> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getFileName()), File::getFileName, queryDTO.getFileName())
                .eq(StrUtil.isNotBlank(queryDTO.getFileType()), File::getFileType, queryDTO.getFileType())
                .eq(StrUtil.isNotBlank(queryDTO.getStorageType()), File::getStorageType, queryDTO.getStorageType())
                .eq(queryDTO.getUploaderId() != null, File::getUploaderId, queryDTO.getUploaderId())
                .eq(StrUtil.isNotBlank(queryDTO.getBizType()), File::getBizType, queryDTO.getBizType())
                .eq(queryDTO.getBizId() != null, File::getBizId, queryDTO.getBizId())
                .eq(queryDTO.getStatus() != null, File::getStatus, queryDTO.getStatus())
                .orderByDesc(File::getCreateTime);

        IPage<File> result = page(page, wrapper);
        
        List<FileVO> voList = new ArrayList<>();
        result.getRecords().forEach(f -> voList.add(convertToVO(f)));

        return new PageResult<>(queryDTO.getCurrent().longValue(), queryDTO.getSize().longValue(), result.getTotal(), voList);
    }

    @Override
    public List<FileVO> getByBizType(String bizType, Long bizId) {
        List<File> files = fileMapper.selectByBiz(bizType, bizId);
        List<FileVO> result = new ArrayList<>();
        files.forEach(f -> result.add(convertToVO(f)));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        File file = new File();
        file.setId(id);
        file.setStatus(status);
        updateById(file);
    }

    @Override
    public FileVO checkByMd5(String fileMd5) {
        File file = fileMapper.selectByMd5(fileMd5);
        return file != null ? convertToVO(file) : null;
    }

    /**
     * 验证文件
     * 增强安全性：同时验证Content-Type和文件头魔数，防止文件伪装攻击
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小超过限制");
        }

        String contentType = file.getContentType();
        if (StrUtil.isNotBlank(allowedTypes)) {
            List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));
            if (StrUtil.isBlank(contentType) || !allowedTypeList.contains(contentType.trim())) {
                throw new BusinessException("不支持的文件类型");
            }
        }
        
        // 验证文件头魔数，防止文件伪装攻击
        try {
            validateFileMagicNumber(file, contentType);
        } catch (IOException e) {
            log.warn("读取文件头失败: {}", e.getMessage());
            throw new BusinessException("文件验证失败，请重新上传");
        }
    }
    
    /**
     * 验证文件头魔数
     * 通过检查文件的实际内容类型来防止恶意文件伪装
     *
     * @param file 上传的文件
     * @param declaredContentType 声明的Content-Type
     * @throws IOException IO异常
     */
    private void validateFileMagicNumber(MultipartFile file, String declaredContentType) throws IOException {
        // 读取文件前几个字节用于魔数检测
        byte[] fileHeader = new byte[16];
        try (java.io.InputStream is = file.getInputStream()) {
            int readBytes = is.read(fileHeader);
            if (readBytes < 2) {
                throw new BusinessException("文件内容无效");
            }
        }
        
        // 常见文件类型的魔数签名
        String detectedType = detectFileTypeByMagicNumber(fileHeader);
        
        // 如果检测到文件类型与声明的类型不匹配，拒绝上传
        if (detectedType != null && !isTypeCompatible(detectedType, declaredContentType)) {
            log.warn("文件类型不匹配，声明的类型: {}, 实际检测类型: {}", declaredContentType, detectedType);
            throw new BusinessException("文件类型与实际内容不匹配，禁止上传");
        }
        
        // 对于可执行文件类型，直接拒绝
        if (isDangerousType(detectedType)) {
            log.warn("检测到危险文件类型: {}", detectedType);
            throw new BusinessException("禁止上传可执行文件");
        }
    }
    
    /**
     * 通过魔数检测文件实际类型
     */
    private String detectFileTypeByMagicNumber(byte[] header) {
        // 图片类型
        if (header.length >= 2) {
            // JPEG: FF D8
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8) {
                return "image/jpeg";
            }
            // PNG: 89 50 4E 47
            if (header.length >= 4 && (header[0] & 0xFF) == 0x89 && 
                header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
                return "image/png";
            }
            // GIF: 47 49 46 38
            if (header.length >= 4 && header[0] == 0x47 && header[1] == 0x49 && 
                header[2] == 0x46 && header[3] == 0x38) {
                return "image/gif";
            }
            // BMP: 42 4D
            if (header[0] == 0x42 && header[1] == 0x4D) {
                return "image/bmp";
            }
            // WEBP: 52 49 46 46 ... 57 45 42 50
            if (header.length >= 12 && header[0] == 0x52 && header[1] == 0x49 && 
                header[2] == 0x46 && header[3] == 0x46 &&
                header[8] == 0x57 && header[9] == 0x45 && 
                header[10] == 0x42 && header[11] == 0x50) {
                return "image/webp";
            }
        }
        
        // 文档类型
        if (header.length >= 4) {
            // PDF: 25 50 44 46
            if (header[0] == 0x25 && header[1] == 0x50 && 
                header[2] == 0x44 && header[3] == 0x46) {
                return "application/pdf";
            }
            // ZIP (包括DOCX, XLSX等): 50 4B 03 04
            if (header[0] == 0x50 && header[1] == 0x4B && 
                header[2] == 0x03 && header[3] == 0x04) {
                return "application/zip";
            }
            // RAR: 52 61 72 21
            if (header[0] == 0x52 && header[1] == 0x61 && 
                header[2] == 0x72 && header[3] == 0x21) {
                return "application/x-rar-compressed";
            }
            // 7Z: 37 7A BC AF 27 1C
            if (header.length >= 6 && header[0] == 0x37 && header[1] == 0x7A && 
                header[2] == (byte)0xBC && header[3] == (byte)0xAF &&
                header[4] == 0x27 && header[5] == 0x1C) {
                return "application/x-7z-compressed";
            }
        }
        
        // 可执行文件类型（危险）
        if (header.length >= 2) {
            // EXE/DLL: 4D 5A (MZ)
            if (header[0] == 0x4D && header[1] == 0x5A) {
                return "application/x-dosexec";
            }
            // ELF: 7F 45 4C 46
            if (header.length >= 4 && header[0] == 0x7F && header[1] == 0x45 && 
                header[2] == 0x4C && header[3] == 0x46) {
                return "application/x-elf";
            }
        }
        
        // 脚本文件（可能危险）
        if (header.length >= 5) {
            // PHP: 3C 3F 70 68 70 (<?php)
            if (header[0] == 0x3C && header[1] == 0x3F && 
                header[2] == 0x70 && header[3] == 0x68 && header[4] == 0x70) {
                return "application/x-php";
            }
            // JSP: 3C 25 40 (or <%@)
            if (header[0] == 0x3C && header[1] == 0x25) {
                return "application/x-jsp";
            }
        }
        
        return null; // 未知类型
    }
    
    /**
     * 检查检测到的类型是否与声明的类型兼容
     */
    private boolean isTypeCompatible(String detectedType, String declaredContentType) {
        if (declaredContentType == null) {
            return true;
        }
        
        declaredContentType = declaredContentType.toLowerCase();
        detectedType = detectedType.toLowerCase();
        
        // 完全匹配
        if (declaredContentType.equals(detectedType)) {
            return true;
        }
        
        // 图片类型兼容性检查
        if (declaredContentType.startsWith("image/")) {
            return detectedType.startsWith("image/");
        }
        
        // Office文档类型（DOCX, XLSX等实际上是ZIP格式）
        if (declaredContentType.contains("officedocument") || 
            declaredContentType.contains("wordprocessingml") ||
            declaredContentType.contains("spreadsheetml") ||
            declaredContentType.contains("presentationml")) {
            return detectedType.equals("application/zip") || detectedType.startsWith("image/");
        }
        
        // PDF
        if (declaredContentType.equals("application/pdf")) {
            return detectedType.equals("application/pdf");
        }
        
        // 压缩文件
        if (declaredContentType.contains("zip") || declaredContentType.contains("rar") || 
            declaredContentType.contains("7z") || declaredContentType.contains("compressed")) {
            return detectedType.contains("zip") || detectedType.contains("rar") || 
                   detectedType.contains("7z") || detectedType.contains("compressed");
        }
        
        // 文本类型
        if (declaredContentType.startsWith("text/")) {
            return true; // 文本类型难以通过魔数验证，允许通过
        }
        
        return false;
    }
    
    /**
     * 判断是否为危险文件类型
     */
    private boolean isDangerousType(String detectedType) {
        if (detectedType == null) {
            return false;
        }
        
        // 可执行文件和脚本文件被认为是危险的
        return detectedType.equals("application/x-dosexec") ||    // EXE
               detectedType.equals("application/x-elf") ||         // Linux ELF
               detectedType.equals("application/x-php") ||         // PHP
               detectedType.equals("application/x-jsp") ||         // JSP
               detectedType.equals("application/x-sh") ||          // Shell脚本
               detectedType.equals("application/x-bat");           // Bat脚本
    }

    /**
     * 上传到MinIO
     */
    private String uploadToMinio(MultipartFile file, String objectName) throws Exception {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioBucketName)
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

        // 返回访问URL
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(minioBucketName)
                .object(objectName)
                .expiry(7, TimeUnit.DAYS)
                .build());
    }

    /**
     * 上传到本地存储
     */
    private String uploadToLocal(MultipartFile file, String filePath) throws Exception {
        Path fullPath = Paths.get(localStoragePath, filePath);
        Files.createDirectories(fullPath.getParent());
        Files.copy(file.getInputStream(), fullPath);
        
        return urlPrefix + "/" + filePath;
    }

    /**
     * 创建文件记录
     */
    private File createFileRecord(MultipartFile file, String filePath, String fileUrl, 
            String storageType, String fileMd5, Long uploaderId, String bizType, Long bizId) throws IOException {
        File fileEntity = new File();
        String originalFilename = file.getOriginalFilename();
        
        fileEntity.setFileName(FileUtil.mainName(originalFilename) + "_" + IdUtil.fastSimpleUUID() + "." + FileUtil.extName(originalFilename));
        fileEntity.setOriginalName(originalFilename);
        fileEntity.setFilePath(filePath);
        fileEntity.setFileUrl(fileUrl);
        fileEntity.setFileSize(file.getSize());
        fileEntity.setFileType(file.getContentType());
        fileEntity.setFileExt(FileUtil.extName(originalFilename));
        fileEntity.setStorageType(storageType);
        fileEntity.setFileMd5(fileMd5);
        fileEntity.setUploaderId(uploaderId);
        fileEntity.setBizType(bizType);
        fileEntity.setBizId(bizId);
        fileEntity.setDownloadCount(0);
        fileEntity.setStatus(0);
        
        return fileEntity;
    }

    /**
     * 转换为VO
     */
    private FileVO convertToVO(File file) {
        FileVO vo = new FileVO();
        BeanUtils.copyProperties(file, vo);
        vo.setFileSizeFormat(formatFileSize(file.getFileSize()));
        return vo;
    }

    /**
     * 格式化文件大小
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
