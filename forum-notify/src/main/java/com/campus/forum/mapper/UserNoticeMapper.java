package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.UserNotice;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 用户通知Mapper接口
 * 
 * 提供用户通知阅读状态数据访问操作
 * 
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface UserNoticeMapper extends BaseMapper<UserNotice> {

    /**
     * 批量插入未读通知记录
     * 为用户创建所有已发布但尚未创建记录的通知的阅读记录
     * 
     * @param userId 用户ID
     * @return 插入记录数
     */
    @Insert("INSERT INTO t_user_notice (user_id, notice_id, is_read, read_time, is_deleted, create_time) " +
            "SELECT #{userId}, n.id, 0, NULL, 0, NOW() " +
            "FROM t_notice n " +
            "WHERE n.status = 1 AND n.delete_flag = 0 " +
            "AND NOT EXISTS (" +
            "   SELECT 1 FROM t_user_notice un " +
            "   WHERE un.notice_id = n.id AND un.user_id = #{userId}" +
            ")")
    int batchInsertUnreadNotices(@Param("userId") Long userId);

    /**
     * 批量标记所有通知为已读
     * 将用户所有未读通知更新为已读状态
     * 
     * @param userId 用户ID
     * @param readTime 阅读时间
     * @return 更新记录数
     */
    @Update("UPDATE t_user_notice SET is_read = 1, read_time = #{readTime} " +
            "WHERE user_id = #{userId} AND is_read = 0")
    int batchMarkAsRead(@Param("userId") Long userId, @Param("readTime") LocalDateTime readTime);
}
