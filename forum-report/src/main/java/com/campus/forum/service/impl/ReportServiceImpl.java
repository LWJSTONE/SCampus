package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.dto.ReportCreateDTO;
import com.campus.forum.dto.ReportHandleDTO;
import com.campus.forum.dto.ReportQueryDTO;
import com.campus.forum.dto.UserBanDTO;
import com.campus.forum.entity.Report;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.ReportMapper;
import com.campus.forum.service.ReportService;
import com.campus.forum.service.UserBanService;
import com.campus.forum.vo.ReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 举报服务实现类
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    private final ReportMapper reportMapper;
    private final UserBanService userBanService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitReport(ReportCreateDTO createDTO, Long reporterId) {
        // 检查是否已举报
        int count = reportMapper.countByReporterAndTarget(reporterId, createDTO.getTargetId());
        if (count > 0) {
            throw new BusinessException("您已举报过该内容，请等待处理");
        }

        Report report = new Report();
        BeanUtils.copyProperties(createDTO, report);
        report.setReporterId(reporterId);
        report.setStatus(0); // 待处理
        report.setDeleteFlag(0);
        
        save(report);
        
        log.info("用户 {} 提交举报成功，举报ID: {}", reporterId, report.getId());
        return report.getId();
    }

    @Override
    public IPage<ReportVO> getReportPage(ReportQueryDTO queryDTO) {
        Page<ReportVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        IPage<ReportVO> result = reportMapper.selectReportPage(page,
                queryDTO.getStatus(),
                queryDTO.getReportType(),
                queryDTO.getReasonType(),
                queryDTO.getReporterId(),
                queryDTO.getReportedUserId(),
                queryDTO.getStartTime(),
                queryDTO.getEndTime());
        
        // 填充名称
        result.getRecords().forEach(this::fillNames);
        
        return result;
    }

    @Override
    public ReportVO getReportDetail(Long id) {
        ReportVO reportVO = reportMapper.selectReportDetail(id);
        if (reportVO == null) {
            throw new BusinessException("举报记录不存在");
        }
        fillNames(reportVO);
        return reportVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleReport(Long id, ReportHandleDTO handleDTO, Long handlerId) {
        Report report = getById(id);
        if (report == null) {
            throw new BusinessException("举报记录不存在");
        }
        
        if (report.getStatus() == 2) {
            throw new BusinessException("该举报已处理");
        }

        int result = reportMapper.updateHandleStatus(id, 2, handlerId, 
                handleDTO.getResult(), handleDTO.getRemark());
        
        if (result > 0) {
            // 如果处理结果为禁言，则禁言用户
            if (handleDTO.getResult() == 3 && handleDTO.getBanDays() != null && handleDTO.getBanDays() > 0) {
                UserBanDTO banDTO = new UserBanDTO();
                banDTO.setUserId(report.getReportedUserId());
                banDTO.setReportId(report.getId());
                banDTO.setBanDays(handleDTO.getBanDays());
                banDTO.setReason("违反社区规定");
                userBanService.banUser(banDTO, handlerId);
            }
            
            log.info("举报 {} 处理完成，处理结果: {}", id, handleDTO.getResult());
            return true;
        }
        
        return false;
    }

    @Override
    public int getPendingCount() {
        return reportMapper.countPending();
    }

    /**
     * 填充名称
     */
    private void fillNames(ReportVO vo) {
        // 举报类型名称
        if (vo.getReportType() != null) {
            String[] typeNames = {"", "帖子", "评论", "用户"};
            vo.setReportTypeName(typeNames[vo.getReportType()]);
        }
        
        // 原因类型名称
        if (vo.getReasonType() != null) {
            String[] reasonNames = {"", "垃圾广告", "色情低俗", "违法违规", "人身攻击", "恶意灌水", "其他"};
            vo.setReasonTypeName(reasonNames[vo.getReasonType()]);
        }
        
        // 状态名称
        if (vo.getStatus() != null) {
            String[] statusNames = {"待处理", "处理中", "已处理", "已驳回"};
            vo.setStatusName(statusNames[vo.getStatus()]);
        }
        
        // 处理结果名称
        if (vo.getResult() != null) {
            String[] resultNames = {"无违规", "警告", "删除内容", "禁言", "封号"};
            vo.setResultName(resultNames[vo.getResult()]);
        }
    }
}
