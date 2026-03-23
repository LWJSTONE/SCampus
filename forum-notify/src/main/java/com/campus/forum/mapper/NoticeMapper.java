package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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

    /**
     * 统计用户未读通知数量
     * 查询已发布但用户未读的通知数量
     * 
     * @param userId 用户ID
     * @return 未读通知数量
     */
    @Select("SELECT COUNT(*) FROM t_notice n " +
            "WHERE n.status = 1 AND n.delete_flag = 0 " +
            "AND NOT EXISTS (" +
            "   SELECT 1 FROM t_user_notice un " +
            "   WHERE un.notice_id = n.id AND un.user_id = #{userId} AND un.is_read = 1" +
            ")")
    int countUnreadByUserId(@Param("userId") Long userId);
}
