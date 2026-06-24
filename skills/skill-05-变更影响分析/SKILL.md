---
skill_name: 变更影响分析
version: 1.0.0
author: AI Assistant
created: 2026-06-22
project: EduFeeMS
tags: [change-management, impact-analysis, traceability, estimation]
---

# 变更影响分析

## 概述

变更影响分析（Change Impact Analysis, CIA）子技能用于在需求变更发生时，系统性地评估变更对已有代码的影响范围、分析变更的可行性和风险、估算变更工作量，并生成结构化的变更影响分析报告。该技能通过解析变更需求文档（CR），追踪代码依赖关系（基于模块依赖拓扑DTS），逐层分析受影响的模块、文件和方法，为CCB（变更控制委员会）提供决策依据，确保变更的可控性和可追溯性。

该技能是EduFeeMS项目六级逆向校验体系中的第五级（L5-变更影响维度），是实现需求变更管理闭环的核心工具，支持从v1到v2的版本迭代管理。

## 输入要求

| 输入项 | 类型 | 是否必需 | 说明 |
|--------|------|----------|------|
| CR变更需求文档 | 文件路径 | 是 | 变更需求文档，描述变更的背景、内容和范围 |
| 源代码路径 | 目录路径 | 是 | 项目全栈源代码根目录 |
| 基线SRS文档 | 文件路径 | 是 | 变更前的基线SRS文档 |
| 模块依赖拓扑 | 文件路径 | 否 | DTS文档，提供模块间依赖关系 |
| MDS模块划分方案 | 文件路径 | 否 | 模块划分方案，提供模块职责边界 |
| OpenAPI契约 | 文件路径 | 否 | 当前版本的接口契约 |
| 历史CIA报告 | 文件路径 | 否 | 以往的变更影响分析报告（用于参考） |
| 约束提示词库 | 目录路径 | 否 | 用于评估变更是否违反三层约束 |

## 处理流程

### 步骤1：变更条目解析

从CR变更需求文档中解析变更条目：

1. **变更类型识别**：
   - **新增功能（NEW）**：增加新的功能需求REQ
   - **修改功能（MODIFY）**：修改已有REQ的功能描述、输入输出或业务规则
   - **删除功能（DELETE）**：移除不再需要的功能需求
   - **重构优化（REFACTOR）**：不改变外部行为，仅改善内部结构
   - **缺陷修复（FIX）**：修复已发现的缺陷

2. **变更条目结构化**：
   ```
   CR-xxx:
     - 类型: MODIFY
     - 标题: 退费规则调整
     - 描述: 将退费比例从...调整为...
     - 影响范围: fee模块, student模块（退学关联退费）
     - 关联REQ: REQ-FEE-004, REQ-STU-006
     - 关联BR: BR-FEE-004-01, BR-STU-006-04
     - 优先级: 高
   ```

### 步骤2：代码依赖追踪

基于模块依赖拓扑（DTS），追踪变更影响的扩散路径：

1. **一级影响（直接变更）**：
   - 变更条目明确涉及的模块和文件
   - 需要直接修改的Controller、Service、Repository、Entity、DTO

2. **二级影响（波及变更）**：
   - 被一级影响模块依赖的其他模块（下游依赖）
   - 根据DTS依赖矩阵，递归追踪所有被依赖方

3. **三级影响（间接变更）**：
   - 前端的对应页面组件和API封装
   - OpenAPI契约的更新
   - 数据库DDL的变更
   - 单元测试和集成测试用例的更新
   - SRS文档的更新

### 步骤3：影响范围评估

对每个受影响的文件和方法进行详细分析：

#### 3.1 代码变更分析

| 分析维度 | 评估内容 |
|----------|----------|
| 文件变更 | 需要修改的文件列表及变更类型（修改/新增/删除） |
| 方法变更 | 需要修改的方法签名、方法体逻辑变更 |
| 数据变更 | Entity字段新增/修改/删除、DTO/VO结构调整 |
| 接口变更 | Controller路径变更、请求参数变更、响应结构变更 |
| 数据库变更 | 表结构变更（加列/改类型/加表）、数据迁移 |
| 配置变更 | application.yml、常量定义、枚举值 |

#### 3.2 约束满足性评估

评估变更是否符合三层约束设计（TLCD）：

| 约束层级 | 检查项 |
|----------|--------|
| 架构层约束 | 变更是否违反分层原则、依赖方向、统一响应格式 |
| 模块层约束 | 变更是否引入新的跨模块依赖、是否违反模块依赖白名单 |
| 代码层约束 | 变更是否遵守BigDecimal使用规范、命名规范、异常处理规范 |

#### 3.3 风险等级评估

| 风险等级 | 定义 | 判定标准 |
|----------|------|----------|
| 极高（CRITICAL） | 变更可能导致系统不可用或数据错误 | 涉及收费计算、数据库核心表结构变更 |
| 高（HIGH） | 变更可能影响核心业务流程 | 涉及模块间接口变更、状态流转规则变更 |
| 中（MEDIUM） | 变更可能影响非核心功能 | 涉及查询条件扩展、新增独立功能 |
| 低（LOW） | 变更影响范围可控 | UI调整、文案修改、日志增强 |

### 步骤4：工作量估算

使用结构化估算法估算变更工作量：

| 工作项 | 估算方法 | 单位 |
|--------|----------|------|
| 后端代码修改 | 受影响的文件数 × 平均修改行数 / 编码速率 | 人时 |
| 前端代码修改 | 受影响的页面/组件数 × 平均复杂度系数 | 人时 |
| 数据库变更 | DDL变更数 × 迁移脚本复杂度 | 人时 |
| 契约更新 | OpenAPI变更条目数 × 平均耗时 | 人时 |
| 测试编写 | 变更关联REQ数 × 测试用例数 × 用例耗时 | 人时 |
| 文档更新 | SRS更新 + 基线创建 + wiki更新 | 人时 |
| 回归校验 | 受影响模块数 × 回归校验平均耗时 | 人时 |

### 步骤5：生成变更影响分析报告

## 提示词模板

```markdown
你是一个软件变更影响分析专家，精通Java Spring Boot项目架构和变更管理流程。请基于以下输入执行变更影响分析。

## 输入信息

### 变更需求文档（CR）
{cr_content}

### 基线SRS文档
{baseline_srs_content}

### 模块依赖拓扑（DTS）
{dts_content}

### 模块划分方案（MDS）
{mds_content}

### 源代码路径
{source_code_path}

### 当前OpenAPI契约
{openapi_content}

## 分析要求

1. 解析CR文档，提取所有变更条目（CR-xxx）
2. 对每个变更条目：
   a. 识别变更类型（NEW/MODIFY/DELETE/REFACTOR/FIX）
   b. 关联受影响的REQ需求和BR业务规则
   c. 在源代码中定位受影响的文件和方法
   d. 基于DTS追踪变更的二级和三级影响范围
3. 评估变更风险等级（CRITICAL/HIGH/MEDIUM/LOW）
4. 估算变更工作量（分后端/前端/数据库/测试/文档维度）
5. 检查变更是否违反三层约束（TLCD）
6. 生成变更影响分析报告（CIA）

## 变更影响追踪模板

| CR编号 | 变更类型 | 一级影响（直接） | 二级影响（波及） | 三级影响（间接） | 风险等级 | 工作量 |
|--------|----------|-----------------|-----------------|-----------------|----------|--------|
| CR-xxx | MODIFY | FeeService.refund() | StudentService.withdraw() | fee.js, FeeRecords.vue | HIGH | 8人时 |

## 输出格式
请以Markdown格式输出完整的变更影响分析报告，包含变更概述、影响范围矩阵、约束检查结果、工作量和排期建议。
```

## 输出格式

### 变更影响分析报告结构

```markdown
# 变更影响分析报告（CIA）

## 1. 报告概述

| 属性 | 值 |
|------|-----|
| CIA报告编号 | CIA-20260622-001 |
| 关联CR | CR-001 ~ CR-003 |
| 基线SRS版本 | BL-20260622-01 |
| 分析日期 | 2026-06-22 |
| 分析师 | AI Assistant |
| 总体风险等级 | MEDIUM |
| 总预估工作量 | 24人时（3人天） |

## 2. 变更概述

变更CR-001描述了将退费规则从固定比例调整为阶梯比例的需求。当前退费规则为：已上1/3以内退60%，1/3~2/3退30%，超过2/3不退。新需求要求根据课程类型（学科类/兴趣类）设置不同的退费阶梯比例...

## 3. 影响范围分析

### 3.1 变更影响依赖图

```
CR-001: 退费规则调整
├── [一级] FeeServiceImpl.refund() — 修改退费计算逻辑
│   ├── [波及] FeeItem entity — 新增refund_rule_type字段
│   ├── [波及] FeeRecordDTO — 新增refundDetail字段
│   └── [波及] CourseService — 读取课程类型以匹配退费规则
├── [二级] StudentServiceImpl.withdraw() — 退学页面退费预估逻辑调整
│   ├── [波及] StudentDetailResponse — 新增estimatedRefund
│   └── [间接] WithdrawResponse Schema — 新增字段
├── [三级] 前端fee.js — 退费API参数调整
├── [三级] 前端FeeRecords.vue — 退费详情展示调整
├── [三级] init.sql — t_fee_record表加列
├── [三级] openapi.yaml — RefundRequest/RefundResponse Schema更新
└── [三级] SRS-正式版.md — REQ-FEE-004和BR-FEE-004-01更新
```

### 3.2 受影响文件清单

| 文件路径 | 变更类型 | 变更说明 | 影响级别 |
|----------|----------|----------|----------|
| edu-fee/.../FeeServiceImpl.java | MODIFY | refund()方法增加阶梯比例计算 | 一级 |
| edu-fee/.../entity/FeeItem.java | MODIFY | 新增refundRuleType字段 | 一级 |
| edu-fee/.../dto/RefundDTO.java | MODIFY | 新增refundRuleType参数 | 一级 |
| edu-student/.../StudentServiceImpl.java | MODIFY | withdraw()退费预估逻辑适配 | 二级 |
| src/frontend/src/api/fee.js | MODIFY | refund()函数参数调整 | 三级 |
| src/frontend/.../FeeRecords.vue | MODIFY | 退费明细展示调整 | 三级 |
| src/backend/sql/migration/V2__add_refund_rule.sql | NEW | 数据迁移脚本 | 三级 |
| designs/contracts/openapi.yaml | MODIFY | RefundRequest Schema更新 | 三级 |

## 4. 约束检查

| 约束编号 | 约束内容 | 检查结果 | 说明 |
|----------|----------|----------|------|
| C-ARCH-004 | @Transactional覆盖 | PASS | refund()方法已有事务注解 |
| C-MOD-026 | BigDecimal金额 | PASS | 阶梯计算使用BigDecimal |
| C-MOD-029 | 退费金额校验 | PASS | 需确保新规则下仍校验≤已收金额 |
| C-CODE-003 | 无float/double | PASS | 现有代码已全部使用BigDecimal |
| DTS规则 | 无循环依赖 | PASS | 变更不引入新依赖 |

## 5. 风险评估

| 风险项 | 等级 | 描述 | 缓解措施 |
|--------|------|------|----------|
| 历史退费记录兼容 | HIGH | 旧版退费记录按旧规则，新需兼容查询 | version字段区分规则版本 |
| 并发退费 | MEDIUM | 改规则期间并发退费可能使用不同规则 | 规则生效时间原子切换 |
| 计算精度 | LOW | 阶梯比例可能导致小数点后多位 | 统一HALF_UP舍入 |

## 6. 工作量估算

| 工作类别 | 工作项 | 预估工时 | 依赖 |
|----------|--------|----------|------|
| 后端开发 | FeeServiceImpl.refund()改造 | 4h | - |
| 后端开发 | 数据库迁移脚本 | 1h | - |
| 后端开发 | DTO/VO调整 | 2h | - |
| 前端开发 | fee.js + FeeRecords.vue | 3h | 后端API就绪 |
| 测试 | 单元测试更新 | 3h | 后端完成 |
| 测试 | 回归校验 | 近20条fee模块规则 | 全部完成 |
| 文档 | SRS/OpenAPI更新 | 2h | - |
| **合计** | | **24h (3人天)** | |

## 7. 回归校验范围

基于变更影响分析，建议回归校验覆盖以下范围：

| 校验维度 | 校验范围 | 关联技能 |
|----------|----------|----------|
| 完备性 | fee模块全部7条REQ | skill-01 |
| 正确性 | fee模块全部BR-FEE规则（约20条） | skill-02 |
| 一致性 | fee模块接口、Refund Schema、t_fee_record表 | skill-03 |
| 有效性 | SC-03退学退费场景 | skill-04 |
```

## 使用示例

### 示例1：退费规则变更影响分析

```
输入：
  - CR文档：描述退费规则从固定比例改为按课程类型阶梯比例
  - 源代码路径：src/backend/edu-fee/
  - 基线SRS：wiki/baselines/BL-20260622-01/SRS-正式版.md
  - DTS文档：designs/diagrams/模块依赖拓扑-DTS.md

处理流程：
1. 解析CR，识别变更类型为MODIFY
2. 确定一级影响为FeeServiceImpl.refund()方法
3. 基于DTS追踪：
   - fee → student（退学流程关联退费）→ 二级影响
   - student → class（班级信息）→ 无直接影响
   - fee ← course（读取课程类型）→ 一级影响
4. 追踪前端影响：fee.js退款API → FeeRecords.vue和Payment.vue
5. 评估数据库变更：需新增refund_rule_config配置表
6. 评估风险：历史数据兼容性为中风险

输出：完整的CIA报告，含影响文件清单、工作量估算和回归校验建议
```

### 示例2：新增"发票管理"功能影响分析

```
输入：
  - CR文档：新增发票管理子模块（发票开具、发票查询、发票红冲）
  - 源代码路径：全栈源代码
  - MDS文档：designs/diagrams/模块划分方案-MDS.md

处理流程：
1. 变更类型：NEW（新增功能）
2. 影响分析：
   - 需要在fee模块下新增Invoice子模块（Controller + Service + Repository + Entity）
   - fee模块增加依赖：可能需要引入发票开具SDK
   - 前端新增InvoiceManage.vue和InvoiceDetail.vue
   - 数据库新增t_invoice表
   - OpenAPI新增/invoices路径的接口定义
3. MDS评估：是否需要将发票管理独立为新模块还是作为fee的子模块
4. 约束检查：评估是否违反模块依赖白名单
5. 工作量：全新功能开发，预估40人时
```
