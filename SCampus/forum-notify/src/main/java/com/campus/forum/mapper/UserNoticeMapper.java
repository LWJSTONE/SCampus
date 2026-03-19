package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.UserNotice;
import org.apache.ibatis.annotations.Mapper;

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

}
