package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.File;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 文件Mapper
 *
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface FileMapper extends BaseMapper<File> {

    /**
     * 根据MD5查询文件
     */
    File selectByMd5(@Param("fileMd5") String fileMd5);

    /**
     * 根据业务类型和业务ID查询文件列表
     */
    List<File> selectByBiz(@Param("bizType") String bizType, @Param("bizId") Long bizId);

    /**
     * 增加下载次数
     */
    @Update("UPDATE sys_file SET download_count = download_count + 1 WHERE id = #{id}")
    int incrementDownloadCount(@Param("id") Long id);

    /**
     * 批量更新状态
     */
    int updateStatusBatch(@Param("ids") List<Long> ids, @Param("status") Integer status);

    /**
     * 根据上传者ID查询文件列表
     */
    List<File> selectByUploaderId(@Param("uploaderId") Long uploaderId);
}
