package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 分类Mapper接口
 *
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 查询所有分类
     *
     * @return 分类列表
     */
    @Select("SELECT * FROM forum_category WHERE delete_flag = 0 ORDER BY sort ASC, id ASC")
    List<Category> selectAllCategories();

    /**
     * 根据父ID查询子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @Select("SELECT * FROM forum_category WHERE parent_id = #{parentId} AND delete_flag = 0 ORDER BY sort ASC, id ASC")
    List<Category> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询顶级分类
     *
     * @return 顶级分类列表
     */
    @Select("SELECT * FROM forum_category WHERE parent_id = 0 AND delete_flag = 0 ORDER BY sort ASC, id ASC")
    List<Category> selectTopCategories();

    /**
     * 更新分类状态
     *
     * @param id     分类ID
     * @param status 状态
     * @return 影响行数
     */
    @Update("UPDATE forum_category SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 检查分类名称是否存在
     *
     * @param name     分类名称
     * @param parentId 父分类ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM forum_category WHERE name = #{name} AND parent_id = #{parentId} AND delete_flag = 0")
    int countByName(@Param("name") String name, @Param("parentId") Long parentId);

    /**
     * 检查分类下是否有子分类
     *
     * @param id 分类ID
     * @return 子分类数量
     */
    @Select("SELECT COUNT(*) FROM forum_category WHERE parent_id = #{id} AND delete_flag = 0")
    int countChildren(@Param("id") Long id);

    /**
     * 检查分类下是否有版块
     *
     * @param categoryId 分类ID
     * @return 版块数量
     */
    @Select("SELECT COUNT(*) FROM forum_forum WHERE category_id = #{categoryId} AND delete_flag = 0")
    int countForums(@Param("categoryId") Long categoryId);

    /**
     * 批量查询分类信息
     * 【性能优化】用于解决N+1查询问题，一次查询获取多个分类信息
     *
     * @param categoryIds 分类ID列表
     * @return 分类列表
     */
    @Select("<script>" +
            "SELECT * FROM forum_category WHERE id IN " +
            "<foreach collection='categoryIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND delete_flag = 0" +
            "</script>")
    List<Category> selectByIds(@Param("categoryIds") List<Long> categoryIds);
}
