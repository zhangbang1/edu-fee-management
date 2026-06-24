# 逆向校验报告（Reverse Check Report）

## 文档信息

| 属性 | 值 |
|------|-----|
| **文档编号** | RCR-001 |
| **适用项目** | 教育培训机构教务收费管理系统（EduFeeMS） |
| **校验版本** | v1 |
| **编制日期** | 2026-06-22 |
| **校验方法** | 静态代码分析（CodeGraph） + 设计文档对比 |

---

## 1. 校验概述

### 1.1 校验范围

| 维度 | 正向图谱来源 | 逆向图谱来源 | 校验方式 |
|------|-------------|-------------|----------|
| 架构层 | ASD（架构风格声明）、TLCD（三层约束设计） | 后端Java源码（9个模块） | 层次调用链分析 |
| 模块层 | MDS（模块划分方案） | 后端模块目录结构 | 包结构/模块命名对比 |
| 拓扑层 | DTS（模块依赖拓扑） | 后端import语句分析 | 依赖关系矩阵对比 |
| 契约层 | OAS（OpenAPI契约 v1.0.0） | Controller方法注解 | 路径/方法/参数/响应对比 |
| 代码工程层 | TLCD-C-CODE（代码层约束15条） | 全量Java源码 | 逐条约束验证 |
| 需求溯源层 | SRS（需求清单v1.0，98条功能需求） | Controller + Service方法 | 需求-代码映射 |

### 1.2 校验工具与方法

- **静态代码分析**：通过源码文本搜索（Grep）、import依赖追踪、注解扫描
- **设计文档对比**：将实际代码结构与MDS/DTS/OAS/TLCD逐项比对
- **抽样校验**：需求溯源维度采用随机抽样（10条）+ 关键路径全覆盖

---

## 2. 校验维度与结果

### 维度1：架构层校验

#### 1.1 Controller → Service 调用链检查

| 检查项 | 约束来源 | 结果 | 详情 |
|--------|---------|------|------|
| Controller是否直接调用Mapper/Repository | C-ARCH-001 | **通过** | 所有Controller仅注入Service接口，未发现直接注入Mapper的情况 |
| Controller是否包含业务逻辑 | C-ARCH-003 | **轻微漂移** | FeeController.makePayment()中直接使用`Map<String, Object>`解析参数并进行类型转换，属于参数处理逻辑弥散到Controller层 |

**校验明细**：

| Controller | 注入依赖 | 直接调用Mapper | 业务逻辑侵入 | 判定 |
|------------|---------|---------------|-------------|------|
| AuthController | AuthService | 否 | 否 | 通过 |
| StudentController | StudentService | 否 | 否 | 通过 |
| CourseController | CourseService | 否 | 否 | 通过 |
| ClassController | ClassService | 否 | 否 | 通过 |
| TeacherController | TeacherService | 否 | 否 | 通过 |
| FeeController | FeeService | 否 | **是**（参数解析） | 轻微漂移 |
| AttendanceController | AttendanceService | 否 | **是**（参数解析） | 轻微漂移 |
| ReportController | ReportService | 否 | 否 | 通过 |

#### 1.2 @Transactional 注解检查

| Service实现类 | 写操作方法 | @Transactional | 判定 |
|--------------|-----------|---------------|------|
| StudentServiceImpl | createStudent, updateStudent, transferStudent, withdrawStudent | 均有 | 通过 |
| FeeServiceImpl | createFeeRecord, makePayment, applyRefund, approveRefund, createDunning | 均有 | 通过 |
| AuthServiceImpl | login（调用userMapper.updateById更新登录时间） | **缺失** | **严重漂移** |
| ClassServiceImpl | 预期有createClass等 | 待确认 | — |
| CourseServiceImpl | 预期有createCourse等 | 待确认 | — |
| TeacherServiceImpl | 预期有createTeacher等 | 待确认 | — |
| AttendanceServiceImpl | 预期有batchCreateAttendance等 | 待确认 | — |
| ReportServiceImpl | 仅查询操作 | 无需 | 通过 |

**关键发现**：`AuthServiceImpl.login()` 方法中调用了 `userMapper.updateById(user)` 更新最近登录时间，但该方法未添加 `@Transactional` 注解，违反 C-ARCH-004 约束。

#### 1.3 实体暴露检查

| Controller | 返回类型 | 直接暴露Entity | 违反C-ARCH-012 |
|------------|---------|---------------|----------------|
| FeeController | `R<FeeRecord>`, `R<Payment>`, `R<List<FeeRecord>>` | **是** | **是** |
| StudentController | `R<StudentDTO.DetailResponse>` | 否（使用DTO） | 否 |
| CourseController | `R<Course>`, `R<PageDTO<Course>>` | **是** | **是** |
| ClassController | `R<EduClass>`, `R<Schedule>` | **是** | **是** |
| TeacherController | `R<Teacher>`, `R<PageDTO<Teacher>>` | **是** | **是** |
| AttendanceController | `R<PageDTO<Attendance>>` | **是** | **是** |
| AuthController | `R<User>`（me接口） | **是** | **是** |
| ReportController | `R<Map<String, Object>>` | 否 | 否 |

**关键发现**：7个Controller中有6个直接暴露Entity对象给前端，违反 C-ARCH-012"数据库实体类禁止被直接暴露到Controller层"约束。这是v1版本中最普遍的系统性漂移。

---

### 维度2：模块层校验

#### 2.1 包结构对比（MDS vs 实际）

| MDS规范 | 实际代码 | 差异 |
|---------|---------|------|
| `com.edufeems.{module}` | `com.edufee.{module}` | 根包名不一致（缺少"s"） |
| `controller/` | `controller/` | 一致 |
| `service/` + `service/impl/` | `service/` + `service/impl/` | 一致 |
| `repository/` | **`mapper/`** | 目录名不一致 |
| `entity/` | `entity/` | 一致 |
| `dto/` | `dto/`（仅student、auth模块） | 部分模块缺失 |
| `vo/` | **缺失** | 全部模块缺失 |
| `constant/` | **缺失** | 全部模块缺失（常量集中在common模块） |

#### 2.2 模块存在性校验

| MDS定义的9模块 | 实际模块（后端） | 匹配状态 |
|---------------|-----------------|----------|
| common（公共基础设施） | edu-common | 匹配 |
| auth（认证授权） | edu-auth | 匹配 |
| student（学员管理） | edu-student | 匹配 |
| course（课程管理） | edu-course | 匹配 |
| class（班级管理） | edu-class | 匹配 |
| teacher（教师管理） | edu-teacher | 匹配 |
| fee（收费管理） | edu-fee | 匹配 |
| attendance（考勤管理） | edu-attendance | 匹配 |
| report（报表统计） | edu-report | 匹配 |
| — | **edu-server**（引导模块） | **MDS未定义** |

**说明**：`edu-server` 为Spring Boot应用启动模块，仅包含 `EduFeeApplication.java` 主类和 `application.yml`，属于工程基础设施而非业务模块，MDS中未单独列出属于合理存在。

---

### 维度3：拓扑校验

#### 3.1 实际模块依赖关系（基于import语句分析）

| 模块 | actual imports from |
|------|---------------------|
| common | 无 |
| auth | common |
| student | common, auth,（via DTO） |
| course | common, auth |
| class | common, auth |
| teacher | common, auth |
| fee | common, auth |
| attendance | common, auth, student（via attendance关联studentId） |
| report | common, auth |

#### 3.2 DTS vs 实际依赖对比

| 依赖关系 | DTS期望 | 实际代码 | 一致性 |
|---------|---------|---------|--------|
| student → course | 允许 | **未发现import** | **偏差** |
| student → class | 允许 | **未发现import** | **偏差** |
| class → course | 允许 | **未发现import** | **偏差** |
| class → teacher | 允许 | **未发现import** | **偏差** |
| fee → student | 允许 | **未发现import** | **偏差** |
| fee → course | 允许 | **未发现import** | **偏差** |
| attendance → student | 允许 | 已确认 | 一致 |
| attendance → class | 允许 | **未发现import** | **偏差** |
| report → all business | 允许 | 仅依赖common+auth | **严重偏差** |

**说明**：v1版本中跨模块Service调用通过Spring注入实现（runtime依赖），编译期import可能通过接口引用。当前分析基于显式import语句，runtime依赖需进一步通过Spring容器验证。多个模块之间存在预期依赖但未在代码中显式import的情况，表明跨模块集成尚未完成，各模块目前处于相对独立开发状态。

---

### 维度4：契约校验

#### 4.1 URL路径对比（OAS vs Controller）

| API资源 | OAS路径规范 | Controller实际路径 | 偏差 |
|---------|------------|-------------------|------|
| 学员 | `/students` | `/api/student` | **路径不一致（单复数）** |
| 课程 | `/courses` | `/api/course` | **路径不一致（单复数）** |
| 班级 | `/classes` | `/api/class` | **路径不一致（单复数）** |
| 收费 | `/fees` | `/api/fee` | **路径不一致（单复数）** |
| 考勤 | `/attendances` | `/api/attendance` | **路径不一致（单复数，缺s）** |
| 认证 | `/auth/*` | `/api/auth/*` | 一致 |

**关键发现**：OAS定义使用RESTful规范的资源名复数形式，但所有Controller的 `@RequestMapping` 使用单数形式。这是v1版本中最显著的前后端契约偏差。前端 `api/` 封装层使用复数形式（如 `request.get('/students', ...)`），与后端Controller路径不匹配，存在集成风险。

#### 4.2 接口方法签名对比

| OAS接口 | OAS方法 | Controller实际方法 | 偏差 |
|---------|---------|-------------------|------|
| GET /students | 分页查询 | GET /api/student/list | **路径多 `/list` 后缀** |
| GET /students/{id} | 查询详情 | GET /api/student/{id} | 一致 |
| POST /students | 新增学员 | POST /api/student | 一致 |
| PUT /students/{id} | 更新学员 | PUT /api/student | **路径无 `/{id}`** |
| POST /students/{id}/enroll | 学员分班 | 缺失 | **严重缺失** |
| POST /students/{id}/transfer | 学员转班 | POST /api/student/transfer | **路径无 `/{id}`** |
| POST /students/{id}/withdraw | 学员退学 | POST /api/student/{id}/withdraw | 一致 |
| POST /fees/payment | 学费收取 | POST /api/fee/payment | 一致 |
| POST /fees/refund | 退费处理 | POST /api/fee/refund/apply | **路径多 `/apply`** |
| GET /fees/records | 收费台账 | GET /api/fee/records | 一致 |
| GET /fees/dunning | 欠费查询 | GET /api/fee/dunning/overdue | **路径多 `/overdue`** |
| POST /attendances/checkin | 签到 | **缺失独立接口** | **严重缺失** |
| POST /attendances/leave | 请假 | **缺失** | **严重缺失** |
| GET /attendances/statistics | 考勤统计 | GET /api/attendance/stats/student/{id} | **路径结构不同** |

#### 4.3 响应格式对比

| OAS Schema | Controller返回类型 | 偏差 |
|------------|-------------------|------|
| StudentResponse（含code/message/data） | `R<StudentDTO.DetailResponse>` | 一致（R类实现统一响应） |
| LoginResponse（含token/tokenType/expiresIn/userInfo） | `R<LoginResponse>` | 一致 |
| FeeRecordDTO | 直接返回`FeeRecord`实体 | **不一致**（暴露entity字段） |
| CourseDTO | 直接返回`Course`实体 | **不一致**（暴露entity字段） |

---

### 维度5：代码工程层校验

逐条验证C-CODE约束（来自TLCD文档）：

| 编号 | 约束内容 | 校验结果 | 违反位置 |
|------|---------|---------|----------|
| C-CODE-001 | 日期时间字段使用LocalDateTime | **通过** | BaseEntity、FeeRecord等使用LocalDateTime |
| C-CODE-002 | 日期格式统一为yyyy-MM-dd HH:mm:ss | **通过** | Constants.DATETIME_FORMAT 定义了统一格式 |
| C-CODE-003 | 金额字段使用BigDecimal | **通过** | FeeRecord.amount等使用BigDecimal |
| C-CODE-004 | Controller使用@Valid参数校验 | **部分通过** | FeeController使用Map<String,Object>代替DTO+@Valid |
| C-CODE-005 | DTO类使用Lombok @Data | **通过**（存在DTO的模块） | StudentDTO使用@Data |
| C-CODE-006 | Entity使用@TableName和@TableId | **通过** | Student、FeeRecord等均使用 |
| C-CODE-007 | 使用@Slf4j日志 | **通过** | 所有Controller和Service均使用 |
| C-CODE-008 | 使用BusinessException | **通过** | 统一使用BusinessException |
| C-CODE-009 | 禁止魔法值 | **严重漂移** | 详见下方 |
| C-CODE-010 | 多表查询使用VO封装 | **不适用** | v1版本尚未涉及多表关联查询 |
| C-CODE-011 | @TableField自动填充时间 | **通过** | BaseEntity定义了@TableField(fill=...) |
| C-CODE-012 | RESTful URL复数形式 | **严重漂移** | 所有Controller使用单数形式 |
| C-CODE-013 | @Operation注解 | **通过** | 所有Controller方法均有@Operation |
| C-CODE-014 | SQL IN分批查询 | **不适用** | 当前未出现IN查询 |
| C-CODE-015 | 禁止循环中查询数据库 | **通过** | 未发现 |

#### 5.1 魔法值详细清单（C-CODE-009违反项）

| 位置 | 魔法值 | 应有常量 | 严重等级 |
|------|--------|---------|----------|
| `StudentServiceImpl.getStatusText()` | `"在读"、"停课"、"转班"、"退学"、"毕业"` | 状态文本常量 | 中 |
| `StudentServiceImpl.convertToDetailResponse()` | `1`, `2`（性别判断） | `GENDER_MALE`, `GENDER_FEMALE` | 高 |
| `AuthServiceImpl.login()` | `0`（账户禁用状态） | `USER_STATUS_DISABLED` | 高 |
| `CourseController.updateStatus()` | `1`（上架状态判断） | `COURSE_STATUS_ACTIVE` | 中 |
| `FeeServiceImpl.makePayment()` | `"PAY"`（缴费编号前缀） | `PAYMENT_NO_PREFIX` | 低 |

---

### 维度6：需求溯源校验

随机抽取10条REQ需求进行代码覆盖检查：

| 编号 | 需求编号 | 需求描述 | Controller | Service | 覆盖状态 |
|------|---------|---------|-----------|---------|---------|
| 1 | REQ-EDU-001 | 学员CRUD | StudentController | StudentServiceImpl | **完全覆盖** |
| 2 | REQ-EDU-005 | 学员转班 | StudentController.transfer() | StudentServiceImpl.transferStudent() | **部分覆盖**（仅状态变更，缺少班级容量+费用重算+通知） |
| 3 | REQ-EDU-010 | 班级创建管理 | ClassController | ClassServiceImpl | **完全覆盖** |
| 4 | REQ-EDU-011 | 排课功能 | ClassController.createSchedule() | ClassServiceImpl | **完全覆盖** |
| 5 | REQ-FIN-006 | 学费收取登记 | FeeController.makePayment() | FeeServiceImpl | **完全覆盖** |
| 6 | REQ-FIN-011 | 退费申请 | FeeController.applyRefund() | FeeServiceImpl | **部分覆盖**（缺少审批流） |
| 7 | REQ-FIN-015 | 欠费识别 | FeeController.listOverdue() | FeeServiceImpl.listOverdueRecords() | **部分覆盖**（缺少自动标记和停课锁定） |
| 8 | REQ-EDU-019 | 学员签到 | AttendanceController.batchCreate() | AttendanceServiceImpl | **部分覆盖**（批量创建，缺少独立签到/签退） |
| 9 | REQ-FIN-008 | 电子收据生成 | 缺失 | 缺失 | **未覆盖** |
| 10 | REQ-ADMIN-005 | RBAC权限控制 | SecurityConfig | JwtUtil | **部分覆盖**（单角色编码，非完整RBAC） |

#### 覆盖统计

| 覆盖等级 | 数量 | 占比 |
|---------|------|------|
| 完全覆盖 | 4 | 40% |
| 部分覆盖 | 5 | 50% |
| 未覆盖 | 1 | 10% |

**说明**：抽样结果显示v1版本实现了核心CRUD流程，但在审批流（退费审批、请假审批）、通知机制、收据生成等业务流程闭环上存在明显缺口。

---

## 3. 漂移清单

| 编号 | 严重等级 | 维度 | 违反约束 | 具体位置 | 说明 |
|:----:|:--------:|------|---------|---------|------|
| D-01 | **严重** | 架构层 | C-ARCH-004 | `AuthServiceImpl.login()` (line 54-101) | login方法调用userMapper.updateById()写库但未加@Transactional |
| D-02 | **严重** | 架构层 | C-ARCH-012 | FeeController/CourseController/ClassController/TeacherController/AttendanceController/AuthController | 6个Controller直接返回Entity对象给前端，未使用DTO/VO转换 |
| D-03 | **严重** | 契约层 | OAS路径规范 | 全部Controller | 所有Controller资源路径使用单数形式（/api/student），与OAS定义的复数形式（/students）不一致 |
| D-04 | **严重** | 代码工程 | C-CODE-009 | `StudentServiceImpl` (line 259-260), `AuthServiceImpl` (line 66) | 使用魔法值进行性别判断(1/2)和状态判断(0)，未使用常量 |
| D-05 | **严重** | 拓扑层 | DTS依赖拓扑 | report模块 | ReportController仅依赖common+auth，未集成student/course/fee等业务模块数据 |
| D-06 | **高** | 模块层 | MDS包结构 | 全局 | 根包名使用com.edufee而非MDS定义的com.edufeems；使用mapper/目录而非repository/ |
| D-07 | **高** | 契约层 | OAS接口定义 | StudentController, FeeController, AttendanceController | 多个接口路径与OAS不一致（/list后缀、缺少enroll独立接口、缺少checkin/leave独立接口） |
| D-08 | **高** | 代码工程 | C-CODE-004 | `FeeController` (line 73-82) | 使用Map<String,Object>接收参数，未使用@Valid校验的DTO |
| D-09 | **高** | 需求溯源 | REQ-FIN-008 | 全局搜索 | 收据生成功能（ReceiptUtil.java存在但未在Controller暴露）— 代码存在但API未暴露 |
| D-10 | **中** | 契约层 | OAS响应Schema | FeeController | 返回FeeRecord实体而非OAS定义的FeeRecordDTO，包含createBy/updateBy等不应暴露的字段 |
| D-11 | **中** | 需求溯源 | REQ-FIN-013 | FeeServiceImpl | 退费多级审批流程仅实现申请阶段，approveRefund()为TODO状态 |
| D-12 | **中** | 需求溯源 | REQ-EDU-019 | AttendanceController | 签到功能仅有batchCreate批量创建，缺少checkIn()/checkOut()独立签到签退接口 |
| D-13 | **低** | 代码工程 | C-CODE-009 | `FeeServiceImpl` (line 172) | "PAY"前缀硬编码，虽在注释中说明格式但未使用常量 |
| D-14 | **低** | 模块层 | MDS命名 | edu-class模块 | 类名使用EduClass（含前缀）而非ClassInfo，Schedule在entity包但也混在entity目录 |
| D-15 | **低** | 架构层 | C-ARCH-011 | 全部Controller | Controller路径前缀不一致——部分使用/api/前缀，与OAS servers定义重叠 |

### 漂移等级分布

| 等级 | 数量 | 占比 |
|:----:|:----:|:----:|
| 严重 | 5 | 33% |
| 高 | 5 | 33% |
| 中 | 3 | 20% |
| 低 | 2 | 13% |
| **合计** | **15** | **100%** |

---

## 4. 校验结论

### 4.1 总分评定

| 维度 | 满分 | 得分 | 扣分说明 |
|------|:----:|:----:|----------|
| 架构层 | 20 | 12 | -8：AuthServiceImpl事务缺失(-2)，6个Controller实体暴露(-5)，FeeController参数解析(-1) |
| 模块层 | 15 | 12 | -3：包名不一致(-1)，mapper替代repository(-1)，vo/constant目录缺失(-1) |
| 拓扑层 | 15 | 10 | -5：跨模块Service依赖未建立(-5，report等模块集成严重不足) |
| 契约层 | 20 | 8 | -12：URL路径全部单数(-5)，5个接口路径不一致(-4)，实体替代DTO(-3) |
| 代码工程层 | 15 | 11 | -4：魔法值问题(-3)，Map参数替代DTO(-1) |
| 需求溯源层 | 15 | 9 | -6：抽样中50%部分覆盖(-3)，10%未覆盖(-2)，审批流/通知等缺失(-1) |
| **总分** | **100** | **62** | |

### 4.2 校验结论

- **总体评价**：v1版本代码实现了核心业务模块的基本CRUD操作，架构分层基本遵循ASD规范，但在API契约一致性、实体安全暴露、跨模块集成和完整业务流程闭环方面存在显著差距。
- **漂移总数**：15项
- **严重等级分布**：严重5项，高5项，中3项，低2项
- **校验判定**：**不通过**（62 < 75 通过线）
- **关键风险**：
  1. **前后端集成风险**：API路径不一致（单复数问题）将导致前端请求全部404
  2. **数据安全风险**：Entity直接暴露导致密码字段（User.password虽在getCurrentUser中置null但非系统性防护）、逻辑删除标识等敏感字段可能泄露
  3. **数据一致性风险**：AuthServiceImpl.login()缺少事务可能导致登录时间更新与数据库状态不一致
  4. **业务流程不完整**：退费审批流、请假审批流、收据生成等关键业务闭环为TODO状态

### 4.3 修复建议

| 优先级 | 修复项 | 建议方案 | 涉及文件 |
|--------|--------|---------|---------|
| P0 | URL路径统一为复数形式 | Controller @RequestMapping改为复数（/api/students等） | 8个Controller |
| P0 | Entity暴露问题修复 | 为每个模块创建VO/DTO，Controller返回VO而非Entity | 6个Controller + 新建VO类 |
| P0 | 补齐AuthServiceImpl事务 | login()方法添加@Transactional | AuthServiceImpl.java |
| P1 | 魔法值消除 | 在Constants中补充GENDER_*、USER_STATUS_*等常量 | Constants.java + 相关Service |
| P1 | Map参数替换为DTO | FeeController使用@Valid DTO替代Map | FeeController.java + 新建DTO |
| P1 | 补齐缺少的API接口 | 实现enroll、checkin、leave等缺失接口 | 相关Controller/Service |
| P2 | 跨模块集成 | 建立report模块与各业务模块的Service依赖 | ReportServiceImpl.java |
| P2 | 补全退费审批流 | 实现多级审批Service逻辑 | FeeServiceImpl.java |
| P2 | 包名规范化 | 考虑统一为com.edufeems（需评估重构成本） | 全局 |

---

## 相关文档

- ASD：架构风格声明 (`designs/diagrams/架构风格声明-ASD.md`)
- MDS：模块划分方案 (`designs/diagrams/模块划分方案-MDS.md`)
- DTS：模块依赖拓扑 (`designs/diagrams/模块依赖拓扑-DTS.md`)
- TLCD：三层约束设计 (`designs/diagrams/三层约束设计-TLCD.md`)
- OAS：OpenAPI契约 (`designs/contracts/openapi.yaml`)
- SRS需求清单 (`wiki/baselines/BL-20260622-01/需求清单.md`)
- 四维度质量校验报告 (`designs/diagrams/四维度质量校验报告.md`)
- ADR-004：逆向校验与质量保障决策 (`designs/adr/ADR-004-逆向校验与质量保障决策.md`)
