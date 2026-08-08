---
name: yukon-auto-update-release
description: AHUTong AIO multi-platform production release orchestration. Use only when the user explicitly includes the Chinese word 发版 and asks to publish a client; determine Android, HarmonyOS, or iOS, enter the matching AIO worktree and subrepository, invoke that platform's release workflow, then update the AIO gitlink only to the exact pushed release-branch tip. Do not use for ordinary build, test, packaging, upload-only, version checks, or requests that do not contain 发版.
---

# AHUTong AIO Release

Coordinate production releases from the AIO root. Keep platform-specific build, signing, upload, and verification behavior inside each client repository.

## Trigger Gate

Use this skill only when the user explicitly includes `发版` and asks for an actual release. Do not use it for release builds, packaging, uploads, version checks, or release discussions that are not authorization to publish.

## Determine the Platform

1. Prefer an explicit platform in the request.
2. Otherwise use a worktree only when the current session created or explicitly selected it for this task and exactly one target platform is clear.
3. Never infer the platform merely because a submodule is initialized or an unrelated historical worktree exists.
4. If the platform is not uniquely determined, ask whether to release Android, HarmonyOS, or iOS before any production action.

## Select the Worktree

1. Reuse a worktree only when the current session already selected it for the same release and its state is safe.
2. Otherwise create `.worktree/<platform>-release-<version-or-purpose>` from AIO `master` with detached HEAD.
3. Initialize only the selected platform submodule.
4. Enter the platform repository, read its `AGENTS.md` and release skill completely, then check out the named source branch required by that repository. Never release from a detached submodule HEAD.
5. A depth-1 submodule is not automatically sufficient for release branches, changelog diffs, or merge-base checks. Fetch the exact required refs and deepen only as far as needed before publishing.

## Platform Routing

- Android: run `Android/.agents/skills/yukon-auto-update-release/SKILL.md` from the Android subrepository root.
- HarmonyOS: if no release skill exists in the HarmonyOS repository, stop and report that its release workflow is not configured.
- iOS: if no release skill exists in the iOS repository, stop and report that its release workflow is not configured.

Never translate Android signing or upload commands to another platform by analogy.

## AIO Configuration

Store Android release credentials only at the main AIO worktree:

```text
.agents/skills/yukon-auto-update-release/config.local.json
```

Resolve the main AIO worktree from `git rev-parse --path-format=absolute --git-common-dir`. Do not copy the config into a client repository or linked worktree. Do not read or print secret values in the conversation. If setup is required, ask the user to run the Android helper's `--setup-config` command in a normal interactive terminal.

The Android helper discovers this path automatically. Pass `--config <absolute AIO config path>` when discovery is unavailable or when verifying an unusual layout.

SSH host keys must already be trusted through the operating-system `known_hosts` file or the optional `server.known_hosts_path` entry. Verify a new server fingerprint through a trusted channel before adding it; the Android helper never accepts an unknown host key automatically.

## Release and Capture the Immutable Target

1. Run the selected platform's tests and production release workflow.
2. Require the platform workflow to return the exact release branch and full commit SHA.
3. Confirm the release branch was pushed and the remote branch tip equals that SHA.
4. Complete the platform's post-publish verification before changing the AIO gitlink.

For Android the branch must be `release/<versionName>`. Do not substitute `master`, a tag, the current checkout, or an unpushed local ref. When the user explicitly requests the version already published by the server, accept the Android workflow's same-version hotfix result: it must keep `versionName`, increment `versionCode`, apply the complete linear work-branch commit range to the existing release branch, and atomically push that release branch with Android `master` before publishing.

## Update the AIO Gitlink

Run the verifier without `--apply` first:

```powershell
python .agents\skills\yukon-auto-update-release\scripts\update_release_gitlink.py `
  --platform Android `
  --release-branch release/<version> `
  --commit <full-40-character-sha>
```

After it confirms the remote branch tip, rerun with `--apply`. The script must reject a dirty AIO main worktree, a non-`master` AIO branch, an unknown platform, a non-release branch, or a commit that differs from the remote branch tip.

Then:

1. Verify the staged diff contains exactly one mode-`160000` gitlink change for the selected platform.
2. Rerun the verifier without `--apply` immediately before committing.
3. Commit the gitlink separately with a Conventional Commit such as `chore(android): point submodule to release 3.2.1`.
4. Push AIO `master` without force and verify the remote AIO commit.

Do not use `git submodule update --remote` and do not hand-edit or directly stage an unverified gitlink.

The repository CI independently validates every changed gitlink against the trusted submodule URL and the exact tip of a remote `release/*` branch. Do not merge or push through a failed gitlink gate. Repository branch protection should require this check on `master`.

## Completion

Report the platform, released version, release branch, immutable release commit, publish verification result, AIO gitlink commit, and whether the dedicated worktree remains. Clean a worktree only after the platform commit and AIO gitlink commit are pushed and all involved repositories are clean.
