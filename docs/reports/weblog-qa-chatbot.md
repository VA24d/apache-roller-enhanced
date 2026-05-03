# Weblog Q&A Chatbot

## 1. Feature Description

This bonus feature adds a grounded question-answering chatbot to public Roller weblog pages. Instead of manually browsing multiple entries, a reader can ask natural-language questions such as "What has this blog said about data privacy?" or "When was topic X last discussed?" and receive an answer based only on the current weblog's published entries.

The implemented scope focuses on public weblog-facing pages that include the shared chatbot widget. The feature is grounded by design: answers are built from published weblog content, and the UI shows supporting source citations so the reader can trace the response back to the original entries.

The main user-visible capabilities are:

- Asking free-form questions about the current weblog's published archive.
- Switching between two answering strategies: `RAG` and `Long Context`.
- Using `Auto Pick` to let Gemini choose the better strategy for the question and explain why.
- Returning grounded answers with cited source entries.
- Keeping the chatbot embedded directly on public weblog pages without disrupting page layout.

## 2. Implementation

### Backend

The backend is centered around a small Q&A pipeline exposed by `WeblogQAServlet`. The servlet accepts JSON requests from the browser, resolves the current weblog handle and requested strategy, and delegates the actual answer generation to `WeblogQaService`.

`WeblogQaService` is the orchestration layer for the feature. It loads the relevant weblog archive through `WeblogQaEntryRepository`, resolves the requested strategy, and returns a structured `WeblogQaAnswer` containing the answer text, source citations, entry counts, truncation metadata, and any strategy-selection explanation used by `Auto Pick`.

The two implemented answering strategies are:

- `RetrievalAugmentedWeblogQaStrategy`
  Retrieves and ranks the most relevant passages first, then sends only that focused grounded context to Gemini for final synthesis.
- `LongContextWeblogQaStrategy`
  Scans a much larger portion of the weblog archive up to a context budget, then sends that broader grounded context to Gemini for final synthesis.

Both strategies use the shared Gemini-backed synthesis helper in `GeminiQaSynthesisSupport`. This keeps the comparison clean: the model remains the same while the context-construction pipeline changes. That makes it easier to justify trade-offs around archive growth, retrieval accuracy, latency, and breadth of coverage.

`Auto Pick` is implemented in `GeminiQaAutoStrategySelector`. For this mode only, Gemini first examines the user prompt and chooses either `RAG` or `Long Context`, along with a short reason. The selected strategy is then executed through the normal pipeline. If Gemini-based strategy selection is unavailable, the selector falls back to a heuristic rule set so the feature continues to function.

The repository abstraction is implemented through `WeblogQaEntryRepository` and `BusinessLayerWeblogQaEntryRepository`, which load published entries from Roller business services and convert them into normalized `WeblogQaEntryDocument` objects suitable for ranking and synthesis.

The following class diagram summarizes the implemented subsystem:

![Weblog Q&A Chatbot Class Diagram](weblog-qa-chatbot-class-diagram.png)

Implemented backend behavior:

- `WeblogQAServlet` exposes a JSON endpoint at `/roller-services/weblog-qa/*`.
- `WeblogQaService` resolves strategy selection and delegates answer generation.
- `RetrievalAugmentedWeblogQaStrategy` retrieves a small, focused set of passages before Gemini synthesis.
- `LongContextWeblogQaStrategy` builds a wider archive context before Gemini synthesis.
- `GeminiQaAutoStrategySelector` is used only for `Auto Pick` and returns both a selected strategy and the reason for the choice.
- `GeminiQaSynthesisSupport` performs the shared Gemini REST call and returns a grounded answer payload.
- `LocalApiPropertiesSupport` loads local-only API credentials from `translation-api.properties`.
- `WeblogQaTextSupport` centralizes passage splitting, tokenization, ranking helpers, normalization, and snippet formatting.

### Frontend (screenshots)

The frontend is implemented as a shared JavaScript widget in `app/src/main/webapp/theme/scripts/weblog-qa.js` with styling in `app/src/main/webapp/theme/styles/weblog-qa.css`. The widget is injected on page load, detects the current weblog from the page URL, and renders a floating chat panel with a question box, strategy selector, answer area, and source citation list.

The widget supports manual strategy selection between `RAG` and `Long Context`, plus the `Auto Pick` mode. When `Auto Pick` is used, the widget also shows why that strategy was chosen. Long answers remain scrollable inside the widget so the controls stay reachable, and initialization is resilient across page load timing differences.

The repository does not currently include committed Q&A UI screenshots, so the report-ready screenshot slots for this section are:

1. Public weblog page with the Q&A widget visible.
2. Same question answered using `RAG`.
3. Same question answered using `Long Context`.
4. `Auto Pick` example showing the chosen strategy and the reason.

Suggested pages for capture:

- `basic/weblog`
- `basic/permalink`
- `fauxcoly/weblog`
- `frontpage`

## 3. Assumptions / Constraints

- The chatbot is currently available only on public theme views that include the shared widget assets.
- Answers are grounded only in published entries from the current weblog archive.
- The feature does not answer questions using comments, admin pages, media metadata, or arbitrary site-wide content.
- `RAG` and `Long Context` both use Gemini for final answer synthesis; their difference lies in how the grounded context is constructed.
- `Auto Pick` uses Gemini only to choose between `RAG` and `Long Context`; it does not replace the two underlying strategies.
- Gemini requires a configured API key in local configuration. If it is missing, Gemini-backed routing or synthesis cannot run successfully.
- Long-context processing still uses a bounded context budget, so very large weblog archives may be truncated.
- The current implementation is designed for weblog-facing public pages rather than every possible Roller page type.

## 4. Files Modified / Added

### Backend

- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/WeblogQAServlet.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/WeblogQaService.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/WeblogQaAnswer.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/WeblogQaSource.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/WeblogQaAnswerStrategy.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/WeblogQaEntryRepository.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/BusinessLayerWeblogQaEntryRepository.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/WeblogQaEntryDocument.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/WeblogQaTextSupport.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/RetrievalAugmentedWeblogQaStrategy.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/LongContextWeblogQaStrategy.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/GeminiQaSynthesisSupport.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/GeminiQaAutoStrategySelector.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/LocalApiPropertiesSupport.java`
- `app/src/main/webapp/WEB-INF/web.xml`

### Frontend

- `app/src/main/webapp/theme/scripts/weblog-qa.js`
- `app/src/main/webapp/theme/styles/weblog-qa.css`

### Theme Integration

- `app/src/main/webapp/themes/basic/weblog.vm`
- `app/src/main/webapp/themes/basic/permalink.vm`
- `app/src/main/webapp/themes/basic/searchresults.vm`
- `app/src/main/webapp/themes/basicmobile/weblog.vm`
- `app/src/main/webapp/themes/basicmobile/weblog-mobile.vm`
- `app/src/main/webapp/themes/basicmobile/permalink.vm`
- `app/src/main/webapp/themes/basicmobile/permalink-mobile.vm`
- `app/src/main/webapp/themes/basicmobile/searchresults.vm`
- `app/src/main/webapp/themes/basicmobile/searchresults-mobile.vm`
- `app/src/main/webapp/themes/fauxcoly/weblog.vm`
- `app/src/main/webapp/themes/fauxcoly/entry.vm`
- `app/src/main/webapp/themes/fauxcoly/search.vm`
- `app/src/main/webapp/themes/fauxcoly/tags_index.vm`
- `app/src/main/webapp/themes/fauxcoly/archives.vm`
- `app/src/main/webapp/themes/gaurav/weblog.vm`
- `app/src/main/webapp/themes/gaurav/entry.vm`
- `app/src/main/webapp/themes/gaurav/search.vm`
- `app/src/main/webapp/themes/gaurav/tags_index.vm`
- `app/src/main/webapp/themes/gaurav/archives.vm`
- `app/src/main/webapp/themes/frontpage/_footer.vm`

### Tests and Documentation

- `app/src/test/java/org/apache/roller/weblogger/ui/rendering/servlets/WeblogQaServiceTest.java`
- `app/src/test/java/org/apache/roller/weblogger/ui/rendering/servlets/GeminiQaSynthesisSupportTest.java`
- `docs/weblog-qa-chatbot.md`

## 5. Design Patterns

The weblog Q&A subsystem was designed around a small set of patterns that keep the feature extensible, grounded, and maintainable while still making it easy to compare two distinct answering approaches.

### Strategy Pattern

The Strategy Pattern is the core architectural pattern in this subsystem. It appears in the `WeblogQaAnswerStrategy` interface and the concrete implementations `RetrievalAugmentedWeblogQaStrategy` and `LongContextWeblogQaStrategy`.

This pattern is appropriate because the main behavior that varies is the method of constructing grounded context from the weblog archive. Both strategies answer the same user-facing question, but they differ in how much of the archive they inspect, how they rank evidence, and how they trade breadth against efficiency.

This improves:

- extensibility, because additional answering strategies can be added without changing the servlet contract
- maintainability, because strategy-specific logic stays isolated
- comparability, because both strategies implement the same answer-generation interface
- testability, because individual strategies can be verified independently

The tradeoff is that the execution path is spread across more classes, which makes the architecture slightly more abstract than a single monolithic answer method.

### Repository Pattern

The Repository Pattern appears in `WeblogQaEntryRepository` and `BusinessLayerWeblogQaEntryRepository`.

This pattern is useful because answer generation should not be tightly coupled to Roller business-layer APIs or database access details. The Q&A subsystem only needs a clean way to obtain normalized published entry documents. By introducing a repository abstraction, the feature separates data access concerns from question-answering concerns.

This improves:

- separation of concerns, because archive loading is isolated from retrieval and synthesis logic
- maintainability, because data-loading changes are localized
- testability, because services and strategies can be tested with stub repositories
- extensibility, because alternative archive sources can be added later if needed

The tradeoff is the extra abstraction layer between the answer-generation code and the underlying Roller services.

### Template Method Style Request Flow

`WeblogQAServlet` and `WeblogQaService` together follow a Template Method style workflow even though the implementation is composition-based rather than inheritance-based.

The stable flow is:

1. parse the request
2. resolve the weblog and requested strategy
3. load the archive
4. build grounded context
5. synthesize the answer
6. return citations and metadata

This is valuable because the high-level request lifecycle stays predictable while the context-building step varies through strategies and the auto-routing step varies through a separate selector.

This improves:

- readability, because the end-to-end flow is easy to follow
- maintainability, because each stage has a defined responsibility
- separation of concerns, because request parsing, data loading, routing, and synthesis are not collapsed together

The tradeoff is that the orchestration layer becomes an important coordination point and may need further refactoring if too many feature-specific branches are added later.

### Adapter-Like Integration Pattern

An adapter-like pattern appears in `GeminiQaSynthesisSupport` and `GeminiQaAutoStrategySelector`. These classes translate internal Q&A data structures into Gemini-compatible REST payloads and interpret the structured responses back into application-specific objects.

This pattern is important because the internal representation of grounded passages and strategy decisions does not match the external API shape. Without an adapter boundary, Gemini request formatting and response parsing logic would leak into strategies and service classes.

This improves:

- interoperability, because internal Q&A logic is insulated from provider-specific payload details
- maintainability, because Gemini-specific integration code lives in focused classes
- extensibility, because the model provider can be swapped or expanded with more localized changes

The tradeoff is that these classes become integration hotspots and must remain aligned with the external API contract.

### Factory-Like Routing Pattern

The strategy resolution logic in `WeblogQaService`, together with the `GeminiQaAutoStrategySelector`, forms a factory-like routing layer. The system resolves a requested strategy name into the actual strategy instance that will answer the question, and in `Auto Pick` mode it first chooses the best strategy before delegating.

This pattern is useful because request-time strategy selection should be centralized rather than scattered across the servlet or frontend.

This improves:

- low coupling, because callers request a strategy by name rather than directly constructing implementations
- maintainability, because selection logic is centralized
- extensibility, because new strategies or selection modes can be integrated without redesigning the UI contract

The tradeoff is that the routing logic becomes a policy layer that needs to be kept consistent with the available strategy implementations.

## 6. Rationale Behind Pattern

The design patterns above work together to support the main goals of the chatbot feature: grounded answers, clean comparison between two answering approaches, and maintainable integration into Roller.

The most important design decision was to separate stable workflow from variable answer-construction behavior. The request and response lifecycle is stable, so a Template Method style orchestration works well. The context-construction behavior varies significantly, so Strategy is the right pattern for `RAG` and `Long Context`.

The second major goal was to keep the feature grounded in weblog content rather than tightly coupled to a single external API. That is why the Repository Pattern and adapter-like Gemini integration layer are both important. The repository isolates content loading, while the integration layer isolates model-provider concerns.

The third goal was to make strategy selection itself extensible. `Auto Pick` should not bypass the main architecture; it should choose between the same two strategies a user can pick manually. The factory-like routing layer preserves that design and makes the comparison story easier to present during evaluation.

Taken together, these patterns give the subsystem several strong properties:

- it is extensible, because new strategies or model integrations can be added with localized changes
- it is maintainable, because responsibilities are divided between servlet, service, repository, routing, and synthesis layers
- it is testable, because strategy logic and orchestration logic can be exercised independently
- it is grounded, because the architecture keeps evidence selection explicit and visible
- it is demonstrable, because the comparison between `RAG` and `Long Context` is structurally clear rather than hidden inside one opaque answer path

The tradeoff is increased structural complexity compared with a single “send all posts to one model call” implementation. That simpler version would be easier to write initially but much weaker in extensibility, evaluation clarity, and control over archive growth behavior.

## 7. User Flow

1. A reader opens a public Roller weblog page that includes the shared Q&A widget.
2. On page load, `weblog-qa.js` injects the floating chatbot panel and detects the current weblog handle.
3. The reader enters a natural-language question and chooses `RAG`, `Long Context`, or `Auto Pick`.
4. The widget sends the question and strategy choice to `/roller-services/weblog-qa/*`.
5. `WeblogQAServlet` parses the request and forwards it to `WeblogQaService`.
6. If the request uses `Auto Pick`, `GeminiQaAutoStrategySelector` chooses either `RAG` or `Long Context` and returns the reason for the selection.
7. `WeblogQaService` loads published weblog entries through `WeblogQaEntryRepository`.
8. The selected strategy constructs grounded context from the archive and sends that context to `GeminiQaSynthesisSupport`.
9. The backend returns a structured answer with citations, entry counts, truncation metadata, and any auto-pick explanation.
10. The widget renders the answer, source links, and chosen strategy information inside the chat panel.

## 8. Verification

Compile:

```powershell
mvn -pl app "-Dmaven.repo.local=.m2/repository" -DskipTests compile
```

Targeted tests:

```powershell
mvn -pl app "-Dmaven.repo.local=.m2/repository" "-Dtest=WeblogQaServiceTest,GeminiQaSynthesisSupportTest" test
```

Existing app test suite:

```powershell
mvn -pl app "-Dmaven.repo.local=.m2/repository" test
```

Manual demo:

1. Start Roller with Jetty.
2. Open a public weblog page with several published posts.
3. Use the `Weblog Q&A Chatbot` widget in the lower-left corner.
4. Ask the same focused question using `RAG` and `Long Context`.
5. Ask a broad summary question using `Auto Pick`.
6. Compare the chosen strategy, explanation, answer style, citations, and whether long-context processing reports truncation on larger archives.
