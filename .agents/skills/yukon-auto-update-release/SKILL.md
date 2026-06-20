---
name: yukon-auto-update-release
description: AHUTong Android release publishing workflow. Use only when the user's request explicitly contains the Chinese word 发版 and asks to publish/release an APK; do not use for ordinary build, test, release-package-only, upload-only, or version-check requests that do not contain 发版.
---

# Yukon Auto Update Release

This skill publishes an AHUTong Android release APK to the update server. It is intentionally narrow and production-affecting.

## Trigger Gate

Use this skill only when the user explicitly includes `发版` in the request.

Do not use this skill for requests such as "打 release 包", "构建 release", "上传 APK", "更新版本号", "检查版本", or "release" unless the same request also contains `发版`.

## Required Workflow

1. Read this `SKILL.md` completely.
2. Confirm the repo is on a project-compliant branch, normally `p/Yukon163/feat`, and do not continue from a dirty worktree unless the dirty files are understood.
3. Run `adb devices`.
4. Run `.\gradlew.bat :app:testDebugUnitTest`.
5. Run the helper script from repo root. If the user says only `发版`, do not pass a version. If the user says a concrete target such as `发版3.1.4`, pass it as `--version-name 3.1.4`:

   ```powershell
   python .agents\skills\yukon-auto-update-release\scripts\release_publish.py
   ```

6. If `config.local.json` is missing, do not merely tell the user to create it. Ask for the exact fields listed in "First-Use Credential Collection" below, preferably with `request_user_input` when that tool is available; otherwise ask a concise direct question with the same field list.
7. If the script reports a local/server version mismatch, stop and ask the user to choose one of the two options printed by the script. Do not modify versions, build, or upload until the user confirms.
8. Before building, publish from `release/{target versionName}`. For normal forward releases, create the target release branch if it is missing. For rollback releases where the target `versionName` is lower than the server `versionName`, the target release branch must already exist locally or on `origin`; never create a rollback target branch from the current `HEAD`.
9. Do not ask the user for release notes by default. Normal releases derive `apk_changelog.txt` from the diff between `release/{previous versionName}` and `release/{target versionName}`. Rollback releases always use generic changelog text unless the user explicitly supplies safe generic notes.
10. After the script succeeds, verify:

   ```powershell
   curl https://openahu.org/api/check_apk_update
   ```

11. Commit the local `app/build.gradle.kts` version change if it was modified and the user expects the release version to be recorded in git, then push the `release/{target versionName}` branch.

## First-Use Credential Collection

If `.agents/skills/yukon-auto-update-release/config.local.json` is absent, collect these fields explicitly before continuing:

- `keystore.path`: absolute path to the signing `.jks` file.
- `keystore.store_password`: keystore store password.
- `keystore.key_alias`: signing key alias.
- `keystore.key_password`: signing key password.
- `server.host`: SSH server host.
- `server.port`: SSH port, normally `22`.
- `server.username`: SSH username.
- `server.auth_method`: exactly `password` or `private_key`.
- If `server.auth_method=password`: `server.password`.
- If `server.auth_method=private_key`: `server.private_key_path` and optional `server.private_key_passphrase`.

Make the security tradeoff clear in one sentence: these values will be stored in ignored local file `config.local.json` on this machine and must not be committed. In Codex shell sessions, the helper script intentionally exits with this field list instead of prompting, so the agent must ask the user for the missing values. In a normal interactive terminal, the same script may prompt field by field and create the file locally.

Use this prompt shape instead of a vague "create the config file" message:

```text
缺少本机发版配置。我需要以下信息才能创建被 git 忽略的 config.local.json：
1. keystore.path
2. keystore.store_password
3. keystore.key_alias
4. keystore.key_password
5. server.host
6. server.port
7. server.username
8. server.auth_method（password 或 private_key）
9. 如果用 password：server.password
10. 如果用 private_key：server.private_key_path，以及可选 server.private_key_passphrase

这些值只会写入本机 .agents/skills/yukon-auto-update-release/config.local.json，不会提交到 git。
```

To print the same required field list from the helper script, run:

```powershell
python .agents\skills\yukon-auto-update-release\scripts\release_publish.py --print-config-fields
```

## Helper Script Behavior

The helper script is `scripts/release_publish.py`.

It performs these actions:

- Creates `config.local.json` on first use by interactively asking for keystore and server login details.
- Stores `config.local.json` in this skill directory. It is ignored by git and must never be committed.
- When run from Codex shell and `config.local.json` is missing, exits with the required field list so the agent can ask the user explicitly.
- Reads local `app/build.gradle.kts` for `versionCode` and `versionName`.
- Reads server `/home/ubuntu/AHUTong/server/update_server/static/apk_version.txt` and, if present, `apk_version_name.txt`.
- Defaults to `versionCode = max(localVersionCode, serverVersionCode) + 1`.
- Defaults to patch-incrementing `versionName`, for example `3.1.5 -> 3.1.6`.
- Accepts explicit `--version-code` and `--version-name` when the user explicitly gives a target version.
- Treats explicit `--version-name` lower than the server `versionName` as rollback publish: build from `release/{versionName}`, keep that display `versionName`, but publish with `versionCode = serverVersionCode + 1` unless a higher explicit `--version-code` is supplied.
- Ensures `release/{previous versionName}` and `release/{target versionName}` exist, creating missing branches as needed.
- Switches to `release/{target versionName}` before updating versions, building, signing, or uploading.
- Uses `--previous-release-ref` only when the previous release branch is missing; the default is `HEAD~1`, but the agent should pass the actual previous-release baseline if that default is wrong.
- Derives changelog lines from `git log` and `git diff --name-only` over `release/{previous versionName}..release/{target versionName}`.
- If the diff is only hidden/internal work such as gray rollout, debug-only controls, or this release skill itself, writes generic notes such as `修复了一些 bug` and `进行了一些体验优化` instead of exposing the hidden feature.
- For rollback publish, writes generic notes and never mentions rollback, downgrade, revert, or the older target version in user-facing changelog text.
- Builds release with `.\gradlew.bat :app:assembleRelease`.
- Locates Android SDK from `local.properties`, then uses the highest installed build-tools `zipalign` and `apksigner`.
- Uploads the signed APK to `/home/ubuntu/AHUTong/server/update_server/static/ahutong.apk`.
- Backs up the old APK as `ahutong.apk_{version}.bak`, appending a timestamp if that backup already exists.
- Updates `apk_version.txt`, creates or updates `apk_version_name.txt`, and writes the generated or explicitly supplied 1-2 changelog lines to `apk_changelog.txt`.

## Release Branch and Changelog Rule

Always publish from `release/{target versionName}`. If the branch exists locally, check it out. If it exists only on `origin`, create a local tracking branch.

For normal forward releases, create a missing target release branch before publishing. During the first migration to release branches, also create `release/{previous versionName}` if missing so future changelog diffs have a stable baseline.

For rollback releases, where the user explicitly requested a `versionName` lower than the server `versionName`, the target branch must already exist locally or on `origin`. Build the old code from that branch, but keep the published `versionCode` greater than the current server `versionCode`; Android will not install an APK whose `versionCode` is less than or equal to the installed app. Keep the requested `versionName` as display text.

Generate release notes from the two release branches, not from memory or from a free-form user prompt:

```powershell
git log --no-merges --format=%s release/{previous}..release/{target}
git diff --name-only release/{previous}..release/{target}
```

Do not mention hidden gray-release functionality in user-facing changelog text. If the diff includes a feature hidden behind gray rollout, debug-only enablement, internal release tooling, or other non-user-visible work, use generic lines instead, for example:

```text
修复了一些 bug
进行了一些体验优化
```

For rollback releases, use generic changelog text and do not write terms such as `回退`, `回滚`, `降级`, `rollback`, `downgrade`, or the older target version as the reason for the update.

## Version Mismatch Rule

If local and server versions differ during a normal forward release, the script must stop before any mutation and print two choices:

1. Upgrade both local and server to the next patch release, for example local `3.1.4` and server `3.1.5` become `3.1.6`.
2. Abort so the user can rebase/sync to the latest version and rerun the release.

Ask the user which option to use. Do not continue automatically.

This mismatch rule does not apply when the user explicitly requests an older target `versionName` such as `发版3.1.4` while the server is already `3.1.5`; that is rollback publish. In rollback publish, require an existing `release/3.1.4` branch and use `serverVersionCode + 1` as the default `versionCode`.

## Dry Run

Use dry-run to validate config, version parsing, mismatch handling, and tool discovery without modifying files or uploading:

```powershell
python .agents\skills\yukon-auto-update-release\scripts\release_publish.py --dry-run
```

Dry-run may prompt for missing config only in a normal interactive terminal and use it in memory, but it must not create `config.local.json`. In Codex shell, dry-run exits with the required config field list.
Dry-run prints the release branches it would ensure and checkout, but it must not create branches, switch branches, build, sign, upload, or modify server files.
