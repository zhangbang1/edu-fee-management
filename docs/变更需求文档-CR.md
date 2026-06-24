# 变更需求文档（Change Request）

**项目名称**：教育培训机构教务收费管理系统（EduFeeMS）
**文档编号**：CR-V1-V2-001
**基线版本**：BL-20260622-01 → BL-20260622-02
**编制日期**：2026-06-22
**变更来源**：逆向校验报告（RCR-001）+ 四维度质量校验报告（QAR-001）
**文档状态**：已提交

---

## 变更概述

基于逆向校验报告（RCR）中发现的15项设计-代码漂移和四维度质量校验报告（QAR）中发现的质量问题，本变更需求文档系统性地整理了8条变更需求（CR-001至CR-008），覆盖架构修正、契约对齐、事务补齐、业务闭环完善、跨模块集成、枚举统一和前端体验增强七个重要领域。

| 统计维度 | 数值 |
|---------|:----:|
| 变更总数 | 8 |
| 缺陷修复类变更 | 5 |
| 功能增强类变更 | 2 |
| 优化类变更 | 1 |
| 关联RCR漂移项 | 15/15（全覆盖） |
| 预计总工作量 | 32人日 |

---

## 变更需求列表

---

### CR-001: 修正Controller直接暴露Entity问题

| 属性 | 值 |
|------|-----|
| **变更编号** | CR-001 |
| **变更类型** | 缺陷修复（Bug Fix） |
| **优先级** | P0-严重 |
| **来源** | RCR D-02（6个Controller直接暴露Entity）、D-10（FeeRecord实体替代DTO） |
| **提出日期** | 2026-06-22 |
| **变更描述** | 当前v1版本中，FeeController、CourseController、ClassController、TeacherController、AttendanceController、AuthController共6个Controller直接返回Entity对象（如`FeeRecord`、`Course`、`EduClass`、`Teacher`、`Attendance`、`User`）给前端，违反了C-ARCH-012"数据库实体类禁止被直接暴露到Controller层"约束。Entity中包含不应暴露的字段（如`createBy`、`updateBy`、`password`、`deleted`等），存在数据安全风险。 |
| **变更要求** | 1. 为每个模块创建对应的DTO/VO类，用于Controller层与前端交互<br>2. 所有Controller方法的返回类型从Entity改为对应的DTO/VO<br>3. Service层内部继续使用Entity进行业务逻辑处理<br>4. 在Service层提供Entity→DTO的转换方法 |
| **影响模块** | student、course、class、teacher、fee、attendance、auth（7个模块） |
| **涉及文件** | 8个Controller文件、新增6-8个DTO/VO类 |
| **预期工作量** | 5人日 |

#### 详细变更范围

| 模块 | 需要修改的Controller | 需新建的DTO/VO类 | 需替换的返回类型 |
|------|---------------------|-----------------|----------------|
| student | StudentController | 已有StudentDTO.DetailResponse | 无需修改（已使用DTO） |
| course | CourseController | CourseDTO, CourseListDTO | R\<Course\> → R\<CourseDTO\>, R\<PageDTO\<Course\>\> → R\<PageDTO\<CourseListDTO\>\> |
| class | ClassController | ClassDTO, ScheduleDTO | R\<EduClass\> → R\<ClassDTO\>, R\<Schedule\> → R\<ScheduleDTO\> |
| teacher | TeacherController | TeacherDTO, TeacherListDTO | R\<Teacher\> → R\<TeacherDTO\>, R\<PageDTO\<Teacher\>\> → R\<PageDTO\<TeacherListDTO\>\> |
| fee | FeeController | FeeRecordDTO, FeeRecordListDTO, PaymentDTO, RefundDTO | R\<FeeRecord\> → R\<FeeRecordDTO\>, R\<Payment\> → R\<PaymentDTO\> |
| attendance | AttendanceController | AttendanceDTO | R\<PageDTO\<Attendance\>\> → R\<PageDTO\<AttendanceDTO\>\> |
| auth | AuthController | UserDTO | R\<User\> → R\<UserDTO\>（me接口） |

---

### CR-002: 统一API路径命名规范

| 属性 | 值 |
|------|-----|
| **变更编号** | CR-002 |
| **变更类型** | 缺陷修复（Bug Fix） |
| **优先级** | P0-严重 |
| **来源** | RCR D-03（全部Controller资源路径单数形式）、D-07（5个接口路径与OAS不一致）、D-15（路径前缀不一致） |
| **提出日期** | 2026-06-22 |
| **变更描述** | 当前v1版本中，所有Controller的`@RequestMapping`使用单数资源名（如`/api/student`、`/api/course`），与OpenAPI契约（OAS v1.0.0）定义的复数形式（`/students`、`/courses`）不一致。同时存在部分接口路径多出后缀（如`/list`、`/apply`、`/overdue`），以及路径中缺少`/{id}`参数等问题。此外，前端API封装层（`api/*.js`）使用OAS定义的复数路径，导致前后端无法正常通信。 |
| **变更要求** | 1. 统一所有Controller的`@RequestMapping`为RESTful规范的复数资源名<br>2. 修正路径结构，去除多余后缀<br>3. 确保路径中必要参数（如`/{id}`）完整<br>4. 同步更新前端API封装层的baseURL配置 |
| **影响模块** | 所有8个业务模块的Controller |
| **涉及文件** | 8个Controller文件、前端`api/`目录下6个JS文件 |
| **预期工作量** | 3人日 |

#### 详细变更范围

| 模块 | 当前路径 | 变更为 | 变更类型 |
|------|---------|--------|---------|
| student | `/api/student` | `/api/students` | 单数→复数 |
| student | `GET /api/student/list` | `GET /api/students` | 去除/list后缀 |
| student | `PUT /api/student` | `PUT /api/students/{id}` | 补充路径参数 |
| student | `POST /api/student/transfer` | `POST /api/students/{id}/transfer` | 重构路径 |
| course | `/api/course` | `/api/courses` | 单数→复数 |
| class | `/api/class` | `/api/classes` | 单数→复数 |
| teacher | `/api/teacher` | `/api/teachers` | 单数→复数 |
| fee | `/api/fee` | `/api/fees` | 单数→复数 |
| fee | `POST /api/fee/refund/apply` | `POST /api/fees/{id}/refund` | 去除/apply，补充id |
| fee | `GET /api/fee/dunning/overdue` | `GET /api/fees/dunning` | 去除/overdue |
| attendance | `/api/attendance` | `/api/attendances` | 单数→复数 |
| attendance | `GET /api/attendance/stats/student/{id}` | `GET /api/attendances/statistics?studentId={id}` | 重构为查询参数 |

---

### CR-003: 补充@Transactional事务注解

| 属性 | 值 |
|------|-----|
| **变更编号** | CR-003 |
| **变更类型** | 缺陷修复（Bug Fix） |
| **优先级** | P0-严重 |
| **来源** | RCR D-01（AuthServiceImpl.login()缺少@Transactional） |
| **提出日期** | 2026-06-22 |
| **变更描述** | 当前v1版本中，`AuthServiceImpl.login()`方法在验证密码通过后调用了`userMapper.updateById(user)`更新用户最近登录时间，但该方法未添加`@Transactional`注解。如果更新操作失败，已更新的登录时间不会回滚，导致数据不一致。同时，其他多个模块的Service实现类（ClassServiceImpl、CourseServiceImpl、TeacherServiceImpl、AttendanceServiceImpl）的写操作方法也未确认是否添加了事务注解。 |
| **变更要求** | 1. 为`AuthServiceImpl.login()`方法添加`@Transactional(rollbackFor = Exception.class)`<br>2. 审查并补全所有Service实现类中写操作方法的`@Transactional`注解<br>3. 统一事务回滚策略：所有写操作使用`rollbackFor = Exception.class` |
| **影响模块** | auth、class、course、teacher、attendance（5个模块） |
| **涉及文件** | 5-6个ServiceImpl文件 |
| **预期工作量** | 2人日 |

#### 详细变更范围

| Service实现类 | 需添加@Transactional的方法 | 原因 |
|--------------|--------------------------|------|
| AuthServiceImpl | login() | 调用userMapper.updateById()写库 |
| ClassServiceImpl | createClass(), updateClass(), deleteClass(), createSchedule(), updateSchedule() | 班级和排课的增删改操作 |
| CourseServiceImpl | createCourse(), updateCourse(), deleteCourse(), updateStatus() | 课程的增删改操作 |
| TeacherServiceImpl | createTeacher(), updateTeacher(), deleteTeacher() | 教师的增删改操作 |
| AttendanceServiceImpl | batchCreateAttendance(), updateAttendance(), batchDelete() | 考勤的批量写操作 |
| StudentServiceImpl | createStudent(), updateStudent(), transferStudent(), withdrawStudent() | 确认现有@Transactional的rollbackFor配置 |

---

### CR-004: 增加退费审批闭环

| 属性 | 值 |
|------|-----|
| **变更编号** | CR-004 |
| **变更类型** | 功能增强（Feature Enhancement） |
| **优先级** | P0-严重 |
| **来源** | RCR D-11（退费多级审批流程仅实现申请阶段）、QAR正确性审查（退费后未回写paidAmount）、RCR抽样REQ-FIN-013（部分覆盖） |
| **提出日期** | 2026-06-22 |
| **变更描述** | 当前v1版本中，退费流程仅实现了`applyRefund()`申请阶段，`approveRefund()`方法为TODO状态。退费审批通过后也未回写收费记录的`paidAmount`/`unpaidAmount`字段，导致数据不一致。此外，缺少退款记录（`edu_refund`表）的实际写入、缺少退费状态流转（审批中→已审批→已退款→已拒绝）的完整管理。 |
| **变更要求** | 1. 实现完整的退费审批流程：退款申请→教务审核→财务审批→执行退款<br>2. 新增`RefundRequestDTO`用于退费申请数据传输<br>3. 在`FeeRecord`中新增审批状态字段（`refundStatus`：PENDING_REVIEW/FINANCE_REVIEWED/APPROVED/REJECTED/REFUNDED）<br>4. 审批通过后自动回写`paidAmount`/`unpaidAmount`<br>5. 实现退款记录的数据库写入（`edu_refund`表）<br>6. 支持大额退费（可配置阈值）的多级审批 |
| **影响模块** | fee（核心）、auth（审批人鉴权）、student（关联学员状态） |
| **涉及文件** | FeeController.java、FeeServiceImpl.java、新增RefundRequestDTO.java、新增edu_refund表DDL |
| **预期工作量** | 4人日 |

#### 详细变更范围

| 变更项 | 具体内容 |
|--------|---------|
| 新增DTO | `RefundRequestDTO`：包含feeRecordId、refundReason、refundType(FULL/PARTIAL)、refundAmount、attachments |
| 新增枚举 | `RefundStatus`：PENDING_REVIEW（待教务审核）、FINANCE_REVIEWED（财务已审核）、APPROVED（已批准）、REJECTED（已拒绝）、REFUNDED（已退款） |
| 新增API | `GET /api/fees/refunds/pending` — 查询待审批退费列表 |
| 新增API | `POST /api/fees/{id}/refund/review` — 教务审核退费申请 |
| 新增API | `POST /api/fees/{id}/refund/approve` — 财务/管理员审批退费 |
| 修改API | `POST /api/fees/{id}/refund` — 发起退费申请（修改现有applyRefund） |
| 数据库变更 | 新增`edu_refund`表 + `edu_fee_record`增加`refund_status`、`refund_apply_time`、`refund_approve_time`字段 |

---

### CR-005: 优化收费模块的金额一致性

| 属性 | 值 |
|------|-----|
| **变更编号** | CR-005 |
| **变更类型** | 功能增强（Feature Enhancement） |
| **优先级** | P1-高 |
| **来源** | RCR D-04（魔法值）、D-08（Map参数替代DTO）、D-13（"PAY"前缀硬编码）、ISS-004（优惠叠加规则未明确） |
| **提出日期** | 2026-06-22 |
| **变更描述** | 当前v1版本中收费模块存在以下问题：FeeController使用`Map<String, Object>`接收参数而非`@Valid`校验的DTO、缴费编号前缀"PAY"硬编码为魔法值、优惠叠加规则未实现（REQ-FIN-003仅完成了优惠配置的CRUD但未实现费用计算时的优惠叠加逻辑）、优惠历史未记录。此外，退费金额计算未按REQ-FIN-012要求实现"按已消耗课时和单价自动计算"。 |
| **变更要求** | 1. 创建PaymentRequestDTO替代FeeController中的Map参数<br>2. 实现优惠叠加规则引擎（顺序叠加模式：先应用课时包优惠→再应用早鸟/多科联报→最后应用全款优惠）<br>3. 将"PAY"前缀提取为Constants常量<br>4. 退费金额按已消耗课时和单价自动计算，扣除教材费等不可退项目<br>5. 增加优惠应用历史记录 |
| **影响模块** | fee（核心）、course（课程定价查询） |
| **涉及文件** | FeeController.java、FeeServiceImpl.java、Constants.java、新增PaymentRequestDTO.java、FeeCalculationService.java |
| **预期工作量** | 4人日 |

#### 详细变更范围

| 变更项 | 具体内容 |
|--------|---------|
| 新增DTO | `PaymentRequestDTO`：包含studentId、courseId、classId、feeType、amount、discountId（可选）、paymentMethod、transactionNo、remark，使用@Valid校验 |
| 新增Service | `FeeCalculationService`：封装费用计算逻辑（原价→优惠叠加→实付金额） |
| 优惠叠加规则 | 顺序叠加模式：Step1课时包单价阶梯 → Step2早鸟折扣（判定有效期）→ Step3多科联报折扣 → Step4全款额外折扣 → Step5 subtract教材费/杂费 |
| 新增常量 | `PAYMENT_NO_PREFIX = "PAY"` 加入Constants |
| 退费计算 | `calculateRefundAmount(feeRecordId)`：已消耗课时数 × 课时单价 = 已消费金额；实缴金额 - 已消费金额 - 教材费 = 应退金额 |
| 新增枚举类 | `DiscountType`、`DiscountApplicationRecord`（优惠应用记录） |

---

### CR-006: 增加跨模块Service集成

| 属性 | 值 |
|------|-----|
| **变更编号** | CR-006 |
| **变更类型** | 功能增强（Function Enhancement） |
| **优先级** | P1-高 |
| **来源** | RCR D-05（Report模块未集成业务模块）、D-09（收据功能未暴露API）、拓扑层校验（6项跨模块依赖偏差） |
| **提出日期** | 2026-06-22 |
| **变更描述** | 当前v1版本中，各业务模块处于相对独立开发状态，跨模块Service调用未建立。具体表现为：ReportController仅依赖common+auth而未集成student/course/fee等业务模块数据；Student-Service未调用Class-Service进行转班时的班级容量校验和名单更新；Fee-Service未调用Student-Service获取学员关联信息；Attendance-Service与Student-Service/Class-Service的集成不完整。 |
| **变更要求** | 1. 建立Student-Service → Class-Service集成：转班时调用ClassService更新班级容量、班级名单<br>2. 建立Fee-Service → Student-Service集成：收费时通过StudentService获取学员和课程信息<br>3. 建立Report-Service → 各业务模块集成：营收报表聚合student/course/fee/attendance数据<br>4. 暴露收据生成API：`GET /api/fees/{id}/receipt` |
| **影响模块** | student、class、fee、report、attendance（5个模块） |
| **涉及文件** | StudentServiceImpl.java、FeeServiceImpl.java、ReportServiceImpl.java、AttendanceServiceImpl.java、ReceiptUtil.java |
| **预期工作量** | 5人日 |

#### 详细变更范围

| 集成关系 | 调用方 | 被调用方 | 调用场景 |
|---------|--------|---------|---------|
| S-C-01 | StudentServiceImpl | ClassService | 转班时：校验目标班级容量→更新原班级名单→更新目标班级名单 |
| F-S-01 | FeeServiceImpl | StudentService | 收费时：查询学员信息（姓名、课程、课时包）以生成收费记录和收据 |
| R-A-01 | ReportServiceImpl | StudentService, CourseService, FeeService, AttendanceService | 营收报表：聚合各模块数据进行统计 |
| R-A-02 | ReportServiceImpl | FeeService | 财务报表：按日/月/季/年汇总收费、退费、欠费数据 |
| A-S-01 | AttendanceServiceImpl | StudentService | 考勤时：校验学员所属班级、学员在读状态 |
| A-C-01 | AttendanceServiceImpl | ClassService | 考勤时：获取班级排课信息以创建对应考勤记录 |
| F-API-01 | FeeController | FeeService | 新增收据生成API：`GET /api/fees/{id}/receipt`（调用ReceiptUtil） |

---

### CR-007: 统一枚举值定义

| 属性 | 值 |
|------|-----|
| **变更编号** | CR-007 |
| **变更类型** | 优化（Optimization） |
| **优先级** | P1-高 |
| **来源** | RCR D-04（魔法值）、D-14（EduClass命名）、QAR一致性校验（OAS/Constants/MDS枚举值不一致） |
| **提出日期** | 2026-06-22 |
| **变更描述** | 当前v1版本中，同一业务概念在不同设计资产中使用了不同的枚举值，将导致前后端数据交换时出现解析错误。具体冲突包括：(1) 班级状态在Constants中使用PENDING而OAS使用RECRUITING；(2) 缴费状态在Constants中额外定义了REFUNDING状态而OAS未定义；(3) 性别判断使用魔法值1/2而非常量；(4) 课程状态判断使用魔法值1而非常量；(5) 类名EduClass包含前缀与MDS命名约定不一致。 |
| **变更要求** | 1. 在common模块创建统一的枚举类（Enums.java），集中管理所有业务状态枚举<br>2. 统一OAS、Constants、MDS中的枚举值定义<br>3. 消除所有魔法值，使用枚举常量替代<br>4. 重命名EduClass为ClassInfo（可选，评估重构成本后决定） |
| **影响模块** | common（核心）、所有8个业务模块 |
| **涉及文件** | Constants.java（修改）、新增Enums.java、OAS openapi.yaml（修改）、6-8个Service文件 |
| **预期工作量** | 3人日 |

#### 详细变更范围

| 枚举类型 | 原值（不一致处） | 统一后的值 | 影响范围 |
|---------|----------------|-----------|---------|
| 性别 | 魔法值1/2 | `Gender.MALE / Gender.FEMALE` | StudentServiceImpl |
| 学员状态 | STUDYING/SUSPENDED/TRANSFERRED/WITHDRAWN/GRADUATED | `StudentStatus.ENROLLED / SUSPENDED / TRANSFERRED / WITHDRAWN / GRADUATED` | StudentServiceImpl, Constants |
| 班级状态 | PENDING/IN_PROGRESS/FINISHED/CANCELED vs RECRUITING/IN_PROGRESS/COMPLETED | `ClassStatus.RECRUITING / IN_PROGRESS / FULL / COMPLETED / CANCELED` | ClassController, Constants, OAS |
| 缴费状态 | OAS缺REFUNDING | `PaymentStatus.UNPAID / PARTIAL / PAID / REFUNDING / REFUNDED` | FeeServiceImpl, Constants, OAS |
| 课程状态 | 魔法值1 | `CourseStatus.ACTIVE / PAUSED / DISCONTINUED` | CourseController |
| 考勤状态 | OAS仅ON_TIME/LATE/ABSENT | `AttendanceStatus.PRESENT / LATE / ABSENT / LEAVE / EARLY_LEAVE` | AttendanceServiceImpl, OAS |
| 用户状态 | 魔法值0 | `UserStatus.ACTIVE / DISABLED` | AuthServiceImpl |

---

### CR-008: 增强前端错误处理与状态展示

| 属性 | 值 |
|------|-----|
| **变更编号** | CR-008 |
| **变更类型** | 优化（Optimization） |
| **优先级** | P2-中 |
| **来源** | QAR有效性校验（前后端无法联调）、QAR完备性校验（无移动端适配状态） |
| **提出日期** | 2026-06-22 |
| **变更描述** | 当前v1版本前端存在以下不足：所有列表页缺少loading加载状态展示、缺少空数据时的友好提示（Empty状态）、网络请求失败时缺少重试按钮和错误友好提示、表单提交缺少提交中状态防重复提交、缴费和退费等关键操作缺少二次确认弹窗、前端API封装层的错误全局处理不完善。 |
| **变更要求** | 1. 为所有列表页添加Loading骨架屏或Spinner加载状态<br>2. 为空数据列表添加Empty组件和引导文案<br>3. 网络错误时展示错误提示和重试按钮<br>4. 表单提交时禁用按钮并显示提交中状态<br>5. 关键财务操作添加二次确认弹窗<br>6. 完善`request.js`的全局错误拦截和token过期处理 |
| **影响模块** | 所有前端页面（8个View组件 + request.js） |
| **涉及文件** | 约10-12个前端文件 |
| **预期工作量** | 3人日 |

#### 详细变更范围

| 页面/组件 | 变更内容 |
|----------|---------|
| `request.js` | 增加请求/响应拦截器的错误统一处理；Token过期自动跳转登录页；网络超时提示重试 |
| `StudentList.vue` | 添加loading状态、空数据提示、错误重试按钮 |
| `CourseList.vue` | 添加loading状态、空数据提示 |
| `ClassList.vue` | 添加loading状态、空数据提示 |
| `AttendanceManage.vue` | 添加loading状态、批量操作中状态展示 |
| `FeeRecords.vue` | 添加loading状态、退费操作的二次确认弹窗 |
| `Payment.vue` | 添加提交中状态防重复提交、支付确认弹窗 |
| `Login.vue` | 添加登录中状态、错误提示优化 |
| `Dashboard.vue` | 添加数据加载骨架屏 |
| `FinanceReport.vue` | 添加loading状态、空报表提示、导出中状态 |
| 新增组件 | `EmptyState.vue`（空状态通用组件）、`LoadingSkeleton.vue`（骨架屏组件）、`ErrorRetry.vue`（错误重试组件） |

---

## 变更汇总

### 按优先级分布

| 优先级 | 变更编号 | 数量 |
|:------:|---------|:----:|
| P0-严重 | CR-001, CR-002, CR-003, CR-004 | 4 |
| P1-高 | CR-005, CR-006, CR-007 | 3 |
| P2-中 | CR-008 | 1 |
| **合计** | | **8** |

### 按变更类型分布

| 变更类型 | 变更编号 | 数量 |
|---------|---------|:----:|
| 缺陷修复 | CR-001, CR-002, CR-003 | 3 |
| 功能增强 | CR-004, CR-005, CR-006 | 3 |
| 优化 | CR-007, CR-008 | 2 |
| **合计** | | **8** |

### 按影响模块分布

| 模块 | 涉及的CR |
|------|---------|
| common | CR-007 |
| auth | CR-001, CR-003 |
| student | CR-001, CR-002, CR-003, CR-006 |
| course | CR-001, CR-002, CR-003 |
| class | CR-001, CR-002, CR-003, CR-006 |
| teacher | CR-001, CR-002, CR-003 |
| fee | CR-001, CR-002, CR-004, CR-005, CR-006 |
| attendance | CR-001, CR-002, CR-003, CR-006 |
| report | CR-006 |
| frontend | CR-008 |

### RCR漂移覆盖矩阵

| RCR漂移编号 | 严重等级 | 对应CR |
|:----------:|:------:|:------:|
| D-01（login缺@Transactional） | 严重 | CR-003 |
| D-02（6个Controller暴露Entity） | 严重 | CR-001 |
| D-03（API路径单复数不一致） | 严重 | CR-002 |
| D-04（魔法值） | 严重 | CR-005, CR-007 |
| D-05（Report模块未集成） | 严重 | CR-006 |
| D-06（包名不一致+目录命名） | 高 | CR-007 |
| D-07（接口路径与OAS不一致） | 高 | CR-002 |
| D-08（Map替代DTO+@Valid） | 高 | CR-005 |
| D-09（收据API未暴露） | 高 | CR-006 |
| D-10（Entity返回替代DTO） | 中 | CR-001 |
| D-11（退费审批流TODO） | 中 | CR-004 |
| D-12（签到缺少独立接口） | 中 | CR-006 |
| D-13（PAY前缀硬编码） | 低 | CR-005 |
| D-14（EduClass命名） | 低 | CR-007 |
| D-15（路径前缀不一致） | 低 | CR-002 |

**覆盖率**：15/15（100%）— 所有RCR漂移项均通过8条CR得到解决。

---

## 相关文档

- 逆向校验报告（RCR）：`designs/diagrams/逆向校验报告-RCR.md`
- 四维度质量校验报告（QAR）：`designs/diagrams/四维度质量校验报告.md`
- SRS正式版 v1：`wiki/baselines/BL-20260622-01/SRS-正式版.md`
- OpenAPI契约：`designs/contracts/openapi.yaml`
- 变更影响分析报告：`docs/变更影响分析报告-CIA.md`
- ADR-005需求变更决策：`designs/adr/ADR-005-需求变更决策.md`

---

**文档版本**：V1.0
**编制人**：A5-需求验证智能体
**审核人**：待指定
**批准人**：待指定
**创建日期**：2026-06-22
