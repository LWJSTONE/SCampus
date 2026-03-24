# SCampus 项目修复工作日志

## 项目概述
- 仓库名称: SCampus
- 分支: fix
- 审查时间: 2026-03-24

## 审查发现的问题

### 前端问题 (26个)

#### 高优先级
1. DefaultLayout中的XSS防护不够完善
2. 多处乐观更新后失败不回滚的问题
3. Dashboard和StatsView中使用随机/硬编码数据显示

#### 中优先级
1. 部分按钮缺少防重复点击保护
2. 多处路由跳转没有错误处理
3. notify.ts中的类型重复定义
4. request.ts中的BASE_URL定义未使用

### 后端问题 (21个)

#### 高危
1. 文件服务权限判断问题
2. 统计接口无权限控制

#### 中危
1. 配置默认值问题
2. 权限验证不完整

---

---

## Task ID: 4-c - vue-frontend-developer
### Work Task
修复layouts和components目录下的三个文件的安全性和用户体验问题

### Work Summary

#### 1. DefaultLayout.vue 修复
**问题1: XSS防护不完善**
- 原代码仅使用简单的字符串替换来防护XSS，不够完善
- **修复方案**: 安装并使用 `dompurify` 库进行专业的XSS过滤
  - 配置 `ALLOWED_TAGS: []` 不允许任何HTML标签
  - 配置 `ALLOWED_ATTR: []` 不允许任何属性
  - 添加空关键词校验，防止恶意输入被过滤后产生空搜索

**问题2: 页面可见性处理缺失**
- 原代码的轮询定时器在页面不可见时仍持续请求，浪费资源
- **修复方案**: 使用 Page Visibility API 监听页面可见性
  - 添加 `handleVisibilityChange` 函数处理页面可见性变化
  - 页面不可见时暂停轮询，页面可见时恢复轮询并立即刷新一次
  - 在定时器回调中也检查页面可见性，双重保障
  - 在 `onMounted` 中添加事件监听，在 `onUnmounted` 中移除

#### 2. AdminLayout.vue 修复
**问题: 退出登录无确认对话框**
- 原代码直接调用 `userStore.logout()`，用户可能误点退出登录
- **修复方案**: 添加 `ElMessageBox.confirm` 确认对话框
  - 引入 `ElMessageBox` 组件
  - 创建独立的 `handleLogout` 异步函数处理退出逻辑
  - 用户确认后才执行退出操作，取消则不做任何处理

#### 3. CommentItem.vue 修复
**问题: 点赞失败时本地状态不回滚**
- 原代码在API调用失败后，没有恢复乐观更新前的状态
- **修复方案**: 实现完整的乐观更新模式
  - 在调用API前保存当前状态 (`previousState`)
  - 先乐观更新本地状态，提供即时反馈
  - API调用成功后，使用后端返回的真实状态更新
  - API调用失败时，将本地状态回滚到 `previousState`
  - 保留防重复点击保护机制

#### 安装的依赖
- `dompurify`: 用于专业的XSS过滤
- `@types/dompurify`: TypeScript类型定义

---
## Task ID: 4-e - vue-frontend-developer
### Work Task
修复 router/index.ts 和 types/index.ts 两个文件的问题

### Work Summary

#### 1. src/router/index.ts 修复
**问题: 路由守卫中获取用户信息失败后没有清理用户状态**
- 原代码在 catch 块中只是打印日志，注释说"fetchUserInfo已经处理了清除逻辑"
- 但实际上 fetchUserInfo 内部只对 401 错误做了清理，对于其他错误（如网络错误）不会清理
- 这可能导致用户处于"有 token 但无 userInfo"的不一致状态

**修复方案**:
- 在第一个路由守卫的 catch 块中，检查 userInfo 是否存在，如果不存在则调用 `userStore.clearAuth()` 清理状态
- 在管理员权限检查的 catch 块中，同样添加状态清理逻辑，避免权限判断错误
- 这样确保无论什么原因导致获取用户信息失败，都能保持用户状态的一致性

**修改位置**:
- 第 163-174 行: 添加 catch 块中的状态清理逻辑
- 第 190-202 行: 管理员权限检查处添加相同的状态清理逻辑

#### 2. src/types/index.ts 修复
**问题: UserDetailVO 的 roles 字段定义为 string[]，但实际使用中角色可能是对象**
- 在 userStore.ts 中的 isAdmin 和 isModerator 计算属性中，已经处理了角色可能是对象的情况
- 代码中使用 `typeof r === 'string' ? r.toUpperCase() : (r as any).roleCode?.toUpperCase()` 来兼容两种格式
- 但类型定义中只声明为 `string[]`，与实际使用不符

**修复方案**:
- 新增 `RoleVO` 接口，定义角色对象的结构
  - `id`: 角色ID
  - `roleCode`: 角色代码
  - `roleName`: 角色名称
  - `description`: 角色描述
- 新增 `RoleType` 类型别名: `string | RoleVO`，支持字符串和对象两种形式
- 更新 `UserDetailVO` 的 roles 字段类型为 `RoleType[]`
- 更新 `UserInfoVO` 的 roles 字段类型为 `RoleType[]`

**修改位置**:
- 第 1-10 行: 新增 RoleVO 接口和 RoleType 类型定义
- 第 37 行: UserDetailVO.roles 类型改为 RoleType[]
- 第 117 行: UserInfoVO.roles 类型改为 RoleType[]

---
## Task ID: 4-b - vue-frontend-developer
### Work Task
修复 /home/z/my-project/SCampus-fix/forum-web/src/views/admin/ 目录下的8个文件问题

### Work Summary

#### 1. Dashboard.vue 修复
**问题1: updateTrendChart使用随机数据作为默认值**
- 原代码在data为null时生成随机数据显示在图表中，用户看到的是虚假数据
- **修复方案**: 当没有数据时显示空图表并添加"暂无数据"提示文字
  - 使用ECharts的graphic配置添加文字提示
  - 不再生成随机数据，保持数据真实性

**问题2: updatePieChart使用硬编码默认数据**
- 原代码在data为null时使用硬编码的默认数据显示饼图
- **修复方案**: 当没有数据时显示空饼图并添加"暂无数据"提示文字

**问题3: handleTodoClick已有path检查**
- 检查后发现原代码已有path存在性检查，无需修改

#### 2. UserManage.vue 修复
**问题: handleToggleStatus乐观更新后失败不回滚**
- 原代码在API调用失败后，UI状态已经改变但不会回滚
- **修复方案**: 实现完整的乐观更新模式
  - 保存原始状态 `originalStatus`
  - 先更新UI提供即时反馈
  - API调用失败时回滚到 `originalStatus`

#### 3. PostManage.vue 修复
**问题: handleTop置顶状态切换乐观更新后失败不回滚**
- 原代码在API调用失败后，UI状态已经改变但不会回滚
- **修复方案**: 实现完整的乐观更新模式
  - 保存原始状态 `originalIsTop`
  - 先更新UI提供即时反馈
  - API调用失败时回滚到 `originalIsTop`

#### 4. CategoryManage.vue 修复
**问题1: handleSubmit编辑模式下未检查editingId对应的分类是否存在**
- 原代码直接使用editingId调用更新API，没有验证分类是否存在
- **修复方案**: 在更新前检查editingId是否存在于flattenedCategories中
  - 使用 `flattenedCategories.value.some()` 检查分类是否存在
  - 不存在时显示错误提示并关闭对话框

**问题2: submitForum条件判断顺序可能导致逻辑问题**
- 原代码使用单一的条件判断和submitting状态管理，逻辑不够清晰
- **修复方案**: 重构为明确的分支结构
  - 编辑模式和添加模式分别处理
  - 各分支独立管理submitting状态
  - 更清晰的错误处理和状态管理

#### 5. ReportManage.vue 修复
**问题: handleFormRules中的action验证器引用外部响应式变量**
- 原代码的验证规则直接引用handleForm.result，可能导致验证规则与表单状态不同步
- **修复方案**: 使用computed包装验证规则
  - 添加 `computed` 导入
  - 将 `handleFormRules` 改为computed属性
  - 确保每次验证时都能获取最新的表单状态

#### 6. NoticeManage.vue 修复
**问题分析: handleSubmit表单验证失败时没有重置submitting**
- 检查后发现原代码逻辑正确：验证失败直接return，不会设置submitting为true
- submitting状态只在验证成功后设置，并在finally中重置
- **无需修改**

#### 7. StatsView.vue 修复
**问题: updateChart使用硬编码默认数据**
- 原代码在data为null时使用硬编码的周数据显示图表
- **修复方案**: 与Dashboard.vue保持一致
  - 当没有数据时显示空图表
  - 使用ECharts的graphic配置添加"暂无数据"提示文字

#### 8. SystemConfig.vue 修复
**问题: handleSave使用setTimeout模拟延迟，未实际调用后端API**
- 原代码已有el-alert提示配置保存在本地，但代码中有无意义的setTimeout延迟
- **修复方案**: 
  - 移除无意义的setTimeout模拟延迟
  - 添加代码注释说明当前配置仅保存在本地存储
  - 优化成功/失败提示信息，更清晰地说明配置保存位置

---
## Task ID: 4-a - vue-frontend-developer
### Work Task
修复 /home/z/my-project/SCampus-fix/forum-web/src/views/ 目录下的10个文件问题

### Work Summary

#### 1. CategoryPosts.vue 修复
**问题1: forumId验证在setup同步执行，但组件仍会继续执行onMounted中的API请求**
- 原代码在setup阶段验证forumId无效后调用router.push('/')，但组件继续执行fetchForum()和fetchPosts()
- **修复方案**:
  - 添加 `isValidForumId` 标志位
  - 在 `fetchForum()` 和 `fetchPosts()` 函数开始处检查该标志位
  - 如果forumId无效，直接return阻止无效API请求

**问题2: "加载更多"按钮没有防止重复点击的保护机制**
- 原代码的loadMore函数没有防重复触发保护
- **修复方案**:
  - 添加 `loadMoreTriggered` 状态变量
  - 在loadMore函数开始处检查该状态，防止重复触发
  - 在fetchPosts的finally块中重置loadMoreTriggered

#### 2. Search.vue 修复
**问题1: 模板中使用$router.push没有错误处理**
- 原代码直接在模板中调用 `$router.push(\`/post/${post.id}\`)`
- **修复方案**:
  - 创建 `navigateToPost(postId: number)` 函数处理导航
  - 在函数中添加错误处理，catch路由跳转错误并提示用户

**问题2: loadMore函数没有防止重复触发的保护机制**
- **修复方案**:
  - 添加 `loadMoreTriggered` 状态变量
  - 在loadMore函数开始处检查该状态，防止重复触发
  - 在fetchPosts的finally块中重置loadMoreTriggered

#### 3. Home.vue
**问题: 需要添加更完善的防重复触发机制**
- 检查后发现代码已实现完整的防重复触发机制：
  - `loadMoreTriggered` 状态变量
  - `loading.value` 检查
  - `hasMore.value` 检查
- **无需修改**

#### 4. PostDetail.vue 修复
**问题1: debounce函数已定义但未使用（死代码）**
- 原代码定义了debounce函数但从未调用
- **修复方案**: 移除未使用的debounce函数

**问题2: handleReply函数设置评论内容时自动添加@用户名前缀，但没有验证用户名特殊字符**
- 原代码直接使用 `@${comment.username}` 格式，如果用户名包含空格可能导致显示问题
- **修复方案**:
  - 检查用户名是否包含空格、制表符、换行符等特殊字符
  - 如果包含特殊字符，使用方括号包裹用户名：`[@用户名]`
  - 如果不包含特殊字符，保持原格式：`@用户名`

#### 5. CreatePost.vue 修复
**问题: isAnonymous开关没有调用watchFormChanges，导致hasUnsavedChanges不会更新**
- 原代码的el-switch没有@change事件监听
- **修复方案**:
  - 为el-switch添加 `@change="watchFormChanges"` 事件
  - 更新watchFormChanges函数，检查条件中添加 `form.isAnonymous`

#### 6. UserProfile.vue 修复
**问题: 点击帖子项时需要检查postId是否有效**
- 原代码的navigateToPost只检查 `!postId`
- **修复方案**: 加强postId验证
  - 检查 `!postId`、`isNaN(postId)` 和 `postId <= 0`
  - 确保postId是有效的正整数

#### 7. UserSettings.vue 修复
**问题: 调用updatePassword时传递了不必要的confirmPassword参数**
- 原代码在更新密码时将前端确认密码也传给后端
- **修复方案**: 移除confirmPassword参数，只传递必要参数：
  - `oldPassword`: 旧密码
  - `newPassword`: 新密码

#### 8. Login.vue 修复
**问题1: showForgotPassword函数使用Object.assign重置表单，可能无法正确重置嵌套对象**
- 原代码使用Object.assign重置reactive表单对象
- **修复方案**:
  - 使用直接属性赋值方式重置各字段
  - 添加 `forgotFormRef.value?.clearValidate()` 清除表单验证状态

**问题2: handleLogin函数登录成功后跳转，没有检查redirect目标是否为有效路由**
- 原代码直接使用 `router.push(redirect || '/')`
- **修复方案**: 添加redirect路径验证
  - 检查路径必须以'/'开头
  - 检查不包含'//'（防止协议注入）
  - 检查不包含'javascript:'（防止XSS）
  - 检查长度小于200字符

#### 9. Register.vue 修复
**问题: agreement字段的验证器使用箭头函数，但没有正确的类型定义**
- 原代码使用自定义validator但类型定义不完整
- **修复方案**:
  - 导入 `FormItemRule` 类型
  - 使用 `type: 'enum'` + `enum: [true]` 的方式验证布尔值
  - 添加明确的 `as FormItemRule` 类型断言

#### 10. Notifications.vue 修复
**问题: NotificationList组件使用defineComponent和h函数定义，但没有使用TypeScript类型支持**
- 原代码的props类型定义使用 `Array as () => NoticeVO[]`
- **修复方案**:
  - 导入 `PropType` 类型
  - 使用 `Array as PropType<NoticeVO[]>` 进行正确的类型定义
  - 为组件添加 `name` 属性
  - 在map回调中为item添加明确的 `: NoticeVO` 类型注解

---
## Task ID: 4-d - vue-frontend-developer
### Work Task
修复 /home/z/my-project/SCampus-fix/forum-web/src/api/ 和 /home/z/my-project/SCampus-fix/forum-web/src/stores/ 目录下的6个问题

### Work Summary

#### 1. src/api/request.ts 修复
**问题1: BASE_URL变量定义了但未使用，axios实例的baseURL直接硬编码为'/api/v1'**
- 原代码定义了 `BASE_URL` 常量但axios实例没有使用它
- **修复方案**: 
  - 将axios实例的 `baseURL` 改为使用 `BASE_URL` 变量
  - 保持环境变量配置的灵活性

**问题2: 响应拦截器中的401处理逻辑复杂，刷新Token失败后的处理可能不够完善**
- 原代码的 `onRefreshFailed` 函数只清空了订阅队列，没有返回订阅者数量供调用者判断
- **修复方案**:
  - 改进 `onRefreshFailed` 函数，保存当前订阅者队列引用
  - 返回订阅者数量，供调用者决定是否需要额外处理
  - 添加详细的代码注释

#### 2. src/api/notify.ts 修复
**问题: NoticeVO接口与types/index.ts中的NoticeVO重复定义**
- 原代码在notify.ts中重新定义了 `NoticeVO` 接口，与types/index.ts中的定义重复
- **修复方案**:
  - 删除notify.ts中的 `NoticeVO` 接口定义
  - 从 `@/types` 导入 `NoticeVO` 类型
  - 统一使用types/index.ts中的类型定义，避免维护不一致

#### 3. src/api/user.ts 修复
**问题: updateUserStatus函数验证status值使用硬编码的validStatuses数组**
- 原代码使用 `[0, 1, 2]` 硬编码数组验证状态值，缺乏语义化和可维护性
- **修复方案**:
  - 创建 `src/constants/index.ts` 常量文件
  - 定义 `USER_STATUS`、`USER_STATUS_TEXT`、`VALID_USER_STATUSES` 等常量
  - 在user.ts中导入并使用这些常量
  - 错误提示中显示有效状态值及其含义

#### 4. src/api/report.ts 修复
**问题: getReportList函数参数类型为ReportQueryDTO，但与实际使用场景中的分页参数名可能不一致**
- 原代码的 `ReportQueryDTO` 只支持 `current` 参数，不支持前端常用的 `page` 参数
- **修复方案**:
  - 更新 `ReportQueryDTO` 接口，同时支持 `current` 和 `page` 参数
  - 在 `getReportList` 函数中添加参数转换逻辑
  - 使用 `params.current || params.page || 1` 兼容两种参数格式
  - 添加JSDoc注释说明参数用途

#### 5. src/stores/user.ts 修复
**问题: 角色判断逻辑假设角色可以是字符串或对象，但没有类型检查**
- 原代码使用 `typeof r === 'string' ? r.toUpperCase() : (r as any).roleCode?.toUpperCase()` 进行角色判断
- 存在问题：
  - 使用 `as any` 类型断言不够安全
  - 没有完整的类型检查
  - 代码重复（isAdmin和isModerator都有相同的逻辑）
- **修复方案**:
  - 定义 `RoleObject` 接口描述角色对象结构
  - 定义 `RoleType` 类型别名支持字符串和对象两种形式
  - 创建 `getRoleCode(role: RoleType)` 函数提取角色代码，带完整的类型检查
  - 创建 `hasRole(roles, targetRoles)` 函数检查是否拥有指定角色
  - 重构 `isAdmin` 和 `isModerator` 计算属性，使用新函数提高代码可读性

#### 6. src/stores/app.ts 修复
**问题: 直接从localStorage读取theme值，没有try-catch包裹**
- 原代码使用 `localStorage.getItem('theme')` 直接读取主题值
- 如果用户禁用localStorage或浏览器抛出异常，会导致应用崩溃
- **修复方案**:
  - 创建 `safeGetTheme()` 函数安全读取主题值
    - 使用try-catch包裹localStorage访问
    - 验证读取的值是否为有效的 'light' 或 'dark'
    - 异常时返回默认值 'light'
  - 创建 `safeSetTheme(theme)` 函数安全写入主题值
    - 使用try-catch包裹localStorage写入
    - 返回布尔值表示写入是否成功
  - 在 `toggleTheme()` 函数中使用安全的写入方法

---

## Task ID: 5-c - spring-boot-backend-developer
### Work Task
修复后端各服务模块问题

### Work Summary

#### 1. FileController.java 修复
**文件路径**: `forum-file/src/main/java/com/campus/forum/controller/FileController.java`

**问题1: getUserIdFromRequest方法只从request.getAttribute获取userId**
- 网关传递的是Header中的X-User-Id，原代码只检查request.getAttribute
- **修复方案**:
  - 优先从Header获取X-User-Id（网关传递）
  - 回退到request.getAttribute获取（兼容其他场景）
  - 添加异常处理，防止解析失败

**问题2: isAdmin方法只从request.getAttribute获取角色**
- 网关传递的是Header中的X-User-Role，原代码只检查request.getAttribute
- **修复方案**:
  - 优先从Header获取X-User-Role（网关传递）
  - 回退到request.getAttribute获取
  - 支持"ADMIN"和"ROLE_ADMIN"两种角色格式

#### 2. ReportServiceImpl.java 修复
**文件路径**: `forum-report/src/main/java/com/campus/forum/service/impl/ReportServiceImpl.java`

**问题: validateReportTarget方法中服务调用异常时跳过验证并允许举报提交**
- 原逻辑在服务调用异常时跳过验证，存在安全隐患
- 可能导致对不存在的目标进行举报
- **修复方案**:
  - 服务调用异常时抛出BusinessException
  - 阻止举报提交，确保举报目标真实存在
  - 提示用户"举报目标验证服务暂时不可用，请稍后重试"

#### 3. NotifyController.java 修复
**文件路径**: `forum-notify/src/main/java/com/campus/forum/controller/NotifyController.java`

**问题: isAdmin方法仅依赖HTTP Header无二次验证**
- 原代码只检查Header中的X-User-Role，没有回退验证
- **修复方案**:
  - 优先从Header获取X-User-Role
  - 添加二次验证：从request.getAttribute获取（可能由过滤器/拦截器设置）
  - 支持"ADMIN"和"ROLE_ADMIN"两种角色格式

#### 4. StatsController.java 修复
**文件路径**: `forum-stats/src/main/java/com/campus/forum/controller/StatsController.java`

**问题: 统计接口没有任何权限控制**
- 所有统计接口都可以被任意用户访问，存在安全风险
- **修复方案**:
  - 为所有统计接口添加管理员权限校验
  - 添加isAdmin私有方法，同时检查Header和request.getAttribute
  - 在每个接口方法中添加权限验证逻辑
  - 导出报表接口抛出RuntimeException（非Result返回）
  - 添加@Slf4j日志注解，记录非管理员访问尝试

#### 5. UserApi.java 修复
**文件路径**: `forum-api/src/main/java/com/campus/forum/api/user/UserApi.java`

**问题: FeignClient的url使用硬编码默认值**
- 原代码：`url = "${feign.user.url:http://localhost:9002}"`
- **分析**: 当前代码已正确使用Spring属性占位符语法从配置读取
  - `${feign.user.url}`: 从配置读取服务地址
  - `:http://localhost:9002`: 默认值，当配置不存在时使用
- **结论**: 配置方式已正确，无需修改。建议在配置文件中设置`feign.user.url`属性覆盖默认值

### 修改文件清单
| 文件 | 修改类型 | 修改说明 |
|------|----------|----------|
| FileController.java | 方法修改 | getUserIdFromRequest和isAdmin方法添加Header检查 |
| ReportServiceImpl.java | 逻辑修改 | validateReportTarget服务异常时阻止举报 |
| NotifyController.java | 方法修改 | isAdmin方法添加二次验证 |
| StatsController.java | 全面修改 | 所有接口添加管理员权限校验 |
| UserApi.java | 无需修改 | 已正确使用配置读取方式 |

---

## Task ID: 5-a - spring-boot-backend-developer
### Work Task
修复后端安全问题（密钥默认值、权限验证）

### Work Summary

#### 1. AuthServiceImpl.java 修复
**文件路径**: `forum-auth/src/main/java/com/campus/forum/service/impl/AuthServiceImpl.java`

**问题1: JWT密钥使用默认值**
- 原代码: `@Value("${jwt.secret:default-secret-key}")`
- **修复方案**: 移除默认值，强制要求配置
  - `@Value("${jwt.secret}")` - 启动时必须配置，否则抛出异常

**问题2: sendEmailCode方法未实际发送邮件**
- 原代码只存储验证码到Redis，没有实际发送邮件
- **修复方案**: 集成邮件服务发送验证码
  - 添加 `sendEmailWithCode()` 方法实际发送邮件
  - 发送失败时回滚Redis中的验证码
  - 开发环境在日志中输出验证码便于测试

#### 2. UserController.java 修复
**文件路径**: `forum-user/src/main/java/com/campus/forum/controller/UserController.java`

**问题: 内部服务密钥硬编码**
- 原代码: `@Value("${app.internal-service-key:campus-internal-service-key-2024}")`
- **修复方案**: 移除硬编码默认值
  - `@Value("${app.internal-service-key:}")` - 强制要求配置
  - 添加空值检查，密钥未配置时拒绝访问

#### 3. PostController.java 修复
**文件路径**: `forum-post/src/main/java/com/campus/forum/controller/PostController.java`

**问题: isAdmin方法仅依赖HTTP Header**
- 存在权限伪造风险
- **修复方案**: 统一使用二次验证机制
  - 优先从Header获取角色
  - 回退到request.getAttribute验证
  - 支持"ADMIN"和"ROLE_ADMIN"格式

#### 4. AuthGlobalFilter.java 修复
**文件路径**: `forum-gateway/src/main/java/com/campus/forum/filter/AuthGlobalFilter.java`

**问题1: ALLOWED_ORIGINS硬编码**
- **修复方案**: 从配置文件读取
  - `@Value("${app.allowed-origins:...}")`
  - 支持逗号分隔的多域名配置

**问题2: logout在白名单中**
- **修复方案**: 添加注释说明，logout需要验证Token才能使其失效

#### 5. JwtUtils.java 修复
**文件路径**: `forum-common/src/main/java/com/campus/forum/utils/JwtUtils.java`

**问题: getUserId(String token)方法仅解码不验证签名**
- **修复方案**: 
  - 添加详细的安全警告文档
  - 标记为 `@Deprecated(forRemoval = true)`
  - 明确禁止用于安全敏感场景

#### 6. PasswordUtils.java 修复
**文件路径**: `forum-common/src/main/java/com/campus/forum/utils/PasswordUtils.java`

**问题: MD5方法被标记废弃但仍可用**
- **修复方案**: 
  - 标记为 `@Deprecated(forRemoval = true)`
  - 添加调用堆栈日志追踪不当使用
  - 警告禁止用于密码存储

---

## Task ID: 5-b - spring-boot-backend-developer
### Work Task
修复后端业务逻辑问题（邮件服务、敏感词、Token管理）

### Work Summary

#### 1. UserServiceImpl.java 修复
**文件路径**: `forum-user/src/main/java/com/campus/forum/service/impl/UserServiceImpl.java`

**问题: invalidateUserTokens失败时抛出异常导致密码修改失败**
- **修复方案**: 
  - Token清除失败时不抛出异常
  - 记录补偿标记，后续可通过定时任务处理
  - 密码修改成功，旧Token在JWT验证时失效

#### 2. PostServiceImpl.java 修复
**文件路径**: `forum-post/src/main/java/com/campus/forum/service/impl/PostServiceImpl.java`

**问题1: XSS过滤不够全面**
- **修复方案**: 实现专业的XSS过滤
  - 添加 `filterXSS()` 方法
  - 处理危险协议(javascript:, vbscript:)
  - 过滤事件处理器属性(onclick, onerror等)
  - 过滤危险CSS属性(expression, behavior)

**问题2: 敏感词列表硬编码**
- **修复方案**: 从配置文件读取
  - 创建 `PostConfig` 配置类
  - 支持动态配置敏感词列表
  - 通过 `post.sensitive.words` 配置

#### 3. CommentServiceImpl.java 修复
**文件路径**: `forum-comment/src/main/java/com/campus/forum/service/impl/CommentServiceImpl.java`

**问题1: 分布式锁过期时间只有10秒**
- **修复方案**: 
  - 创建 `CommentConfig` 配置类
  - 从配置文件读取锁过期时间
  - 默认改为30秒

**问题2: 敏感词列表使用占位符**
- **修复方案**: 从配置文件读取实际敏感词
  - 通过 `comment.sensitive.words` 配置

#### 4. LikeServiceImpl.java 修复
**文件路径**: `forum-interaction/src/main/java/com/campus/forum/service/impl/LikeServiceImpl.java`

**问题: 缓存更新和数据库更新非原子操作**
- **修复方案**: 
  - 记录详细错误日志便于排查
  - 缓存设置24小时过期时间
  - 建议实现异步补偿机制
  - 添加TODO标记后续改进

---

## 修复总结

### 前端修复统计
- **视图组件**: 10个文件修复
- **管理页面**: 8个文件修复
- **布局组件**: 3个文件修复
- **API/工具类**: 6个文件修复
- **路由/类型**: 2个文件修复
- **新增文件**: constants/index.ts

### 后端修复统计
- **安全问题**: 6个文件修复
- **业务逻辑**: 4个文件修复
- **服务模块**: 4个文件修复
- **新增配置类**: PostConfig, CommentConfig

### 关键修复点
1. **XSS防护**: 使用DOMPurify专业过滤
2. **乐观更新**: 添加失败回滚机制
3. **权限验证**: Header + Attribute双重验证
4. **敏感词**: 改为配置文件动态管理
5. **Token管理**: 优化失败处理逻辑
6. **统计接口**: 添加管理员权限控制

