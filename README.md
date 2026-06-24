# 教育培训机构教务收费管理系统 (edu-fee-management)

## 项目简介

本项目为教育培训机构的教务收费管理系统，旨在为培训机构提供一站式的学员收费、课程管理、费用核算与报表分析功能。系统基于 Obsidian Karpathy 四层知识库架构组织项目文档与设计资产。

## 知识库架构（Karpathy 四层模型）

```
edu-fee-management/
├── raw/                    # 第1层：原始输入层
│   ├── notes/              #   涉众原始需求记录
│   └── dialogs/            #   对话记录（与客户/团队沟通纪要）
├── wiki/                   # 第2层：知识提取层
│   ├── summaries/          #   UML模型、分析结果、领域知识卡片
│   └── baselines/          #   基线版本目录（各阶段快照）
├── designs/                # 第3层：设计层
│   ├── adr/                #   架构决策记录（Architecture Decision Records）
│   ├── diagrams/           #   设计图（C4模型、ER图、流程图等）
│   └── contracts/          #   OpenAPI接口契约
├── src/                    # 第4层：实现层（源代码目录，后续创建）
├── docs/                   # 综合文档（项目计划、部署手册等）
├── skills/                 # 子技能文件（可复用的分析/设计技能定义）
├── assets/                 # 设计资产包（原型截图、品牌素材等）
├── .obsidian/              # Obsidian 配置目录
├── compile.js              # 编译验证脚本
└── README.md               # 项目说明文件（本文件）
```

## 各目录职责说明

| 目录 | 职责 | 文件格式 |
|------|------|----------|
| `raw/notes/` | 存放从涉众访谈、需求调研中获取的原始需求记录 | Markdown |
| `raw/dialogs/` | 存放与客户、团队的沟通对话记录 | Markdown |
| `wiki/summaries/` | 存放 UML 用例图、类图、领域分析结果等 | Markdown / Mermaid |
| `wiki/baselines/` | 按版本存放各阶段的知识基线快照 | Markdown |
| `designs/adr/` | 存放 ADR（架构决策记录），记录关键架构决策及理由 | Markdown |
| `designs/diagrams/` | 存放 C4 架构图、ER 图、流程图等设计图 | SVG / PNG / Mermaid |
| `designs/contracts/` | 存放 OpenAPI 3.x 接口契约定义 | YAML / JSON |
| `src/` | 项目源代码，按技术栈组织（后续创建） | 取决于技术选型 |
| `docs/` | 综合项目文档（开发指南、部署文档、API 文档等） | Markdown |
| `skills/` | 可复用的子技能定义文件 | Markdown / JSON |
| `assets/` | 设计相关的资产包（原型图、品牌资源） | PNG / SVG / PDF |

## 快速开始

### 在 Obsidian 中打开

1. 安装 [Obsidian](https://obsidian.md/)
2. 打开 Obsidian，选择「打开其他库」
3. 浏览并选择本目录 `edu-fee-management/`
4. 即可在 Obsidian 中浏览和编辑知识库

### 编译验证

运行编译验证脚本，检查知识库结构完整性：

```bash
node compile.js
```

## 项目阶段规划

- **Phase 0**：知识库初始化与需求调研（当前阶段）
- **Phase 1**：领域建模与架构设计
- **Phase 2**：核心功能实现
- **Phase 3**：集成测试与部署上线

## 维护说明

- 所有 `.gitkeep` 文件用于保持空目录的版本追踪，当目录下有实际文件后可删除
- 知识库内容遵循 Markdown 格式，推荐使用 Obsidian 编辑
- ADR 决策记录采用 [Michael Nygard 的模板](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
