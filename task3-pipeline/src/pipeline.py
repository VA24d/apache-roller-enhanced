"""Task 3C pipeline entrypoint."""

from __future__ import annotations

from pathlib import Path

from dotenv import load_dotenv

from modules.config import PipelineConfig
from modules.detector import DesignSmellDetector
from modules.llm_refactorer import LlmRefactorer
from modules.pr_generator import PullRequestGenerator
from modules.reporter import PipelineReporter


def main() -> None:
    load_dotenv()
    config_path = Path(__file__).resolve().parents[1] / "config" / "pipeline.yml"
    config = PipelineConfig.load(config_path)

    detector = DesignSmellDetector(config)
    smells = detector.scan()

    refactorer = LlmRefactorer(config)
    suggestions = refactorer.generate(smells)

    reporter = PipelineReporter(config)
    report = reporter.write_report(smells, suggestions)

    pr_generator = PullRequestGenerator(config)
    pr_generator.create_pull_request(report)


if __name__ == "__main__":
    main()