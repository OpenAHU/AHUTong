# AHUTong AIO 仓库拆分设计

## 目标

将当前 `AHUTong/master` 的 Android 项目完整迁移到独立仓库 `OpenAHU/AHUTong-Android`，保留所有 Git 历史和现有嵌套 submodule；同时在原 `AHUTong` 仓库创建 `feat/AHUTong-aio` 分支，将 Android 客户端以 `Android/` submodule 形式接入，为以后并列加入 HarmonyOS 和 iOS 客户端预留结构。

## 已确认约束

- Android 独立仓库必须保留当前 `master` 的完整 Git 历史和提交哈希。
- Android 独立仓库的本地路径固定为 `D:\code\AHUTong-Android`。
- Android 独立仓库的远端固定为 `https://github.com/OpenAHU/AHUTong-Android.git`。
- AIO 改造分支固定为 `feat/AHUTong-aio`，从当前 `master` 提交 `2a30a54e74127ce1b4f75763596b470bd0b9d01b` 创建。
- AIO 中的 Android submodule 路径固定为 `Android/`。
- AIO 根目录的 `README.md` 暂时保持当前 `master` 内容不变。
- 原仓库的 `master` 不作修改。

## 迁移方案

采用完整历史镜像方案。以本地当前 `master` 克隆生成 `D:\code\AHUTong-Android`，将克隆仓库的 `origin` 设置为新的 GitHub 仓库，再把 `master` 推送到新远端。由于不重写提交，Android 新仓库的 `master` 将继续指向 `2a30a54e74127ce1b4f75763596b470bd0b9d01b`。

不使用 `git filter-repo` 或 squash。当前仓库本身就是 Android 项目，重写历史没有实际收益，反而会破坏提交哈希和已有追溯关系。

## Android 独立仓库边界

`AHUTong-Android` 保留当前 `master` 的全部受版本控制内容，包括：

- `app/`、Gradle Wrapper、根 Gradle 配置和 Android 工程设置；
- `AGENTS.md` 与 `.agents/skills/` 下的项目开发及生产发版规范；
- `.github/` 下现有 Android 自动化配置；
- `README.md`、`LICENSE`、`pic/` 等项目介绍与资源；
- `sdk` 与 `GuiXu-Rust` 两个现有嵌套 submodule 及其 `.gitmodules` 配置。

本地忽略文件、构建产物、IDE 配置和密钥不会因迁移而新增到版本控制中。新仓库克隆后初始化现有 submodule，确认其 gitlink 与当前 `master` 一致。

## AIO 分支结构

`feat/AHUTong-aio` 在原仓库中保留以下根目录内容：

```text
AHUTong/
├── Android/       # OpenAHU/AHUTong-Android.git submodule
├── docs/          # 仓库改造设计与实施记录
├── pic/           # 当前 README 引用的展示图片
├── .gitignore
├── .gitmodules
├── LICENSE
└── README.md      # 保持当前 master 内容不变
```

以下 Android 专属内容从 AIO 根目录移除，但继续完整存在于 `Android/` submodule 指向的新仓库中：

- `.agents/`、`AGENTS.md`；
- `.github/` 的 Android 工作流；
- `app/`、`gradle/`、Gradle Wrapper 与根 Gradle 配置；
- 原有 `sdk`、`GuiXu-Rust` gitlink；
- 原 Android 仓库使用的 `.gitmodules` 内容。

AIO 根目录重新生成 `.gitmodules`，当前只登记：

```ini
[submodule "Android"]
	path = Android
	url = https://github.com/OpenAHU/AHUTong-Android.git
```

以后 HarmonyOS 与 iOS 仓库按同级目录接入，不在本次工作中创建空目录、占位 submodule 或虚构远端地址。

## 提交与推送

Android 仓库不制造迁移提交，直接把保留历史的 `master` 推送到新远端。AIO 分支的结构调整使用 Conventional Commits 提交信息：

```text
refactor(repo): move Android client into submodule
```

设计文档作为结构调整前的独立提交，使用：

```text
docs: document AIO repository split
```

最终将 `feat/AHUTong-aio` 推送到原仓库 `origin` 并设置 upstream，不合并到 `master`。

## 验证

迁移完成后执行以下验证：

1. 确认两个工作区均无意外未提交改动。
2. 确认 `AHUTong-Android/master` 与迁移前 `AHUTong/master` 的提交哈希相同。
3. 确认 Android 新远端 `refs/heads/master` 指向该提交。
4. 初始化并检查 Android 仓库内的 `sdk`、`GuiXu-Rust` submodule。
5. 确认 AIO 的 `.gitmodules` 只包含 `Android/`，且 gitlink 指向新仓库 `master` 的提交。
6. 确认 AIO 根目录 `README.md` 与迁移前 `master` 完全一致，`pic/` 引用仍可解析。
7. 按项目规范先运行 `adb devices`；有可用设备时构建、安装并启动 Android 应用，无可用设备时至少运行可复现的 Gradle 编译或检查。
8. 确认原仓库 `master` 和 `origin/master` 未发生变化。
9. 确认原远端已存在 `feat/AHUTong-aio`，新远端已存在 `master`。

## 失败处理

- 如果新 Android 远端在执行前出现非空历史，停止推送并报告，不使用 force push 覆盖。
- 如果 submodule 初始化失败，保留已完成的本地克隆并修复或报告具体远端/提交问题，不提交错误 gitlink。
- 如果 Android 基线构建失败，记录失败命令和原始错误；不把已有失败误报为迁移成功。
- 如果任一 push 失败，保留本地分支和提交，不删除或回退原仓库 `master`。
