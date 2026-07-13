# AHUTong AIO 协作规范

本仓库是 AHUTong 的 AIO（All in One）入口仓库，用于管理公共说明、资源以及各平台客户端子仓。当前包含 `Android`、`HarmonyOS` 和 `IOS` 子模块。

## 子仓按需拉取

1. 克隆或更新 AIO 根仓时，默认只拉取根仓内容，不使用 `--recurse-submodules`，也不要执行无目标的 `git submodule update --init --recursive`。
2. 仅修改根仓的 README、文档、图片或子模块配置时，不需要初始化任何客户端子仓。
3. 只有当用户明确要求开发、构建、检查或修改某个平台客户端时，才按下方 Worktree 规范创建独立工作区，并在其中初始化对应子仓。子仓优先使用浅克隆，例如开发 Android 时执行：

   ```bash
   git submodule sync -- Android
   git submodule update --init --depth 1 Android
   ```

4. 子仓内部仍有嵌套子模块时，只初始化当前任务实际需要的依赖，不默认递归拉取。例如 Android 构建需要 Rust 代码时执行：

   ```bash
   git -C Android submodule update --init --depth 1 sdk GuiXu-Rust
   ```

5. 浅克隆的历史不足以完成任务时，先按需使用 `git fetch --deepen <数量>` 补充最少历史；只有确实需要完整历史时才取消浅克隆。
6. 子仓已经初始化时，不要重新克隆、强制重置或覆盖其中的未提交改动。
7. 用户要求开发尚未配置的平台客户端时，应先说明对应子模块尚不存在，不自行猜测仓库地址或创建子模块。

## Worktree 使用规范

1. AIO 根仓的主工作区只用于仓库编排、公共文档和子模块指针维护，不直接在其中进行客户端开发。
2. 每个客户端开发任务都必须从 AIO 根仓创建独立 worktree。AIO worktree 使用 detached HEAD，不为根仓额外创建功能分支；worktree 目录应位于根仓之外，并使用“平台-任务”命名。
3. 创建 worktree 前先执行 `git worktree list` 检查现有工作区。以下命令以 Android 登录任务为例：

   ```bash
   git worktree add --detach ../AHUTong-worktrees/android-login master
   git -C ../AHUTong-worktrees/android-login submodule sync -- Android
   git -C ../AHUTong-worktrees/android-login submodule update --init --depth 1 Android
   ```

4. 一个 worktree 只服务一个明确任务，并且只初始化该任务需要的客户端子仓；不要在同一 worktree 中混合 Android、HarmonyOS 和 iOS 开发。
5. 进入目标子仓后，先阅读该子仓的 `AGENTS.md` 和项目本地 Skills，再切换基线、创建子仓开发分支并开始修改。分支和提交规则属于目标子仓，不属于 detached 的 AIO worktree。
6. 如果客户端开发完成后需要更新 AIO 的 gitlink，应先把客户端 commit 推送到子仓远端，再回到 AIO 根仓主工作区更新子模块指针。
7. 清理 worktree 前必须确认目标子仓改动已经提交并推送，且根仓与子仓工作区均干净。然后从 AIO 根仓主工作区执行：

   ```bash
   git -C ../AHUTong-worktrees/android-login submodule update --checkout Android
   git -C ../AHUTong-worktrees/android-login submodule deinit -- Android
   git worktree remove ../AHUTong-worktrees/android-login
   git worktree prune
   ```

8. 如果 `git worktree remove` 因未提交改动或子模块状态拒绝执行，应先检查并处理状态，不使用 `--force`，也不要手动删除 worktree 目录。

## 开发边界

1. 开始修改前，分别检查根仓和目标子仓的状态；进入子仓后先阅读并遵守子仓自己的 `AGENTS.md` 和项目本地 Skills。
2. AIO 根仓默认直接在 `master` 进行仓库编排和文档维护，用户指定其他目标分支时按用户要求执行；客户端代码的分支规范只在对应子仓内生效。
3. 子模块初始化后可能处于 detached HEAD。编辑前必须切换到目标基线分支并创建符合子仓规范的工作分支。
4. 客户端代码应提交并推送到客户端子仓。AIO 根仓只记录子仓的 commit 指针，不把子仓源码作为普通文件提交。
5. 需要更新 AIO 中的子仓指针时，应先确认对应 commit 已推送到子仓远端，再在 AIO 根仓单独提交 gitlink 变更。
6. 不要仅为了“保持最新”而执行 `git submodule update --remote` 或修改子仓指针；只有任务明确需要时才更新。

## 提交与验证

1. 提交信息使用 Conventional Commits，并保持每个分支只处理一个明确目标。
2. 根仓文档改动只需检查 Markdown、链接和 `git diff --check`；无需为此初始化或构建客户端子仓。
3. 客户端改动在对应子仓内完成构建和测试，并遵循该子仓的验证要求。
4. 提交前确认根仓和涉及的子仓都没有混入无关文件、构建产物、本地配置或敏感信息。
