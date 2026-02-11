"""LLM-based refactoring suggestion module."""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

import os

import openai

from modules.config import PipelineConfig
from modules.detector import DesignSmell
from modules.large_file_context import FileContextBuilder


@dataclass(frozen=True)
class RefactorSuggestion:
    smell: DesignSmell
    affected_file: Path
    summary: str
    diff: str


class LlmRefactorer:
    def __init__(self, config: PipelineConfig) -> None:
        self._config = config
        self._api_key = os.getenv("OPENAI_API_KEY", "")
        self._client = openai.OpenAI(api_key=self._api_key) if self._api_key else None
        self._context_builder = FileContextBuilder(config)

    def generate(self, smells: list[DesignSmell]) -> list[RefactorSuggestion]:
        suggestions: list[RefactorSuggestion] = []
        for smell in smells:
            context = self._context_builder.build(smell)
            summary, diff = self._call_llm(smell, context)
            suggestions.append(
                RefactorSuggestion(
                    smell=smell,
                    affected_file=context.file_path,
                    summary=summary,
                    diff=diff,
                )
            )
        return suggestions

    def _call_llm(self, smell: DesignSmell, context: "FileContext") -> tuple[str, str]:
        prompt = (
            "You are a refactoring assistant. Provide a concise summary of the smell and "
            "a proposed diff (unified format) that improves design while preserving behavior.\n\n"
            f"Smell: {smell.smell}\n"
            f"Package: {smell.package}\n"
            f"Type: {smell.type_name}\n\n"
            "Context:\n"
            f"{context.content}\n"
        )
        if self._client is None:
            summary = "OpenAI API key missing; LLM suggestions skipped."
            return summary, ""

        response = self._client.chat.completions.create(
            model=self._config.llm.model,
            messages=[{"role": "user", "content": prompt}],
        )
        text = response.choices[0].message.content or ""
        return self._split_summary_and_diff(text)

    def _split_summary_and_diff(self, text: str) -> tuple[str, str]:
        parts = text.split("```diff")
        summary = parts[0].strip() if parts else ""
        diff = ""
        if len(parts) > 1:
            diff_section = parts[1]
            diff = diff_section.replace("```", "").strip()
        return summary, diff