package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.dto.NoticeCreateDTO;
import com.campus.forum.dto.NoticeQueryDTO;
import com.campus.forum.dto.NoticeUpdateDTO;
import com.campus.forum.vo.NoticeVO;

/**
 * 通知服务接口
 * 
 * 提供通知相关的业务操作
 * 
 * @author campus
 * @since 2024-01-01
 */
public interface NoticeService {

    /**
     * 获取通知列表
     * 
     * @param queryDTO 查询参数
     * @param userId 当前用户ID（可为空）
     * @return 通知分页列表
     */
    Page<NoticeVO> getNoticeList(NoticeQueryDTO queryDTO, Long userId);

    /**
     * 获取通知详情
     * 
     * @param noticeId 通知ID
     * @param userId 当前用户ID
     * @return 通知详情
     */
    NoticeVO getNoticeDetail(Long noticeId, Long userId);

    /**
     * 发布通知
     * 
     * @param createDTO 通知创建DTO
     * @param userId 发布人ID
     * @param userName 发布人名称
     * @return 通知ID
     */
    Long publishNotice(NoticeCreateDTO createDTO, Long userId, String userName);

    /**
     * 更新通知
     * 
     * @param noticeId 通知ID
     * @param updateDTO 更新DTO
     * @param userId 操作人ID
     * @return 是否成功
     */
    boolean updateNotice(Long noticeId, NoticeUpdateDTO updateDTO, Long userId);

    /**
     * 删除通知
     * 
     * @param noticeId 通知ID
     * @param userId 操作人ID
     * @return 是否成功
     */
    boolean deleteNotice(Long noticeId, Long userId);

    /**
     * 获取未读消息数
     * 
     * @param userId 用户ID
     * @return 未读消息数
     */
    int getUnreadCount(Long userId);

    /**
     * 标记通知已读
     * 
     * @param noticeId 通知ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long noticeId, Long userId);

    /**
     * 全部标记已读
     * 
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllAsRead(Long userId);
}
