package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.dto.ApproveHandleDTO;
import com.campus.forum.dto.ApproveQueryDTO;
import com.campus.forum.entity.Approve;
import com.campus.forum.vo.ApproveVO;

/**
 * 审核服务接口
 *
 * @author campus
 * @since 2024-01-01
 */
public interface ApproveService extends IService<Approve> {

    /**
     * 分页查询待审核列表
     *
     * @param queryDTO 查询参数
     * @return 审核分页列表
     */
    Page<ApproveVO> getApprovePage(ApproveQueryDTO queryDTO);

    /**
     * 获取审核详情
     *
     * @param id 审核ID
     * @return 审核详情
     */
    ApproveVO getApproveDetail(Long id);

    /**
     * 审核通过/驳回
     *
     * @param id 审核ID
     * @param handleDTO 处理DTO
     * @param auditorId 审核人ID
     * @return 是否成功
     */
    boolean approve(Long id, ApproveHandleDTO handleDTO, Long auditorId);

    /**
     * 获取待审核数量
     *
     * @return 待审核数量
     */
    int getPendingCount();

    /**
     * 创建审核记录
     *
     * @param userId 用户ID
     * @param contentType 内容类型
     * @param contentId 内容ID
     * @param title 标题
     * @param content 内容摘要
     * @param sensitiveWords 敏感词
     * @return 审核ID
     */
    Long createApprove(Long userId, Integer contentType, Long contentId, 
                       String title, String content, String sensitiveWords);
}
