# 三层约束设计（Three-Layer Constraint Design）

> 项目：教育培训机构教务收费管理系统（EduFeeMS）
> 版本：v1.0
> 日期：2026-06-22
> 本文档定义项目全生命周期的三层约束：架构层（C-ARCH）、模块层（C-MOD）、代码层（C-CODE）

---

## 一、架构层约束（C-ARCH）

全局架构规则，对所有模块有效，违反即视为架构缺陷。

| 编号 | 约束内容 | 适用范围 | 级别 |
|------|----------|----------|------|
| C-ARCH-001 | Controller层禁止直接操作数据库（必须通过Service层） | 全部Controller | 强制 |
| C-ARCH-002 | 所有数据库操作必须通过Repository/Mapper接口 | 全部模块 | 强制 |
| C-ARCH-003 | Controller层只做参数校验和路由，不包含业务逻辑 | 全部Controller | 强制 |
| C-ARCH-004 | Service层方法必须包含事务注解（@Transactional），除只读查询外 | 全部Service | 强制 |
| C-ARCH-005 | 所有外部API调用必须通过独立的Client类封装 | 全部模块 | 强制 |
| C-ARCH-006 | 跨模块调用只能通过Service接口，不能直接调用Repository | 全部模块 | 强制 |
| C-ARCH-007 | 前端不能直接访问数据库，所有数据请求通过API | 前端+后端 | 强制 |
| C-ARCH-008 | 敏感数据（密码、手机号）在日志中必须脱敏 | 全部模块 | 强制 |
| C-ARCH-009 | 所有异常统一由GlobalExceptionHandler处理，禁止Controller内try-catch吞异常 | 全部Controller | 强制 |
| C-ARCH-010 | 分页查询统一使用IPage<T>作为返回类型，分页参数使用Page<T> | 全部Controller | 强制 |
| C-ARCH-011 | 所有API响应必须使用统一响应格式R<T>（code/message/data） | 全部Controller | 强制 |
| C-ARCH-012 | 数据库实体类禁止被直接暴露到Controller层，必须通过DTO/VO转换 | 全部模块 | 强制 |

---

## 二、模块层约束（C-MOD）

定义每个模块的职责边界与依赖白名单。

### 2.1 common模块

common模块为基础设施层，提供通用工具、常量、异常定义、统一响应。

| 编号 | 约束内容 | 级别 |
|------|----------|------|
| C-MOD-001 | common模块不依赖任何业务模块 | 强制 |
| C-MOD-002 | R类（统一响应）为所有Controller的唯一返回类型 | 强制 |
| C-MOD-003 | Constants类集中管理所有常量，禁止分散定义 | 强制 |
| C-MOD-004 | BusinessException为全局唯一业务异常类，子类通过errorCode区分 | 强制 |
| C-MOD-005 | 工具类（DateUtil、StringUtil等）必须为静态方法，禁止实例化 | 强制 |

### 2.2 auth模块

auth模块负责认证、授权、Token管理。

| 编号 | 约束内容 | 级别 |
|------|----------|------|
| C-MOD-006 | 仅依赖common模块 | 强制 |
| C-MOD-007 | JWT Token验证为全局拦截器，不绑定特定模块 | 强制 |
| C-MOD-008 | 权限验证注解@PreAuthorize仅用于Controller方法 | 强制 |
| C-MOD-009 | Token刷新逻辑由AuthService内部处理，外部不可直接操作Token存储 | 强制 |
| C-MOD-010 | 登录日志和操作日志由AOP切面统一记录，不侵入业务代码 | 强制 |

### 2.3 student模块

student模块负责学员信息管理、分班、转班、退学。

| 编号 | 约束内容 | 级别 |
|------|----------|------|
| C-MOD-011 | 依赖common、auth、course、class模块 | 强制 |
| C-MOD-012 | StudentService只能通过ClassService接口操作班级数据 | 强制 |
| C-MOD-013 | 学员分班/转班/退学操作必须校验班级状态（开课中/已结课） | 强制 |
| C-MOD-014 | 学员信息中的手机号在查询列表时默认脱敏显示 | 强制 |
| C-MOD-015 | 学员退学操作必须关联退费流程，无退费记录不允许标记退学 | 强制 |

### 2.4 course模块

course模块负责课程定义、课程定价管理。

| 编号 | 约束内容 | 级别 |
|------|----------|------|
| C-MOD-016 | 依赖common、auth模块 | 强制 |
| C-MOD-017 | 课程定价变更后，已创建的班级不受影响（价格快照机制） | 强制 |
| C-MOD-018 | 课程被班级引用后不可删除，只能标记为停用 | 强制 |
| C-MOD-019 | CourseService暴露的课程查询方法需支持按课程类型、状态筛选 | 强制 |

### 2.5 class模块

class模块负责班级创建、排课、班级状态管理。

| 编号 | 约束内容 | 级别 |
|------|----------|------|
| C-MOD-020 | 依赖common、auth、course模块 | 强制 |
| C-MOD-021 | 班级创建时必须关联一个有效课程（状态=启用） | 强制 |
| C-MOD-022 | 排课时间冲突检测由ClassService内部实现，对外暴露冲突检查接口 | 强制 |
| C-MOD-023 | 班级容量上限在创建时设定，学员人数达到上限后禁止继续分班 | 强制 |
| C-MOD-024 | 班级状态流转：招生中→开课中→已结课，禁止逆向流转 | 强制 |

### 2.6 fee模块

fee模块负责收费、退费、台账、欠费催缴。

| 编号 | 约束内容 | 级别 |
|------|----------|------|
| C-MOD-025 | 依赖common、auth、student、course模块 | 强制 |
| C-MOD-026 | 所有金额操作必须使用BigDecimal，禁止使用float/double | 强制 |
| C-MOD-027 | 收费操作必须在数据库事务中完成（@Transactional） | 强制 |
| C-MOD-028 | FeeService的public方法仅允许auth模块中指定角色（财务、管理员）调用 | 强制 |
| C-MOD-029 | 退费金额不得超过已收金额，退费前需校验收费记录状态 | 强制 |
| C-MOD-030 | 每笔收费/退费必须生成唯一业务流水号，格式：FEE-YYYYMMDD-XXXXXX | 强制 |

### 2.7 attendance模块

attendance模块负责考勤签到、请假、考勤统计。

| 编号 | 约束内容 | 级别 |
|------|----------|------|
| C-MOD-031 | 依赖common、auth、student、class模块 | 强制 |
| C-MOD-032 | 签到操作需校验学员是否属于该班级 | 强制 |
| C-MOD-033 | 请假申请需关联课程安排记录 | 强制 |
| C-MOD-034 | 考勤统计查询需支持按班级、日期范围、学员维度 | 强制 |
| C-MOD-035 | 考勤数据为只追加模式，不允许修改已确认的考勤记录 | 强制 |

---

## 三、代码层约束（C-CODE）

编码细节规范，适用于所有模块。

| 编号 | 约束内容 | 适用范围 | 级别 |
|------|----------|----------|------|
| C-CODE-001 | 所有日期时间字段使用LocalDateTime类型 | 全部实体 | 强制 |
| C-CODE-002 | 所有日期格式统一为 yyyy-MM-dd HH:mm:ss | 全部序列化 | 强制 |
| C-CODE-003 | 所有金额字段使用BigDecimal，禁止float/double | 全部模块 | 强制 |
| C-CODE-004 | Controller方法使用@Valid注解进行参数校验 | 全部Controller | 强制 |
| C-CODE-005 | DTO类使用Lombok @Data注解 | 全部DTO | 强制 |
| C-CODE-006 | 数据库实体类使用@TableName和@TableId注解（MyBatis-Plus） | 全部Entity | 强制 |
| C-CODE-007 | 日志使用@Slf4j注解，日志级别：debug/info/warn/error | 全部类 | 强制 |
| C-CODE-008 | 异常使用自定义BusinessException，统一由GlobalExceptionHandler处理 | 全部模块 | 强制 |
| C-CODE-009 | 禁止使用魔法值，常量定义在Constants类中 | 全部模块 | 强制 |
| C-CODE-010 | SQL查询返回多表数据使用VO类封装 | 全部Mapper | 强制 |
| C-CODE-011 | 数据库实体类使用@TableField(fill = FieldFill.INSERT/UPDATE)自动填充createTime/updateTime | 全部Entity | 强制 |
| C-CODE-012 | RESTful URL命名规范：资源名复数形式，层级不超过3层 | 全部Controller | 强制 |
| C-CODE-013 | 每个Controller方法必须有对应的Swagger @Operation注解 | 全部Controller | 建议 |
| C-CODE-014 | SQL中的IN查询参数个数不超过1000，超过时需分批查询 | 全部Mapper | 强制 |
| C-CODE-015 | 禁止在循环中执行数据库查询，必须使用批量操作 | 全部Service | 强制 |

---

## 四、约束关系矩阵

说明各约束之间的关联与优先级。

| 优先级 | 层级 | 说明 |
|--------|------|------|
| P0 | 架构层（C-ARCH） | 最高优先级，违反即阻断CI/CD流水线 |
| P1 | 模块层（C-MOD） | 高优先级，违反导致Code Review不通过 |
| P2 | 代码层（C-CODE） | 中优先级，违反触发Warning，需修复后方可合并 |

### 冲突解决规则

1. 架构层约束优先于模块层约束，模块层约束优先于代码层约束
2. 同一层级内的约束冲突，以编号较小的为准
3. 模块层约束的依赖白名单具有排他性——未列入白名单的依赖即禁止

---

## 五、版本历史

| 版本 | 日期 | 变更内容 | 作者 |
|------|------|----------|------|
| v1.0 | 2026-06-22 | 初始版本，定义三层约束共55条 | AI Agent |
