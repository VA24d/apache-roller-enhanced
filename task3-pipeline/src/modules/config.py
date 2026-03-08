"""Configuration loading for the Task 3 pipeline."""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

import yaml


@dataclass(frozen=True)
class RepositoryConfig:
    root: Path
    designite_smells_csv: Path


@dataclass(frozen=True)
class PipelineSettings:
    schedule: str
    max_smells: int
    output_dir: Path


@dataclass(frozen=True)
class LlmSettings:
    model: str
    max_context_tokens: int
    max_file_chunk_lines: int
    summary_prompt: str


@dataclass(frozen=True)
class PullRequestSettings:
    title: str
    body_template: Path
    base_branch: str
    head_branch: str
    draft: bool


@dataclass(frozen=True)
class PipelineConfig:
    repository: RepositoryConfig
    pipeline: PipelineSettings
    llm: LlmSettings
    pr: PullRequestSettings
    config_path: Path

    @staticmethod
    def load(path: Path) -> "PipelineConfig":
        data = yaml.safe_load(path.read_text())
        config_dir = path.parent

        repo = RepositoryConfig(
            root=(config_dir / data["repository"]["root"]).resolve(),
            designite_smells_csv=(
                config_dir / data["repository"]["designite_smells_csv"]
            ).resolve(),
        )
        pipeline = PipelineSettings(
            schedule=data["pipeline"]["schedule"],
            max_smells=int(data["pipeline"]["max_smells"]),
            output_dir=(config_dir / data["pipeline"]["output_dir"]).resolve(),
        )
        llm = LlmSettings(
            model=data["llm"]["model"],
            max_context_tokens=int(data["llm"]["max_context_tokens"]),
            max_file_chunk_lines=int(data["llm"]["max_file_chunk_lines"]),
            summary_prompt=data["llm"]["summary_prompt"],
        )
        pr = PullRequestSettings(
            title=data["pr"]["title"],
            body_template=(config_dir / data["pr"]["body_template"]).resolve(),
            base_branch=data["pr"]["base_branch"],
            head_branch=data["pr"]["head_branch"],
            draft=bool(data["pr"]["draft"]),
        )
        return PipelineConfig(
            repository=repo,
            pipeline=pipeline,
            llm=llm,
            pr=pr,
            config_path=path.resolve(),
        )