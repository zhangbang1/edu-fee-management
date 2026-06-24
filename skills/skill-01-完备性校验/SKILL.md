---
skill_name: 需求完备性校验
version: 1.0.0
author: AI Assistant
created: 2026-06-22
project: EduFeeMS
tags: [verification, completeness, requirement-traceability]
---

# 需求完备性校验

## 概述

需求完备性校验子技能用于验证代码实现是否全面覆盖了软件需求规格说明书（SRS）中定义的所有功能需求。该技能通过系统性地将SRS中的每一条REQ需求与源代码中的Controller和Service方法进行逐条对照，生成覆盖率报告，标识出已实现、部分实现和未实现的需求项，为开发团队提供实现完备性的量化评估。

该技能是EduFeeMS项目六级逆向校验体系中的第一级（L1-完备性维度），旨在确保所有SRS规定的功能需求在代码层面均有对应实现，防止功能遗漏导致的交付缺陷。

## 输入要求

| 输入项 | 类型 | 是否必需 | 说明 |
|--------|------|----------|------|
| SRS文档路径 | 文件路径 | 是 | IEEE 830格式的SRS文档，包含所有REQ-xxx需求条目 |
| 源代码路径 | 目录路径 | 是 | 项目源代码根目录，如`src/backend/` |
| 需求清单 | 文件路径 | 否 | 从SRS中提取的结构化需求清单（markdown表格格式），若未提供则自动从SRS文档中解析 |
| 模块范围 | 字符串列表 | 否 | 限定检查的模块列表，如`["student","fee","course"]`，默认检查全部9个模块 |
| 输出格式 | 枚举 | 否 | `markdown`、`json`、`html`，默认为`markdown` |

## 处理流程

### 步骤1：SRS需求解析

从SRS文档中提取所有功能需求条目，解析每个需求的以下属性：

- **需求编号**：如 REQ-STU-001、REQ-FEE-002
- **功能描述**：需求的简要说明
- **所属模块**：如 student、course、class、teacher、fee、attendance、report、auth
- **优先级**：高/中/低
- **输入输出**：明确的输入参数和预期输出
- **业务规则**：关联的业务规则编号

解析策略：
1. 扫描SRS文档中所有以`REQ-`开头的需求标识符
2. 提取每个需求的编号、标题、功能描述
3. 根据需求前缀判断所属模块（STU→student、CRS→course、CLS→class、TCH→teacher、FEE→fee、ATT→attendance、RPT→report、AUTH→auth）
4. 构建需求清单（Requirements Checklist）

### 步骤2：源代码扫描

在源代码路径下扫描所有Controller类和Service接口/实现类：

1. **Controller扫描**：搜索`*Controller.java`文件，提取`@RequestMapping`路径和`@PostMapping/@GetMapping/@PutMapping/@DeleteMapping`方法
2. **Service扫描**：搜索`*Service.java`和`*ServiceImpl.java`文件，提取public方法签名
3. **方法解析**：提取每个方法的以下元数据：
   - 方法名
   - HTTP方法和URL路径（Controller）
   - 参数列表（参数名、类型、注解）
   - 返回类型
   - JavaDoc注释（如有）
   - @Operation Swagger注解（如有）

### 步骤3：需求-代码映射

将SRS中的每一条需求与源代码中的方法进行匹配：

1. **关键词匹配**：根据需求编号中的功能关键词（如"分班"→assignClass、"退学"→withdraw、"退费"→refund）在方法名和注解中搜索
2. **语义匹配**：分析需求描述文本，生成候选匹配词列表，在方法名和JavaDoc中模糊匹配
3. **路径匹配**：根据需求的RESTful语义推断可能的API路径，与Controller的@RequestMapping路径进行对比

匹配结果分为四类状态：
- **完全覆盖（COVERED）**：需求有明确的对应Controller方法和Service方法实现
- **部分覆盖（PARTIAL）**：需求有对应的Service方法但缺少Controller暴露，或Controller存在但Service逻辑不完整
- **未覆盖（MISSING）**：需求在代码中找不到对应实现
- **待确认（UNCLEAR）**：由于命名不清晰或需求描述模糊，无法确定覆盖状态

### 步骤4：覆盖率计算

计算各维度的覆盖率指标：

| 指标 | 计算公式 | 说明 |
|------|----------|------|
| 总体覆盖率 | COVERED数量 / 总需求数 × 100% | 整体实现完备度 |
| 模块覆盖率 | 各模块内COVERED数 / 模块需求总数 | 按模块维度的完备度 |
| 高优先级覆盖率 | 高优先级需求COVERED数 / 高优先级需求总数 | 核心功能完备度 |
| 业务规则覆盖率 | 已实现业务规则数 / 总业务规则数 | 业务逻辑完备度 |

### 步骤5：生成覆盖率报告

根据验证结果生成结构化的覆盖率报告，包含：

1. **报告摘要**：总体覆盖率、各模块覆盖率、高优需求覆盖率
2. **需求-代码映射表**：每条需求的覆盖状态和对应的代码位置
3. **缺口清单**：未覆盖（MISSING）和部分覆盖（PARTIAL）需求的详细列表
4. **建议行动**：针对缺口提出的补全建议

## 提示词模板

```markdown
你是一个软件需求完备性校验专家。请基于以下SRS文档和源代码，执行需求完备性校验。

## 输入信息

### SRS文档
{srs_content}

### 需求清单
{requirements_checklist}

### 源代码路径
{source_code_path}

### 模块范围
{module_scope}

## 校验要求

1. 从SRS中提取所有REQ-xxx编号的功能需求，构建完整需求清单
2. 在源代码中搜索每个需求对应的Controller方法和Service方法
3. 对每个需求标注覆盖状态：COVERED / PARTIAL / MISSING / UNCLEAR
4. 计算各维度覆盖率指标
5. 生成覆盖率报告，包含：
   - 需求-代码映射矩阵
   - 覆盖率统计（总体/模块/优先级维度）
   - 未覆盖需求清单及建议
   - PARTIAL状态需求的缺口说明

## 注意
- 方法名匹配时注意驼峰命名规范和中英文术语映射
- 对于通过工具类或公共组件实现的功能，尝试追踪调用链路
- 单元测试代码不纳入覆盖率统计范围
- 标记为UNCLEAR的需求需要人工审核确认

## 输出格式
请以Markdown表格格式输出覆盖率报告，包含摘要表和详细映射表。
```

## 输出格式

### 覆盖率报告结构

```markdown
# 需求完备性校验报告

## 1. 报告概览
- 校验日期：YYYY-MM-DD
- SRS版本：Vx.x
- 源代码路径：xxx
- 校验范围：x个模块
- 总体覆盖率：xx.x%

## 2. 覆盖率摘要

| 模块 | 需求总数 | COVERED | PARTIAL | MISSING | UNCLEAR | 覆盖率 |
|------|----------|---------|---------|---------|---------|--------|
| student | 8 | 7 | 1 | 0 | 0 | 87.5% |
| course | 5 | 5 | 0 | 0 | 0 | 100% |
| class | 5 | 4 | 0 | 1 | 0 | 80% |
| ... | ... | ... | ... | ... | ... | ... |

## 3. 需求-代码映射表

| 需求编号 | 需求描述 | 覆盖状态 | Controller方法 | Service方法 | 备注 |
|----------|----------|----------|----------------|-------------|------|
| REQ-STU-001 | 学员信息录入 | COVERED | StudentController.create() | StudentService.create() | 完整实现 |
| REQ-STU-004 | 学员分班 | COVERED | StudentController.enroll() | StudentService.assignClass() | 完整实现 |
| ... | ... | ... | ... | ... | ... |

## 4. 缺口清单

### 4.1 MISSING需求

| 需求编号 | 需求描述 | 所属模块 | 优先级 | 建议 |
|----------|----------|----------|--------|------|

### 4.2 PARTIAL需求

| 需求编号 | 需求描述 | 缺失部分 | 建议 |
|----------|----------|----------|------|

## 5. 业务规则覆盖率

| 规则编号 | 关联需求 | 是否实现 | 实现位置 |
|----------|----------|----------|----------|
```

## 使用示例

### 示例1：全项目完备性校验

```
输入：
  - SRS文档路径：wiki/baselines/BL-20260622-01/SRS-正式版.md
  - 源代码路径：src/backend/
  - 模块范围：全部
  - 输出格式：markdown

输出：
  - 覆盖率报告文件：reports/completeness-report-20260622.md
```

### 示例2：按模块校验

```
输入：
  - SRS文档路径：wiki/baselines/BL-20260622-01/SRS-正式版.md
  - 源代码路径：src/backend/
  - 模块范围：["fee", "attendance"]
  - 输出格式：json

处理：
  1. 从SRS中筛选FEE和ATT前缀的需求（REQ-FEE-001~007, REQ-ATT-001~005等）
  2. 在edu-fee和edu-attendance模块源码中搜索对应实现
  3. 生成JSON格式的覆盖率报告
```

### 示例3：增量校验

当SRS或代码发生变更后，仅校验变更影响范围内的需求覆盖：

```
输入：
  - SRS文档路径：wiki/baselines/BL-20260622-01/SRS-正式版.md
  - 源代码路径：src/backend/
  - 变更模块：["student"]
  - 输出格式：markdown

处理：
  仅对student模块相关的REQ需求进行重新校验，其他模块使用上次校验的缓存结果
```
