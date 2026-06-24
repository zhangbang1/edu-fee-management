/**
 * compile.js — 教育培训机构教务收费管理系统 知识库编译验证脚本
 *
 * 功能：
 *   1. 验证 Karpathy 四层知识库目录结构完整性
 *   2. 检查关键配置文件的完整性
 *   3. 统计知识库内容规模
 *   4. 输出验证报告
 *
 * 用法：
 *   node compile.js
 *   node compile.js --verbose    (详细模式)
 *   node compile.js --summary    (仅摘要)
 */

const fs = require("fs");
const path = require("path");

// ============================================================
// 配置
// ============================================================

const ROOT = __dirname;

// Karpathy 四层架构期望目录清单
const EXPECTED_DIRS = [
  "raw/notes",
  "raw/dialogs",
  "wiki/summaries",
  "wiki/baselines",
  "designs/adr",
  "designs/diagrams",
  "designs/contracts",
  "src",
  "docs",
  "skills",
  "assets",
  ".obsidian",
];

// 关键配置文件
const KEY_FILES = [
  "README.md",
  "compile.js",
  ".obsidian/app.json",
  ".obsidian/appearance.json",
  ".obsidian/core-plugins.json",
];

// ============================================================
// 工具函数
// ============================================================

/** 递归获取目录下所有文件（排除 node_modules, .git） */
function walkDir(dirPath) {
  const results = [];
  if (!fs.existsSync(dirPath)) return results;

  let entries;
  try {
    entries = fs.readdirSync(dirPath, { withFileTypes: true });
  } catch (e) {
    // 跳过无法访问的目录（如已删除的符号链接）
    return results;
  }
  for (const entry of entries) {
    const fullPath = path.join(dirPath, entry.name);
    if (entry.name === "node_modules" || entry.name === ".git") continue;
    if (entry.isDirectory()) {
      results.push(...walkDir(fullPath));
    } else {
      results.push(fullPath);
    }
  }
  return results;
}

/** 统计文件的扩展名分布 */
function countFileTypes(files) {
  const counts = {};
  for (const f of files) {
    const ext = path.extname(f).toLowerCase() || "(no-ext)";
    counts[ext] = (counts[ext] || 0) + 1;
  }
  return counts;
}

// ============================================================
// 验证逻辑
// ============================================================

function verifyDirectories() {
  const results = { ok: [], missing: [] };

  for (const dir of EXPECTED_DIRS) {
    const dirPath = path.join(ROOT, dir);
    if (fs.existsSync(dirPath) && fs.statSync(dirPath).isDirectory()) {
      results.ok.push(dir);
    } else {
      results.missing.push(dir);
    }
  }
  return results;
}

function verifyKeyFiles() {
  const results = { ok: [], missing: [] };

  for (const file of KEY_FILES) {
    const filePath = path.join(ROOT, file);
    if (fs.existsSync(filePath)) {
      results.ok.push(file);
    } else {
      results.missing.push(file);
    }
  }
  return results;
}

// ============================================================
// 主入口
// ============================================================

function main() {
  const args = process.argv.slice(2);
  const verbose = args.includes("--verbose");
  const summaryOnly = args.includes("--summary");

  console.log("============================================================");
  console.log("  教育培训机构教务收费管理系统 — 知识库编译验证");
  console.log("  架构: Obsidian Karpathy 四层模型");
  console.log("============================================================\n");

  // 1. 目录结构验证
  const dirs = verifyDirectories();
  console.log("[1] 目录结构验证:");
  console.log(`    OK: ${dirs.ok.length} 个目录`);
  if (verbose) dirs.ok.forEach((d) => console.log(`      + ${d}`));
  console.log(`    缺失: ${dirs.missing.length} 个目录`);
  if (dirs.missing.length > 0) {
    dirs.missing.forEach((d) => console.log(`      ! ${d} (缺失)`));
  }

  // 2. 关键文件验证
  const files = verifyKeyFiles();
  console.log("\n[2] 关键文件验证:");
  console.log(`    OK: ${files.ok.length} 个文件`);
  if (verbose) files.ok.forEach((f) => console.log(`      + ${f}`));
  console.log(`    缺失: ${files.missing.length} 个文件`);
  if (files.missing.length > 0) {
    files.missing.forEach((f) => console.log(`      ! ${f} (缺失)`));
  }

  // 3. 内容统计
  const allFiles = walkDir(ROOT);
  const fileTypes = countFileTypes(allFiles);
  const totalContentFiles = allFiles.filter((f) => !f.includes(".gitkeep")).length;

  console.log("\n[3] 内容统计:");
  console.log(`    知识库文件总数: ${totalContentFiles}`);
  console.log(`    总文件数（含占位）: ${allFiles.length}`);
  if (verbose) {
    console.log("    文件类型分布:");
    const sorted = Object.entries(fileTypes).sort((a, b) => b[1] - a[1]);
    sorted.forEach(([ext, count]) => console.log(`      ${ext}: ${count} 个`));
  }

  // 4. 验证结论
  const allOk = dirs.missing.length === 0 && files.missing.length === 0;
  console.log("\n============================================================");
  if (allOk) {
    console.log("  验证通过! 知识库结构完整。");
  } else {
    console.log("  验证未通过! 存在缺失的目录或文件，请检查。");
    console.log(`  缺失目录: ${dirs.missing.join(", ") || "无"}`);
    console.log(`  缺失文件: ${files.missing.join(", ") || "无"}`);