# Task 3C Refactoring Pipeline (LLM-Assisted)

This folder contains the **Task 3C** pipeline implementation for automating design smell
detection, LLM-based refactoring suggestions, and GitHub PR creation. The pipeline is
**non-destructive**: it generates refactoring suggestions and documentation artifacts
without applying changes to the codebase, aligning with the assignment requirement.

## Features

1. **Detects** design smells using Designite outputs (CSV).
2. **Refactors** by generating suggested code changes using the OpenAI API.
3. **PR Generation** creates a GitHub pull request containing the suggested changes and
   a summary of smells, refactoring techniques, and metrics.

## Quick Start

```bash
cd task3-pipeline
python3 -m venv .venv
source .venv/bin/activate
pip install -r src/requirements.txt
python3 src/pipeline.py --config config/pipeline.yml
```

## Configuration

- `.env` (not committed)
  - `OPENAI_API_KEY`
  - `GITHUB_TOKEN`
  - `GITHUB_REPO` (e.g., `apache/roller`)

- `config/pipeline.yml`
  - Paths to Designite outputs and repository root
  - PR metadata, scheduling controls, and output destinations
  - Ensure `head_branch` exists if you want PR creation to succeed

See `docs/pipeline.md` for details.