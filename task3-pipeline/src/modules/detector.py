"""Design smell detection based on Designite CSV outputs."""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

import csv

from modules.config import PipelineConfig


@dataclass(frozen=True)
class DesignSmell:
    package: str
    type_name: str
    smell: str


class DesignSmellDetector:
    def __init__(self, config: PipelineConfig) -> None:
        self._config = config
        self._csv_path = config.repository.designite_smells_csv

    def scan(self) -> list[DesignSmell]:
        rows = self._read_csv(self._csv_path)
        smells = [
            DesignSmell(
                package=row["Package Name"],
                type_name=row["Type Name"],
                smell=row["Code Smell"],
            )
            for row in rows
        ]
        return smells[: self._config.pipeline.max_smells]

    def _read_csv(self, path: Path) -> list[dict[str, str]]:
        with path.open(newline="", encoding="utf-8") as handle:
            reader = csv.DictReader(handle)
            return [row for row in reader]