package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.UserBan;
import com.campus.forum.vo.UserBanVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户禁言Mapper接口
 *
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface UserBanMapper extends BaseMapper<UserBan> {

    /**
     * 分页查询禁言列表
     */
    @Select("<script>" +
            "SELECT ub.*, u.nickname as user_name, u.avatar as user_avatar, " +
            "       o.nickname as operator_name, ro.nickname as release_operator_name " +
            "FROM t_user_ban ub " +
            "LEFT JOIN t_user u ON ub.user_id = u.id " +
            "LEFT JOIN t_user o ON ub.operator_id = o.id " +
            "LEFT JOIN t_user ro ON ub.release_operator_id = ro.id " +
            "WHERE ub.delete_flag = 0 " +
            "<if test='userId != null'> AND ub.user_id = #{userId} </if>" +
            "<if test='status != null'> AND ub.status = #{status} </if>" +
            "<if test='banType != null'> AND ub.ban_type = #{banType} </if>" +
            "ORDER BY ub.create_time DESC" +
            "</script>")
    IPage<UserBanVO> selectBanPage(Page<UserBanVO> page,
                                    @Param("userId") Long userId,
                                    @Param("status") Integer status,
                                    @Param("banType") Integer banType);

    /**
     * 查询用户当前禁言状态
     */
    @Select("SELECT ub.*, u.nickname as user_name, u.avatar as user_avatar, " +
            "       o.nickname as operator_name " +
            "FROM t_user_ban ub " +
            "LEFT JOIN t_user u ON ub.user_id = u.id " +
            "LEFT JOIN t_user o ON ub.operator_id = o.id " +
            "WHERE ub.user_id = #{userId} AND ub.status = 1 AND ub.end_time &gt; NOW() AND ub.delete_flag = 0 " +
            "ORDER BY ub.end_time DESC LIMIT 1")
    UserBanVO selectActiveBan(@Param("userId") Long userId);

    /**
     * 解除禁言
     */
    @Update("UPDATE t_user_ban SET status = 0, release_time = NOW(), " +
            "release_operator_id = #{operatorId}, release_reason = #{reason} " +
            "WHERE id = #{id}")
    int releaseBan(@Param("id") Long id,
                   @Param("operatorId") Long operatorId,
                   @Param("reason") String reason);

    /**
     * 批量过期禁言记录
     */
    @Update("UPDATE t_user_ban SET status = 2 WHERE status = 1 AND end_time &lt; NOW()")
    int expireBans();

    /**
     * 查询用户禁言历史
     */
    @Select("SELECT ub.*, u.nickname as user_name, u.avatar as user_avatar, " +
            "       o.nickname as operator_name, ro.nickname as release_operator_name " +
            "FROM t_user_ban ub " +
            "LEFT JOIN t_user u ON ub.user_id = u.id " +
            "LEFT JOIN t_user o ON ub.operator_id = o.id " +
            "LEFT JOIN t_user ro ON ub.release_operator_id = ro.id " +
            "WHERE ub.user_id = #{userId} AND ub.delete_flag = 0 " +
            "ORDER BY ub.create_time DESC")
    List<UserBanVO> selectBanHistory(@Param("userId") Long userId);

    /**
     * 统计用户禁言次数
     */
    @Select("SELECT COUNT(*) FROM t_user_ban WHERE user_id = #{userId} AND delete_flag = 0")
    int countByUserId(@Param("userId") Long userId);
}
