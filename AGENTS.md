# AHUTong AIO 协作规范

本仓库是 AHUTong 的 AIO（All in One）入口仓库，用于管理公共说明、资源以及各平台客户端子仓。当前包含 `Android`、`HarmonyOS` 和 `iOS` 子模块。

## 任务平台判定

1. 收到客户端开发、构建、检查、修改或发版任务时，先确定目标平台，再初始化或进入子仓。
2. 优先根据用户明确提到的平台、文件路径、技术栈或当前 session 已明确创建/选中的 worktree 判断目标子仓。平台映射为：Android -> `Android`、鸿蒙/HarmonyOS -> `HarmonyOS`、iOS/iPhone/iPad/Swift -> `iOS`。
3. 不得仅因主工作区已经初始化某个子模块，或 `git worktree list` 中存在某个平台的历史 worktree，就推断当前任务属于该平台。
4. 只有当前 session 已明确创建或选中一个与当前任务一致的 worktree，且其中目标子仓唯一明确时，才可以据此判断平台并复用。
5. 如果目标平台无法唯一确定，必须先询问用户；跨端任务应让用户确认涉及的平台集合，不自行缩小或扩大范围。

## 子仓按需拉取

1. 克隆或更新 AIO 根仓时，默认只拉取根仓内容，不使用 `--recurse-submodules`，也不要执行无目标的 `git submodule update --init --recursive`。
2. 仅修改根仓的 README、文档、图片或子模块配置时，不需要初始化任何客户端子仓。
3. 只有当用户明确要求开发、构建、检查或修改客户端时，才按下方 Worktree 规范创建独立工作区，并在其中初始化任务涉及的一个或多个子仓。子仓优先使用浅克隆，例如开发 Android 时执行：

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
2. 每个客户端开发任务都必须从 AIO 根仓创建独立 worktree。AIO worktree 使用 detached HEAD，不为根仓额外创建功能分支；所有 worktree 必须统一放在 AIO 根仓内的 `.worktree/<任务名称>` 目录，并使用能表达平台范围和任务目标的名称。`.worktree/` 必须由根仓 `.gitignore` 忽略，不得提交其中内容。
3. 创建 worktree 前先执行 `git worktree list` 检查现有工作区。只复用当前 session 已创建或明确选中、服务同一任务且状态安全的 worktree；不得占用名称相似但属于其他任务或其他 session 的 worktree。以下命令以 Android 登录任务为例：

   ```bash
   git worktree add --detach .worktree/android-login master
   git -C .worktree/android-login submodule sync -- Android
   git -C .worktree/android-login submodule update --init --depth 1 Android
   ```

4. 一个 worktree 只服务一个明确任务，但可以初始化该任务需要的多个客户端子仓。单端任务默认只拉取一个子仓；迁移、跨端对照或联调任务可以在同一 worktree 中浅拉所有相关子仓，但仍不得拉取无关子仓。例如把 Android 功能迁移到 iOS 时可以执行：

   ```bash
   git worktree add --detach .worktree/android-to-ios-schedule master
   git -C .worktree/android-to-ios-schedule submodule sync -- Android iOS
   git -C .worktree/android-to-ios-schedule submodule update --init --depth 1 Android iOS
   ```

5. 进入每个目标子仓后，先阅读该子仓的 `AGENTS.md` 和项目本地 Skills，再切换基线、创建子仓开发分支并开始修改。分支和提交规则属于目标子仓，不属于 detached 的 AIO worktree。
6. 如果客户端开发完成后需要更新 AIO 的 gitlink，应先把涉及的客户端 commit 分别推送到对应子仓远端，再回到 AIO 根仓主工作区更新子模块指针。
7. 清理 worktree 前必须确认所有目标子仓的改动已经提交并推送，且根仓与各子仓工作区均干净。然后从 AIO 根仓主工作区执行；多仓任务需要在子模块命令中列出全部已初始化的子仓：

   ```bash
   git -C .worktree/android-login submodule update --checkout Android
   git -C .worktree/android-login submodule deinit -- Android
   git worktree remove .worktree/android-login
   git worktree prune
   ```

8. 如果 `git worktree remove` 因未提交改动或子模块状态拒绝执行，应先检查并处理状态，不使用 `--force`，也不要手动删除 worktree 目录。

## 发版编排

1. 只有用户请求中显式包含“发版”并要求实际发布客户端时，才进入生产发版流程。
2. 发版前必须确定目标系统。用户未明确 Android、HarmonyOS 或 iOS，且当前 session 的 worktree 也不能唯一确定平台时，先询问用户发版哪个系统。
3. 平台确定后，只进入当前 session 对应的 worktree 和目标子仓。没有可复用 worktree 时，按本文件规范创建专用 worktree，并只初始化目标子仓。
4. 进入目标子仓后，读取该子仓的 `AGENTS.md` 和发版 skill，再执行平台自己的构建、签名、上传和验证流程。不得把 Android 发版命令套用到其他平台。
5. 发版需要 release 分支、变更日志或 merge-base 历史时，不得假设 `--depth 1` 足够。先获取明确需要的分支和最少历史；仍无法验证时按需 deepen，只有确实需要时才取消浅克隆。
6. 当前只有 Android 配置了生产发版 skill。用户选择 iOS 或 HarmonyOS 时，如果对应子仓仍没有发版 skill，应停止并说明该平台发版流程尚未配置，不猜测证书、商店或上传命令。
7. Android 本机发版配置统一存放在 AIO 主工作区的 `.agents/skills/yukon-auto-update-release/config.local.json`。该文件必须被忽略，不复制到子仓或各个 worktree，也不得提交。
8. 平台发版成功后必须更新 AIO gitlink；该更新只能指向本次已确定并已推送的远端 `release/<版本>` 分支的精确提交，遵守下方 gitlink 门禁。

## Gitlink 发版门禁

1. 不允许为了“保持最新”或因为子仓当前 `HEAD`、`master`、tag 发生变化而自行更新 gitlink。
2. 发版后的 gitlink 目标必须来自本次平台发版流程返回的明确 release 分支和 commit。先确认远端 `refs/heads/release/<版本>` 存在，并要求它的分支尖端与目标 commit 完全相同。
3. 禁止使用未推送 commit、任意本地分支、普通开发分支、`master`、模糊 ref 或 `git submodule update --remote` 作为发版 gitlink 来源。
4. 更新前确认 AIO 主工作区除被忽略的本地配置外没有无关 tracked 改动；更新后暂存范围只能包含目标平台的一个 gitlink，并使用独立 Conventional Commit 提交。
5. release 分支不存在、远端尖端与目标 commit 不一致、目标子仓发版未完成或 AIO 主工作区不干净时，必须停止，不选择“最接近”的 commit。
6. 使用 AIO 发版 skill 提供的校验脚本验证并暂存 gitlink；不得绕过脚本直接改指针。提交前再次检查 staged gitlink 与远端 release 分支尖端一致。
7. 所有 gitlink 变更都必须通过 `.github/workflows/verify-release-gitlinks.yml`；CI 会拒绝不在受信任子仓列表中的 gitlink、被修改的子仓 URL，以及不等于远端 `release/*` 分支尖端的目标 commit。

## 开发边界

1. 开始修改前，分别检查根仓和目标子仓的状态；进入子仓后先阅读并遵守子仓自己的 `AGENTS.md` 和项目本地 Skills。
2. AIO 根仓默认直接在 `master` 进行仓库编排和文档维护，用户指定其他目标分支时按用户要求执行；客户端代码的分支规范只在对应子仓内生效。
3. 子模块初始化后可能处于 detached HEAD。编辑前必须切换到目标基线分支并创建符合子仓规范的工作分支。
4. 客户端代码应提交并推送到客户端子仓。AIO 根仓只记录子仓的 commit 指针，不把子仓源码作为普通文件提交。
5. 需要更新 AIO 中的子仓指针时，应先获得用户明确授权并确认对应 commit 已推送到子仓远端，再在 AIO 根仓单独提交 gitlink 变更。发版产生的 gitlink 更新还必须满足“Gitlink 发版门禁”。
6. 不要仅为了“保持最新”而执行 `git submodule update --remote` 或修改子仓指针；只有任务明确需要时才更新。

## 提交与验证

1. 提交信息使用 Conventional Commits，并保持每个分支只处理一个明确目标。
2. 根仓文档改动只需检查 Markdown、链接和 `git diff --check`；无需为此初始化或构建客户端子仓。
3. 客户端改动在对应子仓内完成构建和测试，并遵循该子仓的验证要求。
4. 提交前确认根仓和涉及的子仓都没有混入无关文件、构建产物、本地配置或敏感信息。
