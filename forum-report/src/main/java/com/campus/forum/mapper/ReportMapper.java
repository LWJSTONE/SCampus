package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.Report;
import com.campus.forum.vo.ReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 举报Mapper接口
 *
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {

    /**
     * 分页查询举报列表
     */
    @Select("<script>" +
            "SELECT r.*, " +
            "       ru.nickname as reporter_name, ru.avatar as reporter_avatar, " +
            "       ru2.nickname as reported_user_name, " +
            "       h.nickname as handler_name " +
            "FROM t_report r " +
            "LEFT JOIN t_user ru ON r.reporter_id = ru.id " +
            "LEFT JOIN t_user ru2 ON r.reported_user_id = ru2.id " +
            "LEFT JOIN t_user h ON r.handler_id = h.id " +
            "WHERE r.delete_flag = 0 " +
            "<if test='status != null'> AND r.status = #{status} </if>" +
            "<if test='reportType != null'> AND r.report_type = #{reportType} </if>" +
            "<if test='reasonType != null'> AND r.reason_type = #{reasonType} </if>" +
            "<if test='reporterId != null'> AND r.reporter_id = #{reporterId} </if>" +
            "<if test='reportedUserId != null'> AND r.reported_user_id = #{reportedUserId} </if>" +
            "<if test='startTime != null and startTime != \"\"> AND r.create_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null and endTime != \"\"> AND r.create_time &lt;= #{endTime} </if>" +
            "ORDER BY r.status ASC, r.create_time DESC" +
            "</script>")
    IPage<ReportVO> selectReportPage(Page<ReportVO> page,
                                      @Param("status") Integer status,
                                      @Param("reportType") Integer reportType,
                                      @Param("reasonType") Integer reasonType,
                                      @Param("reporterId") Long reporterId,
                                      @Param("reportedUserId") Long reportedUserId,
                                      @Param("startTime") String startTime,
                                      @Param("endTime") String endTime);

    /**
     * 查询举报详情
     */
    @Select("SELECT r.*, " +
            "       ru.nickname as reporter_name, ru.avatar as reporter_avatar, " +
            "       ru2.nickname as reported_user_name, " +
            "       h.nickname as handler_name " +
            "FROM t_report r " +
            "LEFT JOIN t_user ru ON r.reporter_id = ru.id " +
            "LEFT JOIN t_user ru2 ON r.reported_user_id = ru2.id " +
            "LEFT JOIN t_user h ON r.handler_id = h.id " +
            "WHERE r.id = #{id} AND r.delete_flag = 0")
    ReportVO selectReportDetail(@Param("id") Long id);

    /**
     * 更新处理状态
     */
    @Update("UPDATE t_report SET status = #{status}, handler_id = #{handlerId}, " +
            "result = #{result}, remark = #{remark}, handle_time = NOW() " +
            "WHERE id = #{id}")
    int updateHandleStatus(@Param("id") Long id,
                           @Param("status") Integer status,
                           @Param("handlerId") Long handlerId,
                           @Param("result") Integer result,
                           @Param("remark") String remark);

    /**
     * 统计待处理举报数量
     */
    @Select("SELECT COUNT(*) FROM t_report WHERE status = 0 AND delete_flag = 0")
    int countPending();

    /**
     * 查询用户举报记录
     */
    @Select("SELECT COUNT(*) FROM t_report " +
            "WHERE reporter_id = #{reporterId} AND target_id = #{targetId} AND delete_flag = 0")
    int countByReporterAndTarget(@Param("reporterId") Long reporterId, @Param("targetId") Long targetId);
}
