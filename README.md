# SCampus - 校园论坛微服务系统

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Spring%20Cloud-2021.0.8-blue" alt="Spring Cloud">
  <img src="https://img.shields.io/badge/JDK-1.8-green" alt="JDK">
  <img src="https://img.shields.io/badge/MySQL-8.0-blue" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-7.x-red" alt="Redis">
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License">
</p>

---

## 📖 目录

- [项目介绍](#项目介绍)
- [系统架构](#系统架构)
- [功能模块](#功能模块)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [API文档](#api文档)
- [配置说明](#配置说明)
- [部署说明](#部署说明)
- [默认账号](#默认账号)
- [许可证](#许可证)

## 📚 相关文档

| 文档 | 说明 |
|------|------|
| [本地部署零基础傻瓜式指南](./本地部署零基础傻瓜式指南.md) | 面向零基础用户的详细部署教程 |
| [系统使用说明书](./系统使用说明书.md) | 用户操作手册，包含所有功能使用说明 |
| [数据库设计文档](./数据库设计文档.md) | 完整的数据库表结构设计文档 |

---

## 项目介绍

### 项目名称

**SCampus** - 基于 Spring Cloud 的校园论坛微服务系统

### 项目描述

SCampus 是一个功能完善的校园论坛系统，采用微服务架构设计，实现了用户管理、帖子发布、评论互动、举报审核等核心功能。系统具备高可用、高并发、易扩展的特点，适用于高校校园社区建设。

### 技术栈概览

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 基础框架 |
| Spring Cloud | 2021.0.8 | 微服务框架 |
| MySQL | 8.0+ | 关系型数据库 |
| MyBatis Plus | 3.5.5 | ORM框架 |
| Druid | 1.2.21 | 数据库连接池 |
| Redis | 7.x | 缓存中间件 |
| Gateway | - | API网关 |
| JWT | 4.4.0 | 身份认证 |
| Knife4j | 4.3.0 | API文档 |
| MinIO | 8.5.7 | 对象存储 |
| Hutool | 5.8.25 | 工具类库 |
| Lombok | 1.18.30 | 代码简化 |

---

## 系统架构

### 架构图说明

```
                                    ┌─────────────────┐
                                    │    前端应用      │
                                    │   (Vue.js)      │
                                    └────────┬────────┘
                                             │
                                             ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                              API 网关层                                      │
│                         forum-gateway :8080                                 │
│            (路由转发 / 认证鉴权 / 限流熔断 / 日志记录)                          │
└────────────────────────────────────────────────────────────────────────────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
                    ▼                        ▼                        ▼
┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐
│   forum-auth :9001   │  │   forum-user :9002   │  │ forum-category :9003 │
│     认证服务          │  │     用户服务          │  │      版块服务         │
└──────────────────────┘  └──────────────────────┘  └──────────────────────┘
          │                        │                        │
          └────────────────────────┼────────────────────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    │              │              │
                    ▼              ▼              ▼
┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐
│   forum-post :9004   │  │ forum-comment :9005  │  │ forum-interaction    │
│     帖子服务          │  │     评论服务          │  │     :9006 互动服务    │
└──────────────────────┘  └──────────────────────┘  └──────────────────────┘
          │                        │                        │
          └────────────────────────┼────────────────────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    │              │              │
                    ▼              ▼              ▼
┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐
│  forum-report :9007  │  │  forum-stats :9008   │  │  forum-notify :9009  │
│     审核服务          │  │     统计服务          │  │      通知服务         │
└──────────────────────┘  └──────────────────────┘  └──────────────────────┘
                                   │
                                   ▼
                         ┌──────────────────────┐
                         │  forum-file :9010    │
                         │     文件服务          │
                         └──────────────────────┘

                    ┌─────────────────────────────────┐
                    │         基础设施层               │
                    │  ┌───────┐  ┌───────┐  ┌──────┐ │
                    │  │ Redis │  │ MySQL │  │MinIO │ │
                    │  └───────┘  └───────┘  └──────┘ │
                    └─────────────────────────────────┘
```

### 微服务列表

| 序号 | 服务名称 | 端口 | 功能描述 |
|:----:|----------|:----:|----------|
| 1 | forum-gateway | 8080 | API网关服务 - 统一入口、路由转发、认证鉴权、限流熔断 |
| 2 | forum-auth | 9001 | 认证服务 - 用户登录、注册、Token管理、验证码 |
| 3 | forum-user | 9002 | 用户服务 - 用户管理、用户关注、个人信息维护 |
| 4 | forum-category | 9003 | 版块服务 - 版块分类、版块管理、版主管理 |
| 5 | forum-post | 9004 | 帖子服务 - 帖子发布、编辑、删除、查询、审核 |
| 6 | forum-comment | 9005 | 评论服务 - 评论发布、回复、点赞、删除 |
| 7 | forum-interaction | 9006 | 互动服务 - 点赞、收藏、@提及功能 |
| 8 | forum-report | 9007 | 审核服务 - 举报处理、内容审核、用户禁言 |
| 9 | forum-stats | 9008 | 统计服务 - 数据统计、报表导出、趋势分析 |
| 10 | forum-notify | 9009 | 通知服务 - 系统通知、消息推送、公告管理 |
| 11 | forum-file | 9010 | 文件服务 - 文件上传、下载、管理 |

### 技术选型

#### 后端技术

| 技术 | 说明 |
|------|------|
| Spring Cloud Gateway | API网关，实现路由转发、限流熔断 |
| OpenFeign | 声明式HTTP客户端，服务间调用 |
| JWT | JSON Web Token，无状态身份认证 |
| MyBatis Plus | 增强版MyBatis，简化CRUD操作 |
| Druid | 阿里数据库连接池，提供监控功能 |
| Redis | 高性能缓存，支持分布式锁、消息队列 |

#### 数据存储

| 技术 | 说明 |
|------|------|
| MySQL 8.0 | 主数据库，采用分库分表设计 |
| Redis 7.x | 缓存数据库，存储热点数据 |
| MinIO | 对象存储，存储文件图片 |

---

## 功能模块

### 用户管理模块

| 功能 | 描述 |
|------|------|
| 用户注册 | 支持用户名、邮箱、手机号注册 |
| 用户登录 | 支持账号密码登录、验证码登录 |
| 个人信息 | 用户头像、昵称、签名等信息的修改 |
| 用户关注 | 关注/取消关注其他用户 |
| 粉丝列表 | 查看关注者和粉丝列表 |

### 版块管理模块

| 功能 | 描述 |
|------|------|
| 版块分类 | 对版块进行分类管理 |
| 版块列表 | 展示所有版块信息 |
| 版主管理 | 设置和管理版块版主 |
| 版块统计 | 统计版块帖子数、评论数 |

### 帖子管理模块

| 功能 | 描述 |
|------|------|
| 发布帖子 | 支持普通文本、Markdown、富文本 |
| 帖子列表 | 支持分页、排序、筛选 |
| 帖子详情 | 查看帖子详细内容 |
| 帖子搜索 | 全文检索帖子标题和内容 |
| 置顶精华 | 管理员可设置置顶和精华帖 |
| 帖子审核 | 敏感词过滤、内容审核 |

### 评论管理模块

| 功能 | 描述 |
|------|------|
| 发表评论 | 对帖子发表评论 |
| 评论回复 | 回复他人的评论 |
| 评论点赞 | 对评论进行点赞 |
| 评论删除 | 删除自己的评论 |
| 热门评论 | 按点赞数排序热门评论 |

### 互动服务模块

| 功能 | 描述 |
|------|------|
| 点赞功能 | 对帖子、评论点赞/取消点赞 |
| 收藏功能 | 收藏/取消收藏帖子 |
| @提及 | 在内容中@其他用户 |
| 互动统计 | 统计点赞数、收藏数 |

### 举报审核模块

| 功能 | 描述 |
|------|------|
| 内容举报 | 举报违规帖子、评论、用户 |
| 举报处理 | 管理员处理举报信息 |
| 用户禁言 | 对违规用户进行禁言处罚 |
| 审核记录 | 记录所有审核操作 |

### 统计分析模块

| 功能 | 描述 |
|------|------|
| 数据概览 | 用户数、帖子数、评论数统计 |
| 趋势分析 | 数据增长趋势图表 |
| 排行榜 | 活跃用户、热门帖子排行 |
| 报表导出 | 数据报表Excel导出 |

### 通知公告模块

| 功能 | 描述 |
|------|------|
| 系统公告 | 发布系统公告 |
| 消息通知 | 评论、点赞、关注消息通知 |
| 未读消息 | 查看未读消息数量 |
| 消息已读 | 标记消息为已读 |

### 系统管理模块

| 功能 | 描述 |
|------|------|
| 用户管理 | 管理用户信息、状态 |
| 角色管理 | 管理系统角色 |
| 权限管理 | 管理菜单和操作权限 |
| 操作日志 | 记录用户操作日志 |
| 登录日志 | 记录用户登录日志 |
| 系统配置 | 系统参数配置 |
| 敏感词管理 | 管理敏感词库 |

---

## 快速开始

### 环境要求

| 环境 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 1.8+ | Java开发环境 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.x | 缓存服务 |
| Maven | 3.6+ | 项目构建工具 |
| Node.js | 18+ | 前端开发环境（可选） |

### 数据库初始化

1. **创建数据库**

```bash
# 登录MySQL
mysql -u root -p

# 执行数据库创建脚本
source /path/to/databases.sql
```

2. **导入表结构**

```bash
# 导入表结构脚本
source /path/to/schema.sql

# 导入初始数据（可选）
source /path/to/data.sql
```

### 后端启动步骤

1. **克隆项目**

```bash
git clone https://github.com/your-repo/SCampus.git
cd SCampus
```

2. **修改数据库配置**

修改各服务 `application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/forum_xxx_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

3. **修改Redis配置**

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: 
```

4. **编译项目**

```bash
mvn clean install -DskipTests
```

5. **启动服务**

按以下顺序启动各服务：

```bash
# 1. 启动网关服务
java -jar forum-gateway/target/forum-gateway-1.0.0.jar

# 2. 启动认证服务
java -jar forum-auth/target/forum-auth-1.0.0.jar

# 3. 启动用户服务
java -jar forum-user/target/forum-user-1.0.0.jar

# 4. 启动版块服务
java -jar forum-category/target/forum-category-1.0.0.jar

# 5. 启动帖子服务
java -jar forum-post/target/forum-post-1.0.0.jar

# 6. 启动评论服务
java -jar forum-comment/target/forum-comment-1.0.0.jar

# 7. 启动互动服务
java -jar forum-interaction/target/forum-interaction-1.0.0.jar

# 8. 启动审核服务
java -jar forum-report/target/forum-report-1.0.0.jar

# 9. 启动统计服务
java -jar forum-stats/target/forum-stats-1.0.0.jar

# 10. 启动通知服务
java -jar forum-notify/target/forum-notify-1.0.0.jar

# 11. 启动文件服务
java -jar forum-file/target/forum-file-1.0.0.jar
```

### 前端启动步骤

```bash
# 进入前端项目目录
cd scampus-web

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

---

## 项目结构

```
SCampus/
├── doc/                          # 文档目录
│   ├── databases.sql             # 数据库创建脚本
│   ├── schema.sql                # 表结构脚本
│   └── data.sql                  # 初始数据脚本
│
├── forum-common/                 # 公共模块
│   └── src/main/java/com/campus/forum/
│       ├── config/               # 公共配置
│       ├── constant/             # 常量定义
│       ├── dto/                  # 数据传输对象
│       ├── entity/               # 基础实体类
│       ├── exception/            # 异常处理
│       ├── utils/                # 工具类
│       └── vo/                   # 视图对象
│
├── forum-api/                    # API接口模块
│   └── src/main/java/com/campus/forum/api/
│       ├── comment/              # 评论服务API
│       ├── file/                 # 文件服务API
│       ├── notify/               # 通知服务API
│       ├── post/                 # 帖子服务API
│       └── user/                 # 用户服务API
│
├── forum-gateway/                # API网关服务 (8080)
│   └── src/main/
│       ├── java/                 # Java源码
│       └── resources/            # 配置文件
│
├── forum-auth/                   # 认证服务 (9001)
│   └── src/main/
│       ├── java/                 # Java源码
│       └── resources/            # 配置文件
│
├── forum-user/                   # 用户服务 (9002)
│   └── src/main/
│       ├── java/                 # Java源码
│       └── resources/            # 配置文件
│
├── forum-category/               # 版块服务 (9003)
│   └── src/main/
│       ├── java/                 # Java源码
│       └── resources/            # 配置文件
│
├── forum-post/                   # 帖子服务 (9004)
│   └── src/main/
│       ├── java/                 # Java源码
│       └── resources/            # 配置文件
│
├── forum-comment/                # 评论服务 (9005)
│   └── src/main/
│       ├── java/                 # Java源码
│       └── resources/            # 配置文件
│
├── forum-interaction/            # 互动服务 (9006)
│   └── src/main/
│       ├── java/                 # Java源码
│       └── resources/            # 配置文件
│
├── forum-report/                 # 审核服务 (9007)
│   └── src/main/
│       ├── java/                 # Java源码
│       └── resources/            # 配置文件
│
├── forum-stats/                  # 统计服务 (9008)
│   └── src/main/
│       ├── java/                 # Java源码
│       └── resources/            # 配置文件
│
├── forum-notify/                 # 通知服务 (9009)
│   └── src/main/
│       ├── java/                 # Java源码
│       └── resources/            # 配置文件
│
├── forum-file/                   # 文件服务 (9010)
│   └── src/main/
│       ├── java/                 # Java源码
│       └── resources/            # 配置文件
│
└── pom.xml                       # Maven父项目配置
```

---

## API文档

### Swagger/Knife4j 在线API文档

系统集成了Knife4j（增强版Swagger），提供在线API文档和接口测试功能。

**访问方式**：启动对应服务后，在浏览器中访问 `http://localhost:{端口}/doc.html`

#### 如何使用API文档

1. **访问文档页面**：在浏览器中打开对应服务的API文档地址
2. **查看接口列表**：左侧展示所有接口分组和接口列表
3. **测试接口**：
   - 点击具体接口查看参数说明
   - 点击"调试"按钮进入测试模式
   - 填写参数后点击"发送"进行测试
4. **认证接口**：需要登录的接口需在Header中添加 `Authorization: Bearer {token}`

#### 获取认证Token

1. 先调用 `/api/v1/auth/login` 接口登录
2. 从响应中获取 `accessToken`
3. 在需要认证的接口中添加 Authorization 请求头

### 各服务API文档地址

| 服务 | 文档地址 |
|------|----------|
| 网关服务 | http://localhost:8080/doc.html |
| 认证服务 | http://localhost:9001/doc.html |
| 用户服务 | http://localhost:9002/doc.html |
| 版块服务 | http://localhost:9003/doc.html |
| 帖子服务 | http://localhost:9004/doc.html |
| 评论服务 | http://localhost:9005/doc.html |
| 互动服务 | http://localhost:9006/doc.html |
| 审核服务 | http://localhost:9007/doc.html |
| 统计服务 | http://localhost:9008/doc.html |
| 通知服务 | http://localhost:9009/doc.html |
| 文件服务 | http://localhost:9010/doc.html |

### 主要接口列表

#### 认证服务 API

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/v1/auth/login | POST | 用户登录 |
| /api/v1/auth/register | POST | 用户注册 |
| /api/v1/auth/logout | POST | 用户登出 |
| /api/v1/auth/refresh | POST | 刷新Token |
| /api/v1/auth/captcha | GET | 获取验证码 |

#### 用户服务 API

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/v1/users/{id} | GET | 获取用户信息 |
| /api/v1/users/{id} | PUT | 更新用户信息 |
| /api/v1/users/{id}/follow | POST | 关注用户 |
| /api/v1/users/{id}/unfollow | DELETE | 取消关注 |
| /api/v1/users/{id}/followers | GET | 获取粉丝列表 |
| /api/v1/users/{id}/following | GET | 获取关注列表 |

#### 帖子服务 API

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/v1/posts | GET | 获取帖子列表 |
| /api/v1/posts | POST | 发布帖子 |
| /api/v1/posts/{id} | GET | 获取帖子详情 |
| /api/v1/posts/{id} | PUT | 更新帖子 |
| /api/v1/posts/{id} | DELETE | 删除帖子 |

#### 评论服务 API

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/v1/comments | GET | 获取评论列表 |
| /api/v1/comments | POST | 发表评论 |
| /api/v1/comments/{id} | DELETE | 删除评论 |
| /api/v1/comments/{id}/like | POST | 点赞评论 |

---

## 配置说明

### 数据库配置

系统采用分库设计，每个微服务使用独立的数据库：

| 数据库名 | 所属服务 | 端口 |
|----------|----------|------|
| forum_auth_db | 认证服务 | 9001 |
| forum_user_db | 用户服务 | 9002 |
| forum_category_db | 版块服务 | 9003 |
| forum_post_db | 帖子服务 | 9004 |
| forum_comment_db | 评论服务 | 9005 |
| forum_interaction_db | 互动服务 | 9006 |
| forum_report_db | 审核服务 | 9007 |
| forum_stats_db | 统计服务 | 9008 |
| forum_notify_db | 通知服务 | 9009 |
| forum_file_db | 文件服务 | 9010 |

### Redis配置

各服务使用不同的Redis数据库索引：

| 服务 | Redis DB | 说明 |
|------|----------|------|
| 认证服务 | 0 | Token缓存 |
| 用户服务 | 0 | 用户信息缓存 |
| 版块服务 | 0 | 版块信息缓存 |
| 帖子服务 | 0 | 帖子缓存 |
| 评论服务 | 5 | 评论缓存 |
| 互动服务 | 6 | 点赞/收藏缓存 |
| 审核服务 | 7 | 审核数据缓存 |
| 统计服务 | 0 | 统计数据缓存 |
| 通知服务 | 9 | 通知消息缓存 |
| 文件服务 | 7 | 文件上传缓存 |

---

## 部署说明

### 本地部署

1. 准备基础环境（JDK、MySQL、Redis）
2. 执行数据库初始化脚本
3. 修改配置文件中的连接信息
4. 按顺序启动各微服务

### Docker部署

提供 `docker-compose.yml` 一键部署：

```yaml
version: '3.8'

services:
  # MySQL
  mysql:
    image: mysql:8.0
    container_name: forum-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123456
    ports:
      - "3306:3306"
    volumes:
      - ./mysql-data:/var/lib/mysql
      - ./doc/databases.sql:/docker-entrypoint-initdb.d/01-databases.sql
      - ./doc/schema.sql:/docker-entrypoint-initdb.d/02-schema.sql

  # Redis
  redis:
    image: redis:7-alpine
    container_name: forum-redis
    ports:
      - "6379:6379"

  # MinIO
  minio:
    image: minio/minio
    container_name: forum-minio
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports:
      - "9000:9000"
      - "9001:9001"
    command: server /data --console-address ":9001"
```

启动命令：

```bash
docker-compose up -d
```

---

## 默认账号

### 管理员账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | admin123 | 超级管理员 |

### 数据库连接

| 配置项 | 默认值 |
|--------|--------|
| 地址 | localhost:3306 |
| 用户名 | root |
| 密码 | root |

### MinIO控制台

| 账号 | 密码 |
|------|------|
| minioadmin | minioadmin |

---

## 许可证

本项目基于 [MIT License](LICENSE) 开源协议发布。

```
MIT License

Copyright (c) 2024 SCampus

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

<p align="center">
  如有问题或建议，欢迎提交 Issue 或 Pull Request
</p>
