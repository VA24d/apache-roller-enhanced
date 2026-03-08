"""GitHub Pull Request generator for Task 3 pipeline."""

from __future__ import annotations

import os
from pathlib import Path

import requests

from modules.config import PipelineConfig


class PullRequestGenerator:
    def __init__(self, config: PipelineConfig) -> None:
        self._config = config
        self._repo = os.getenv("GITHUB_REPO", "")
        self._token = os.getenv("GITHUB_TOKEN", "")

    def create_pull_request(self, report_path: Path) -> None:
        if not self._repo or not self._token:
            report_path.write_text(
                report_path.read_text(encoding="utf-8")
                + "\n\n> PR generation skipped: missing GITHUB_REPO or GITHUB_TOKEN.\n",
                encoding="utf-8",
            )
            return

        body_template = self._config.pr.body_template.read_text(encoding="utf-8")
        report = report_path.read_text(encoding="utf-8")
        body = body_template.replace("{{REPORT}}", report)

        url = f"https://api.github.com/repos/{self._repo}/pulls"
        payload = {
            "title": self._config.pr.title,
            "head": self._config.pr.head_branch,
            "base": self._config.pr.base_branch,
            "body": body,
            "draft": self._config.pr.draft,
        }
        headers = {
            "Authorization": f"Bearer {self._token}",
            "Accept": "application/vnd.github+json",
        }
        response = requests.post(url, headers=headers, json=payload, timeout=30)
        if response.status_code >= 400:
            report_path.write_text(
                report_path.read_text(encoding="utf-8")
                + "\n\n> PR generation failed. "
                + f"Status: {response.status_code}.\n",
                encoding="utf-8",
            )