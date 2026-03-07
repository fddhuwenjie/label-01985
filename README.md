# 学生成绩管理系统

## How to Run

```bash
# 克隆项目后，在项目根目录执行
docker-compose up --build -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

首次启动需要等待 MySQL 初始化完成（约30秒），后端服务会自动等待 MySQL 和 Redis 就绪后启动。

## Services

| 服务 | 地址 | 说明 |
|------|------|------|
| 管理后台前端 | http://localhost:8081 | Vue3 + Element Plus |
| 后端 API | http://localhost:8080 | SpringBoot 2.7 |
| API 文档 | http://localhost:8080/doc.html | Knife4j (Swagger) |
| MySQL | localhost:3306 | 数据库 |
| Redis | localhost:6379 | 缓存 |

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |


## 题目内容

Task 开发一个"学生成绩管理系统"，采用前后端分离架构。 Tech Stack Requirements 后端 SpringBoot 2.7+ MyBatis Plus（需使用代码生成器） MySQL 8.0+（需设计ER图和表结构） Redis缓存 Swagger接口文档 Lombok 前端 Vue2 或 Vue3 Element-UI / Uni-app（可选跨端） Vue Router Axios ECharts（数据可视化） Vite构建工具 ES6+语法 Core Features 必须实现 用户管理：注册/登录、JWT认证、RBAC角色权限控制 数据管理：CRUD操作、分页查询、条件筛选、Excel导入导出 可选实现（选1-2个） 微服务扩展：Spring Cloud Alibaba + Nacos注册中心 实时通信：WebSocket消息推送 数据分析：Quartz定时任务生成报表 多端适配：Uni-app开发移动端 Coding Standards 后端：遵循《阿里巴巴Java开发手册》，Controller层统一返回JSON格式 前端：组件化开发，Vuex状态管理，Axios拦截器统一处理请求 数据库：字段命名采用小写+下划线，索引优化查询性能 Quality Requirements SonarQube检测：无严重漏洞，代码重复率<15% 性能测试：JMeter并发测试，响应时间<2s 安全性：密码加密存储 Output Requirements 请按以下顺序输出： 数据库ER图设计（用Mermaid或ASCII表示） 数据库表结构SQL语句 项目整体架构说明 后端核心代码（Controller、Service、Mapper、Entity） 前端核心代码（路由、组件、API接口） 配置文件（application.yml、pom.xml、package.json等）

## 项目介绍

本项目是一个基于前后端分离架构的学生成绩管理系统，支持用户管理、学生管理、教师管理、课程管理、成绩管理及成绩数据可视化分析。

### 技术栈

**后端：**
- SpringBoot 2.7.18
- MyBatis Plus 3.5.5（含代码生成器）
- MySQL 8.0
- Redis 7
- Spring Security + JWT 认证
- Knife4j (Swagger) 接口文档
- EasyExcel（Excel 导入导出）
- Lombok

**前端：**
- Vue 3.4
- Element Plus 2.5
- Pinia 状态管理
- Vue Router 4
- Axios（含请求/响应拦截器）
- ECharts 5.5（成绩分析可视化）
- Vite 5 构建工具
- ES6+ 语法

### 核心功能

- 用户管理：注册/登录、JWT 认证、RBAC 角色权限控制（管理员/教师/学生）
- 学生管理：CRUD、分页查询、关键字搜索
- 教师管理：CRUD、分页查询、关键字搜索
- 课程管理：CRUD、分页查询
- 成绩管理：CRUD、多条件筛选、Excel 导入导出
- 成绩分析：各科平均分柱状图、雷达图对比、学生排名

### 项目结构

```
student-grade-system/
├── backend/              # SpringBoot 后端服务
├── frontend-admin/       # Vue3 管理后台
├── docs/                 # 数据库设计文档
│   ├── er-diagram.md     # ER 图 (Mermaid)
│   └── schema.sql        # 建表 SQL
├── docker-compose.yml    # Docker 编排
├── .gitignore
└── README.md
```
