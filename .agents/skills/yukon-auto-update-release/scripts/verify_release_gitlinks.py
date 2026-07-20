#!/usr/bin/env python3
from __future__ import annotations

import argparse
import configparser
import subprocess
import sys
import time
from pathlib import Path


SKILL_DIR = Path(__file__).resolve().parents[1]
DEFAULT_AIO_ROOT = SKILL_DIR.parents[2]
TRUSTED_SUBMODULES = {
    "Android": "https://github.com/OpenAHU/AHUTong-Android.git",
    "HarmonyOS": "https://github.com/OpenAHU/AHUTong-HarmonyOS.git",
    "iOS": "https://github.com/OpenAHU/AHUTong-iOS.git",
}


class VerificationError(RuntimeError):
    pass


def git(repo: Path, args: list[str]) -> str:
    result = subprocess.run(
        ["git", *args],
        cwd=str(repo),
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    if result.returncode != 0:
        raise VerificationError(f"git {' '.join(args)} failed:\n{result.stderr.strip()}")
    return result.stdout.strip()


def normalize_commit(repo: Path, revision: str) -> str:
    return git(repo, ["rev-parse", "--verify", f"{revision}^{{commit}}"])


def submodules_at(repo: Path, revision: str) -> dict[str, str]:
    text = git(repo, ["show", f"{revision}:.gitmodules"])
    parser = configparser.ConfigParser()
    parser.read_string(text)
    result: dict[str, str] = {}
    for section in parser.sections():
        if not section.startswith("submodule "):
            continue
        path = parser.get(section, "path", fallback="").strip()
        url = parser.get(section, "url", fallback="").strip()
        if not path or not url:
            raise VerificationError(f"Incomplete submodule entry in {revision}:.gitmodules: {section}")
        if path in result:
            raise VerificationError(f"Duplicate submodule path in .gitmodules: {path}")
        result[path] = url
    return result


def verify_trusted_submodules(configured: dict[str, str]) -> None:
    if configured != TRUSTED_SUBMODULES:
        raise VerificationError(
            "Configured submodules differ from the trusted AIO platform map.\n"
            f"Expected: {TRUSTED_SUBMODULES}\n"
            f"Actual:   {configured}"
        )


def changed_gitlinks(repo: Path, base: str, head: str) -> dict[str, str]:
    output = git(
        repo,
        ["diff", "--raw", "--full-index", "--abbrev=40", "--no-renames", base, head],
    )
    changes: dict[str, str] = {}
    for line in output.splitlines():
        if not line.startswith(":") or "\t" not in line:
            continue
        metadata, path = line.split("\t", 1)
        fields = metadata[1:].split()
        if len(fields) != 5:
            raise VerificationError(f"Unexpected git diff --raw entry: {line}")
        old_mode, new_mode, _old_sha, new_sha, _status = fields
        if old_mode != "160000" and new_mode != "160000":
            continue
        if path not in TRUSTED_SUBMODULES:
            raise VerificationError(f"Untrusted gitlink path changed: {path}")
        if new_mode != "160000":
            raise VerificationError(f"Removing or replacing a trusted gitlink is not allowed: {path}")
        changes[path] = new_sha.lower()
    return changes


def remote_release_tips(repo: Path, url: str) -> dict[str, str]:
    output = ""
    last_error: VerificationError | None = None
    for attempt in range(1, 4):
        try:
            output = git(repo, ["ls-remote", "--heads", url, "refs/heads/release/*"])
            last_error = None
            break
        except VerificationError as exc:
            last_error = exc
            if attempt == 3:
                raise
            print(
                f"Remote release-ref query failed for {url}; retrying ({attempt}/3)...",
                file=sys.stderr,
            )
            time.sleep(attempt)
    if last_error is not None:
        raise last_error
    tips: dict[str, str] = {}
    for line in output.splitlines():
        parts = line.split()
        if len(parts) == 2 and parts[1].startswith("refs/heads/release/"):
            tips[parts[1]] = parts[0].lower()
    return tips


def verify_range(repo: Path, base: str, head: str) -> None:
    base_commit = normalize_commit(repo, base)
    head_commit = normalize_commit(repo, head)
    configured = submodules_at(repo, head_commit)
    verify_trusted_submodules(configured)
    changes = changed_gitlinks(repo, base_commit, head_commit)
    if not changes:
        print("No gitlink changes detected.")
        return

    for path, target in sorted(changes.items()):
        url = TRUSTED_SUBMODULES[path]
        matching_refs = sorted(
            ref
            for ref, commit in remote_release_tips(repo, url).items()
            if commit == target
        )
        if not matching_refs:
            raise VerificationError(
                f"{path} gitlink target {target} is not the tip of any remote release/* branch."
            )
        print(f"Verified {path} -> {target} via {', '.join(matching_refs)}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Verify AIO gitlink changes point to trusted remote release-branch tips."
    )
    parser.add_argument("--aio-root", type=Path, default=DEFAULT_AIO_ROOT)
    parser.add_argument("--base", required=True)
    parser.add_argument("--head", required=True)
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    verify_range(args.aio_root.resolve(), args.base, args.head)
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except VerificationError as exc:
        print(f"[ERROR] {exc}", file=sys.stderr)
        raise SystemExit(1)
