# Weblog Q&A Chatbot

## Overview

This bonus feature adds a grounded Q&A chatbot to public Roller weblog pages. Readers can ask natural-language questions such as:

- What has this blog said about data privacy?
- When was topic X last discussed?

The chatbot searches the published entries for the current weblog and returns a synthesized answer with linked supporting sources. It is intentionally grounded: every answer includes citations back to the weblog posts that supported the response.

## Implemented Strategies

Two answering strategies are implemented and exposed directly in the UI:

- `RAG`
  Retrieves the most relevant entries first, then sends only those grounded passages to Gemini for answer synthesis.
- `Long Context`
  Scans a much larger slice of the weblog archive until a context budget is reached, then sends that broader grounded context to Gemini for answer synthesis.

Both strategies use the same Gemini-backed synthesis layer and answer payload, which keeps the feature maintainable while making the comparison easy to demonstrate. The difference is the context-construction pipeline, not the model provider.

## Design

### Backend

The backend lives under `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/`.

- `WeblogQaEntryRepository`
  Repository abstraction for loading published weblog entries.
- `BusinessLayerWeblogQaEntryRepository`
  Uses Roller managers to load and sanitize the weblog archive.
- `WeblogQaService`
  Entry point that resolves the requested strategy and delegates answer generation.
- `WeblogQaAnswerStrategy`
  Strategy interface for pluggable answering approaches.
- `RetrievalAugmentedWeblogQaStrategy`
  RAG implementation for focused retrieval before Gemini synthesis.
- `LongContextWeblogQaStrategy`
  Wider-archive scan with a bounded context budget before Gemini synthesis.
- `GeminiQaSynthesisSupport`
  Shared Gemini REST-backed synthesis layer used by both strategies.
- `LocalApiPropertiesSupport`
  Loads local-only API secrets from `translation-api.properties` so Sarvam and Gemini keys stay out of Git.
- `WeblogQaTextSupport`
  Shared text normalization, tokenization, passage splitting, ranking, date formatting, and snippet helpers.
- `WeblogQAServlet`
  JSON endpoint mounted at `/roller-services/weblog-qa/*`.

### Frontend

The public UI is implemented with:

- `app/src/main/webapp/theme/scripts/weblog-qa.js`
- `app/src/main/webapp/theme/styles/weblog-qa.css`

The widget is included on the same public theme pages that already host translation support. It detects the current weblog handle from the URL, lets the reader switch between `RAG` and `Long Context`, and renders answers plus source citations. Both modes use Gemini for final synthesis when a local Google API key is configured.
The `Auto Pick` option uses Gemini only to choose between those two strategies and explain why it made that choice; the selected strategy then runs through the existing pipeline as usual.

## Growth Trade-offs

### RAG

Observed strengths:

- scales better as the number of weblog posts grows
- keeps latency lower by ranking a smaller candidate set for synthesis
- works especially well for focused topical questions

Observed trade-offs:

- retrieval quality matters a lot, so very vague questions can miss useful posts
- if the best entry is not retrieved, the final answer can be narrower than the archive actually supports

### Long Context

Observed strengths:

- better for broad, exploratory questions that may benefit from scanning a wider archive slice
- more likely to surface recent mentions for “when was this last discussed?” style questions

Observed trade-offs:

- context growth becomes expensive as weblog size grows
- the strategy eventually hits a character budget, so very large archives may still be truncated
- latency and processing cost grow faster than RAG

Both strategies now share Gemini for final answer generation, so the practical comparison is cleaner:

- `RAG + Gemini` is the better fit when you want lower latency and more targeted evidence selection.
- `Long Context + Gemini` is the better fit when you want archive-wide coverage or timeline-heavy questions answered from a broader slice of the weblog.
- Since both use the same model, the observed differences now come mainly from context construction, which makes the trade-off discussion easier to justify during evaluation.

## Local API Key Setup

Gemini reads its key from the same untracked `translation-api.properties` file already used for Sarvam. Any of these keys are accepted:

- `qa.gemini.apiKey`
- `qa.google.apiKey`
- `google.apiKey`

Example:

```properties
qa.google.apiKey=YOUR_GOOGLE_API_KEY
```

## Verification

Compile:

```powershell
mvn -pl app "-Dmaven.repo.local=.m2/repository" -DskipTests compile
```

Targeted tests:

```powershell
mvn -pl app "-Dmaven.repo.local=.m2/repository" "-Dtest=WeblogQaServiceTest,GeminiQaSynthesisSupportTest" test
```

Manual demo:

1. Start Roller with Jetty.
2. Open a public weblog page.
3. Use the `Weblog Q&A Chatbot` widget in the lower-left corner.
4. Ask the same question using `RAG` and `Long Context`.
5. Compare the answer wording, sources, and whether the long-context strategy reports context truncation on larger archives while both modes still use Gemini for final synthesis.
