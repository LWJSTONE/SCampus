package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.api.comment.CommentApi;
import com.campus.forum.api.post.PostApi;
import com.campus.forum.api.user.UserApi;
import com.campus.forum.dto.ReportCreateDTO;
import com.campus.forum.dto.ReportHandleDTO;
import com.campus.forum.dto.ReportQueryDTO;
import com.campus.forum.dto.UserBanDTO;
import com.campus.forum.entity.Report;
import com.campus.forum.entity.Result;
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

import java.util.HashMap;
import java.util.Map;

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
    private final PostApi postApi;
    private final CommentApi commentApi;
    private final UserApi userApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitReport(ReportCreateDTO createDTO, Long reporterId) {
        // 验证举报类型
        if (createDTO.getReportType() == null || createDTO.getReportType() < 1 || createDTO.getReportType() > 3) {
            throw new BusinessException("举报类型不合法");
        }
        
        // 验证原因类型
        if (createDTO.getReasonType() == null || createDTO.getReasonType() < 1 || createDTO.getReasonType() > 6) {
            throw new BusinessException("举报原因类型不合法");
        }
        
        // 验证不能举报自己
        if (createDTO.getReportedUserId() != null && createDTO.getReportedUserId().equals(reporterId)) {
            throw new BusinessException("不能举报自己");
        }
        
        // 验证举报目标是否存在
        validateReportTarget(createDTO);
        
        // 检查是否已举报（待处理或处理中的举报）
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
    
    /**
     * 验证举报目标是否存在
     *
     * @param createDTO 举报创建DTO
     */
    private void validateReportTarget(ReportCreateDTO createDTO) {
        try {
            switch (createDTO.getReportType()) {
                case 1: // 帖子
                    Result<?> postResult = postApi.getPostById(createDTO.getTargetId());
                    if (postResult == null || postResult.getData() == null) {
                        throw new BusinessException("举报的帖子不存在");
                    }
                    break;
                case 2: // 评论
                    Result<?> commentResult = commentApi.getCommentById(createDTO.getTargetId());
                    if (commentResult == null || commentResult.getData() == null) {
                        throw new BusinessException("举报的评论不存在");
                    }
                    break;
                case 3: // 用户
                    if (createDTO.getReportedUserId() == null) {
                        throw new BusinessException("举报用户时必须提供被举报用户ID");
                    }
                    Result<?> userResult = userApi.getUserById(createDTO.getReportedUserId());
                    if (userResult == null || userResult.getData() == null) {
                        throw new BusinessException("举报的用户不存在");
                    }
                    break;
                default:
                    throw new BusinessException("举报类型不合法");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("验证举报目标时发生异常: {}", e.getMessage());
            // 服务调用失败时，不阻断举报流程，只记录警告日志
        }
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
        
        if (report.getStatus() == 2 || report.getStatus() == 3) {
            throw new BusinessException("该举报已处理，无法重复操作");
        }

        // 根据处理结果确定状态：无违规(0)设为驳回(3)，其他设为已处理(2)
        int newStatus = (handleDTO.getResult() != null && handleDTO.getResult() == 0) ? 3 : 2;
        
        int result = reportMapper.updateHandleStatus(id, newStatus, handlerId, 
                handleDTO.getResult(), handleDTO.getRemark());
        
        if (result > 0) {
            // 如果处理结果为禁言，则禁言用户
            if (handleDTO.getResult() != null && handleDTO.getResult() == 3 
                    && handleDTO.getBanDays() != null && handleDTO.getBanDays() > 0) {
                UserBanDTO banDTO = new UserBanDTO();
                banDTO.setUserId(report.getReportedUserId());
                banDTO.setReportId(report.getId());
                banDTO.setBanDays(handleDTO.getBanDays());
                banDTO.setReason("违反社区规定");
                userBanService.banUser(banDTO, handlerId);
            }
            
            log.info("举报 {} 处理完成，处理结果: {}, 状态: {}", id, handleDTO.getResult(), newStatus == 3 ? "已驳回" : "已处理");
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
            Map<Integer, String> typeMap = new HashMap<>();
            typeMap.put(1, "帖子");
            typeMap.put(2, "评论");
            typeMap.put(3, "用户");
            vo.setReportTypeName(typeMap.getOrDefault(vo.getReportType(), "未知类型"));
        }
        
        // 原因类型名称
        if (vo.getReasonType() != null) {
            Map<Integer, String> reasonMap = new HashMap<>();
            reasonMap.put(1, "垃圾广告");
            reasonMap.put(2, "色情低俗");
            reasonMap.put(3, "违法违规");
            reasonMap.put(4, "人身攻击");
            reasonMap.put(5, "恶意灌水");
            reasonMap.put(6, "其他");
            vo.setReasonTypeName(reasonMap.getOrDefault(vo.getReasonType(), "其他"));
        }
        
        // 状态名称
        if (vo.getStatus() != null) {
            Map<Integer, String> statusMap = new HashMap<>();
            statusMap.put(0, "待处理");
            statusMap.put(1, "处理中");
            statusMap.put(2, "已处理");
            statusMap.put(3, "已驳回");
            vo.setStatusName(statusMap.getOrDefault(vo.getStatus(), "未知状态"));
        }
        
        // 处理结果名称
        if (vo.getResult() != null) {
            Map<Integer, String> resultMap = new HashMap<>();
            resultMap.put(0, "无违规");
            resultMap.put(1, "警告");
            resultMap.put(2, "删除内容");
            resultMap.put(3, "禁言");
            resultMap.put(4, "封号");
            vo.setResultName(resultMap.getOrDefault(vo.getResult(), "未知结果"));
        }
    }
}
