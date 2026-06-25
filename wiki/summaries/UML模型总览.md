# 📊 UML 模型总览

> 教育培训机构教务收费管理系统（EduFeeMS）
> 共 6 份 UML 模型 | 基线编号 BL-20260622-01

---

## 一、用例图 — 系统功能全景

```plantuml
@startuml 教育培训机构教务收费管理系统-用例图
left to right direction

actor "机构管理员" as Admin
actor "教务人员" as Academic
actor "财务人员" as Finance
actor "教师" as Teacher
actor "学生/家长" as Student

rectangle "教育培训机构教务收费管理系统" {
    package "学员管理" {
        usecase "新增学员" as UC_AddStudent
        usecase "修改学员信息" as UC_EditStudent
        usecase "查询学员" as UC_SearchStudent
        usecase "学员分班" as UC_AssignClass
        usecase "学员转班" as UC_TransferClass
        usecase "学员退学" as UC_Withdraw
    }
    package "课程管理" {
        usecase "课程类型设置" as UC_CourseType
        usecase "课时管理" as UC_CourseHour
        usecase "课程定价" as UC_CoursePricing
    }
    package "班级管理" {
        usecase "开班" as UC_OpenClass
        usecase "排课" as UC_Schedule
        usecase "班级学员管理" as UC_ClassStudentMgmt
        usecase "班级结业" as UC_CloseClass
    }
    package "教师管理" {
        usecase "教师信息管理" as UC_TeacherInfo
        usecase "教师排课" as UC_TeacherSchedule
        usecase "教师课时统计" as UC_TeacherHourStat
    }
    package "收费管理" {
        usecase "收费" as UC_ChargeFee
        usecase "退费" as UC_Refund
        usecase "欠费催缴" as UC_Dunning
        usecase "打印收据" as UC_PrintReceipt
    }
    package "考勤管理" {
        usecase "签到" as UC_CheckIn
        usecase "请假" as UC_Leave
        usecase "考勤统计" as UC_AttendanceStat
    }
    package "报表统计" {
        usecase "财务报表" as UC_FinanceReport
        usecase "学员统计" as UC_StudentReport
        usecase "教师绩效统计" as UC_TeacherReport
    }
    package "系统管理" {
        usecase "用户管理" as UC_UserMgmt
        usecase "权限管理" as UC_Permission
        usecase "数据备份" as UC_Backup
    }
    usecase "用户登录" as UC_Login
}

Admin --> UC_UserMgmt
Admin --> UC_Permission
Admin --> UC_Backup
Admin --> UC_FinanceReport
Admin --> UC_TeacherReport
Academic --> UC_AddStudent
Academic --> UC_EditStudent
Academic --> UC_SearchStudent
Academic --> UC_AssignClass
Academic --> UC_TransferClass
Academic --> UC_Withdraw
Academic --> UC_CourseType
Academic --> UC_Schedule
Academic --> UC_OpenClass
Academic --> UC_CloseClass
Academic --> UC_TeacherSchedule
Academic --> UC_TeacherHourStat
Finance --> UC_ChargeFee
Finance --> UC_Refund
Finance --> UC_Dunning
Finance --> UC_PrintReceipt
Finance --> UC_FinanceReport
Teacher --> UC_CheckIn
Teacher --> UC_Leave
Teacher --> UC_TeacherHourStat
Student --> UC_ChargeFee
Student --> UC_CheckIn
Student --> UC_Leave

UC_Login ..> UC_ChargeFee : <<include>>
UC_ChargeFee ..> UC_PrintReceipt : <<include>>
UC_Refund ..> UC_PrintReceipt : <<include>>
UC_Dunning ..> UC_StudentReport : <<include>>
UC_AssignClass ..> UC_SearchStudent : <<include>>
UC_Withdraw ..> UC_Refund : <<extend>>
@enduml
```

---

## 二、类图 — 核心领域模型

```plantuml
@startuml 核心类图
skinparam classBackgroundColor #E3F2FD
skinparam classBorderColor #1565C0

class Student {
    - Long studentId <<PK>>
    - String name
    - String gender
    - String phone
    - String status
    - Date enrollDate
    --
    + register()
    + updateInfo()
}

class Course {
    - Long courseId <<PK>>
    - String courseName
    - String category
    - BigDecimal basePrice
    - String status
    --
    + createCourse()
    + setPricing()
}

class Class {
    - Long classId <<PK>>
    - Long courseId <<FK>>
    - Long teacherId <<FK>>
    - Integer maxCapacity
    - Integer currentCount
    - String status
    --
    + openClass()
    + addStudent()
    + isFull()
}

class Teacher {
    - Long teacherId <<PK>>
    - String name
    - String subject
    - String status
    --
    + getSchedule()
    + getHourlyStats()
}

class Enrollment {
    - Long enrollmentId <<PK>>
    - Long studentId <<FK>>
    - Long classId <<FK>>
    - BigDecimal totalFee
    - BigDecimal paidAmount
    - String status
    --
    + submit()
    + assignClass()
    + withdraw()
}

class FeeRecord {
    - Long feeId <<PK>>
    - Long enrollmentId <<FK>>
    - BigDecimal amount
    - String payMethod
    - String receiptNo
    - Date payTime
    --
    + charge()
    + refund()
    + printReceipt()
}

class Attendance {
    - Long attendanceId <<PK>>
    - Long studentId <<FK>>
    - Long scheduleId <<FK>>
    - String status
    - Date checkInTime
    --
    + checkIn()
    + requestLeave()
    + getStats()
}

class Schedule {
    - Long scheduleId <<PK>>
    - Long classId <<FK>>
    - Long teacherId <<FK>>
    - Date scheduleDate
    - String status
    --
    + createSchedule()
    + checkTeacherConflict()
    + checkRoomConflict()
}

class Payment {
    - Long paymentId <<PK>>
    - Long feeRecordId <<FK>
    - String transactionNo
    - BigDecimal amount
    - String payStatus
    --
    + processPayment()
    + refund()
}

class Discount {
    - Long discountId <<PK>>
    - String discountType
    - BigDecimal discountValue
    - Date startDate
    - Date endDate
    --
    + calculate()
    + isApplicable()
}

class DunningNotice {
    - Long noticeId <<PK>>
    - BigDecimal unpaidAmount
    - Integer overdueDays
    - String noticeLevel
    --
    + generate()
    + send()
}

Student "1" -- "*" Enrollment
Student "1" -- "*" Attendance
Student "1" -- "*" FeeRecord
Course "1" -- "*" Class
Course "1" -- "*" Enrollment
Class "1" -- "*" Enrollment
Class "1" -- "*" Schedule
Class "1" -- "*" Attendance
Teacher "1" -- "*" Schedule
Teacher "1" -- "*" Class
Enrollment "1" -- "*" FeeRecord
Schedule "1" -- "*" Attendance
Discount "0..*" -- "*" FeeRecord
Enrollment "1" -- "*" DunningNotice
Payment ||--|| FeeRecord
@enduml
```

---

## 三、时序图 — 学员报名缴费全流程

```plantuml
@startuml 学员报名缴费时序图
title 学员报名缴费时序图

actor "学生/家长" as Student
participant "Web前端" as Frontend
participant "Controller" as Controller
participant "Service" as Service
participant "FeeService" as FeeService
database "数据库" as DB

== 阶段一：学员信息登记 ==
Student -> Frontend : 1. 填写报名信息
Frontend -> Controller : 2. POST /api/enrollment/submit
Controller -> Service : 3. submitEnrollment(dto)
Service -> DB : 4. INSERT INTO student
DB --> Service : 5. 返回studentId
Service -> DB : 6. INSERT INTO enrollment
DB --> Service : 7. 返回enrollmentId
Service --> Controller : 8. 报名成功
Controller --> Frontend : 9. 返回报名成功
Frontend --> Student : 10. 展示"报名成功，请选择课程"

== 阶段二：选课与分班 ==
Student -> Frontend : 11. 选择课程和班级
Frontend -> Controller : 12. POST /api/enrollment/assignClass
Controller -> Service : 13. assignClass(enrollmentId, classId)
Service -> DB : 14. UPDATE enrollment + UPDATE class
DB --> Service : 15. 分班成功
Service --> Controller : 16. 分班成功
Controller --> Frontend : 17. 返回分班结果
Frontend --> Student : 18. 展示"分班成功，请缴费"

== 阶段三：费用计算与缴费 ==
Controller -> FeeService : 19. calculateFee(enrollmentId)
FeeService -> DB : 20. 查询课程费用 + 适用折扣
FeeService -> FeeService : 21. 计算最终费用(原价-折扣)
FeeService --> Controller : 22. 返回费用明细
Controller --> Frontend : 23. 费用详情JSON
Frontend --> Student : 24. 展示费用与支付方式

Student -> Frontend : 25. 选择支付方式并支付
Frontend -> Controller : 26. POST /api/payment/pay
Controller -> FeeService : 27. processPayment()
FeeService -> DB : 28. BEGIN TRANSACTION
FeeService -> DB : 29. INSERT fee_record
FeeService -> DB : 30. UPDATE enrollment payStatus
FeeService -> DB : 31. COMMIT
FeeService --> Controller : 32. 支付成功
Controller --> Frontend : 33. 支付成功
Frontend --> Student : 34. 展示"缴费成功"及电子收据
@enduml
```

---

## 四、活动图 — 学员报名业务流程

```plantuml
@startuml 学员报名活动图
title 学员报名活动图

|学生/家长|
start
:了解培训课程;
:到店咨询;

|教务人员|
:接待咨询;
:介绍课程体系;

|系统|
:查询匹配课程;
:展示课程详情;

|学生/家长|
if (是否试听?) then (是)
    :申请试听;
    |教务人员|
    :安排试听课;
    |教师|
    :执行试听教学;
    |学生/家长|
    if (试听满意?) then (是)
        :确认报名;
    else (否)
        :放弃报名;
        stop
    endif
else (否)
    :确认报名;
endif

|教务人员|
:引导填写报名信息;

|系统|
:录入学员信息;
:生成学员档案;

|学生/家长|
:选择课程套餐;

|教务人员|
:计算课程费用;

|系统|
if (有适合开班?) then (是)
    :匹配现有班级;
else (否)
    :加入等候队列;
endif
:分配学员至班级;
:更新班级名册;

|财务人员|
:生成缴费通知单;

|学生/家长|
:缴纳学费;

|财务人员|
:确认到账;
:开具收据;

|系统|
:更新状态为"已缴费";
:发送开课通知;

|学生/家长|
:按时到课;
stop
@enduml
```

---

## 五、活动图 — 收费管理流程

```plantuml
@startuml 收费管理活动图
title 收费管理活动图

== 流程一：学费收取 ==
|教务人员|
start
:选择目标学员;

|系统|
:加载学员课程列表;
:计算应缴费用;
:确认费用明细;

|教务人员|
:确认缴费金额;
:选择支付方式;

|系统|
if (支付成功?) then (是)
    :更新缴费状态;
    :生成电子收据;
    :写入收费台账;
    stop
else (否)
    :记录失败日志;
    :提示重新支付;
    detach
endif

== 流程二：退费处理 ==
|学生/家长|
start
:提交退费申请;

|教务人员|
:核实信息与缴费记录;

if (符合退费条件?) then (是)
    :核算应退金额;
    :提交财务审批;
else (否)
    :驳回申请;
    stop
endif

|财务人员|
:接收退费审批;
:复核退费金额;

if (大额需管理员审批?) then (是)
    |机构管理员|
    :审批退费申请;
endif

|财务人员|
:执行退费操作;
:更新收费台账;
:生成退费凭证;
stop

== 流程三：欠费催缴 ==
|系统|
start
:定时扫描欠费数据;

:根据逾期天数分级;
:一级(≤7天): 温和提醒;
:二级(≤30天): 正式催缴;
:三级(>30天): 严重警告;

:推送催缴通知;

|学生/家长|
:收到通知后缴费;

|系统|
:更新催缴状态;
stop
@enduml
```

---

## 六、活动图 — 排课管理流程

```plantuml
@startuml 排课管理活动图
title 排课管理活动图

|教务人员|
start
:选择目标班级;
:确定排课周期;

|系统|
:加载班级信息;
:加载可选教师列表;
:加载可用教室资源;

|教务人员|
:设定排课计划;
:选择主讲教师;

|系统|
:检查教师时间冲突;
if (教师冲突?) then (是)
    :提示冲突;
    |教务人员|
    :调整教师或时间;
else (否)
endif

:检查教室资源;
if (教室冲突?) then (是)
    :提示冲突;
    |教务人员|
    :调整教室或时间;
else (否)
endif

:检查学员时间;
if (学员冲突?) then (有)
    :列出冲突学员;
    |教务人员|
    :调整或标记;
else (否)
endif

:生成排课确认单;

|教务人员|
:复核确认;

|系统|
:保存排课方案;
:更新班级课表;
:更新教师课表;
:更新学员课表;
:发送各方通知;

|教师|
:接收排课通知;

|学生/家长|
:查看课程表;

stop
@enduml
```

---

> 💡 **提示**：以上所有 UML 图的完整源文件位于 `wiki/summaries/` 目录下（6个 .puml 文件）。
> 在 Obsidian 阅读模式下，PlantUML 插件会自动渲染以上代码块为图形。
