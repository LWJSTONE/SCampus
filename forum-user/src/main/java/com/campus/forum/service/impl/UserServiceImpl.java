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
        
        // 判断是否已关注
        if (currentUserId != null && !currentUserId.equals(id)) {
            vo.setFollowed(userFollowService.isFollowing(currentUserId, id));
        } else {
            vo.setFollowed(false);
        }
        
        return vo;
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
        
        // 验证原密码
        if (!BCrypt.checkpw(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        
        // 验证密码强度：长度必须在6-20位之间
        String newPassword = passwordDTO.getNewPassword();
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 20) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "密码长度必须在6-20位之间");
        }
        
        // 验证两次密码是否一致
        if (!newPassword.equals(passwordDTO.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_NOT_MATCH);
        }
        
        // 加密新密码
        String encodedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        
        return userMapper.updatePassword(id, encodedPassword) > 0;
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
