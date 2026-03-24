package com.campus.forum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.constant.ResultCode;
import com.campus.forum.dto.PasswordUpdateDTO;
import com.campus.forum.dto.UserQueryDTO;
import com.campus.forum.dto.UserUpdateDTO;
import com.campus.forum.entity.PageResult;
import com.campus.forum.entity.User;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.UserFollowMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.service.UserService;
import com.campus.forum.service.UserFollowService;
import com.campus.forum.utils.RedisUtils;
import com.campus.forum.vo.UserDetailVO;
import com.campus.forum.vo.UserListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 
 * 实现用户相关的业务逻辑：
 * - 用户信息CRUD
 * - 密码加密处理
 * - 用户统计信息维护
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final UserFollowMapper userFollowMapper;
    private final UserFollowService userFollowService;
    private final RedisUtils redisUtils;

    @Override
    public PageResult<UserListVO> getUserList(UserQueryDTO queryDTO) {
        log.info("查询用户列表，条件：{}", queryDTO);
        
        // 构建分页对象
        Page<User> page = queryDTO.toPage();
        
        // 查询用户列表
        IPage<User> userPage = userMapper.selectUserPage(page, queryDTO.getKeyword(), queryDTO.getStatus());
        
        // 转换为VO
        List<UserListVO> voList = userPage.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());
        
        return PageResult.build(userPage.getCurrent(), userPage.getSize(), userPage.getTotal(), voList);
    }

    @Override
    public UserDetailVO getUserDetail(Long id, Long currentUserId) {
        log.info("获取用户详情，用户ID：{}，当前用户ID：{}", id, currentUserId);
        
        // 查询用户
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 转换为VO
        UserDetailVO vo = convertToDetailVO(user);
        
        // 判断是否为本人查询
        boolean isSelf = currentUserId != null && currentUserId.equals(id);
        
        if (isSelf) {
            // 本人查询，返回完整信息
            vo.setFollowed(false);
        } else {
            // 非本人查询，对敏感信息进行脱敏处理
            vo.setEmail(maskEmail(vo.getEmail()));
            vo.setPhone(maskPhone(vo.getPhone()));
            vo.setStudentNo(maskStudentNo(vo.getStudentNo()));
            
            // 判断是否已关注
            if (currentUserId != null) {
                vo.setFollowed(userFollowService.isFollowing(currentUserId, id));
            } else {
                vo.setFollowed(false);
            }
        }
        
        return vo;
    }
    
    /**
     * 邮箱脱敏处理
     * 保留前缀的前几个字符，中间用*替代，保留@后的域名
     *
     * @param email 原始邮箱
     * @return 脱敏后的邮箱
     */
    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }
        String prefix = email.substring(0, atIndex);
        String suffix = email.substring(atIndex);
        if (prefix.length() <= 2) {
            return prefix.charAt(0) + "***" + suffix;
        }
        return prefix.substring(0, 2) + "***" + suffix;
    }
    
    /**
     * 手机号脱敏处理
     * 保留前3位和后4位，中间用*替代
     *
     * @param phone 原始手机号
     * @return 脱敏后的手机号
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        if (phone.length() <= 7) {
            return phone.substring(0, 3) + "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
    
    /**
     * 学号脱敏处理
     * 保留前2位和后2位，中间用*替代
     *
     * @param studentNo 原始学号
     * @return 脱敏后的学号
     */
    private String maskStudentNo(String studentNo) {
        if (studentNo == null || studentNo.isEmpty()) {
            return studentNo;
        }
        if (studentNo.length() <= 4) {
            return studentNo.substring(0, 2) + "**";
        }
        return studentNo.substring(0, 2) + "****" + studentNo.substring(studentNo.length() - 2);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserInfo(Long id, UserUpdateDTO updateDTO) {
        log.info("更新用户信息，用户ID：{}，更新内容：{}", id, updateDTO);
        
        // 检查用户是否存在
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 检查用户状态，被封禁用户不能修改信息
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "账户已被禁用，无法修改信息");
        }
        
        // 检查邮箱是否已被其他用户使用
        if (StringUtils.isNotBlank(updateDTO.getEmail()) && !updateDTO.getEmail().equals(user.getEmail())) {
            User existUser = getByEmail(updateDTO.getEmail());
            if (existUser != null) {
                throw new BusinessException(ResultCode.EMAIL_EXISTS);
            }
        }
        
        // 检查手机号是否已被其他用户使用
        if (StringUtils.isNotBlank(updateDTO.getPhone()) && !updateDTO.getPhone().equals(user.getPhone())) {
            User existUser = getByPhone(updateDTO.getPhone());
            if (existUser != null) {
                throw new BusinessException(ResultCode.PHONE_EXISTS);
            }
        }
        
        // 复制属性
        BeanUtil.copyProperties(updateDTO, user, "id");
        
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAvatar(Long id, String avatar) {
        log.info("更新用户头像，用户ID：{}，头像URL：{}", id, avatar);
        
        // 检查用户是否存在
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 修复：检查用户状态，被封禁用户不能修改头像
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "账户已被禁用，无法修改头像");
        }
        
        return userMapper.updateAvatar(id, avatar) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long id, PasswordUpdateDTO passwordDTO) {
        log.info("修改用户密码，用户ID：{}", id);
        
        // 检查用户是否存在
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 【修复】检查用户状态，被封禁用户不能修改密码
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "账户已被禁用，无法修改密码");
        }
        
        // 验证原密码
        if (!BCrypt.checkpw(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        
        // 验证密码强度：长度必须在6-20位之间
        String newPassword = passwordDTO.getNewPassword();
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 20) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "密码长度必须在6-20位之间");
        }
        
        // 验证密码强度：必须包含字母和数字
        if (!newPassword.matches("^(?=.*[a-zA-Z])(?=.*\\d).+$")) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "密码必须包含字母和数字");
        }
        
        // 【修复】检查新密码是否与原密码相同
        if (BCrypt.checkpw(newPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "新密码不能与原密码相同");
        }
        
        // 验证两次密码是否一致
        if (!newPassword.equals(passwordDTO.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_NOT_MATCH);
        }
        
        // 加密新密码
        String encodedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        
        int result = userMapper.updatePassword(id, encodedPassword);
        if (result > 0) {
            // 密码修改成功后，清除该用户所有的Token，强制重新登录
            invalidateUserTokens(id);
            log.info("密码修改成功，已清除用户 {} 的所有登录状态", id);
            return true;
        }
        return false;
    }
    
    /**
     * 使指定用户的所有Token失效
     * 密码修改后调用此方法强制用户重新登录
     * 
     * 【安全修复】Token清除失败时抛出异常，确保事务回滚
     * 如果Token清除失败，密码修改操作应该回滚，避免用户密码已修改但旧Token仍然有效的情况
     *
     * @param userId 用户ID
     * @throws BusinessException 当Token清除失败时抛出
     */
    private void invalidateUserTokens(Long userId) {
        try {
            // 删除用户的主Token
            String tokenKey = "token:user:" + userId;
            redisUtils.del(tokenKey);
            
            // 删除用户所有相关的Token（使用模式匹配）
            String tokenPattern = "token:user:" + userId + ":*";
            redisUtils.deleteByPattern(tokenPattern);
            
            // 将用户ID添加到密码修改时间戳，用于JWT验证时检查Token是否在密码修改前签发
            String passwordUpdateKey = "password:update:time:" + userId;
            redisUtils.set(passwordUpdateKey, System.currentTimeMillis(), 86400 * 30); // 保存30天
            
            log.info("已清除用户 {} 的所有登录Token", userId);
        } catch (Exception e) {
            log.error("清除用户Token失败，用户ID: {}", userId, e);
            // 【安全修复】Token清除失败时抛出业务异常，让事务回滚
            // 用户需要重试密码修改操作
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "密码修改成功但Token清除失败，请重新登录。如问题持续，请联系管理员。");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        log.info("更新用户状态，用户ID：{}，状态：{}", id, status);
        
        // 检查用户是否存在
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        return userMapper.updateStatus(id, status) > 0;
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public User getByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public User getByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public void incrementPostCount(Long userId) {
        userMapper.incrementPostCount(userId);
    }

    @Override
    public void decrementPostCount(Long userId) {
        userMapper.decrementPostCount(userId);
    }

    @Override
    public void incrementCommentCount(Long userId) {
        userMapper.incrementCommentCount(userId);
    }

    @Override
    public void decrementCommentCount(Long userId) {
        userMapper.decrementCommentCount(userId);
    }

    @Override
    public void updateLoginInfo(Long userId, String loginIp) {
        userMapper.updateLoginInfo(userId, loginIp);
    }

    @Override
    public UserDetailVO getCurrentUser(Long userId) {
        log.info("获取当前登录用户信息，用户ID：{}", userId);
        
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        return convertToDetailVO(user);
    }

    /**
     * 转换为列表VO
     *
     * @param user 用户实体
     * @return 列表VO
     */
    private UserListVO convertToListVO(User user) {
        UserListVO vo = new UserListVO();
        BeanUtil.copyProperties(user, vo);
        
        // 设置性别描述
        vo.setGenderDesc(getGenderDesc(user.getGender()));
        // 设置状态描述
        vo.setStatusDesc(getStatusDesc(user.getStatus()));
        
        return vo;
    }

    /**
     * 转换为详情VO
     *
     * @param user 用户实体
     * @return 详情VO
     */
    private UserDetailVO convertToDetailVO(User user) {
        UserDetailVO vo = new UserDetailVO();
        BeanUtil.copyProperties(user, vo);
        
        // 设置性别描述
        vo.setGenderDesc(getGenderDesc(user.getGender()));
        // 设置状态描述
        vo.setStatusDesc(getStatusDesc(user.getStatus()));
        
        return vo;
    }

    /**
     * 获取性别描述
     *
     * @param gender 性别
     * @return 性别描述
     */
    private String getGenderDesc(Integer gender) {
        if (gender == null) {
            return "未知";
        }
        switch (gender) {
            case 1:
                return "男";
            case 2:
                return "女";
            default:
                return "未知";
        }
    }

    /**
     * 获取状态描述
     *
     * @param status 状态
     * @return 状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        return status == 1 ? "正常" : "禁用";
    }
}
