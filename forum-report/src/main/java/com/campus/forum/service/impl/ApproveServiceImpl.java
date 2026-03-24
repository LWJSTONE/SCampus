package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.dto.ApproveHandleDTO;
import com.campus.forum.dto.ApproveQueryDTO;
import com.campus.forum.entity.Approve;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.ApproveMapper;
import com.campus.forum.service.ApproveService;
import com.campus.forum.vo.ApproveVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 审核服务实现类
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApproveServiceImpl extends ServiceImpl<ApproveMapper, Approve> implements ApproveService {

    private final ApproveMapper approveMapper;

    @Override
    public IPage<ApproveVO> getApprovePage(ApproveQueryDTO queryDTO) {
        Page<ApproveVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        IPage<ApproveVO> result = approveMapper.selectApprovePage(page,
                queryDTO.getStatus(),
                queryDTO.getContentType(),
                queryDTO.getUserId(),
                queryDTO.getStartTime(),
                queryDTO.getEndTime());
        
        // 填充名称
        result.getRecords().forEach(this::fillNames);
        
        return result;
    }

    @Override
    public ApproveVO getApproveDetail(Long id) {
        ApproveVO approveVO = approveMapper.selectApproveDetail(id);
        if (approveVO == null) {
            throw new BusinessException("审核记录不存在");
        }
        fillNames(approveVO);
        return approveVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long id, ApproveHandleDTO handleDTO, Long auditorId) {
        Approve approve = getById(id);
        if (approve == null) {
            throw new BusinessException("审核记录不存在");
        }
        
        // 【高严重度问题修复】添加状态值合法性校验
        // 合法状态值：0-待审核，1-审核通过，2-审核拒绝
        if (handleDTO.getStatus() == null || handleDTO.getStatus() < 0 || handleDTO.getStatus() > 2) {
            throw new BusinessException("审核状态不合法，有效值为：0(待审核)、1(审核通过)、2(审核拒绝)");
        }

        // 乐观锁保护：通过SQL的WHERE status = 0条件实现并发控制
        // 不需要在这里检查状态，因为SQL更新会自动判断
        // 如果记录已被其他管理员处理，updateApproveStatus将返回0

        int result = approveMapper.updateApproveStatus(id, handleDTO.getStatus(), 
                auditorId, handleDTO.getAuditRemark());
        
        if (result > 0) {
            log.info("审核 {} 完成，状态: {}", id, handleDTO.getStatus());
            return true;
        }
        
        // 更新失败，说明记录已被其他管理员处理
        log.warn("审核 {} 失败，可能已被其他管理员处理", id);
        throw new BusinessException("该内容已被其他管理员审核");
    }

    @Override
    public int getPendingCount() {
        return approveMapper.countPending();
    }

    @Override
    public Long createApprove(Long userId, Integer contentType, Long contentId, 
                               String title, String content, String sensitiveWords) {
        Approve approve = new Approve();
        approve.setUserId(userId);
        approve.setContentType(contentType);
        approve.setContentId(contentId);
        approve.setTitle(title);
        approve.setContent(content);
        approve.setSensitiveWords(sensitiveWords);
        approve.setStatus(0); // 待审核
        approve.setPriority(0); // 普通优先级
        approve.setDeleteFlag(0);
        
        save(approve);
        
        log.info("创建审核记录成功，ID: {}", approve.getId());
        return approve.getId();
    }

    /**
     * 填充名称
     */
    private void fillNames(ApproveVO vo) {
        // 内容类型名称 - 使用Map替代数组避免越界
        if (vo.getContentType() != null) {
            java.util.Map<Integer, String> typeNames = new java.util.HashMap<>();
            typeNames.put(0, "");
            typeNames.put(1, "帖子");
            typeNames.put(2, "评论");
            typeNames.put(3, "头像");
            typeNames.put(4, "昵称");
            vo.setContentTypeName(typeNames.getOrDefault(vo.getContentType(), "未知类型"));
        }
        
        // 审核状态名称 - 使用Map替代数组避免越界
        if (vo.getStatus() != null) {
            java.util.Map<Integer, String> statusNames = new java.util.HashMap<>();
            statusNames.put(0, "待审核");
            statusNames.put(1, "审核通过");
            statusNames.put(2, "审核拒绝");
            vo.setStatusName(statusNames.getOrDefault(vo.getStatus(), "未知状态"));
        }
    }
}
