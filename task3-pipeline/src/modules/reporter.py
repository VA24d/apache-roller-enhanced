"""Reporting utilities for Task 3 pipeline output."""

from __future__ import annotations

from datetime import datetime
from pathlib import Path

from modules.config import PipelineConfig
from modules.detector import DesignSmell
from modules.llm_refactorer import RefactorSuggestion


class PipelineReporter:
    def __init__(self, config: PipelineConfig) -> None:
        self._config = config
        self._output_dir = config.pipeline.output_dir

    def write_report(
        self, smells: list[DesignSmell], suggestions: list[RefactorSuggestion]
    ) -> Path:
        self._output_dir.mkdir(parents=True, exist_ok=True)
        report_path = self._output_dir / "refactor_report.md"
        report_path.write_text(
            self._render_report(smells, suggestions),
            encoding="utf-8",
        )
        return report_path

    def _render_report(
        self, smells: list[DesignSmell], suggestions: list[RefactorSuggestion]
    ) -> str:
        timestamp = datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S UTC")
        lines = [
            "# Task 3C LLM Refactoring Report",
            "",
            f"Generated: {timestamp}",
            "",
            "## Detected Design Smells",
            "",
        ]
        for smell in smells:
            lines.append(f"- **{smell.smell}** `{smell.package}.{smell.type_name}`")
        lines.append("")
        lines.append("## LLM Refactoring Suggestions")
        lines.append("")
        for suggestion in suggestions:
            lines.extend(
                [
                    f"### {suggestion.smell.type_name} ({suggestion.smell.smell})",
                    f"- File: `{suggestion.affected_file}`",
                    f"- Summary: {suggestion.summary}",
                    "",
                    "```diff",
                    suggestion.diff or "(No diff returned)",
                    "```",
                    "",
                ]
            )
        return "\n".join(lines)