#!/usr/bin/env python3
from __future__ import annotations

import argparse
import configparser
import re
import subprocess
import sys
from pathlib import Path


SKILL_DIR = Path(__file__).resolve().parents[1]
DEFAULT_AIO_ROOT = SKILL_DIR.parents[2]
PLATFORM_PATHS = {
    "android": "Android",
    "harmonyos": "HarmonyOS",
    "harmony": "HarmonyOS",
    "鸿蒙": "HarmonyOS",
    "ios": "iOS",
}


class GateError(RuntimeError):
    pass


def git(repo: Path, args: list[str], check: bool = True) -> str:
    result = subprocess.run(
        ["git", *args],
        cwd=str(repo),
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    if check and result.returncode != 0:
        raise GateError(f"git {' '.join(args)} failed:\n{result.stderr.strip()}")
    return result.stdout.strip()


def normalize_platform(value: str) -> str:
    path = PLATFORM_PATHS.get(value.strip().lower())
    if not path:
        supported = ", ".join(sorted(set(PLATFORM_PATHS.values())))
        raise GateError(f"Unknown platform {value!r}; expected one of: {supported}")
    return path


def validate_release_branch(branch: str) -> None:
    if not re.fullmatch(r"release/[A-Za-z0-9][A-Za-z0-9._-]*", branch):
        raise GateError(
            "Release branch must use the exact release/<version> form; "
            f"received {branch!r}."
        )


def validate_commit(commit: str) -> str:
    normalized = commit.strip().lower()
    if not re.fullmatch(r"[0-9a-f]{40}", normalized):
        raise GateError("Commit must be a full 40-character hexadecimal SHA.")
    return normalized


def submodule_url(aio_root: Path, path: str) -> str:
    modules_path = aio_root / ".gitmodules"
    if not modules_path.is_file():
        raise GateError(f"Missing .gitmodules in AIO root: {aio_root}")

    parser = configparser.ConfigParser()
    parser.read(modules_path, encoding="utf-8")
    for section in parser.sections():
        if section.startswith("submodule ") and parser.get(section, "path", fallback="") == path:
            url = parser.get(section, "url", fallback="").strip()
            if not url:
                raise GateError(f"Submodule {path} has no URL in .gitmodules.")
            return url
    raise GateError(f"Platform path {path} is not configured as an AIO submodule.")


def remote_branch_tip(aio_root: Path, url: str, branch: str) -> str:
    ref = f"refs/heads/{branch}"
    output = git(aio_root, ["ls-remote", "--heads", url, ref])
    matches = []
    for line in output.splitlines():
        parts = line.split()
        if len(parts) == 2 and parts[1] == ref:
            matches.append(parts[0].lower())
    if len(matches) != 1:
        raise GateError(f"Expected exactly one remote ref {ref}, found {len(matches)}.")
    return matches[0]


def parse_gitlink(entry: str, source: str) -> str:
    match = re.fullmatch(r"160000 (?:commit )?([0-9a-f]{40})(?: 0)?\t.+", entry.strip())
    if not match:
        raise GateError(f"{source} is not a single mode-160000 gitlink entry: {entry!r}")
    return match.group(1)


def head_gitlink(aio_root: Path, path: str) -> str:
    return parse_gitlink(git(aio_root, ["ls-tree", "HEAD", "--", path]), f"HEAD:{path}")


def index_gitlink(aio_root: Path, path: str) -> str:
    return parse_gitlink(git(aio_root, ["ls-files", "-s", "--", path]), f"index:{path}")


def require_main_worktree_ready(aio_root: Path) -> None:
    top = Path(git(aio_root, ["rev-parse", "--show-toplevel"])).resolve()
    if top != aio_root:
        raise GateError(f"--aio-root must be the AIO Git root; resolved {top}.")
    branch = git(aio_root, ["branch", "--show-current"])
    if branch != "master":
        raise GateError(f"AIO gitlink updates must run on master, not {branch or 'detached HEAD'}.")
    status = git(aio_root, ["status", "--porcelain", "--untracked-files=no"])
    if status:
        raise GateError(
            "AIO main worktree has tracked changes. Commit or move them before applying a release gitlink:\n"
            f"{status}"
        )


def checkout_initialized_submodule(
    aio_root: Path,
    path: str,
    branch: str,
    commit: str,
) -> None:
    submodule_root = aio_root / path
    if not (submodule_root / ".git").exists():
        print(f"Submodule worktree is not initialized; staging gitlink without checkout: {path}")
        return

    status = git(submodule_root, ["status", "--porcelain"])
    if status:
        raise GateError(f"Submodule worktree {path} is not clean:\n{status}")
    git(submodule_root, ["fetch", "origin", f"refs/heads/{branch}"])
    fetched = git(submodule_root, ["rev-parse", "FETCH_HEAD"]).lower()
    if fetched != commit:
        raise GateError(
            f"Fetched {branch} at {fetched}, not verified release commit {commit}."
        )
    git(submodule_root, ["checkout", "--detach", commit])


def apply_gitlink(aio_root: Path, path: str, branch: str, commit: str) -> None:
    require_main_worktree_ready(aio_root)
    current = head_gitlink(aio_root, path)
    if current == commit:
        print(f"AIO gitlink already points to {path}@{commit}.")
        return

    checkout_initialized_submodule(aio_root, path, branch, commit)
    git(aio_root, ["update-index", "--add", "--cacheinfo", "160000", commit, path])
    staged = index_gitlink(aio_root, path)
    if staged != commit:
        raise GateError(f"Staged gitlink {staged} does not match verified release commit {commit}.")

    changed_paths = [
        line.strip()
        for line in git(aio_root, ["diff", "--cached", "--name-only"]).splitlines()
        if line.strip()
    ]
    if changed_paths != [path]:
        raise GateError(
            "Staged scope is not limited to the selected platform gitlink: "
            + ", ".join(changed_paths)
        )
    unstaged_paths = [
        line.strip()
        for line in git(aio_root, ["diff", "--name-only"]).splitlines()
        if line.strip()
    ]
    if unstaged_paths:
        raise GateError(
            "Applying the gitlink left unstaged tracked changes: "
            + ", ".join(unstaged_paths)
        )
    print(f"Staged verified gitlink: {path} {current} -> {commit}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Verify and optionally stage an AIO gitlink at an exact remote release-branch tip."
    )
    parser.add_argument("--aio-root", type=Path, default=DEFAULT_AIO_ROOT)
    parser.add_argument("--platform", required=True)
    parser.add_argument("--release-branch", required=True)
    parser.add_argument("--commit", required=True)
    parser.add_argument(
        "--apply",
        action="store_true",
        help="Stage the verified gitlink in a clean AIO master worktree.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    aio_root = args.aio_root.resolve()
    platform_path = normalize_platform(args.platform)
    validate_release_branch(args.release_branch)
    target_commit = validate_commit(args.commit)
    url = submodule_url(aio_root, platform_path)
    remote_tip = remote_branch_tip(aio_root, url, args.release_branch)
    if remote_tip != target_commit:
        raise GateError(
            f"{args.release_branch} points to {remote_tip}, not requested {target_commit}; "
            "refusing to update the AIO gitlink."
        )

    current = head_gitlink(aio_root, platform_path)
    print(f"Verified remote release: {url} {args.release_branch} -> {remote_tip}")
    print(f"Current AIO gitlink: {platform_path} -> {current}")
    if args.apply:
        apply_gitlink(
            aio_root,
            platform_path,
            args.release_branch,
            target_commit,
        )
    else:
        print("Verification only; no gitlink was changed.")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except GateError as exc:
        print(f"[ERROR] {exc}", file=sys.stderr)
        raise SystemExit(1)
