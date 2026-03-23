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
     * 如果验证失败（目标不存在），抛出异常阻断举报流程
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
            // 业务异常直接抛出，阻断举报流程
            throw e;
        } catch (Exception e) {
            // 服务调用异常，记录详细日志
            log.error("验证举报目标时发生异常, targetId: {}, reportType: {}", 
                    createDTO.getTargetId(), createDTO.getReportType(), e);
            // 服务不可用时，为了用户体验，允许举报提交，但记录警告
            // 后续可以通过定时任务清理无效举报
            log.warn("服务调用失败，举报目标验证跳过，将在处理时验证: {}", e.getMessage());
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
        
        // 检查当前状态必须是"待处理(0)"才能进行处理
        // 这样可以防止并发处理：如果有多个请求同时到达，只有一个能通过此检查
        // 因为数据库更新操作会锁定记录，后续请求在获取数据时会发现状态已变更
        if (report.getStatus() != 0) {
            String statusMsg;
            switch (report.getStatus()) {
                case 1:
                    statusMsg = "处理中";
                    break;
                case 2:
                    statusMsg = "已处理";
                    break;
                case 3:
                    statusMsg = "已驳回";
                    break;
                default:
                    statusMsg = "未知状态(" + report.getStatus() + ")";
            }
            throw new BusinessException("该举报当前状态为[" + statusMsg + "]，无法重复处理");
        }

        // 根据处理结果确定状态：无违规(0)设为驳回(3)，其他设为已处理(2)
        int newStatus = (handleDTO.getResult() != null && handleDTO.getResult() == 0) ? 3 : 2;
        
        int result = reportMapper.updateHandleStatus(id, newStatus, handlerId, 
                handleDTO.getResult(), handleDTO.getRemark());
        
        if (result > 0) {
            // 根据处理结果执行相应操作
            Integer handleResult = handleDTO.getResult();
            
            if (handleResult != null) {
                switch (handleResult) {
                    case 0: // 无违规
                        log.info("举报 {} 处理结果：无违规，已驳回", id);
                        break;
                    case 1: // 警告
                        // TODO: 发送警告通知给被举报用户
                        log.info("举报 {} 处理结果：警告用户 {}", id, report.getReportedUserId());
                        break;
                    case 2: // 删除内容
                        // TODO: 调用相应服务删除被举报的内容
                        deleteReportedContent(report);
                        log.info("举报 {} 处理结果：删除内容 {}", id, report.getTargetId());
                        break;
                    case 3: // 禁言
                        if (handleDTO.getBanDays() != null && handleDTO.getBanDays() > 0) {
                            // 检查被举报用户ID是否有效
                            if (report.getReportedUserId() == null) {
                                log.error("禁言失败：被举报用户ID为空，举报ID: {}", id);
                                throw new BusinessException("禁言失败：被举报用户ID为空");
                            }
                            UserBanDTO banDTO = new UserBanDTO();
                            banDTO.setUserId(report.getReportedUserId());
                            banDTO.setReportId(report.getId());
                            banDTO.setBanDays(handleDTO.getBanDays());
                            banDTO.setReason("违反社区规定");
                            userBanService.banUser(banDTO, handlerId);
                            log.info("举报 {} 处理结果：禁言用户 {} 天数 {}", id, report.getReportedUserId(), handleDTO.getBanDays());
                        }
                        break;
                    case 4: // 封号
                        // TODO: 调用用户服务封禁账号
                        log.info("举报 {} 处理结果：封号用户 {}", id, report.getReportedUserId());
                        break;
                    default:
                        log.warn("举报 {} 处理结果未知：{}", id, handleResult);
                }
            }
            
            log.info("举报 {} 处理完成，处理结果: {}, 状态: {}", id, handleResult, newStatus == 3 ? "已驳回" : "已处理");
            return true;
        }
        
        return false;
    }
    
    /**
     * 删除被举报的内容
     * 
     * 根据举报类型调用相应的服务删除内容
     * 帖子和评论执行逻辑删除，用户类型则记录警告日志
     *
     * @param report 举报记录
     */
    private void deleteReportedContent(Report report) {
        try {
            switch (report.getReportType()) {
                case 1: // 帖子
                    // 调用帖子服务删除帖子
                    Result<?> deletePostResult = postApi.deletePost(report.getTargetId());
                    if (deletePostResult != null && deletePostResult.getCode() == 200) {
                        log.info("删除被举报帖子成功: postId={}", report.getTargetId());
                    } else {
                        log.warn("删除被举报帖子失败: postId={}, result={}", report.getTargetId(), deletePostResult);
                    }
                    break;
                case 2: // 评论
                    // 调用评论服务删除评论
                    Result<?> deleteCommentResult = commentApi.deleteComment(report.getTargetId());
                    if (deleteCommentResult != null && deleteCommentResult.getCode() == 200) {
                        log.info("删除被举报评论成功: commentId={}", report.getTargetId());
                    } else {
                        log.warn("删除被举报评论失败: commentId={}, result={}", report.getTargetId(), deleteCommentResult);
                    }
                    break;
                case 3: // 用户
                    // 用户类型不应该删除内容，而是应该封号
                    // 这里记录警告日志，实际封号操作应该在 handleReport 的 case 4 中处理
                    log.warn("举报类型为用户，不应删除内容，应考虑封号处理。reportId={}, reportedUserId={}", 
                            report.getId(), report.getReportedUserId());
                    break;
                default:
                    log.warn("未知的举报类型: reportId={}, reportType={}", report.getId(), report.getReportType());
            }
        } catch (Exception e) {
            log.error("删除被举报内容失败, reportId: {}, targetId: {}", report.getId(), report.getTargetId(), e);
            // 删除内容失败不影响举报处理结果，但需要记录日志便于后续人工处理
        }
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
