---
skill_name: 接口与数据一致性校验
version: 1.0.0
author: AI Assistant
created: 2026-06-22
project: EduFeeMS
tags: [verification, consistency, api, database, contract]
---

# 接口与数据一致性校验

## 概述

接口与数据一致性校验子技能用于验证系统各端之间的接口契约一致性和数据模型一致性。该技能在OpenAPI契约（后端接口定义）、前端API调用代码（实际调用方式）和数据库DDL（数据存储结构）三个维度之间进行交叉对比，检测不一致项并标注偏差类型。核心目标是确保"前后端接口约定"、"代码实现"和"数据存储"三者保持同步，防止因接口字段不匹配、数据类型不一致或数据库字段遗漏导致的运行时错误。

该技能是EduFeeMS项目六级逆向校验体系中的第三级（L3-一致性维度），是保障前后端联调成功率和数据库操作正确性的关键校验环节。

## 输入要求

| 输入项 | 类型 | 是否必需 | 说明 |
|--------|------|----------|------|
| OpenAPI契约文件 | 文件路径 | 是 | OpenAPI 3.0.3 YAML格式的契约文件 |
| 前端API代码路径 | 目录路径 | 是 | 前端项目中api/目录的路径，包含所有API封装函数 |
| 数据库DDL文件 | 文件路径 | 是 | 数据库初始化SQL脚本（含建表语句） |
| 后端Controller代码路径 | 目录路径 | 否 | 用于四人对比的补充维度，默认使用OpenAPI契约替代 |
| 忽略规则 | JSON/YAML | 否 | 需要忽略的已知不一致项配置（白名单） |

## 处理流程

### 步骤1：OpenAPI契约解析

解析OpenAPI 3.0.3 YAML契约文件，提取以下结构化信息：

1. **接口清单**：
   - 每个path的操作方法（GET/POST/PUT/DELETE）
   - 接口路径、标签（tags）、摘要（summary）
   - 请求参数（path参数、query参数、header参数）
   - 请求体schema（$ref引用或内联定义）
   - 响应体schema（各HTTP状态码对应的返回结构）

2. **Schema定义解析**：
   - 提取components/schemas下所有数据模型定义
   - 解析每个schema的属性名、类型、是否必填、格式、约束（minimum/maximum/pattern/enum）
   - 展开$ref引用，构建完整的扁平化属性列表

3. **构建契约索引**：
   - 按tag（业务域）分组接口
   - 为每个接口生成签名：`{HTTP方法} {路径}`
   - 建立schema引用关系图

### 步骤2：前端API代码解析

扫描前端`api/`目录下的JavaScript/Vue文件：

1. **API函数提取**：
   - 识别所有HTTP请求调用（axios.get/post/put/delete或封装的request方法）
   - 提取每个调用的URL路径、HTTP方法、请求参数对象结构
   - 识别URL中的路径参数拼接模式（模板字符串`/student/${id}`）

2. **请求体结构分析**：
   - 从函数参数和请求配置中提取请求体对象的字段结构
   - 识别字段名（使用驼峰命名camelCase）

3. **响应处理分析**：
   - 提取响应数据的解构模式（`response.data.data.records`等）
   - 识别前端期望的响应字段名

### 步骤3：数据库DDL解析

解析数据库初始化SQL文件：

1. **表结构提取**：
   - 提取所有CREATE TABLE语句
   - 解析每个表的列定义：列名、数据类型、长度、是否可为NULL、默认值、注释
   - 识别主键、外键、索引、唯一约束

2. **字段映射规则**：
   - 数据库字段名：snake_case（如`student_name`、`fee_amount`）
   - OpenAPI Schema属性名：camelCase（如`studentName`、`feeAmount`）
   - 前端请求字段名：camelCase（如`studentName`、`feeAmount`）
   - 后端Entity字段名：camelCase（通过@TableField映射）

### 步骤4：三端交叉对比

按以下维度执行三维交叉对比：

#### 4.1 接口路径一致性（OpenAPI ↔ 前端API）

| 检查项 | OpenAPI契约 | 前端API代码 | 一致性判定 |
|--------|-------------|-------------|------------|
| URL路径 | `/students/{id}/enroll` | `/student/${id}/enroll` | 路径名不一致（students vs student） |
| HTTP方法 | POST | POST | 一致 |
| 路径参数 | `id` (integer) | `${id}` | 一致 |
| Query参数 | `pageNum`, `pageSize` | `{page: 1, size: 10}` | 参数名不一致（pageNum vs page） |

#### 4.2 请求/响应字段一致性（OpenAPI ↔ 前端API ↔ DDL）

| 维度 | OpenAPI Schema | 前端请求体 | 数据库DDL | 一致性 |
|------|---------------|-----------|-----------|--------|
| 字段名 | studentName | studentName | student_name | 命名风格映射正确 |
| 字段类型 | string | string | VARCHAR(50) | 类型兼容 |
| 必填性 | required | 前端未校验必填 | NOT NULL | 前端缺少必填校验 |
| 枚举值 | ["MALE","FEMALE"] | 未限制枚举 | VARCHAR(10) | 枚举约束不一致 |
| 字段存在性 | 有`remark`字段 | 未传递`remark` | 有`remark`列 | 前端遗漏字段 |

#### 4.3 数据库字段完整性（OpenAPI ↔ DDL）

| 检查项 | 说明 |
|--------|------|
| Schema属性是否都有对应数据库列 | OpenAPI定义的属性必须在DDL中有对应的列 |
| 数据库列是否都有API暴露 | DDL中不应有完全无API暴露的"死列" |
| 数据类型映射是否合理 | string↔VARCHAR、integer↔INT/BIGINT、number↔DECIMAL |
| 默认值一致性 | Schema的default值是否与DDL的DEFAULT一致 |

### 步骤5：不一致项分类与标注

对发现的不一致项进行分类：

| 不一致类型 | 标识 | 说明 | 影响 |
|------------|------|------|------|
| 路径不匹配 | PATH_MISMATCH | URL路径不一致 | 前端请求404 |
| 参数名不一致 | PARAM_NAME | 参数名称差异 | 参数传递失败 |
| 类型不一致 | TYPE_MISMATCH | 数据类型不兼容 | 数据解析错误或截断 |
| 必填不一致 | REQUIRED_MISMATCH | 必填约束差异 | 校验失败或数据不完整 |
| 字段缺失 | FIELD_MISSING | 一端存在而另一端缺失 | 数据丢失 |
| 字段冗余 | FIELD_REDUNDANT | DDL有列但无API暴露 | 死列/技术债 |
| 枚举不一致 | ENUM_MISMATCH | 枚举值定义不统一 | 枚举校验失败 |
| 约束不一致 | CONSTRAINT_MISMATCH | 长度/范围/格式约束差异 | 校验不一致 |

### 步骤6：生成一致性报告

## 提示词模板

```markdown
你是一个接口契约与数据一致性校验专家。请基于以下输入执行前后端接口和数据库字段一致性校验。

## 输入信息

### OpenAPI契约
{openapi_content}

### 前端API代码
{frontend_api_code}

### 数据库DDL
{ddl_content}

### 忽略规则（白名单）
{ignore_rules}

## 校验要求

1. 解析OpenAPI契约，提取所有接口定义和Schema模型
2. 解析前端API代码，提取所有HTTP请求调用
3. 解析数据库DDL，提取所有表结构定义
4. 从以下三个维度执行交叉对比：
   - 接口路径一致性（OpenAPI ↔ 前端API）
   - 请求/响应字段一致性（OpenAPI ↔ 前端API ↔ DDL）
   - 数据库字段完整性（OpenAPI ↔ DDL）
5. 对每个不一致项标注类型、影响范围和修复建议
6. 特别关注以下高风险不一致：
   - 金额字段类型不一致
   - 日期时间格式不一致
   - 路径参数位置不一致
   - 枚举值定义不一致

## 输出格式
请以Markdown表格格式输出一致性校验报告，包含：
- 接口路径一致性矩阵
- 字段一致性对比表
- 数据库字段完整性检查结果
- 不一致项修复优先级排序
```

## 输出格式

### 一致性校验报告结构

```markdown
# 接口与数据一致性校验报告

## 1. 报告概览
- 校验日期：YYYY-MM-DD
- OpenAPI契约版本：Vx.x
- 接口总数：xx个
- 数据库表总数：xx个
- 一致性评分：xx/100

## 2. 校验摘要

| 校验维度 | 检查项数 | 一致 | 不一致 | 一致率 |
|----------|----------|------|--------|--------|
| 接口路径 | 21 | 19 | 2 | 90.5% |
| 请求字段 | 85 | 78 | 7 | 91.8% |
| 响应字段 | 92 | 85 | 7 | 92.4% |
| 数据库字段 | 120 | 112 | 8 | 93.3% |
| 枚举定义 | 15 | 13 | 2 | 86.7% |

## 3. 接口路径一致性矩阵

| OpenAPI路径 | HTTP方法 | 前端API路径 | 前端方法 | 一致性 | 偏差说明 |
|-------------|----------|-------------|----------|--------|----------|
| /students | GET | /students | get | 一致 | - |
| /students/{id}/enroll | POST | /student/{id}/enroll | POST | 不一致 | 路径前缀students vs student |
| ... | ... | ... | ... | ... | ... |

## 4. Schema字段一致性对比

### 4.1 StudentDTO

| 字段名(OpenAPI) | 类型(OpenAPI) | 前端请求字段 | 数据库列 | DDL类型 | 一致性 |
|-----------------|---------------|-------------|----------|---------|--------|
| id | integer(int64) | id | id | BIGINT | 一致 |
| name | string | name | student_name | VARCHAR(50) | 命名映射正确 |
| phone | string | phone | phone | VARCHAR(20) | 一致 |
| ... | ... | ... | ... | ... | ... |

## 5. 数据库字段完整性检查

| 表名 | DDL列 | OpenAPI暴露 | 前端使用 | 状态 | 说明 |
|------|-------|-------------|----------|------|------|
| t_student_info | create_by | ✓ | ✗ | OK | 后端自动填充，前端无需传递 |
| t_fee_record | internal_note | ✗ | ✗ | 冗余 | DDL中存在但无API暴露的列 |

## 6. 不一致项修复计划

| 优先级 | 不一致项 | 类型 | 影响范围 | 修复方案 | 预估工时 |
|--------|----------|------|----------|----------|----------|
| P0 | 退费金额字段类型为float | TYPE_MISMATCH | fee模块 | 改为BigDecimal | 1h |
| P1 | 前端分页参数名page vs pageNum | PARAM_NAME | 全部列表页 | 前端统一参数名 | 2h |
| ... | ... | ... | ... | ... | ... |
```

## 使用示例

### 示例1：全栈一致性校验

```
输入：
  - OpenAPI契约：designs/contracts/openapi.yaml
  - 前端API代码：src/frontend/src/api/
  - 数据库DDL：src/backend/sql/init.sql

处理流程：
1. 解析openapi.yaml中21个接口定义和40+个Schema
2. 解析api/目录下8个JS文件的API调用
3. 解析init.sql中所有CREATE TABLE语句
4. 逐接口对比URL路径一致性
5. 逐Schema对比字段名、类型、必填性
6. 对比DDL列和Schema属性的对应关系

预期发现：
- 路径前缀不一致：前端/api/v1/students vs OpenAPI /students
- 分页参数名差异：前端page/size vs OpenAPI pageNum/pageSize
- DDL中有audit相关列但OpenAPI未暴露
```

### 示例2：收费模块接口一致性

```
输入：
  - OpenAPI路径筛选：tags包含"fees"的接口
  - 前端API代码：src/frontend/src/api/fee.js
  - 数据库DDL：fee相关表（t_fee_record, t_fee_item, t_refund等）

重点关注：
- PaymentCreateRequest中amount字段类型（number ↔ DECIMAL(10,2)）
- RefundRequest中金额校验约束的一致性
- 收费台账查询的分页参数一致性
- fee模块特有的Enum值定义一致性（PaymentStatus, PaymentMethod）
```
