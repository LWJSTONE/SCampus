package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.PostAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子附件Mapper接口
 * 提供帖子附件数据的数据库操作
 *
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface PostAttachmentMapper extends BaseMapper<PostAttachment> {

    /**
     * 根据帖子ID查询附件列表
     *
     * @param postId 帖子ID
     * @return 附件列表
     */
    List<PostAttachment> selectByPostId(@Param("postId") Long postId);

    /**
     * 批量插入附件
     *
     * @param attachments 附件列表
     * @return 影响行数
     */
    int batchInsert(@Param("attachments") List<PostAttachment> attachments);

    /**
     * 根据帖子ID删除附件
     *
     * @param postId 帖子ID
     * @return 影响行数
     */
    int deleteByPostId(@Param("postId") Long postId);

    /**
     * 根据帖子ID查询图片列表
     *
     * @param postId 帖子ID
     * @param limit 数量限制
     * @return 图片URL列表
     */
    List<String> selectImageUrlsByPostId(@Param("postId") Long postId, @Param("limit") Integer limit);

    /**
     * 更新附件状态
     *
     * @param id 附件ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 统计帖子附件数量
     *
     * @param postId 帖子ID
     * @return 附件数量
     */
    int countByPostId(@Param("postId") Long postId);
}
