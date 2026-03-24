# 后端服务代码审查与修复日志

## 审查概述

**审查日期**: 2024年
**审查文件**:
1. `forum-auth/src/main/java/com/campus/forum/service/impl/AuthServiceImpl.java`
2. `forum-user/src/main/java/com/campus/forum/service/impl/UserServiceImpl.java`
3. `forum-post/src/main/java/com/campus/forum/service/impl/PostServiceImpl.java`

**审查重点**: 安全漏洞、业务逻辑错误、数据验证、异常处理、事务管理、并发处理、权限验证

---

## 一、AuthServiceImpl.java - 认证服务修复记录

### 1. 安全漏洞修复

#### 1.1 时序攻击防护
**问题**: 验证码比对使用普通字符串比较，可能遭受时序攻击推断验证码内容
**修复**: 实现常量时间比较方法 `constantTimeEquals()`，使用 `MessageDigest.isEqual()` 进行安全比对
```java
private boolean constantTimeEquals(String str1, String str2) {
    if (str1 == null || str2 == null) {
        return str1 == str2;
    }
    byte[] bytes1 = str1.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    byte[] bytes2 = str2.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    return MessageDigest.isEqual(bytes1, bytes2);
}
```
**影响范围**: 登录验证码、注册邮箱验证码、密码重置验证码

#### 1.2 日志脱敏处理
**问题**: 日志中记录完整用户名和邮箱，存在敏感信息泄露风险
**修复**: 对日志输出进行脱敏处理
```java
log.info("用户登录请求：username={}***", 
        loginDTO.getUsername() != null && loginDTO.getUsername().length() > 2 
                ? loginDTO.getUsername().substring(0, 2) : "**");
```
**影响范围**: login()、register()、resetPassword() 方法

#### 1.3 密码强度校验增强
**问题**: 原密码校验仅检查长度，安全性不足
**修复**: 增加密码复杂度要求，必须包含字母和数字
```java
if (!password.matches("^(?=.*[a-zA-Z])(?=.*\\d).+$")) {
    return Result.fail(400, "密码必须包含字母和数字");
}
```

#### 1.4 Token黑名单机制
**问题**: Token刷新后旧Token可能被重复使用
**修复**: 
- 刷新Token时将旧Access Token加入黑名单
- 密码重置后使所有已发出Token失效
```java
if (StrUtil.isNotBlank(oldAccessToken)) {
    String oldAccessBlacklistKey = Constants.CACHE_PREFIX + "token:blacklist:" + oldAccessToken;
    long ttl = JwtUtils.getDefaultExpirationSeconds();
    redisUtils.set(oldAccessBlacklistKey, "1", ttl);
}
```

#### 1.5 验证码防重放
**问题**: 验证码可被多次使用
**修复**: 验证码使用后立即删除
```java
// 删除已使用的验证码
redisUtils.del(emailCodeKey);
```

### 2. 并发处理修复

#### 2.1 登录失败锁定 - 分布式锁
**问题**: 并发登录失败可能导致计数不准确
**修复**: 使用Redis分布式锁保证原子性操作
```java
String lockKey = Constants.CACHE_PREFIX + "login:fail:lock:" + userId;
try {
    boolean locked = redisUtils.setIfAbsent(lockKey, "1", 10);
    if (!locked) {
        log.warn("获取登录失败锁失败，可能有并发请求：userId={}", userId);
        return;
    }
    // 原子操作增加失败次数并检查是否需要锁定
    int updatedRows = authUserMapper.incrementLoginFailCountAndLockIfNeeded(...);
} finally {
    redisUtils.del(lockKey);
}
```

### 3. 业务逻辑修复

#### 3.1 密码重置安全增强
**问题**: 密码重置后登录失败计数未清除，用户可能仍被锁定
**修复**: 密码重置成功后清除登录失败计数和锁定状态
```java
authUserMapper.resetLoginFailCount(user.getId());
```

#### 3.2 邮箱验证码频率限制
**问题**: 验证码可无限发送，存在滥用风险
**修复**: 
- 60秒内只能发送一次
- 每日最多发送10次
```java
private static final int MAX_DAILY_EMAIL_COUNT = 10;
// 检查发送频率限制（60秒内只能发送一次）
if (redisUtils.hasKey(rateLimitKey)) {
    return Result.fail(429, "发送过于频繁，请稍后再试");
}
// 检查每日发送次数限制
if (dailyCount >= MAX_DAILY_EMAIL_COUNT) {
    return Result.fail(429, "今日发送次数已达上限，请明天再试");
}
```

---

## 二、UserServiceImpl.java - 用户服务修复记录

### 1. 权限验证修复

#### 1.1 用户状态检查
**问题**: 被禁用用户仍可修改头像、密码
**修复**: 在 updateAvatar() 和 updatePassword() 方法中添加用户状态检查
```java
if (user.getStatus() == null || user.getStatus() != 1) {
    throw new BusinessException(ResultCode.BUSINESS_ERROR, "账户已被禁用，无法修改信息");
}
```

### 2. 业务逻辑修复

#### 2.1 密码修改安全检查
**问题**: 新密码可与原密码相同
**修复**: 添加新密码与原密码比对检查
```java
if (BCrypt.checkpw(newPassword, user.getPassword())) {
    throw new BusinessException(ResultCode.BUSINESS_ERROR, "新密码不能与原密码相同");
}
```

### 3. 事务管理修复

#### 3.1 Token清除失败处理
**问题**: Token清除失败时事务不回滚，可能导致密码已修改但旧Token仍有效
**修复**: Token清除失败时抛出异常，让事务回滚
```java
private void invalidateUserTokens(Long userId) {
    try {
        // Token清除逻辑...
    } catch (Exception e) {
        log.error("清除用户Token失败，用户ID: {}", userId, e);
        throw new BusinessException(ResultCode.BUSINESS_ERROR, 
            "密码修改成功但Token清除失败，请重新登录。如问题持续，请联系管理员。");
    }
}
```

---

## 三、PostServiceImpl.java - 帖子服务修复记录

### 1. 安全漏洞修复

#### 1.1 Service层权限验证
**问题**: 置顶/加精操作仅依赖Controller层验证，存在Header伪造风险
**修复**: 在Service层添加管理员权限验证
```java
@Override
@Transactional(rollbackFor = Exception.class)
public boolean setTop(Long id, Integer isTop, Long operatorId, boolean isAdmin) {
    if (!isAdmin) {
        log.warn("置顶操作权限校验失败: 非管理员尝试置顶帖子, operatorId: {}, postId: {}", operatorId, id);
        throw new BusinessException(ResultCode.FORBIDDEN, "无权限执行置顶操作，需要管理员权限");
    }
    // ...
}
```

#### 1.2 帖子类型范围校验
**问题**: 帖子类型参数未校验，可能传入非法值
**修复**: 添加帖子类型范围校验（0-3）
```java
if (updateDTO.getType() != null && (updateDTO.getType() < 0 || updateDTO.getType() > 3)) {
    throw new BusinessException(ResultCode.PARAM_ERROR, "帖子类型无效，有效范围：0-3");
}
```

### 2. 业务逻辑修复

#### 2.1 浏览量防刷机制
**问题**: 浏览量可被恶意刷取
**修复**: 实现防刷机制，同一用户/IP在24小时内只计一次浏览
```java
@Override
public boolean incrementViewCountWithAntiSpam(Long id, Long userId, String ipAddress) {
    String viewedKey;
    String identifier;
    
    if (userId != null) {
        viewedKey = REDIS_KEY_POST_VIEWED_USER + id;
        identifier = userId.toString();
    } else if (ipAddress != null && !ipAddress.isEmpty()) {
        viewedKey = REDIS_KEY_POST_VIEWED_IP + id;
        identifier = ipAddress;
    } else {
        incrementViewCount(id);
        return true;
    }
    
    Boolean hasViewed = redisTemplate.opsForSet().isMember(viewedKey, identifier);
    if (Boolean.TRUE.equals(hasViewed)) {
        return false; // 重复浏览被过滤
    }
    
    redisTemplate.opsForSet().add(viewedKey, identifier);
    // 设置过期时间
    redisTemplate.expire(viewedKey, Duration.ofHours(VIEW_ANTI_SPAM_HOURS));
    
    incrementViewCount(id);
    return true;
}
```

#### 2.2 Redis缓存过期时间
**问题**: 部分Redis key未设置过期时间，可能导致内存泄漏
**修复**: 为所有Redis key设置合理的过期时间
```java
String countKey = REDIS_KEY_POST_COUNT + userId;
Long newCount = redisTemplate.opsForValue().increment(countKey);
if (newCount != null && newCount == 1) {
    redisTemplate.expire(countKey, Duration.ofHours(24));
}
```

#### 2.3 帖子删除计数同步
**问题**: 帖子删除时Redis缓存不存在会导致计数不同步
**修复**: 缓存不存在时从数据库重新获取并设置缓存
```java
String countKey = REDIS_KEY_POST_COUNT + post.getUserId();
String count = redisTemplate.opsForValue().get(countKey);
if (count != null) {
    long currentCount = Long.parseLong(count);
    if (currentCount > 0) {
        redisTemplate.opsForValue().decrement(countKey);
    }
} else {
    // 缓存不存在时，从数据库重新获取
    int actualCount = postMapper.countByUserId(post.getUserId());
    redisTemplate.opsForValue().set(countKey, String.valueOf(actualCount), Duration.ofHours(24));
}
```

---

## 四、安全修复总结

| 类别 | 修复项 | 严重程度 | 状态 |
|------|--------|----------|------|
| 时序攻击 | 验证码常量时间比较 | 高 | ✅ 已修复 |
| 信息泄露 | 日志脱敏处理 | 中 | ✅ 已修复 |
| 认证安全 | Token黑名单机制 | 高 | ✅ 已修复 |
| 密码安全 | 密码强度校验增强 | 中 | ✅ 已修复 |
| 并发安全 | 登录失败分布式锁 | 高 | ✅ 已修复 |
| 权限验证 | Service层权限检查 | 高 | ✅ 已修复 |
| 防刷机制 | 浏览量防刷 | 中 | ✅ 已修复 |
| 事务管理 | Token清除失败回滚 | 高 | ✅ 已修复 |
| 缓存管理 | Redis key过期时间 | 低 | ✅ 已修复 |
| 频率限制 | 邮箱验证码限制 | 中 | ✅ 已修复 |

---

## 五、后续建议

1. **XSS防护增强**: 当前使用基本字符转义，建议集成专业XSS过滤库（如OWASP AntiSamy）
2. **SQL审计**: 建议对复杂SQL语句进行审计，确保没有SQL注入风险
3. **分布式锁优化**: 考虑使用Redis Lua脚本实现更安全的分布式锁释放机制
4. **日志审计**: 建议增加关键操作的审计日志记录
5. **配置安全**: 生产环境需要更换默认的JWT密钥配置

---

## 六、前端页面审查与修复记录

### 审查日期: 2024年
### 审查文件:
1. `forum-web/src/views/Home.vue`
2. `forum-web/src/views/UserProfile.vue`
3. `forum-web/src/views/UserSettings.vue`
4. `forum-web/src/views/CreatePost.vue`
5. `forum-web/src/views/Notifications.vue`
6. `forum-web/src/layouts/DefaultLayout.vue`

### 审查重点: 按钮点击事件、表单验证、API调用、错误处理、安全漏洞、状态管理、分页逻辑

---

### 1. Home.vue - 首页帖子列表

#### 1.1 分页逻辑修复
**问题**: 分页字段名不兼容，导致分页失效；加载更多时无防重复触发机制
**修复**: 
- 兼容多种分页字段名 (`current/pageNum/page`, `pages/totalPages/total`)
- 添加 `loadMoreTriggered` 状态防止重复触发
- 添加 `initialLoading` 状态区分首次加载
- 请求失败时回滚页码

#### 1.2 空数据处理
**问题**: 无数据时无友好提示
**修复**: 添加 `<el-empty>` 组件展示空状态

#### 1.3 数据处理增强
**问题**: `res.records` 可能为 undefined 导致错误
**修复**: 使用 `(res.records || [])` 安全访问

---

### 2. UserProfile.vue - 用户主页

#### 2.1 参数验证时机修复
**问题**: 参数验证在 setup 顶层执行，组件未完全初始化就会调用 `router.push`
**修复**: 将参数验证逻辑移到 `onMounted` 钩子中，使用 `validateUserId()` 函数

#### 2.2 路由错误处理
**问题**: 直接使用 `$router.push` 无错误捕获，可能导致未处理的Promise错误
**修复**: 
- 添加 `navigateToPost()` 函数统一处理路由跳转
- 所有 `router.push` 调用添加 `.catch()` 处理

#### 2.3 加载状态
**问题**: 无加载状态指示器，用户体验差
**修复**: 
- 添加 `loading` 和 `postsLoading` 状态
- 使用 `<el-skeleton>` 展示加载状态

#### 2.4 收藏权限控制
**问题**: 收藏标签页对所有用户可见，实际只有用户本人可查看
**修复**: 收藏标签页添加 `v-if="isOwnProfile"` 条件渲染

---

### 3. UserSettings.vue - 用户设置页

#### 3.1 表单类型修复
**问题**: `UserUpdateDTO & { avatar?: string }` 类型定义冗余
**修复**: 使用 `Partial<UserUpdateDTO>` 简化类型定义

#### 3.2 头像上传限制
**问题**: 2MB限制对部分用户场景过小
**修复**: 将头像大小限制从2MB提升到5MB

#### 3.3 密码修改后自动登出
**问题**: 密码修改成功后未强制重新登录
**修复**: 
- 修改成功提示"请重新登录"
- 1.5秒后自动登出并跳转登录页

#### 3.4 登录状态检查
**问题**: 未登录用户可直接访问设置页
**修复**: 在 `onMounted` 中添加登录状态检查，未登录时重定向到登录页

#### 3.5 防重复提交
**问题**: 保存按钮可重复点击
**修复**: 在 `handleSave` 和 `handleChangePwd` 函数开头添加防重复检查

---

### 4. CreatePost.vue - 发布帖子页

#### 4.1 取消确认机制
**问题**: 点击取消直接返回，未保存内容会丢失
**修复**: 
- 添加 `handleCancel()` 函数，表单有内容时弹出确认对话框
- 使用 `onBeforeRouteLeave` 路由守卫防止意外离开

#### 4.2 表单变化追踪
**问题**: 无法判断表单是否有未保存内容
**修复**: 
- 添加 `hasUnsavedChanges` 状态
- 监听表单输入变化

#### 4.3 防重复提交
**问题**: 发布按钮可重复点击
**修复**: 在 `handleSubmit` 函数开头添加防重复检查

---

### 5. Notifications.vue - 消息通知页

#### 5.1 登录状态检查
**问题**: 未登录用户可直接访问通知页
**修复**: 在 `onMounted` 和 `fetchNotifications` 中添加登录状态检查

#### 5.2 API错误回滚
**问题**: 标记已读API失败后，UI状态已更新但未回滚
**修复**: 
- 使用乐观更新策略，先更新UI再调用API
- API失败时回滚到原始状态

#### 5.3 分页逻辑
**问题**: 一次性加载100条通知，无分页机制
**修复**: 
- 添加分页参数 `page` 和 `size`
- 支持"加载更多"功能
- 添加 `hasMore` 判断是否还有更多数据

#### 5.4 刷新功能
**问题**: 无刷新按钮
**修复**: 添加刷新按钮，支持重新加载通知列表

#### 5.5 全部已读优化
**问题**: 点击"全部已读"时无未读提示，且可重复点击
**修复**: 
- 检查是否有未读通知，无则提示
- 添加 `markAllLoading` 防止重复点击

---

### 6. DefaultLayout.vue - 默认布局

#### 6.1 登出错误处理
**问题**: `ElMessageBox.confirm` 使用 `.then()` 未处理错误情况
**修复**: 
- 使用 `async/await` 重构 `handleLogout()` 函数
- 使用 `try/catch` 处理用户取消操作

#### 6.2 搜索XSS防护
**问题**: 搜索关键词未过滤，可能存在XSS风险
**修复**: 
- 对搜索关键词进行HTML实体编码
- 过滤 `<`, `>`, `"`, `'` 等特殊字符

#### 6.3 未读消息定时刷新
**问题**: 未读消息数不会自动更新
**修复**: 
- 添加 `startUnreadPolling()` 定时刷新（每分钟）
- 添加 `stopUnreadPolling()` 在组件销毁时清除定时器
- 使用 `onUnmounted` 清理资源

#### 6.4 发帖按钮权限
**问题**: 未登录用户点击发帖按钮会跳转到空白页
**修复**: 
- 添加 `handleCreatePost()` 函数
- 未登录时提示并跳转登录页

#### 6.5 路由错误处理
**问题**: 所有 `router.push` 调用无错误捕获
**修复**: 所有路由跳转添加 `.catch(() => {})` 防止未处理的Promise错误

#### 6.6 导航栏固定
**问题**: 滚动页面后导航栏不可见
**修复**: 添加 `position: sticky; top: 0; z-index: 100` 使导航栏固定在顶部

---

### 前端修复总结

| 文件 | 修复项 | 严重程度 | 状态 |
|------|--------|----------|------|
| Home.vue | 分页逻辑兼容 | 高 | ✅ 已修复 |
| Home.vue | 防重复加载 | 中 | ✅ 已修复 |
| Home.vue | 空数据提示 | 低 | ✅ 已修复 |
| UserProfile.vue | 参数验证时机 | 高 | ✅ 已修复 |
| UserProfile.vue | 路由错误处理 | 中 | ✅ 已修复 |
| UserProfile.vue | 加载状态 | 低 | ✅ 已修复 |
| UserProfile.vue | 收藏权限控制 | 中 | ✅ 已修复 |
| UserSettings.vue | 密码修改后登出 | 高 | ✅ 已修复 |
| UserSettings.vue | 登录状态检查 | 高 | ✅ 已修复 |
| UserSettings.vue | 防重复提交 | 中 | ✅ 已修复 |
| CreatePost.vue | 取消确认机制 | 高 | ✅ 已修复 |
| CreatePost.vue | 路由守卫 | 中 | ✅ 已修复 |
| CreatePost.vue | 防重复提交 | 中 | ✅ 已修复 |
| Notifications.vue | 登录状态检查 | 高 | ✅ 已修复 |
| Notifications.vue | API错误回滚 | 高 | ✅ 已修复 |
| Notifications.vue | 分页加载 | 中 | ✅ 已修复 |
| DefaultLayout.vue | 登出错误处理 | 中 | ✅ 已修复 |
| DefaultLayout.vue | XSS防护 | 高 | ✅ 已修复 |
| DefaultLayout.vue | 定时刷新未读 | 中 | ✅ 已修复 |
| DefaultLayout.vue | 发帖按钮权限 | 中 | ✅ 已修复 |
| DefaultLayout.vue | 导航栏固定 | 低 | ✅ 已修复 |
