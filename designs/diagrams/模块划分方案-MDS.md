# 模块划分方案（Module Division Scheme）

## 文档信息

| 属性 | 值 |
|------|-----|
| **文档编号** | MDS-001 |
| **适用项目** | 教育培训机构教务收费管理系统（EduFeeMS） |
| **版本** | v1.0 |
| **编制日期** | 2026-06-22 |

---

## 1. 模块总览

系统划分为 **9个模块**，按功能领域组织：

```
┌────────────────────────────────────────────────────────────┐
│                        EduFeeMS                            │
├──────────┬──────────┬──────────┬──────────┬───────────────┤
│  common  │   auth   │ student  │  course  │     class     │
│ 公共基础 │ 认证授权  │ 学员管理  │ 课程管理  │   班级管理     │
├──────────┼──────────┼──────────┼──────────┼───────────────┤
│ teacher  │   fee    │attendance│  report  │               │
│ 教师管理  │ 收费管理  │ 考勤管理  │ 报表统计  │               │
└──────────┴──────────┴──────────┴──────────┴───────────────┘
```

### 模块分组

| 分组 | 包含模块 | 说明 |
|------|----------|------|
| **基础设施组** | common, auth | 为所有业务模块提供基础能力和安全认证 |
| **教务管理组** | student, course, class, teacher | 核心教务业务：学员、课程、班级、教师管理 |
| **收费运营组** | fee, attendance | 收费、考勤等运营管理 |
| **分析决策组** | report | 跨模块数据分析与报表 |

---

## 2. 模块详细设计

### 2.1 common模块 — 公共基础设施

**模块职责**：提供系统级公共组件和基础设施，包括统一响应封装、全局异常处理、基础实体抽象、通用工具类、分页封装等。不包含任何业务逻辑，被所有其他模块依赖。

**核心实体**：

| 实体 | 说明 |
|------|------|
| BaseEntity | 所有实体类的父类，定义id、createTime、updateTime、createBy、updateBy、isDeleted等通用字段 |
| PageQuery | 分页查询请求基类，封装pageNum、pageSize、排序字段等 |
| PageResult\<T\> | 分页查询响应封装，包含total、pages、records等 |

**对外接口（Service）**：

| 接口 | 说明 |
|------|------|
| — | common模块不提供业务Service接口，仅提供工具类和基类 |

**依赖的模块**：无（最底层模块）

---

### 2.2 auth模块 — 认证授权

**模块职责**：负责系统用户认证、访问授权和权限管理。提供登录/登出、JWT Token签发与验证、基于RBAC的用户-角色-权限三级权限模型。支持多校区角色隔离（同一用户在不同校区可有不同角色）。所有业务模块的操作权限由auth模块统一管控。

**核心实体**：

| 实体 | 说明 |
|------|------|
| SysUser | 系统用户（用户名、密码、真实姓名、手机号、所属校区、状态） |
| SysRole | 系统角色（角色名、角色编码、角色级别） |
| SysPermission | 系统权限（权限名、权限编码、资源路径、请求方法） |
| SysUserRole | 用户-角色关联 |
| SysRolePermission | 角色-权限关联 |
| SysCampus | 校区信息（校区名、地址、联系人） |

**对外接口（Service）**：

| 接口 | 说明 |
|------|------|
| `AuthService.login(username, password)` | 用户登录，返回JWT Token |
| `AuthService.logout(token)` | 用户登出，Token失效 |
| `AuthService.refreshToken(refreshToken)` | 刷新JWT Token |
| `UserService.getById(userId)` | 查询用户信息 |
| `UserService.listByCampus(campusId)` | 按校区查询用户列表 |
| `UserService.create/update/delete(userDTO)` | 用户CRUD |
| `RoleService.getRolesByUser(userId)` | 查询用户角色 |
| `RoleService.create/update/delete(roleDTO)` | 角色CRUD |
| `PermissionService.getPermissionsByRole(roleId)` | 查询角色权限 |
| `PermissionService.checkPermission(userId, permission)` | 权限校验 |
| `CampusService.listAll()` | 查询所有校区 |

**依赖的模块**：common

---

### 2.3 student模块 — 学员管理

**模块职责**：管理教育培训机构的所有学员信息，包括学员基本信息维护、学员档案管理、报名记录管理，以及学员的班级归属管理（分班、转班、退学）。学员状态流转：咨询→报名→在读→休学→退学→毕业。

**核心实体**：

| 实体 | 说明 |
|------|------|
| Student | 学员信息（姓名、性别、出生日期、身份证号、联系电话、监护人信息、所属校区） |
| StudentEnrollment | 报名记录（报名日期、报名课程、来源渠道、销售顾问） |
| StudentClass | 学员班级归属（学员ID、班级ID、入班日期、出班日期、状态） |
| StudentStatusLog | 学员状态变更日志（旧状态、新状态、操作人、操作时间、备注） |

**对外接口（Service）**：

| 接口 | 说明 |
|------|------|
| `StudentService.page(queryDTO)` | 分页查询学员列表 |
| `StudentService.getById(studentId)` | 查询学员详情（含档案信息） |
| `StudentService.create(studentDTO)` | 新增学员 |
| `StudentService.update(studentDTO)` | 更新学员信息 |
| `StudentService.delete(studentId)` | 逻辑删除学员 |
| `StudentService.enroll(enrollDTO)` | 学员报名 |
| `StudentService.assignClass(studentId, classId)` | 学员分班 |
| `StudentService.transferClass(studentId, sourceClassId, targetClassId)` | 学员转班 |
| `StudentService.withdraw(studentId, reason)` | 学员退学 |
| `StudentService.changeStatus(studentId, newStatus)` | 变更学员状态 |
| `StudentService.getStatusLog(studentId)` | 查询学员状态变更记录 |

**依赖的模块**：common, auth, course, class

---

### 2.4 course模块 — 课程管理

**模块职责**：管理机构开设的所有课程信息，包括课程类型定义（如学科类、兴趣类、考级类）、课程基本信息维护、课时配置（总课时、单次课时长）以及课程费用标准设置（标准价格、优惠价格策略）。

**核心实体**：

| 实体 | 说明 |
|------|------|
| CourseCategory | 课程类型（类型名称、父类型ID、排序） |
| Course | 课程信息（课程名称、课程类型、适用年龄段、课程描述、状态） |
| CourseHourConfig | 课时配置（课程ID、总课时数、单次课时长(分钟)、课次频率） |
| CourseFeeStandard | 费用标准（课程ID、标准单价、优惠方案类型、生效日期、失效日期） |

**对外接口（Service）**：

| 接口 | 说明 |
|------|------|
| `CourseCategoryService.tree()` | 查询课程类型树 |
| `CourseCategoryService.create/update/delete(categoryDTO)` | 课程类型CRUD |
| `CourseService.page(queryDTO)` | 分页查询课程列表 |
| `CourseService.getById(courseId)` | 查询课程详情（含课时配置和费用标准） |
| `CourseService.create/update/delete(courseDTO)` | 课程CRUD |
| `CourseService.getFeeStandard(courseId)` | 查询课程当前有效费用标准 |
| `CourseService.setFeeStandard(courseId, feeDTO)` | 设置课程费用标准 |
| `CourseHourConfigService.getByCourse(courseId)` | 查询课程课时配置 |
| `CourseHourConfigService.update(configDTO)` | 更新课时配置 |

**依赖的模块**：common, auth

---

### 2.5 class模块 — 班级管理

**模块职责**：管理教学班级的开设、运行和资源调度。包括班级基本信息维护、排课管理（上课时间、频次）、学员班级归属查询、教室资源管理。班级是课程的具体执行单位，一个课程可开设多个班级。

**核心实体**：

| 实体 | 说明 |
|------|------|
| ClassInfo | 班级信息（班级名称、所属课程、教师、校区、最大人数、开班日期、状态） |
| Schedule | 排课记录（班级ID、上课日期、开始时间、结束时间、教室ID、授课教师ID） |
| Classroom | 教室信息（教室名称、所在校区、容量、设施） |
| ClassStudent | 班级学员（同StudentClass，冗余视图） |

**对外接口（Service）**：

| 接口 | 说明 |
|------|------|
| `ClassService.page(queryDTO)` | 分页查询班级列表 |
| `ClassService.getById(classId)` | 查询班级详情（含学员列表、排课信息） |
| `ClassService.create/update/delete(classDTO)` | 班级CRUD |
| `ClassService.getStudents(classId)` | 查询班级学员列表 |
| `ClassService.addStudent(classId, studentId)` | 将学员加入班级 |
| `ClassService.removeStudent(classId, studentId)` | 从班级移除学员 |
| `ScheduleService.getByClass(classId)` | 查询班级排课列表 |
| `ScheduleService.create/update/delete(scheduleDTO)` | 排课CRUD |
| `ScheduleService.getByDateRange(startDate, endDate)` | 按日期范围查询排课 |
| `ClassroomService.page(queryDTO)` | 分页查询教室 |
| `ClassroomService.create/update/delete(classroomDTO)` | 教室CRUD |
| `ClassroomService.checkAvailability(classroomId, timeSlot)` | 检查教室可用性 |

**依赖的模块**：common, auth, course, teacher

---

### 2.6 teacher模块 — 教师管理

**模块职责**：管理机构的教学人员信息，包括教师基本信息维护（资质、专业领域）、授课安排查看、课时工作量统计以及课酬计算。支持教师跨校区授课场景。

**核心实体**：

| 实体 | 说明 |
|------|------|
| Teacher | 教师信息（姓名、性别、联系电话、专业领域、资质等级、所属校区、状态） |
| TeacherCourse | 教师授课范围（教师可教授的课程类型） |
| TeacherSchedule | 教师课表（从排课记录聚合的教师视角视图） |
| TeacherSalary | 课酬记录（教师ID、统计周期、总课时、课酬单价、应发金额） |

**对外接口（Service）**：

| 接口 | 说明 |
|------|------|
| `TeacherService.page(queryDTO)` | 分页查询教师列表 |
| `TeacherService.getById(teacherId)` | 查询教师详情（含授课范围和课表） |
| `TeacherService.create/update/delete(teacherDTO)` | 教师CRUD |
| `TeacherService.getSchedule(teacherId, dateRange)` | 查询教师课表 |
| `TeacherService.getTeachingHours(teacherId, period)` | 统计教师课时量 |
| `TeacherService.calculateSalary(teacherId, period)` | 计算教师课酬 |
| `TeacherService.setCourseScope(teacherId, courseIds)` | 设置教师授课范围 |

**依赖的模块**：common, auth

---

### 2.7 fee模块 — 收费管理

**模块职责**：系统核心模块，负责教育培训机构的所有收费相关业务，包括收费项目管理、学费收取与记录、退费处理（全额/部分退费）、欠费催缴提醒、收据生成与管理、优惠活动管理（折扣、减免、赠送课时等）。所有金额操作需记录完整审计链路。

**核心实体**：

| 实体 | 说明 |
|------|------|
| FeeItem | 收费项目（项目名称、项目类型：学费/教材费/活动费等、所属课程） |
| FeeRecord | 收费记录（学员ID、收费项目ID、应收金额、实收金额、支付方式、支付时间、收据号、操作人、校区） |
| Refund | 退费记录（原收费记录ID、退费金额、退费原因、审批人、退费时间） |
| Arrearage | 欠费记录（学员ID、欠费金额、欠费项目、催缴次数、最近催缴时间） |
| Receipt | 收据信息（收据号、关联收费记录、开具时间、收据类型：电子/纸质） |
| Promotion | 优惠方案（优惠名称、优惠类型：折扣/减免/赠送、优惠力度、有效期） |
| FeePromotionApply | 优惠应用记录（收费记录ID、优惠方案ID、优惠金额） |

**对外接口（Service）**：

| 接口 | 说明 |
|------|------|
| `FeeItemService.page(queryDTO)` | 分页查询收费项目 |
| `FeeItemService.create/update/delete(itemDTO)` | 收费项目CRUD |
| `FeeService.createRecord(recordDTO)` | 创建收费记录（含优惠计算） |
| `FeeService.getRecordsByStudent(studentId)` | 查询学员所有收费记录 |
| `FeeService.getRecordsByDateRange(startDate, endDate)` | 按日期范围查询收费记录 |
| `FeeService.processRefund(refundDTO)` | 处理退费（需二次鉴权） |
| `FeeService.getRefundDetail(refundId)` | 查询退费详情 |
| `ArrearageService.getByStudent(studentId)` | 查询学员欠费情况 |
| `ArrearageService.remind(arrearageId)` | 发送欠费催缴提醒 |
| `ArrearageService.getOverdueList(campusId)` | 查询逾期欠费列表 |
| `ReceiptService.generate(feeRecordId)` | 生成收据 |
| `ReceiptService.print(receiptId)` | 打印收据 |
| `PromotionService.page(queryDTO)` | 分页查询优惠方案 |
| `PromotionService.create/update/delete(promotionDTO)` | 优惠方案CRUD |
| `PromotionService.calculateDiscount(feeRecordId, promotionId)` | 计算优惠金额 |

**依赖的模块**：common, auth, student, course

---

### 2.8 attendance模块 — 考勤管理

**模块职责**：管理学员的日常考勤签到，包括签到/签退记录、请假申请与审批、缺勤自动记录与统计。考勤数据是课时消耗和退费计算的重要依据。支持多种签到方式（扫码、手动录入）。

**核心实体**：

| 实体 | 说明 |
|------|------|
| Attendance | 考勤记录（学员ID、班级ID、排课ID、签到时间、签退时间、考勤状态：正常/迟到/早退/缺勤/请假） |
| LeaveRequest | 请假申请（学员ID、请假开始时间、请假结束时间、请假类型：事假/病假/其他、原因、审批状态） |
| LeaveApproval | 请假审批记录（请假申请ID、审批人、审批结果、审批意见、审批时间） |
| AttendanceStats | 考勤统计（学员ID、统计周期、出勤次数、缺勤次数、请假次数、迟到次数） |

**对外接口（Service）**：

| 接口 | 说明 |
|------|------|
| `AttendanceService.checkIn(studentId, scheduleId)` | 签到 |
| `AttendanceService.checkOut(attendanceId)` | 签退 |
| `AttendanceService.getByStudent(studentId, dateRange)` | 查询学员考勤记录 |
| `AttendanceService.getByClass(classId, dateRange)` | 查询班级考勤记录 |
| `AttendanceService.getStats(studentId, period)` | 查询学员考勤统计 |
| `LeaveService.apply(leaveDTO)` | 提交请假申请 |
| `LeaveService.approve(leaveId, approveDTO)` | 审批请假 |
| `LeaveService.reject(leaveId, rejectDTO)` | 驳回请假 |
| `LeaveService.getByStudent(studentId)` | 查询学员请假记录 |
| `LeaveService.getPendingApprovals()` | 查询待审批请假 |

**依赖的模块**：common, auth, student, class

---

### 2.9 report模块 — 报表统计

**模块职责**：提供跨模块的数据统计分析和报表导出功能。汇总各业务模块数据，生成财务报表（收费汇总、退费统计、欠费分析）、课时统计（学员课时消耗、教师授课量）、学员统计（在册人数、出勤率分析）等。支持报表的查看、筛选和数据导出（Excel/PDF）。

**核心实体**：

| 实体 | 说明 |
|------|------|
| ReportTemplate | 报表模板（模板名称、报表类型、查询SQL/逻辑、参数定义） |
| ReportExportLog | 报表导出日志（导出时间、导出人、报表类型、筛选条件、文件路径） |

**对外接口（Service）**：

| 接口 | 说明 |
|------|------|
| `ReportService.financeReport(campusId, dateRange)` | 财务报表（收费/退费/欠费汇总） |
| `ReportService.teachingHoursReport(campusId, dateRange)` | 课时统计报表 |
| `ReportService.studentStatsReport(campusId)` | 学员统计报表 |
| `ReportService.attendanceReport(classId, dateRange)` | 考勤统计报表 |
| `ReportService.teacherWorkloadReport(campusId, dateRange)` | 教师工作量报表 |
| `ReportService.exportExcel(reportType, params)` | 导出Excel报表 |
| `ReportService.exportPDF(reportType, params)` | 导出PDF报表 |
| `ReportService.getExportHistory()` | 查询导出历史 |

**依赖的模块**：common, auth, student, course, class, teacher, fee, attendance

---

## 3. 模块接口规范

### 3.1 Service接口命名规范

所有对外提供的Service接口遵循以下命名：

```
{EntityName}Service.{methodName}({parameters})
```

### 3.2 模块间通信契约

| 规范 | 说明 |
|------|------|
| 通信方式 | 同进程内Service接口调用 |
| 参数传递 | DTO对象或基本类型，禁止直接传递Entity |
| 返回值 | VO/DTO对象或基本类型，禁止直接返回Entity |
| 异常处理 | 业务异常统一抛出BusinessException，携带错误码 |
| 事务边界 | 调用方不感知被调用方的事务，跨模块事务由上层Service协调 |

### 3.3 模块包结构规范

```
com.edufeems.{module}/
├── controller/          # Controller层（HTTP接口）
├── service/             # Service接口定义
│   └── impl/            # Service接口实现
├── repository/          # Repository层（MyBatis-Plus Mapper）
├── entity/              # 数据库实体（Entity）
├── dto/                 # 数据传输对象（DTO）
├── vo/                  # 视图对象（VO）
└── constant/            # 模块常量定义
```

---

## 相关文档

- ADR-001：系统整体架构风格选型
- 模块依赖拓扑-DTS.md
- 架构风格声明-ASD.md
