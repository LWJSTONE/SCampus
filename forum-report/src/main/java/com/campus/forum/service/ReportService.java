package com.campus.forum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.dto.ReportCreateDTO;
import com.campus.forum.dto.ReportHandleDTO;
import com.campus.forum.dto.ReportQueryDTO;
import com.campus.forum.entity.Report;
import com.campus.forum.vo.ReportVO;

/**
 * 举报服务接口
 *
 * @author campus
 * @since 2024-01-01
 */
public interface ReportService extends IService<Report> {

    /**
     * 提交举报
     *
     * @param createDTO 举报创建DTO
     * @param reporterId 举报人ID
     * @return 举报ID
     */
    Long submitReport(ReportCreateDTO createDTO, Long reporterId);

    /**
     * 分页查询举报列表
     *
     * @param queryDTO 查询参数
     * @return 举报分页列表
     */
    IPage<ReportVO> getReportPage(ReportQueryDTO queryDTO);

    /**
     * 获取举报详情
     *
     * @param id 举报ID
     * @return 举报详情
     */
    ReportVO getReportDetail(Long id);

    /**
     * 处理举报
     *
     * @param id 举报ID
     * @param handleDTO 处理DTO
     * @param handlerId 处理人ID
     * @return 是否成功
     */
    boolean handleReport(Long id, ReportHandleDTO handleDTO, Long handlerId);

    /**
     * 获取待处理举报数量
     *
     * @return 待处理数量
     */
    int getPendingCount();
}
