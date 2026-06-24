# 架构风格声明（Architecture Style Declaration）

## 文档信息

| 属性 | 值 |
|------|-----|
| **文档编号** | ASD-001 |
| **适用项目** | 教育培训机构教务收费管理系统（EduFeeMS） |
| **适用架构风格** | 分层架构（Layered Architecture）+ 前后端分离 |
| **版本** | v1.0 |
| **生效日期** | 2026-06-22 |
| **关联ADR** | ADR-001-架构选型决策 |

---

## 1. 分层架构规则

系统后端严格分为三层，各层职责和约束如下：

### 1.1 层次定义

```
┌──────────────────────────────────────────┐
│            Controller 层（控制层）          │
│  职责: HTTP请求处理、参数校验、路由分发       │
│  路径: edu-fee-controller/src/main/java/..  │
│         .controller.{module}包下              │
├──────────────────────────────────────────┤
│             Service 层（业务层）             │
│  职责: 核心业务逻辑、事务管理、业务编排       │
│  路径: edu-fee-service/src/main/java/..     │
│         .service.{module}包下                 │
├──────────────────────────────────────────┤
│           Repository 层（数据访问层）         │
│  职责: 数据库CRUD操作、数据持久化            │
│  路径: edu-fee-repository/src/main/java/..   │
│         .repository.{module}包下              │
└──────────────────────────────────────────┘
```

### 1.2 依赖方向规则

**规则1：单向向下依赖**

```
Controller ──→ Service ──→ Repository
    ↑             ↑             ↑
    └─────────── 禁止反向穿透 ──┘
```

- **不允许**：Repository 调用 Service
- **不允许**：Repository 调用 Controller
- **不允许**：Service 调用 Controller

### 1.3 同层调用规则

**规则2：Service层同层调用**

```
Service A ←──→ Service B   (允许，通过接口)
```

- Service 之间可以互相调用，但必须通过接口注入，而非直接依赖实现类
- 同层调用需遵循模块依赖拓扑（参见 `模块依赖拓扑-DTS.md`）
- 避免循环依赖：若出现 A → B 且 B → A，需重构提取公共逻辑到第三方Service或公共模块

### 1.4 跨层通信规则

**规则3：Controller层调用约束**

```
Controller 允许调用：
  ✓ Service 接口（通过 @Autowired/@RequiredArgsConstructor 注入）

Controller 禁止：
  ✗ 直接注入或调用 Repository/Mapper
  ✗ 直接操作数据库连接
  ✗ 包含业务逻辑（如条件判断、事务控制）
  ✗ 直接操作 HttpSession（应通过Service层处理）
```

**规则4：Service层调用约束**

```
Service 允许调用：
  ✓ 其他 Service 接口（通过接口注入）
  ✓ Repository/Mapper 接口
  ✓ 工具类（Utils）、外部API封装类

Service 禁止：
  ✗ 直接操作 HttpServletRequest/HttpServletResponse
  ✗ 返回 ModelAndView 等视图对象
  ✗ 处理文件上传的 MultipartFile（应在Controller层转换为DTO后传入）
```

### 1.5 事务管理规则

- 事务边界在 **Service 层**：所有数据库写操作必须包裹在 `@Transactional` 注解中
- Controller 层不得开启事务
- Repository 层不得开启事务（单个数据库操作的事务由MyBatis-Plus自动管理）
- 跨Service调用的事务传播行为默认为 `REQUIRED`

---

## 2. 前端架构规则

### 2.1 组件层级

```
Pages（页面组件）        — 对应路由，每个路由一个页面组件
    │
    ▼
Components（业务组件）    — 可复用的业务组件（如学员选择器、费用计算器）
    │
    ▼
Base Components（基础组件） — 通用UI组件（如表格、表单、弹窗），基于Element Plus封装
```

**规则**：
- Pages 只能组合 Components 和 Base Components
- Components 可以组合 Base Components
- Base Components 不得依赖业务模块代码
- 页面间跳转通过 Vue Router，不得直接引用其他 Page 组件

### 2.2 目录结构规范

```
src/
├── api/                    # API调用封装，按模块分文件
│   ├── auth.js
│   ├── student.js
│   ├── course.js
│   ├── class.js
│   ├── teacher.js
│   ├── fee.js
│   ├── attendance.js
│   └── report.js
├── views/                  # 页面组件
│   ├── auth/
│   ├── student/
│   ├── course/
│   ├── class/
│   ├── teacher/
│   ├── fee/
│   ├── attendance/
│   └── report/
├── components/             # 可复用业务组件
├── base-components/        # 通用基础组件
├── store/                  # Pinia 状态管理
│   ├── user.js
│   ├── app.js
│   └── permission.js
├── router/                 # 路由配置
├── utils/                  # 工具函数
│   ├── request.js          # Axios 封装（含JWT拦截器）
│   └── auth.js             # 认证相关工具函数
└── assets/                 # 静态资源
```

### 2.3 状态管理规则

- 使用 **Pinia** 进行全局状态管理
- 仅以下状态放入 Store：
  - 当前登录用户信息（user）
  - 应用全局配置（app）
  - 用户权限和菜单（permission）
- 页面级状态（如表单数据、列表筛选条件）使用组件本地状态，不得放入Store
- 跨页面共享的业务数据（如当前所选校区）放入对应的业务Store

### 2.4 API调用规则

- 所有后端API调用统一封装在 `api/` 目录下
- 每个API函数返回 Promise，由调用方处理成功/失败
- API函数命名规范：`动词 + 名词`，如 `getStudentList`、`createFeeRecord`
- 禁止在 `.vue` 组件中直接使用 `axios.get/post`，必须通过 `api/` 封装层调用

---

## 3. 数据流规则

### 3.1 请求处理流程

```
用户操作 → Vue组件 → api/封装层 → Axios请求
    │
    ├── 请求拦截器（JWT Token 注入）
    │       │
    │       └── 无Token或过期 → 跳转登录页
    │
    ▼
后端 Filter/Security 拦截
    │
    ├── JWT Token 验证
    │       │
    │       └── 验证失败 → 返回 401 Unauthorized
    │
    ├── 权限校验（@PreAuthorize 注解）
    │       │
    │       └── 权限不足 → 返回 403 Forbidden
    │
    ├── 敏感操作二次校验
    │       │（如退费、修改财务数据）
    │       └── 校验失败 → 返回 403 Forbidden
    │
    ▼
Controller → Service → Repository → Database
    │
    ▼
统一响应封装 → JSON返回 → Axios响应拦截器 → 前端处理
    │
    ├── 业务异常 → 统一异常处理 → 错误码 + 错误信息
    │       │
    │       └── 前端ElMessage提示
    │
    └── 成功 → 数据渲染
```

### 3.2 统一响应格式

所有后端API返回统一的JSON响应结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1719032400000
}
```

**响应码规范**：

| 状态码范围 | 含义 |
|------------|------|
| 200 | 操作成功 |
| 400-499 | 客户端错误（参数校验失败、权限不足等） |
| 500-599 | 服务端错误（系统异常、数据库错误等） |

### 3.3 认证与授权规则

- 所有API请求（除登录接口外）必须在Header中携带 `Authorization: Bearer {token}`
- JWT Token 有效期设置为 2 小时，支持刷新
- 敏感操作（退费、修改财务数据、删除数据）需二次密码或短信验证
- 接口级权限通过 `@PreAuthorize` 注解声明所需角色

### 3.4 数据库访问规则

- 所有数据库操作通过 **MyBatis-Plus** 进行
- 禁止在Service层拼接SQL字符串
- 复杂查询优先使用MyBatis-Plus的LambdaQueryWrapper
- 极复杂查询（多表关联、聚合统计）使用XML Mapper自定义SQL
- 所有数据库操作记录需包含 `create_by` / `update_by` 操作人信息

---

## 4. 命名规范

### 4.1 Java命名规范

| 元素 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase | `StudentController`, `FeeService`, `CourseMapper` |
| 方法名 | camelCase | `getStudentById()`, `createFeeRecord()`, `updateClassInfo()` |
| 变量名 | camelCase | `studentName`, `feeAmount`, `classId` |
| 常量 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE` |
| 包名 | 小写，点分隔 | `com.edufeems.controller.student` |
| 接口名 | PascalCase（不加I前缀） | `StudentService`, `FeeRepository` |
| 实现类 | 接口名 + Impl | `StudentServiceImpl`, `FeeRepositoryImpl` |

### 4.2 数据库命名规范

| 元素 | 规范 | 示例 |
|------|------|------|
| 表名 | snake_case，前缀 `t_` | `t_student_info`, `t_fee_record`, `t_course_type` |
| 字段名 | snake_case | `student_name`, `fee_amount`, `create_time` |
| 主键 | `id` (BIGINT) | `id` |
| 外键 | `关联表名_id` | `student_id`, `course_id`, `class_id` |
| 索引名 | `idx_表名_字段名` | `idx_t_student_phone`, `idx_t_fee_pay_time` |
| 唯一约束 | `uk_表名_字段名` | `uk_t_student_id_card`, `uk_t_user_username` |

### 4.3 RESTful API命名规范

| 规范 | 示例 |
|------|------|
| URL全部小写 | `/api/v1/students` |
| 多个单词用连字符 `-` 分隔 | `/api/v1/fee-records` |
| 资源名使用复数形式 | `/api/v1/courses` |
| 版本号在URL中体现 | `/api/v1/students` |
| HTTP方法表示操作类型 | `GET` 查询, `POST` 创建, `PUT` 更新, `DELETE` 删除 |

**URL设计示例**：

```
GET    /api/v1/students              # 分页查询学员列表
GET    /api/v1/students/{id}         # 查询单个学员详情
POST   /api/v1/students              # 新增学员
PUT    /api/v1/students/{id}         # 更新学员信息
DELETE /api/v1/students/{id}         # 逻辑删除学员
GET    /api/v1/students/{id}/fees    # 查询某学员的缴费记录
POST   /api/v1/fee-records           # 创建缴费记录
POST   /api/v1/fee-records/{id}/refund  # 退费操作
```

### 4.4 前端命名规范

| 元素 | 规范 | 示例 |
|------|------|------|
| Vue组件文件 | PascalCase | `StudentList.vue`, `FeeRecordForm.vue` |
| 路由路径 | kebab-case | `/student-management`, `/fee-records` |
| JS函数 | camelCase | `fetchStudentList()`, `handleSubmit()` |
| CSS类名 | kebab-case | `.student-form`, `.fee-table` |
| Pinia Store | camelCase | `useUserStore`, `useAppStore` |

---

## 5. 架构约束与禁止事项

### 5.1 强制约束

| 编号 | 约束内容 | 违反后果 |
|:----:|----------|----------|
| C01 | Controller不得直接调用Repository/Mapper | 代码审查不通过 |
| C02 | 所有API返回必须使用统一响应格式 `R<T>` | 前端解析异常 |
| C03 | 数据库操作必须通过MyBatis-Plus，禁止JDBC直接操作 | 数据安全风险 |
| C04 | 所有表必须包含通用字段（id, create_time, update_time, create_by, update_by, is_deleted） | 审计追溯缺失 |
| C05 | 新增模块必须在模块依赖拓扑中注册依赖关系 | 架构腐化 |
| C06 | 所有API接口（除登录）必须通过JWT认证 | 安全漏洞 |

### 5.2 禁止事项

| 编号 | 禁止内容 | 原因 |
|:----:|----------|------|
| F01 | 禁止在Controller中编写业务逻辑 | 违反分层原则 |
| F02 | 禁止Service直接返回Entity对象给Controller（应返回DTO/VO） | 数据暴露风险 |
| F03 | 禁止循环依赖（A → B → A） | 启动失败或运行时异常 |
| F04 | 禁止魔法数字和硬编码常量 | 可维护性差 |
| F05 | 禁止在生产环境开启Swagger UI | 安全风险 |
| F06 | 禁止物理删除业务数据（使用逻辑删除is_deleted=1） | 数据无法恢复 |

---

## 6. 架构演进路线

```
当前阶段 (v1.0)                    未来可能演进
┌─────────────────────┐          ┌─────────────────────┐
│ 分层架构 + 前后端分离  │  ──────→ │ 模块化单体            │
│ (Layered + SPA)     │  业务增长  │ (Modular Monolith)  │
└─────────────────────┘          └──────────┬──────────┘
                                            │ 业务进一步增长
                                            ▼
                                 ┌─────────────────────┐
                                 │ 微服务架构            │
                                 │ (Microservices)     │
                                 └─────────────────────┘
```

当前Maven多模块项目结构已为后续演进预留了模块边界，确保架构演进的平滑性。

---

## 相关文档

- ADR-001：系统整体架构风格选型
- ADR-002：技术栈选型决策
- 模块划分方案-MDS.md
- 模块依赖拓扑-DTS.md
