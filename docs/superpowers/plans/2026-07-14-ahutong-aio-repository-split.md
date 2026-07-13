# AHUTong AIO Repository Split Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Preserve the complete Android history in `OpenAHU/AHUTong-Android`, then replace the Android project at the root of `AHUTong/feat/AHUTong-aio` with an `Android/` submodule.

**Architecture:** Clone the unchanged `AHUTong/master` object graph into the new Android repository so its commit hashes remain stable. After validating that clone, convert the already-created AIO branch into a thin umbrella repository that retains the current README assets and points `Android/` at the new remote.

**Tech Stack:** Git, GitHub HTTPS remotes, Git submodules, PowerShell, Android Gradle Wrapper, ADB

## Global Constraints

- The Android repository local path is exactly `D:\code\AHUTong-Android`.
- The Android repository remote is exactly `https://github.com/OpenAHU/AHUTong-Android.git`.
- The Android `master` branch must remain at `2a30a54e74127ce1b4f75763596b470bd0b9d01b`; do not rewrite or add a migration commit.
- The AIO branch is exactly `feat/AHUTong-aio` and must not modify `AHUTong/master`.
- The AIO Android submodule path is exactly `Android/`.
- The AIO root `README.md`, `LICENSE`, and `pic/` must remain byte-for-byte identical to `master`.
- The Android repository keeps `AGENTS.md`, `.agents/skills/`, `.github/`, `sdk`, and `GuiXu-Rust` from `master`.
- Do not force-push either remote.
- Before every Gradle build, run `adb devices`; install and launch when a usable device exists, otherwise only build and check.
- Do not use `.agents/skills/yukon-auto-update-release`; the request does not contain “发版”.

---

### Task 1: Materialize the Android repository with unchanged history

**Files:**
- Create: `D:\code\AHUTong-Android\` as a complete Git clone of `D:\code\AHUTong` branch `master`
- Preserve: every file and gitlink tracked by `D:\code\AHUTong` at commit `2a30a54e74127ce1b4f75763596b470bd0b9d01b`

**Interfaces:**
- Consumes: local branch `D:\code\AHUTong@master`
- Produces: clean repository `D:\code\AHUTong-Android@master` whose `origin` is the new GitHub repository

- [ ] **Step 1: Reconfirm the immutable source and empty destinations**

Run from `D:\code\AHUTong`:

```powershell
$expected = '2a30a54e74127ce1b4f75763596b470bd0b9d01b'
if ((git rev-parse master) -ne $expected) { throw 'Local master moved' }
if ((git rev-parse origin/master) -ne $expected) { throw 'origin/master moved' }
if (git status --porcelain) { throw 'AHUTong worktree is not clean' }
$remoteRefs = git ls-remote https://github.com/OpenAHU/AHUTong-Android.git
if ($remoteRefs) { throw 'AHUTong-Android remote is no longer empty' }
$targetItems = Get-ChildItem -Force 'D:\code\AHUTong-Android'
if ($targetItems) { throw 'D:\code\AHUTong-Android is not empty' }
```

Expected: the command exits successfully and prints no exception.

- [ ] **Step 2: Clone only the current master checkout into the requested path**

Run:

```powershell
git clone --no-hardlinks --branch master --single-branch 'D:\code\AHUTong' 'D:\code\AHUTong-Android'
```

Expected: Git reports a completed clone and checks out `master`.

- [ ] **Step 3: Point the clone at the new Android remote**

Run from `D:\code\AHUTong-Android`:

```powershell
git remote set-url origin https://github.com/OpenAHU/AHUTong-Android.git
git remote -v
```

Expected: both fetch and push URLs for `origin` are `https://github.com/OpenAHU/AHUTong-Android.git`.

- [ ] **Step 4: Initialize the existing nested submodules**

Run:

```powershell
git submodule update --init --recursive
git submodule status --recursive
```

Expected: top-level gitlinks are `sdk@8c2d6b8113cb0f2ea6bb45cd74fa950e39dc956d` and `GuiXu-Rust@2481ab378395b5ee6db21021524ad051d98b888f`, without a leading `-` or `+`.

- [ ] **Step 5: Verify history and repository cleanliness**

Run:

```powershell
$expected = '2a30a54e74127ce1b4f75763596b470bd0b9d01b'
if ((git rev-parse HEAD) -ne $expected) { throw 'Android HEAD changed' }
if ((git branch --show-current) -ne 'master') { throw 'Android clone is not on master' }
if (git status --porcelain) { throw 'Android clone is not clean' }
git log -1 --oneline --decorate
```

Expected: `HEAD` is `2a30a54`, branch is `master`, and the worktree is clean. Do not create a commit in this repository.

### Task 2: Validate Android and publish its unchanged master

**Files:**
- Read: `D:\code\AHUTong-Android\app\build.gradle.kts`
- Read: `D:\code\AHUTong-Android\app\src\main\AndroidManifest.xml`
- Produce locally when building: ignored Gradle output under `D:\code\AHUTong-Android\app\build\`

**Interfaces:**
- Consumes: clean Android clone from Task 1
- Produces: `OpenAHU/AHUTong-Android.git` branch `master` at the preserved commit

- [ ] **Step 1: Detect whether an Android device is usable**

Run from `D:\code\AHUTong-Android`:

```powershell
$adbOutput = adb devices
$adbOutput
$devices = @($adbOutput | Select-Object -Skip 1 | Where-Object { $_ -match '\sdevice$' })
if ($devices.Count -gt 0) { 'DEVICE_AVAILABLE' } else { 'NO_DEVICE' }
```

Expected: the output ends with either `DEVICE_AVAILABLE` or `NO_DEVICE`.

- [ ] **Step 2: Build, and install plus launch only when a device exists**

Run in the same PowerShell session:

```powershell
if ($devices.Count -gt 0) {
    .\gradlew.bat assembleDebug installDebug
    if ($LASTEXITCODE -ne 0) { throw 'Gradle build/install failed' }
    adb shell am start -n com.ahu.ahutong/.MainActivity
    if ($LASTEXITCODE -ne 0) { throw 'App launch failed' }
} else {
    .\gradlew.bat assembleDebug
    if ($LASTEXITCODE -ne 0) { throw 'Gradle build failed' }
}
```

Expected with a device: Gradle reports `BUILD SUCCESSFUL`, installation succeeds, and ADB reports that `com.ahu.ahutong/.MainActivity` started. Expected without a device: Gradle reports `BUILD SUCCESSFUL` for `assembleDebug`.

- [ ] **Step 3: Reconfirm the destination is empty immediately before push**

Run:

```powershell
$remoteRefs = git ls-remote origin
if ($remoteRefs) { throw 'AHUTong-Android remote became non-empty; stop without pushing' }
```

Expected: no exception and no refs.

- [ ] **Step 4: Push the preserved master without force**

Run:

```powershell
git push -u origin master
```

Expected: Git creates remote branch `master` and sets local `master` to track `origin/master`.

- [ ] **Step 5: Verify the published commit and clean state**

Run:

```powershell
$expected = '2a30a54e74127ce1b4f75763596b470bd0b9d01b'
$remoteMaster = (git ls-remote origin refs/heads/master).Split("`t")[0]
if ($remoteMaster -ne $expected) { throw 'Published Android master has the wrong commit' }
if (git status --porcelain) { throw 'Android repository has tracked changes after validation' }
git status --short --branch
```

Expected: `master` tracks `origin/master`, both point to the expected commit, and there are no tracked changes.

### Task 3: Replace the root Android project with the Android submodule

**Files:**
- Delete from AIO root: `.agents/`, `.github/`, `AGENTS.md`, `app/`, `build.gradle.kts`, `gradle.properties`, `gradle/`, `gradlew`, `gradlew.bat`, `settings.gradle.kts`, `sdk`, `GuiXu-Rust`, old `.gitmodules`
- Create: `D:\code\AHUTong\Android` gitlink and checkout
- Create: `D:\code\AHUTong\.gitmodules` containing only the Android submodule
- Preserve unchanged: `.gitignore`, `README.md`, `LICENSE`, `pic/`, `docs/`

**Interfaces:**
- Consumes: published `OpenAHU/AHUTong-Android/master`
- Produces: an AIO branch whose only client gitlink is `Android/`

- [ ] **Step 1: Confirm the branch, source SHA, and nested-submodule cleanliness**

Run from `D:\code\AHUTong`:

```powershell
if ((git branch --show-current) -ne 'feat/AHUTong-aio') { throw 'Wrong AIO branch' }
if ((git rev-parse master) -ne '2a30a54e74127ce1b4f75763596b470bd0b9d01b') { throw 'master moved' }
if (git status --porcelain) { throw 'AIO worktree is not clean' }
if (git -C sdk status --porcelain) { throw 'sdk contains local changes' }
if (git -C GuiXu-Rust status --porcelain) { throw 'GuiXu-Rust contains local changes' }
```

Expected: no exception.

- [ ] **Step 2: Deinitialize the old nested submodules in the AIO worktree**

Run:

```powershell
git submodule deinit -f -- sdk GuiXu-Rust
```

Expected: Git clears the `sdk` and `GuiXu-Rust` worktrees while leaving the committed gitlinks ready for removal.

- [ ] **Step 3: Remove Android-specific tracked content from the AIO root**

Run:

```powershell
git rm -r -- .agents .github AGENTS.md app build.gradle.kts gradle.properties gradle gradlew gradlew.bat settings.gradle.kts sdk GuiXu-Rust .gitmodules
```

Expected: Git stages deletion of the Android project, its project rules, its CI, the old nested gitlinks, and the old submodule configuration.

- [ ] **Step 4: Add the published Android repository as the only client submodule**

Run:

```powershell
git submodule add https://github.com/OpenAHU/AHUTong-Android.git Android
```

Expected: Git creates and stages `Android` as a mode `160000` gitlink and creates `.gitmodules` with only the `Android` section.

- [ ] **Step 5: Verify preserved repository-introduction files and staged scope**

Run:

```powershell
git diff --exit-code master -- README.md LICENSE pic .gitignore
if ($LASTEXITCODE -ne 0) { throw 'A root presentation file changed' }
$modules = Get-Content -Raw .gitmodules
$expectedModules = "[submodule `"Android`"]`n`tpath = Android`n`turl = https://github.com/OpenAHU/AHUTong-Android.git`n"
if (($modules -replace "`r`n", "`n") -ne $expectedModules) { throw '.gitmodules has unexpected content' }
if ((git -C Android rev-parse HEAD) -ne '2a30a54e74127ce1b4f75763596b470bd0b9d01b') { throw 'Android gitlink checkout is wrong' }
git diff --cached --check
git diff --cached --stat
```

Expected: presentation files have no diff from `master`, `.gitmodules` contains only Android, the gitlink target is correct, and `git diff --cached --check` prints no errors.

- [ ] **Step 6: Commit the AIO structure conversion**

Run:

```powershell
git commit -m "refactor(repo): move Android client into submodule"
```

Expected: one commit records only the root Android removals, `.gitmodules` replacement, and the `Android` gitlink.

### Task 4: Verify and publish the AIO branch

**Files:**
- Verify: `D:\code\AHUTong\.gitmodules`
- Verify: `D:\code\AHUTong\Android`
- Verify: `D:\code\AHUTong\README.md`, `LICENSE`, `pic/`

**Interfaces:**
- Consumes: committed AIO structure from Task 3
- Produces: remote branch `OpenAHU/AHUTong.git/feat/AHUTong-aio`

- [ ] **Step 1: Verify the final tracked root and gitlink**

Run from `D:\code\AHUTong`:

```powershell
$expectedRoot = @('.gitignore', '.gitmodules', 'Android', 'LICENSE', 'README.md', 'docs', 'pic')
$actualRoot = @(git ls-tree --name-only HEAD)
if (Compare-Object $expectedRoot $actualRoot) { throw 'AIO root contains unexpected tracked paths' }
$treeEntry = git ls-tree HEAD Android
if ($treeEntry -notmatch '^160000 commit 2a30a54e74127ce1b4f75763596b470bd0b9d01b\s+Android$') { throw 'Android gitlink is wrong' }
git submodule status
```

Expected: the tracked root contains exactly seven entries, and `Android` is a clean gitlink at `2a30a54`.

- [ ] **Step 2: Verify README, license, and image assets against master**

Run:

```powershell
git diff --exit-code master -- README.md LICENSE pic
if ($LASTEXITCODE -ne 0) { throw 'Repository introduction content changed' }
```

Expected: no diff.

- [ ] **Step 3: Verify master is unchanged before publishing**

Run:

```powershell
$expected = '2a30a54e74127ce1b4f75763596b470bd0b9d01b'
if ((git rev-parse master) -ne $expected) { throw 'Local master changed' }
$originMaster = (git ls-remote origin refs/heads/master).Split("`t")[0]
if ($originMaster -ne $expected) { throw 'origin/master changed' }
if (git status --porcelain) { throw 'AIO worktree is not clean' }
```

Expected: both master refs remain at the original commit and the AIO branch is clean.

- [ ] **Step 4: Push the AIO branch without force**

Run:

```powershell
git push -u origin feat/AHUTong-aio
```

Expected: Git creates remote branch `feat/AHUTong-aio` and configures upstream tracking.

- [ ] **Step 5: Perform final cross-repository verification**

Run:

```powershell
$expectedAndroid = '2a30a54e74127ce1b4f75763596b470bd0b9d01b'
$androidRemote = (git -C 'D:\code\AHUTong-Android' ls-remote origin refs/heads/master).Split("`t")[0]
if ($androidRemote -ne $expectedAndroid) { throw 'Android remote verification failed' }
$aioLocal = git rev-parse feat/AHUTong-aio
$aioRemote = (git ls-remote origin refs/heads/feat/AHUTong-aio).Split("`t")[0]
if ($aioRemote -ne $aioLocal) { throw 'AIO remote verification failed' }
git status --short --branch
git -C 'D:\code\AHUTong-Android' status --short --branch
```

Expected: both remote refs equal their local refs, both worktrees are clean, and neither branch is ahead of its upstream.
