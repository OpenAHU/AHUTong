# AHUTong AIO 协作规范

本仓库是 AHUTong 的 AIO（All in One）入口仓库，用于管理公共说明、资源以及各平台客户端子仓。当前包含 `Android` 子模块，后续可能加入 HarmonyOS 和 iOS 子模块。

## 子仓按需拉取

1. 克隆或更新 AIO 根仓时，默认只拉取根仓内容，不使用 `--recurse-submodules`，也不要执行无目标的 `git submodule update --init --recursive`。
2. 仅修改根仓的 README、文档、图片或子模块配置时，不需要初始化任何客户端子仓。
3. 只有当用户明确要求开发、构建、检查或修改某个平台客户端时，才初始化对应子仓，并优先使用浅克隆。例如开发 Android 时执行：

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
7. 用户要求开发尚未配置的 HarmonyOS 或 iOS 客户端时，应先说明对应子模块尚不存在，不自行猜测仓库地址或创建子模块。

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
