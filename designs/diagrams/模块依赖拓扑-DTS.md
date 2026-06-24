# 模块依赖拓扑（Dependency Topology Scheme）

## 文档信息

| 属性 | 值 |
|------|-----|
| **文档编号** | DTS-001 |
| **适用项目** | 教育培训机构教务收费管理系统（EduFeeMS） |
| **版本** | v1.0 |
| **编制日期** | 2026-06-22 |

---

## 1. 依赖拓扑总图

### 1.1 有向依赖图

```
                    ┌──────────┐
                    │  common  │  (L0 — 基础设施层，无依赖)
                    └────┬─────┘
                         │
            ┌────────────┼────────────┐
            ▼            ▼            ▼
       ┌─────────┐ ┌─────────┐ ┌──────────┐
       │  auth   │ │ course  │ │ teacher  │  (L1 — 公共业务层)
       └────┬────┘ └────┬────┘ └────┬─────┘
            │            │           │
            │   ┌────────┘           │
            ▼   ▼                    │
       ┌──────────┐                  │
       │  class   │ ◄────────────────┘  (L2 — 教务编排层)
       └────┬─────┘
            │
    ┌───────┼───────┐
    ▼       ▼       ▼
┌────────┐ ┌──────────────┐ ┌────────────┐
│student │ │  attendance  │ │    fee     │  (L3 — 业务流程层)
└───┬────┘ └──────┬───────┘ └─────┬──────┘
    │              │               │
    └──────────────┼───────────────┘
                   ▼
            ┌──────────┐
            │  report  │  (L4 — 分析汇总层)
            └──────────┘
```

### 1.2 分层说明

| 层级 | 名称 | 包含模块 | 特征 |
|:----:|------|----------|------|
| L0 | 基础设施层 | common | 无业务依赖，被所有模块依赖 |
| L1 | 公共业务层 | auth, course, teacher | 依赖L0，被上层模块依赖 |
| L2 | 教务编排层 | class | 依赖L0和L1的部分模块，编排教务资源 |
| L3 | 业务流程层 | student, fee, attendance | 依赖L0-L2，实现核心业务流程 |
| L4 | 分析汇总层 | report | 依赖所有下层模块，跨模块数据聚合 |

**核心原则**：依赖方向**严格向下**，L_N 只能依赖 L_{N-1}、L_{N-2}、...、L0，禁止反向依赖。

---

## 2. 逐模块依赖分析

### 2.1 common模块

```
common
 └── 无依赖（根模块）
```

| 属性 | 说明 |
|------|------|
| **直接依赖** | 无 |
| **被依赖方** | 所有其他8个模块 |
| **依赖合理性** | common是基础设施层，提供通用工具、基类、异常处理等，天然被所有业务模块依赖；其无依赖的特性保证了基础设施的纯粹性和稳定性 |

### 2.2 auth模块

```
auth ──→ common
```

| 属性 | 说明 |
|------|------|
| **直接依赖** | common |
| **被依赖方** | student, course, class, teacher, fee, attendance, report |
| **依赖合理性** | auth依赖common使用BaseEntity、R类等基础设施；auth被所有业务模块依赖是因为所有业务操作都需要认证鉴权——用户身份验证（JWT）、操作权限校验（RBAC）、校区数据隔离校验均由auth模块提供 |

### 2.3 student模块

```
student ──→ common, auth, course, class
```

| 属性 | 说明 |
|------|------|
| **直接依赖** | common, auth, course, class |
| **被依赖方** | fee, attendance, report |
| **依赖合理性** | 学员报名需选择课程（course），分班需指定班级（class），所有操作需认证（auth）；fee模块收费需关联学员，attendance考勤需关联学员，report统计需聚合学员数据 |
| **禁止依赖** | teacher, fee, attendance, report |

### 2.4 course模块

```
course ──→ common, auth
```

| 属性 | 说明 |
|------|------|
| **直接依赖** | common, auth |
| **被依赖方** | student, class, fee, report |
| **依赖合理性** | course是核心基础数据，被多个模块引用：student报名时选择课程，class开班时关联课程，fee收费项目关联课程费用标准；course本身仅依赖auth（认证）和common（基础设施），不依赖其他业务模块，保持数据纯粹性 |
| **禁止依赖** | student, class, teacher, fee, attendance, report |

### 2.5 class模块

```
class ──→ common, auth, course, teacher
```

| 属性 | 说明 |
|------|------|
| **直接依赖** | common, auth, course, teacher |
| **被依赖方** | student, attendance, report |
| **依赖合理性** | 班级开设需关联课程（course）和授课教师（teacher），所有操作需认证（auth）；student分班需班级信息，attendance考勤需关联班级和排课，report需统计班级相关数据 |
| **禁止依赖** | student, fee, attendance, report |

### 2.6 teacher模块

```
teacher ──→ common, auth
```

| 属性 | 说明 |
|------|------|
| **直接依赖** | common, auth |
| **被依赖方** | class, report |
| **依赖合理性** | teacher是独立的基础资源模块，仅依赖auth（认证）和common（基础设施）；class排课时需指定授课教师，report需统计教师课时和课酬 |
| **禁止依赖** | student, course, class, fee, attendance, report |

### 2.7 fee模块

```
fee ──→ common, auth, student, course
```

| 属性 | 说明 |
|------|------|
| **直接依赖** | common, auth, student, course |
| **被依赖方** | report |
| **依赖合理性** | 收费需关联学员（student）和课程费用标准（course），所有收费操作需认证和权限校验（auth）；fee作为核心财务模块职责集中，仅被report依赖用于财务报表生成 |
| **禁止依赖** | class, teacher, fee, attendance |

### 2.8 attendance模块

```
attendance ──→ common, auth, student, class
```

| 属性 | 说明 |
|------|------|
| **直接依赖** | common, auth, student, class |
| **被依赖方** | report |
| **依赖合理性** | 考勤需关联学员（student）和班级排课（class），所有操作需认证（auth）；attendance数据仅被report用于考勤统计报表，模块间耦合度低 |
| **禁止依赖** | course, teacher, fee, report |

### 2.9 report模块

```
report ──→ common, auth, student, course, class, teacher, fee, attendance
```

| 属性 | 说明 |
|------|------|
| **直接依赖** | common, auth, student, course, class, teacher, fee, attendance |
| **被依赖方** | 无（顶层模块） |
| **依赖合理性** | report是分析汇总层，需要跨模块聚合数据生成综合报表，天然需要依赖所有业务模块。但其依赖方向是单向的——report只读数据、不修改数据，所有依赖均为数据查询而非数据写入。这样可以避免报表模块影响业务模块的数据一致性 |
| **特别约束** | report模块对业务模块的调用**仅限查询操作**，禁止通过report模块修改其他模块的数据 |

---

## 3. 依赖矩阵

| 依赖方 \ 被依赖方 | common | auth | student | course | class | teacher | fee | attendance | report |
|:----------------:|:------:|:----:|:-------:|:------:|:-----:|:-------:|:---:|:----------:|:------:|
| **common** | — | | | | | | | | |
| **auth** | ✓ | — | | | | | | | |
| **student** | ✓ | ✓ | — | ✓ | ✓ | | | | |
| **course** | ✓ | ✓ | | — | | | | | |
| **class** | ✓ | ✓ | | ✓ | — | ✓ | | | |
| **teacher** | ✓ | ✓ | | | | — | | | |
| **fee** | ✓ | ✓ | ✓ | ✓ | | | — | | |
| **attendance** | ✓ | ✓ | ✓ | | ✓ | | | — | |
| **report** | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | — |

> 图例：✓ = 允许依赖，空白 = 禁止依赖，— = 自身

---

## 4. 循环依赖检查

### 4.1 检查结果

**结论：当前模块依赖拓扑无循环依赖。** 依赖方向严格遵循 L4 → L3 → L2 → L1 → L0 的向下原则。

### 4.2 高风险依赖场景与预防

| 风险场景 | 预防措施 |
|----------|----------|
| student ↔ class 循环 | student依赖class（分班），class不得反向依赖student。class如需查询学员数量等统计信息，通过report模块或直接查询数据库视图实现 |
| fee ↔ student 循环 | fee依赖student（收费关联学员），student不得依赖fee。学员详情页如需展示缴费记录，由前端分别调用两个接口组装，而非后端循环依赖 |
| attendance ↔ student 循环 | attendance依赖student（考勤关联学员），student不得依赖attendance |

---

## 5. 依赖合理性总结

| 模块 | 依赖数量 | 被依赖数量 | 角色定位 |
|------|:--------:|:----------:|----------|
| common | 0 | 8 | 基础设施提供者 |
| auth | 1 | 7 | 安全认证提供者 |
| student | 4 | 3 | 核心业务实体 |
| course | 2 | 4 | 基础数据提供者 |
| class | 4 | 3 | 教务编排节点 |
| teacher | 2 | 2 | 基础资源提供者 |
| fee | 4 | 1 | 核心财务模块 |
| attendance | 4 | 1 | 运营管理模块 |
| report | 8 | 0 | 数据聚合消费者 |

**设计原则验证**：
- **最少依赖原则**：common(0)、teacher(2)、course(2) 保持了最少依赖，降低耦合
- **依赖收敛原则**：所有模块依赖收敛于common和auth，形成稳定的底层抽象
- **无环原则**：dep检测确认无循环依赖，依赖图是有向无环图（DAG）
- **单向数据流**：业务数据从基础模块流向业务模块，最终汇聚到report模块

---

## 相关文档

- ADR-001：系统整体架构风格选型
- 架构风格声明-ASD.md
- 模块划分方案-MDS.md