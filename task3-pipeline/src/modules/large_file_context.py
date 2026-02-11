"""Utilities to build LLM context for large files."""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

from modules.config import PipelineConfig
from modules.detector import DesignSmell


@dataclass(frozen=True)
class FileContext:
    file_path: Path
    content: str


class FileContextBuilder:
    def __init__(self, config: PipelineConfig) -> None:
        self._config = config
        self._max_lines = config.llm.max_file_chunk_lines
        self._source_roots = [
            config.repository.root / "app" / "src" / "main" / "java",
            config.repository.root / "db-utils" / "src" / "main" / "java",
            config.repository.root / "assembly-release" / "src" / "main" / "java",
        ]

    def build(self, smell: DesignSmell) -> FileContext:
        file_path = self._resolve_path(smell)
        if file_path is None:
            return FileContext(
                file_path=Path("unknown"),
                content=(
                    "Could not locate source file for the smell. "
                    "Review package/type mapping or adjust source roots."
                ),
            )

        lines = file_path.read_text(encoding="utf-8").splitlines()
        content = self._summarize_lines(lines)
        return FileContext(file_path=file_path, content=content)

    def _resolve_path(self, smell: DesignSmell) -> Path | None:
        relative_path = Path(*smell.package.split(".")) / f"{smell.type_name}.java"
        for root in self._source_roots:
            candidate = root / relative_path
            if candidate.exists():
                return candidate
        for root in self._source_roots:
            if not root.exists():
                continue
            matches = list(root.rglob(f"{smell.type_name}.java"))
            if matches:
                return matches[0]
        return None

    def _summarize_lines(self, lines: list[str]) -> str:
        if len(lines) <= self._max_lines:
            return self._format_lines(lines, 1)

        head_count = self._max_lines // 2
        tail_count = self._max_lines - head_count
        head = lines[:head_count]
        tail = lines[-tail_count:]
        omitted = len(lines) - (head_count + tail_count)

        summary = [
            self._format_lines(head, 1),
            f"\n... ({omitted} lines omitted for context window) ...\n",
            self._format_lines(tail, len(lines) - tail_count + 1),
        ]
        return "".join(summary)

    def _format_lines(self, lines: list[str], start_line: int) -> str:
        numbered = [
            f"{line_number:4d}: {line}"
            for line_number, line in enumerate(lines, start=start_line)
        ]
        return "\n".join(numbered) + "\n"