# 变更影响分析报告（Change Impact Analysis）

**项目名称**：教育培训机构教务收费管理系统（EduFeeMS）
**文档编号**：CIA-V1-V2-001
**基线版本**：BL-20260622-01 → BL-20260622-02
**变更来源**：变更需求文档（CR-001至CR-008）
**编制日期**：2026-06-22
**文档状态**：已完成

---

## 1. 分析概述

本报告对8条变更需求（CR-001至CR-008）进行九维影响评估，包括：代码变更范围、接口变更范围、数据库变更范围、前端变更范围、测试回归范围、文档更新范围、跨模块影响传播、风险评估、工作量估算。每条CR逐项分析，最后汇总交叉影响。

---

## 2. 九维影响评估矩阵

### 2.1 CR-001：修正Controller直接暴露Entity问题

| 影响维度 | 影响详情 |
|---------|---------|
| **代码变更范围** | 8个Controller文件（CourseController、ClassController、TeacherController、FeeController、AttendanceController、AuthController需修改返回类型）；新增6个DTO/VO类（CourseDTO、ClassDTO、TeacherDTO、FeeRecordDTO、AttendanceDTO、UserDTO）；各Service层新增Entity→DTO转换方法 |
| **接口变更范围** | 所有Controller的返回响应结构变更：Entity字段→DTO字段。影响18+个API端点的响应Schema。前端需同步更新期望的响应字段结构 |
| **数据库变更范围** | 无直接数据库变更。但DTO层的引入可能需要在Service层增加字段屏蔽逻辑（如password、deleted等敏感字段不映射到DTO） |
| **前端变更范围** | 6个前端View组件和对应API调用可能需调整响应数据解析路径（StudentController已使用DTO无需变更）。fee.js、course.js、class.js、teacher.js、attendance.js、auth.js共6个API封装文件 |
| **测试回归范围** | 所有Controller单元测试需重写（Mock返回类型从Entity变为DTO）；Service层转换方法的单元测试；集成测试中API响应断言的字段路径需更新 |
| **文档更新范围** | OAS（openapi.yaml）需更新所有接口的响应Schema定义，使其与DTO字段对齐；MDS需补充DTO目录结构说明 |
| **跨模块影响传播** | 中等。StudentController已使用DTO（低影响）；FeeController的DTO变更会传导至report模块（如果report模块引用了FeeRecord实体作为数据源）；AuthController的UserDTO变更会影响前端用户状态的全局store |
| **风险评估** | **中风险**。变更量大（6个Controller+18+接口），兼容性风险高（前端需同步更新），但风险可控——不涉及业务逻辑变更，仅数据传输层转换。回滚成本中等 |
| **工作量估算** | 后端5人日 + 前端2人日 = **7人日** |

#### 变更文件清单

```
需修改文件（后端）：
  src/backend/edu-course/.../controller/CourseController.java
  src/backend/edu-class/.../controller/ClassController.java
  src/backend/edu-teacher/.../controller/TeacherController.java
  src/backend/edu-fee/.../controller/FeeController.java
  src/backend/edu-attendance/.../controller/AttendanceController.java
  src/backend/edu-auth/.../controller/AuthController.java
  src/backend/edu-fee/.../service/impl/FeeServiceImpl.java（新增DTO转换方法）
  src/backend/edu-course/.../service/impl/CourseServiceImpl.java（新增DTO转换方法）
  src/backend/edu-class/.../service/impl/ClassServiceImpl.java（新增DTO转换方法）
  src/backend/edu-teacher/.../service/impl/TeacherServiceImpl.java（新增DTO转换方法）
  src/backend/edu-attendance/.../service/impl/AttendanceServiceImpl.java（新增DTO转换方法）
  src/backend/edu-auth/.../service/impl/AuthServiceImpl.java（新增DTO转换方法）

新增文件（后端）：
  src/backend/edu-course/.../dto/CourseDTO.java
  src/backend/edu-class/.../dto/ClassDTO.java
  src/backend/edu-teacher/.../dto/TeacherDTO.java
  src/backend/edu-fee/.../dto/FeeRecordDTO.java
  src/backend/edu-attendance/.../dto/AttendanceDTO.java
  src/backend/edu-auth/.../dto/UserDTO.java

需修改文件（前端）：
  src/frontend/src/api/course.js
  src/frontend/src/api/class.js
  src/frontend/src/api/teacher.js
  src/frontend/src/api/fee.js
  src/frontend/src/api/attendance.js
  src/frontend/src/api/auth.js
  src/frontend/src/views/course/CourseList.vue
  src/frontend/src/views/class/ClassList.vue
  src/frontend/src/views/fee/FeeRecords.vue
  src/frontend/src/views/fee/Payment.vue
  src/frontend/src/views/attendance/AttendanceManage.vue
  src/frontend/src/stores/user.js
```

---

### 2.2 CR-002：统一API路径命名规范

| 影响维度 | 影响详情 |
|---------|---------|
| **代码变更范围** | 8个Controller的`@RequestMapping`注解路径；6个Controller中部分方法的`@PostMapping`/`@GetMapping`路径；3个Controller方法的路径结构重构（补充`/{id}`参数） |
| **接口变更范围** | 15+个API端点的URL路径变更（均为不兼容变更）。所有API从单数路径变更为复数路径，部分路径去除多余后缀，部分路径增加`/{id}`参数 |
| **数据库变更范围** | 无 |
| **前端变更范围** | 所有API调用路径需同步修改。6个API封装文件（js）中的请求URL需更新。前端路由配置可能需要调整 |
| **测试回归范围** | 所有Controller集成测试的请求URL需更新；端到端测试的API路径需更新 |
| **文档更新范围** | OAS（openapi.yaml）需与最终路径对齐确认（当前OAS已是复数形式，该变更实际上是让代码对齐OAS）；MDS需更新API命名规范说明；部署文档中的API base URL说明 |
| **跨模块影响传播** | **高**。这是全局性变更，影响所有前端和后端的API路径约定。变更后可以实现前后端正常联调（当前状态下前后端无法通信） |
| **风险评估** | **高风险**。此变更是破坏性变更（Breaking Change），所有API路径改变，前后端必须同步上线。但风险可控——当前v1状态下前后端本就无法正常通信，变更后反而修复了此问题 |
| **工作量估算** | 后端2人日 + 前端1人日 = **3人日** |

#### 变更文件清单

```
需修改文件（后端）：
  src/backend/edu-student/.../controller/StudentController.java  — @RequestMapping + 路径调整
  src/backend/edu-course/.../controller/CourseController.java    — @RequestMapping
  src/backend/edu-class/.../controller/ClassController.java      — @RequestMapping
  src/backend/edu-teacher/.../controller/TeacherController.java  — @RequestMapping
  src/backend/edu-fee/.../controller/FeeController.java          — @RequestMapping + refund/dunning路径调整
  src/backend/edu-attendance/.../controller/AttendanceController.java — @RequestMapping + stats路径调整
  src/backend/edu-auth/.../controller/AuthController.java        — @RequestMapping
  src/backend/edu-report/.../controller/ReportController.java    — @RequestMapping

需修改文件（前端）：
  src/frontend/src/api/student.js      — 所有请求URL
  src/frontend/src/api/course.js       — 所有请求URL
  src/frontend/src/api/class.js        — 所有请求URL
  src/frontend/src/api/teacher.js      — 所有请求URL
  src/frontend/src/api/fee.js          — 所有请求URL（含refund/dunning子路径）
  src/frontend/src/api/attendance.js   — 所有请求URL
  src/frontend/src/api/auth.js         — 所有请求URL
  src/frontend/src/api/request.js      — baseURL配置确认
```

---

### 2.3 CR-003：补充@Transactional事务注解

| 影响维度 | 影响详情 |
|---------|---------|
| **代码变更范围** | 5-6个ServiceImpl文件（AuthServiceImpl、ClassServiceImpl、CourseServiceImpl、TeacherServiceImpl、AttendanceServiceImpl），每个文件1-4个方法的注解添加 |
| **接口变更范围** | 无（事务注解不影响API契约） |
| **数据库变更范围** | 无 |
| **前端变更范围** | 无 |
| **测试回归范围** | 事务相关集成测试（验证回滚行为）；AuthServiceImpl.login()的异常场景测试（更新登录时间失败时是否回滚） |
| **文档更新范围** | TLCD文档（三层约束设计）中C-ARCH-004的验证状态更新 |
| **跨模块影响传播** | 低。事务注解仅影响单个模块内的数据一致性，不产生跨模块传播 |
| **风险评估** | **低风险**。变更范围明确，仅添加注解，不修改业务逻辑。但需注意：如果方法内部存在嵌套事务调用或多数据源场景，需确认事务传播行为 |
| **工作量估算** | 后端1人日 + 测试0.5人日 = **1.5人日** |

#### 变更文件清单

```
需修改文件（后端）：
  src/backend/edu-auth/.../service/impl/AuthServiceImpl.java          — login()添加@Transactional
  src/backend/edu-class/.../service/impl/ClassServiceImpl.java         — 3-4个写方法添加@Transactional
  src/backend/edu-course/.../service/impl/CourseServiceImpl.java       — 4个写方法添加@Transactional
  src/backend/edu-teacher/.../service/impl/TeacherServiceImpl.java     — 3个写方法添加@Transactional
  src/backend/edu-attendance/.../service/impl/AttendanceServiceImpl.java — 3个写方法添加@Transactional
  src/backend/edu-student/.../service/impl/StudentServiceImpl.java     — 审查确认现有@Transactional的rollbackFor配置
```

---

### 2.4 CR-004：增加退费审批闭环

| 影响维度 | 影响详情 |
|---------|---------|
| **代码变更范围** | FeeController新增3个审批相关接口；FeeServiceImpl重写approveRefund()逻辑，新增回写收费记录金额逻辑；新增RefundRequestDTO；新增RefundStatus枚举；新增审批通知服务调用 |
| **接口变更范围** | 新增3个审批API：GET /api/fees/refunds/pending、POST /api/fees/{id}/refund/review、POST /api/fees/{id}/refund/approve；修改现有POST /api/fees/{id}/refund接口的请求参数（从Map改为RefundRequestDTO） |
| **数据库变更范围** | edu_fee_record表新增refund_status、refund_apply_time、refund_review_time、refund_approve_time、refund_reviewer_id、refund_approver_id字段；新增edu_refund表（id、fee_record_id、refund_amount、refund_method、refund_transaction_no、refund_time、operator_id、created_at） |
| **前端变更范围** | FeeRecords.vue需增加退费审批列表Tab/区域；新增退费详情审批弹窗；Payment.vue需配合退费申请的请求参数调整 |
| **测试回归范围** | FeeServiceImpl.applyRefund()的单元测试；新增审批流程的Service和Controller测试；集成测试验证完整的退费审批流程（申请→教务审核→财务审批→退款执行）；退费回写paidAmount/unpaidAmount的金额校验 |
| **文档更新范围** | OAS新增3个审批接口定义；SRS中REQ-FIN-013从"部分覆盖"更新为"完全覆盖"；MDS中fee模块的API清单更新；活动图（fee-management.puml）更新退费审批分支 |
| **跨模块影响传播** | **中高**。退费审批涉及auth模块（审批人权限校验）、student模块（关联学员状态——退费后学员状态是否变更）；审批通知需触发通知模块（向家长和教务发送通知） |
| **风险评估** | **中风险**。退费涉及资金操作，金额计算和审批流程的正确性至关重要。数据库新增字段需确保向后兼容（v1的edu_fee_record记录中refund_status默认为NULL，需处理为"无退费申请"语义）。多级审批的状态流转需严格校验 |
| **工作量估算** | 后端3人日 + 前端1人日 + 测试1人日 = **5人日** |

#### 变更文件清单

```
需修改文件（后端）：
  src/backend/edu-fee/.../controller/FeeController.java           — 新增3个审批接口
  src/backend/edu-fee/.../service/FeeService.java                 — 新增审批接口方法声明
  src/backend/edu-fee/.../service/impl/FeeServiceImpl.java         — 实现审批流程+回写逻辑
  src/backend/edu-fee/.../entity/FeeRecord.java                   — 新增refund相关字段
  src/backend/sql/init.sql                                        — 新增edu_refund表DDL + edu_fee_record ALTER

新增文件（后端）：
  src/backend/edu-fee/.../dto/RefundRequestDTO.java
  src/backend/edu-common/.../enums/RefundStatus.java（或并入统一Enums.java）

需修改文件（前端）：
  src/frontend/src/views/fee/FeeRecords.vue                       — 增加审批列表/操作区域
  src/frontend/src/views/fee/Payment.vue                          — 调整退费申请参数
  src/frontend/src/api/fee.js                                     — 新增审批API方法
```

---

### 2.5 CR-005：优化收费模块的金额一致性

| 影响维度 | 影响详情 |
|---------|---------|
| **代码变更范围** | FeeController.makePayment()和applyRefund()的参数从Map改为DTO；FeeServiceImpl新增优惠叠加计算逻辑；Constants.java新增PAYMENT_NO_PREFIX常量；新增PaymentRequestDTO、FeeCalculationService |
| **接口变更范围** | POST /api/fees/payment接口的请求参数结构从Map变更为PaymentRequestDTO（破坏性变更）；POST /api/fees/{id}/refund的退费金额计算逻辑从手工输入改为自动计算+人工确认 |
| **数据库变更范围** | 可能需要新增edu_discount_application_record表记录优惠应用历史（非强制，建议） |
| **前端变更范围** | Payment.vue的表单字段和提交数据结构需与PaymentRequestDTO对齐；退费金额展示从纯输入框改为"自动计算金额+确认" |
| **测试回归范围** | FeeServiceImpl.createFeeRecord/makePayment的单元测试参数调整；新增优惠叠加规则的单元测试（覆盖多种优惠组合场景）；退费金额自动计算的正确性测试 |
| **文档更新范围** | OAS更新Payment接口的requestBody Schema；SRS中REQ-FIN-003从"部分覆盖"更新为"完全覆盖" |
| **跨模块影响传播** | 中等。优惠计算需查询Course模块的课时包定价和优惠配置；退费金额计算需查询Attendance模块的已消耗课时数 |
| **风险评估** | **中风险**。费用计算是核心业务逻辑，优惠叠加规则的实现需严格测试。破坏性变更（参数结构改变）需前端同步上线 |
| **工作量估算** | 后端2人日 + 前端1人日 + 测试1人日 = **4人日** |

#### 变更文件清单

```
需修改文件（后端）：
  src/backend/edu-fee/.../controller/FeeController.java           — payment/refund方法参数改为DTO
  src/backend/edu-fee/.../service/impl/FeeServiceImpl.java         — 实现优惠叠加+退费自动计算
  src/backend/edu-common/.../Constants.java                       — 新增PAYMENT_NO_PREFIX

新增文件（后端）：
  src/backend/edu-fee/.../dto/PaymentRequestDTO.java
  src/backend/edu-fee/.../service/FeeCalculationService.java      — 费用计算引擎
  src/backend/edu-fee/.../service/impl/FeeCalculationServiceImpl.java
  src/backend/edu-fee/.../entity/DiscountApplicationRecord.java   — 优惠应用历史（可选）

需修改文件（前端）：
  src/frontend/src/views/fee/Payment.vue                          — 表单字段对齐PaymentRequestDTO
  src/frontend/src/api/fee.js                                     — 更新请求参数结构
```

---

### 2.6 CR-006：增加跨模块Service集成

| 影响维度 | 影响详情 |
|---------|---------|
| **代码变更范围** | StudentServiceImpl增加ClassService依赖注入和调用；FeeServiceImpl增加StudentService依赖注入；ReportServiceImpl增加StudentService/CourseService/FeeService/AttendanceService依赖注入；AttendanceServiceImpl增加StudentService/ClassService依赖注入；FeeController新增收据生成API |
| **接口变更范围** | 新增1个收据API：GET /api/fees/{id}/receipt；转班、收费、报表统计等现有接口的后端行为变更（非契约变更，但业务逻辑更完整） |
| **数据库变更范围** | 无直接数据库变更（收据生成为应用层逻辑） |
| **前端变更范围** | FeeRecords.vue新增"查看/下载收据"按钮；报表页面（FinanceReport.vue）数据将从后端真实聚合而非mock数据 |
| **测试回归范围** | StudentServiceImpl.transferStudent()增加班级容量和名单更新的集成测试；ReportServiceImpl各报表方法的集成测试；收据生成API的响应格式和PDF内容测试 |
| **文档更新范围** | MDS更新模块依赖拓扑（DTS）中的依赖关系确认；OAS新增收据接口定义 |
| **跨模块影响传播** | **高**。跨模块集成本身就是建立模块间的服务调用关系，需要谨慎处理循环依赖问题（例如Student模块注入Class模块、Fee模块注入Student模块，需确认依赖方向不产生循环） |
| **风险评估** | **中高风险**。跨模块Service注入可能引入循环依赖、启动顺序问题。需在`@Autowired`时使用`@Lazy`注解打破潜在的循环依赖。集成测试需覆盖完整的模块间调用链 |
| **工作量估算** | 后端4人日 + 测试1人日 = **5人日** |

#### 变更文件清单

```
需修改文件（后端）：
  src/backend/edu-student/.../service/impl/StudentServiceImpl.java   — 注入ClassService
  src/backend/edu-fee/.../service/impl/FeeServiceImpl.java           — 注入StudentService
  src/backend/edu-fee/.../controller/FeeController.java              — 新增receipt接口
  src/backend/edu-report/.../service/impl/ReportServiceImpl.java     — 注入各业务模块Service
  src/backend/edu-attendance/.../service/impl/AttendanceServiceImpl.java — 注入StudentService, ClassService
  src/backend/edu-fee/.../util/ReceiptUtil.java                      — 通过Controller暴露为API

需修改文件（前端）：
  src/frontend/src/views/fee/FeeRecords.vue                          — 新增收据查看/下载按钮
  src/frontend/src/views/report/FinanceReport.vue                    — 使用真实聚合数据
  src/frontend/src/api/fee.js                                        — 新增receipt API方法
```

---

### 2.7 CR-007：统一枚举值定义

| 影响维度 | 影响详情 |
|---------|---------|
| **代码变更范围** | common模块新增Enums.java统一枚举类；Constants.java移出散落的常量到枚举类；6-8个Service文件中的魔法值替换为枚举常量；EduClass类名重命名评估（低优先级） |
| **接口变更范围** | OAS中枚举值定义需与实际枚举对齐（如班级状态从RECRUITING改为含FULL状态）；API响应中的状态字段值将使用统一的枚举常量 |
| **数据库变更范围** | 无直接数据库变更（数据库存储使用枚举的name()或ordinal()值，需确认数据一致性） |
| **前端变更范围** | 状态映射表需与后端统一枚举值对齐（前端下拉选项、状态标签的颜色和文案） |
| **测试回归范围** | 所有依赖状态判断的单元测试需更新断言中的枚举值；端到端测试中的状态流转验证 |
| **文档更新范围** | OAS openapi.yaml枚举Schema更新；MDS中状态枚举定义更新；TLCD中C-CODE-009的验证状态更新 |
| **跨模块影响传播** | **中高**。枚举统一影响全局——所有模块Controller返回的状态值会因枚举变更而改变。如果前端（包括移动端）依赖了特定的状态字符串，需要同步更新 |
| **风险评估** | **中风险**。枚举值变更属于破坏性变更（Breaking Change）。如果数据库中已存储了旧枚举值的字符串表示（如status字段存储为varchar "PENDING"），需要执行数据迁移脚本将其更新为新的枚举值（如"RECRUITING"）。建议采用枚举值映射兼容策略 |
| **工作量估算** | 后端2人日 + 前端0.5人日 + 数据迁移0.5人日 = **3人日** |

#### 变更文件清单

```
需修改文件（后端）：
  src/backend/edu-common/.../Constants.java                  — 移出枚举类到独立文件
  src/backend/edu-common/.../enums/Enums.java                — 新增统一枚举类（Gender, StudentStatus, ClassStatus,
                                                               PaymentStatus, CourseStatus, AttendanceStatus, UserStatus）
  src/backend/edu-student/.../service/impl/StudentServiceImpl.java     — getStatusText()、convertToDetailResponse()消除魔法值
  src/backend/edu-auth/.../service/impl/AuthServiceImpl.java           — login()中用户状态判断使用枚举
  src/backend/edu-course/.../controller/CourseController.java          — updateStatus()使用枚举常量
  src/backend/edu-fee/.../service/impl/FeeServiceImpl.java             — 缴费编号前缀使用常量
  src/backend/edu-fee/.../service/impl/FeeServiceImpl.java             — 缴费状态使用统一枚举
  src/backend/edu-class/.../controller/ClassController.java            — 班级状态使用统一枚举

需修改文件（文档）：
  designs/contracts/openapi.yaml                                       — 枚举Schema更新

需修改文件（前端）：
  src/frontend/src/views/student/StudentList.vue                       — 状态映射更新
  src/frontend/src/views/course/CourseList.vue                         — 课程状态映射更新
  src/frontend/src/views/class/ClassList.vue                           — 班级状态映射更新
  src/frontend/src/views/fee/FeeRecords.vue                            — 缴费状态映射更新
  src/frontend/src/views/attendance/AttendanceManage.vue               — 考勤状态映射更新
```

---

### 2.8 CR-008：增强前端错误处理与状态展示

| 影响维度 | 影响详情 |
|---------|---------|
| **代码变更范围** | request.js：增强全局错误拦截；8个View组件：添加loading/empty/error状态；Login.vue：登录状态优化 |
| **接口变更范围** | 无（纯前端变更） |
| **数据库变更范围** | 无 |
| **前端变更范围** | 全部前端View组件和request.js。新增3个通用组件（EmptyState、LoadingSkeleton、ErrorRetry） |
| **测试回归范围** | 前端组件渲染测试（各状态的正确展示）；E2E测试（网络错误场景的自动重试行为）；移动端适配验证 |
| **文档更新范围** | 无直接文档更新（前端UX规范可单独记录） |
| **跨模块影响传播** | 低。纯前端体验优化，不涉及后端变更 |
| **风险评估** | **低风险**。纯前端增强，不改变业务逻辑，可独立上线。注意：新增的通用组件需确保在各个页面的样式一致性 |
| **工作量估算** | 前端2.5人日 + 测试0.5人日 = **3人日** |

#### 变更文件清单

```
需修改文件（前端）：
  src/frontend/src/api/request.js                       — 增强错误拦截、token过期处理、超时重试
  src/frontend/src/views/student/StudentList.vue         — loading/empty/error状态
  src/frontend/src/views/student/StudentDetail.vue       — loading/error状态
  src/frontend/src/views/course/CourseList.vue           — loading/empty状态
  src/frontend/src/views/class/ClassList.vue             — loading/empty状态
  src/frontend/src/views/fee/FeeRecords.vue              — loading/empty/二次确认弹窗
  src/frontend/src/views/fee/Payment.vue                 — 提交状态/防止重复提交
  src/frontend/src/views/attendance/AttendanceManage.vue — loading/批量操作中状态
  src/frontend/src/views/report/FinanceReport.vue        — loading/空报表/导出中状态
  src/frontend/src/views/Dashboard.vue                   — 骨架屏加载
  src/frontend/src/views/Login.vue                       — 登录中状态/错误提示

新增文件（前端）：
  src/frontend/src/components/EmptyState.vue
  src/frontend/src/components/LoadingSkeleton.vue
  src/frontend/src/components/ErrorRetry.vue
```

---

## 3. 交叉影响汇总

### 3.1 影响维度汇总

| 维度 | 影响程度 | 说明 |
|------|:------:|------|
| 代码变更范围 | **高** | 涉及30+个后端文件、20+个前端文件 |
| 接口变更范围 | **高** | CR-001和CR-002共同影响18+个API的路径和响应结构 |
| 数据库变更范围 | **中** | CR-004新增表和字段，CR-007可能需数据迁移 |
| 前端变更范围 | **高** | 所有API调用层和View组件均受影响 |
| 测试回归范围 | **高** | 接口契约变更导致大量测试需更新 |
| 文档更新范围 | **高** | OAS、SRS、MDS、DTS、TLCD均需更新 |
| 跨模块影响传播 | **中高** | CR-006直接建立跨模块依赖 |
| 风险评估 | **中** | 破坏性变更集中但可管控 |
| 工作量估算 | **32人日** | 后续章节详述 |

### 3.2 模块变更热力图

| 模块 | CR-001 | CR-002 | CR-003 | CR-004 | CR-005 | CR-006 | CR-007 | CR-008 | 变更密度 |
|------|:------:|:------:|:------:|:------:|:------:|:------:|:------:|:------:|:------:|
| common | | | | | | | **H** | | 中 |
| auth | **M** | **M** | **H** | | | | **M** | | 中 |
| student | **L** | **M** | **L** | | | **H** | **M** | | 中高 |
| course | **H** | **M** | **H** | | | | **M** | | 中高 |
| class | **H** | **M** | **H** | | | **H** | **M** | | 高 |
| teacher | **H** | **M** | **H** | | | | **M** | | 中高 |
| fee | **H** | **M** | | **H** | **H** | **H** | **M** | | **极高** |
| attendance | **H** | **M** | **H** | | | **H** | **M** | | 高 |
| report | | **M** | | | | **H** | | | 中 |
| frontend | **H** | **H** | | **M** | **M** | **M** | **M** | **H** | **极高** |

*图例：H=高影响，M=中影响，L=低影响，空白=无影响*

### 3.3 CR间依赖关系

```
CR-001（Entity→DTO） ←→ CR-002（API路径统一） — 接口层变更需协同
CR-004（退费闭环） → CR-005（金额一致性） — 退费金额计算依赖CR-005的计算引擎
CR-006（跨模块集成） → CR-004（退费闭环） — 退费审批需StudentService集成
CR-007（枚举统一） ← CR-001（Entity→DTO） — DTO中的状态字段类型统一为枚举
CR-008（前端增强） → CR-001+CR-002 — 前端需在后端接口稳定后适配

建议实施顺序：
  第一批（基础设施）：CR-003（事务）、CR-007（枚举统一）
  第二批（接口层）：  CR-001（DTO）+ CR-002（路径）— 同时进行
  第三批（业务层）：  CR-005（金额）+ CR-004（退费）+ CR-006（集成）— 依次进行
  第四批（体验层）：  CR-008（前端增强）— 在前三批完成后进行
```

---

## 4. 工作量估算汇总

| CR编号 | CR名称 | 后端(人日) | 前端(人日) | 测试(人日) | 合计(人日) |
|:------:|------|:--------:|:--------:|:--------:|:--------:|
| CR-001 | 修正Controller暴露Entity | 5.0 | 2.0 | — | 7.0 |
| CR-002 | 统一API路径命名 | 2.0 | 1.0 | — | 3.0 |
| CR-003 | 补充@Transactional | 1.0 | 0 | 0.5 | 1.5 |
| CR-004 | 退费审批闭环 | 3.0 | 1.0 | 1.0 | 5.0 |
| CR-005 | 收费金额一致性 | 2.0 | 1.0 | 1.0 | 4.0 |
| CR-006 | 跨模块Service集成 | 4.0 | 0 | 1.0 | 5.0 |
| CR-007 | 统一枚举值定义 | 2.0 | 0.5 | 0.5 | 3.0 |
| CR-008 | 前端错误处理 | 0 | 2.5 | 0.5 | 3.0 |
| **合计** | | **19.0** | **8.0** | **4.5** | **31.5** |

**总计**：约 **32人日**（含文档更新和代码审查）

*测试工作量未单独计入CR-001/CR-002，因其测试回归将在整体回归校验(CRR)中统一覆盖。*

---

## 5. 风险矩阵

| 风险编号 | 风险描述 | 影响CR | 概率 | 影响 | 等级 | 缓解措施 |
|:------:|---------|:------:|:---:|:---:|:---:|---------|
| RSK-01 | CR-001+CR-002同时上线导致前后端大面积不兼容 | CR-001, CR-002 | 高 | 严重 | 制定灰度上线方案；先发布后端（保留单数路径兼容期），前端适配后再下掉旧路径 |
| RSK-02 | 退费审批流状态机实现缺陷导致金额错误 | CR-004 | 中 | 严重 | 编写完整的状态机单元测试；增加审批前置条件和金额计算的集成测试 |
| RSK-03 | 跨模块Service注入产生循环依赖 | CR-006 | 中 | 高 | 使用@Lazy注解或事件驱动模式解耦；依赖关系拓扑分析 |
| RSK-04 | 枚举值变更导致数据库中已存储状态值不匹配 | CR-007 | 中 | 高 | 编写数据迁移脚本；提供枚举值映射兼容层 |
| RSK-05 | 优惠叠加规则理解偏差导致费用计算错误 | CR-005 | 低 | 严重 | 与财务人员确认优惠叠加规则；编写覆盖所有优惠组合的单元测试 |
| RSK-06 | 变更范围过大导致回归测试不充分 | 全部CR | 高 | 中 | 制定回归测试检查表；基于RCR漂移项反向验证 |

---

## 6. 回归测试策略

### 6.1 回归测试优先级

| 优先级 | 测试范围 | 覆盖CR |
|:------:|---------|:------:|
| P0 | 收费模块完整业务流程（创建收费→缴费→退费申请→审批→退款） | CR-001, CR-002, CR-004, CR-005, CR-006 |
| P0 | 前后端API联调（所有18+接口的请求/响应验证） | CR-001, CR-002, CR-007 |
| P1 | 学员管理完整生命周期（创建→分班→转班→退学） | CR-001, CR-006 |
| P1 | 考勤管理（签到→统计→关联课时消耗） | CR-001, CR-006 |
| P2 | 报表统计（多维度数据聚合验证） | CR-006 |
| P2 | 前端体验（loading/empty/error状态展示） | CR-008 |

### 6.2 回滚策略

如v2上线后出现严重缺陷，可按以下优先级回滚：
1. **回滚CR-004（退费审批）**：风险最高，可独立回滚到v1的简单退费模式
2. **回滚CR-006（跨模块集成）**：如产生循环依赖或性能问题
3. **整体回滚到BL-20260622-01**：保留完整v1代码基线

---

## 相关文档

- 变更需求文档：`docs/变更需求文档-CR.md`
- 逆向校验报告：`designs/diagrams/逆向校验报告-RCR.md`
- 四维度质量校验报告：`designs/diagrams/四维度质量校验报告.md`
- ADR-005需求变更决策：`designs/adr/ADR-005-需求变更决策.md`
- 变更回归校验报告：`docs/变更回归校验报告-CRR.md`

---

**文档版本**：V1.0
**编制人**：A5-需求验证智能体
**审核人**：待指定
**批准人**：待指定
**创建日期**：2026-06-22
