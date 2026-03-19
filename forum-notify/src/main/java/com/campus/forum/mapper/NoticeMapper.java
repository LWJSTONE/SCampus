package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 通知Mapper接口
 * 
 * 提供通知数据访问操作
 * 
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

    /**
     * 增加阅读数
     * 
     * @param noticeId 通知ID
     * @return 影响行数
     */
    @Update("UPDATE t_notice SET read_count = read_count + 1 WHERE id = #{noticeId}")
    int incrementReadCount(@Param("noticeId") Long noticeId);

    /**
     * 撤回通知
     * 
     * @param noticeId 通知ID
     * @return 影响行数
     */
    @Update("UPDATE t_notice SET status = 2 WHERE id = #{noticeId}")
    int revokeNotice(@Param("noticeId") Long noticeId);
}
