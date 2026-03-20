package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.Approve;
import com.campus.forum.vo.ApproveVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 审核记录Mapper接口
 *
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface ApproveMapper extends BaseMapper<Approve> {

    /**
     * 分页查询审核列表
     */
    @Select("<script>" +
            "SELECT a.*, u.nickname as user_name, u.avatar as user_avatar, " +
            "       au.nickname as auditor_name " +
            "FROM t_approve a " +
            "LEFT JOIN t_user u ON a.user_id = u.id " +
            "LEFT JOIN t_user au ON a.auditor_id = au.id " +
            "WHERE a.delete_flag = 0 " +
            "<if test='status != null'> AND a.status = #{status} </if>" +
            "<if test='contentType != null'> AND a.content_type = #{contentType} </if>" +
            "<if test='userId != null'> AND a.user_id = #{userId} </if>" +
            "<if test='startTime != null and startTime != \"\"'> AND a.create_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null and endTime != \"\"'> AND a.create_time &lt;= #{endTime} </if>" +
            "ORDER BY a.priority DESC, a.create_time ASC" +
            "</script>")
    IPage<ApproveVO> selectApprovePage(Page<ApproveVO> page,
                                        @Param("status") Integer status,
                                        @Param("contentType") Integer contentType,
                                        @Param("userId") Long userId,
                                        @Param("startTime") String startTime,
                                        @Param("endTime") String endTime);

    /**
     * 查询审核详情
     */
    @Select("SELECT a.*, u.nickname as user_name, u.avatar as user_avatar, " +
            "       au.nickname as auditor_name " +
            "FROM t_approve a " +
            "LEFT JOIN t_user u ON a.user_id = u.id " +
            "LEFT JOIN t_user au ON a.auditor_id = au.id " +
            "WHERE a.id = #{id} AND a.delete_flag = 0")
    ApproveVO selectApproveDetail(@Param("id") Long id);

    /**
     * 更新审核状态
     */
    @Update("UPDATE t_approve SET status = #{status}, auditor_id = #{auditorId}, " +
            "audit_remark = #{auditRemark}, audit_time = NOW() " +
            "WHERE id = #{id}")
    int updateApproveStatus(@Param("id") Long id,
                            @Param("status") Integer status,
                            @Param("auditorId") Long auditorId,
                            @Param("auditRemark") String auditRemark);

    /**
     * 统计待审核数量
     */
    @Select("SELECT COUNT(*) FROM t_approve WHERE status = 0 AND delete_flag = 0")
    int countPending();
}
