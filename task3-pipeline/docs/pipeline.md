# Task 3C Pipeline Details

This pipeline implements the **Detect → Refactor → PR** workflow described in the
assignment while keeping the codebase unchanged. All refactorings are **proposed only**
and are stored in `task3-pipeline/output/refactor_report.md` for reporting.

## Pipeline Stages

1. **Detect**
   - Parses `analysis/designite/designCodeSmells.csv`.
   - Produces a bounded list (configurable) of smell entries.

2. **Refactor (LLM Suggestions)**
   - Builds contextual snippets from the affected source file.
   - Uses OpenAI API to generate refactoring summaries and diffs.
   - Ensures the refactoring is **non-destructive**.

3. **PR Generation**
   - Builds a PR body containing the report with smells, refactor summaries, and diffs.
   - Opens a GitHub PR with the report (no code changes committed).
   - Uses a configurable `head_branch` separate from `base_branch`.

## Large File Handling

Large files are chunked using `FileContextBuilder`:

- Only the first and last portions of the file are included.
- The number of lines is controlled by `llm.max_file_chunk_lines`.
- The omitted middle portion is clearly annotated with line counts.
- This provides LLM context while respecting token limits.

## Inputs & Outputs

- **Input:** Designite CSV, source files in `app/`, `db-utils/`, `assembly-release/`
- **Output:** `task3-pipeline/output/refactor_report.md`

## Environment Variables

Create a `.env` file with:

```
OPENAI_API_KEY=...
GITHUB_TOKEN=...
GITHUB_REPO=apache/roller
```

## Running Locally

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r src/requirements.txt
python3 src/pipeline.py --config config/pipeline.yml
```

## Flowchart

See `docs/pipeline_flowchart.puml` for the PlantUML diagram.