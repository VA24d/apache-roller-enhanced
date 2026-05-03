# Task 5: Web Translation and Caching

## 1. Feature Description

Task 5 adds a translation subsystem for public Roller weblog pages and combines it with section-level caching so repeated translations do not always call an external provider again. The feature lets a reader open a public weblog page, choose a source language, target language, and provider from a floating translation widget, and translate the visible weblog text without changing the page structure.

The implemented scope covers theme-driven public pages that include the shared translation widget. In this repository, the widget is wired into public weblog, permalink, search, tags, archives, and frontpage views across the shipped themes. The backend is generic enough to support more views later if those views adopt the same frontend contract.

The main user-visible capabilities are:

- Translation of already rendered weblog text on public pages.
- Runtime switching between `MyMemory` and `Sarvam AI`.
- Marathi support in both the UI and backend normalization layer.
- Restore-to-original behavior after translation.
- Automatic reuse of the last saved translation configuration on revisit.
- Section-level cache reuse when content has not changed significantly.

## 2. Implementation

### Backend

The backend is centered around a small translation pipeline exposed by `TranslationServlet`. The servlet accepts JSON requests from the browser, validates language inputs, resolves the active provider through `TranslationProviderFactory`, and delegates section-based translation work to `TranslationCacheService`.

`TranslationCacheService` is the core optimization for Task 5B. It computes a SHA-256 hash over normalized section text, where normalization trims leading and trailing whitespace and collapses repeated whitespace. Because of that, cosmetic spacing changes do not invalidate the cache, but actual content edits do. The cache key includes provider, source language, target language, and content hash, so translations are reused only when the effective translation request is truly the same.

Language handling is centralized in `TranslationLanguageSupport`, which normalizes values such as `en-US` or `mr_IN` into the supported internal codes and maps those codes to provider-specific formats for Sarvam such as `mr-IN`. The provider layer is implemented through the `TranslationProvider` strategy interface with `MyMemoryTranslationProvider` and `SarvamTranslationProvider` as concrete implementations.

The following class diagram summarizes the implemented subsystem:

![Task 5 Translation System Class Diagram](task5-translation-system-class-diagram.png)

Implemented backend behavior:

- `TranslationServlet` supports both section-based requests and the older text-list request format.
- `TranslationProviderFactory` chooses the provider and falls back to `mymemory` for unknown names.
- `TranslationCacheService` reuses cached section translations and retranslates only changed sections.
- `TranslationLanguageSupport` centralizes supported-language checks and provider-specific code mapping.
- `SarvamTranslationProvider` reads the API key from Roller configuration and falls back to `translation-api.properties` if needed.
- `MyMemoryTranslationProvider` and `SarvamTranslationProvider` both translate text in batches while preserving item order in the response.

### Frontend (screenshots)

The frontend is implemented as a shared JavaScript widget in `app/src/main/webapp/theme/scripts/translation.js` with styling in `app/src/main/webapp/theme/styles/translation.css`. The widget is injected on page load, detects the source language from the page `lang` attribute or URL, scans the primary visible content, groups text nodes into sections, and sends only those sections to the backend translation endpoint.

The widget also stores the most recent translation configuration in local storage, supports restore-to-original behavior, and automatically reapplies the saved translation on revisit. Theme templates opt in by including the shared script and stylesheet and by exposing the correct `lang` attribute on the page.

The repository does not currently include committed Task 5 UI screenshots, so the report-ready screenshot slots for this section are:

1. Public weblog page with the translation widget visible.
2. Same page after translating to Hindi or Marathi.
3. Revisit flow showing the cached-section status message.
4. Provider switch example showing `MyMemory` and `Sarvam AI`.

Suggested pages for capture:

- `basic/weblog` or `basic/permalink`
- `fauxcoly/search`
- `gaurav/archives`
- `frontpage`

## 3. Assumptions / Constraints

- Translation is currently available only on public theme views that include the shared translation assets.
- The supported languages are limited to `en`, `hi`, `bn`, `ta`, `te`, `kn`, and `mr`.
- Source auto-detection uses the page `lang` attribute first, then path-based hints, and finally falls back to `en`.
- Only rendered text nodes are translated. The implementation does not translate images, links, HTML attributes, or dynamically excluded UI regions.
- The cache is an in-memory server-side cache inside `TranslationCacheService`, so it is process-local and not persisted across restarts.
- Cache reuse is based on normalized text content, provider, source language, and target language.
- Sarvam translation requires a configured API key. If it is missing, Sarvam requests fail and the feature should be used with `MyMemory` or configured credentials.
- The current frontend integration focuses on weblog-facing views rather than every possible Roller page type.

## 4. Files Modified / Added

### Backend

- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/TranslationServlet.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/TranslationCacheService.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/TranslationLanguageSupport.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/TranslationProvider.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/TranslationProviderFactory.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/MyMemoryTranslationProvider.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/SarvamTranslationProvider.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/TranslationSectionRequest.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/TranslationSectionResponse.java`
- `app/src/main/webapp/WEB-INF/web.xml`
- `app/src/main/resources/org/apache/roller/weblogger/config/roller.properties`

### Frontend

- `app/src/main/webapp/theme/scripts/translation.js`
- `app/src/main/webapp/theme/styles/translation.css`

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
- `app/src/main/webapp/themes/frontpage/_header.vm`
- `app/src/main/webapp/themes/frontpage/_footer.vm`

### Tests and Documentation

- `app/src/test/java/org/apache/roller/weblogger/ui/rendering/servlets/TranslationCacheServiceTest.java`
- `app/src/test/java/org/apache/roller/weblogger/ui/rendering/servlets/TranslationLanguageSupportTest.java`
- `docs/task5-translation-system.md`
- `docs/task5-translation-system-class-diagram.puml`
- `docs/task5-translation-system-class-diagram.png`

## 5. Design Patterns

The translation subsystem was deliberately designed around a small set of patterns that make the feature extensible, easier to reason about, and aligned with the two main goals of Task 5: provider flexibility and efficient reuse of translation work. The most important patterns are described below.

### Strategy Pattern

The Strategy Pattern is the core extensibility pattern in this subsystem. It appears in the `TranslationProvider` interface and its two concrete implementations, `MyMemoryTranslationProvider` and `SarvamTranslationProvider`.

This pattern is useful here because the actual translation behavior is the part of the system that varies the most. Each provider has different API endpoints, authentication requirements, request formats, response structures, and language-code expectations. Without a strategy abstraction, `TranslationServlet` or `TranslationCacheService` would need provider-specific `if` or `switch` logic and would quickly become tightly coupled to external APIs.

By isolating translation behavior behind `TranslationProvider`, the rest of the system can treat all providers uniformly:

- the servlet only asks for a provider that can translate a list of strings
- the cache service does not need to know whether the backend call is REST `GET`, REST `POST`, authenticated, or unauthenticated
- new providers can be introduced by adding another implementation without redesigning the request pipeline

In quality-attribute terms, Strategy improves:

- extensibility, because new providers can be plugged in with localized changes
- maintainability, because provider-specific code stays out of orchestration logic
- testability, because provider behavior can be stubbed behind one contract
- separation of concerns, because provider integration is not mixed with HTTP request handling or cache logic

The tradeoff is that the pattern introduces another abstraction layer, so a reader has to jump through more classes to follow the full execution path. That tradeoff is worthwhile here because provider variability is a first-class requirement of the feature.

### Factory Pattern

The Factory Pattern appears in `TranslationProviderFactory`, which centralizes provider creation.

This pattern complements Strategy. Once translation behavior has been abstracted behind `TranslationProvider`, the system still needs one place to decide which concrete strategy to instantiate. If provider creation were scattered across servlets or frontend-driven code paths, adding or changing a provider would require updates in many places. The factory avoids that by keeping provider selection logic in one class.

In this implementation, the factory:

- resolves the configured default provider when none is explicitly supplied
- creates the correct concrete provider for names such as `mymemory` and `sarvam`
- falls back safely when an unknown provider name is requested

This improves:

- low coupling, because callers depend on the abstraction rather than on constructors of concrete classes
- maintainability, because instantiation rules live in one place
- configurability, because default-provider behavior is easy to manage centrally

The tradeoff is that every new provider requires a factory update. That is acceptable here because the number of providers is small and explicit control over supported providers is desirable.

### Cache Pattern

The Cache Pattern is the main performance pattern of Task 5B and is implemented in `TranslationCacheService`.

Caching is not just an optimization add-on in this project; it is one of the main design requirements. Repeatedly translating the same weblog content through an external service would increase latency, API usage, and cost. A cache is therefore used to store and reuse previous section translations.

What makes this implementation more interesting is that it uses section-level caching rather than page-level caching. This is an important design decision. If the system cached only whole pages, even a small edit to one paragraph would force the entire page to be translated again. By caching at the section level, the system can selectively invalidate and retranslate only the parts whose normalized content changed.

The cache design has several key properties:

- the cache key includes provider, source language, target language, and content hash
- the hash is computed from normalized text, not raw text
- normalization ignores meaningless whitespace variation
- unchanged sections are reused even when neighboring sections have changed

This improves:

- performance, by reducing repeated external translation calls
- responsiveness, by making revisits faster
- scalability, by decreasing repeated provider work under repeated access
- cost efficiency, by reducing dependence on paid or rate-limited APIs

The tradeoffs are real as well:

- cache invalidation logic adds complexity
- the cache consumes memory
- an in-memory cache is local to the running server process and disappears on restart

Even with those tradeoffs, Cache is the right pattern here because efficiency and selective reuse are central to the feature’s value.

### Adapter-Like Normalization Pattern

An adapter-like pattern is used in `TranslationLanguageSupport`. While not a textbook object adapter with wrapped instances, it plays the same architectural role: translating one representation into another so different parts of the system can cooperate cleanly.

The frontend, backend, and providers do not all speak the exact same language-code format. For example:

- the browser may expose values such as `en-US` or `mr_IN`
- the internal translation flow wants compact normalized codes such as `en` and `mr`
- Sarvam expects provider-specific values such as `en-IN` and `mr-IN`

If that conversion logic were spread across the widget, servlet, and provider classes, the system would be harder to maintain and more error-prone. Centralizing it in `TranslationLanguageSupport` gives the subsystem one normalization boundary.

This improves:

- interoperability, because all parts of the system can exchange a consistent internal representation
- maintainability, because language mapping rules live in one place
- extensibility, because adding a new supported language becomes a focused change

The main tradeoff is that this helper becomes an important dependency for the rest of the subsystem, so its rules must stay synchronized with provider capabilities. That is still preferable to duplicating normalization logic throughout the codebase.

### Template Method Style Request Flow

`TranslationServlet` follows a Template Method style workflow even though it is not implemented through an inheritance hierarchy. The idea is that the servlet enforces a stable algorithmic sequence while allowing specific steps inside that sequence to vary through collaborators.

The flow is consistently:

1. parse and validate the incoming JSON payload
2. normalize language inputs
3. resolve the translation provider
4. delegate translation and cache reuse
5. assemble the JSON response
6. handle errors uniformly

This is pattern-relevant because it keeps the request lifecycle predictable. The servlet remains an orchestrator rather than becoming a monolithic class that performs every task itself. Variation is pushed into collaborators such as `TranslationProviderFactory`, `TranslationLanguageSupport`, and `TranslationCacheService`.

This improves:

- readability, because the end-to-end flow is easy to follow
- maintainability, because each processing stage has a clear role
- separation of concerns, because parsing, normalization, provider behavior, and caching are not collapsed into one method body

The tradeoff is that `TranslationServlet` becomes a central coordination point. If too many special cases are added later, it may need to be further refactored. At the current scale, though, this orchestration style is clean and appropriate.

### State Preservation / Memento-Like Client Pattern

There is also a lightweight memento-like pattern on the frontend. The widget stores the user’s last translation settings in local storage and restores them on revisit. This is not a formal GoF Memento implementation, but architecturally it serves the same purpose: preserving UI state outside the active interaction so it can be reapplied later.

In `translation.js`, the saved configuration contains:

- selected source language
- selected target language
- selected provider
- auto-apply preference

This pattern is valuable because Task 5 is not only about correct translation; it is also about reducing repeated user effort. Persisting the previous configuration means users do not need to manually reselect the same translation on every visit.

This improves:

- usability, because repeat actions become faster
- continuity of experience, because revisits feel stateful rather than stateless
- perceived performance, because the system feels smarter and more personalized

The tradeoff is that stored client state can become stale if supported options change, which is why the code sanitizes stored values before reusing them.

## 6. Rationale Behind Pattern

The design patterns above were not chosen independently; they work together as a coordinated architecture for the translation feature.

The most important architectural decision was to separate variable behavior from stable workflow. Provider-specific behavior is highly variable, so Strategy and Factory were used to isolate and manage that variability. In contrast, the servlet request flow is stable, so the Template Method style keeps that path predictable while delegating specialized work outward.

The second major design goal was efficient reuse. That is why the Cache Pattern is not a minor optimization but one of the feature’s defining structural choices. Section-level caching directly addresses the project requirement to avoid retranslating unchanged content and gives the implementation a strong performance story beyond basic functionality.

The third design goal was compatibility between layers. The frontend, internal backend flow, and external APIs all represent language information differently, so an adapter-like normalization layer was necessary to stop translation-specific format logic from leaking into multiple classes. This keeps the codebase cleaner and lowers the cost of later language expansion.

Finally, the frontend state-preservation behavior supports the overall product rationale of the feature. A translation system should not feel like a one-time action that resets on every page load. Persisting and reusing the user’s last configuration makes the feature feel integrated into browsing rather than bolted on.

Taken together, these patterns give the subsystem several strong properties:

- it is extensible, because new providers and languages can be added with localized changes
- it is maintainable, because responsibilities are clearly divided between servlet, cache, normalization, and provider layers
- it is performant, because repeated translation work is reduced through selective section reuse
- it is testable, because the major responsibilities are decomposed into focused units
- it is user-friendly, because the system remembers configuration and minimizes repeated interaction cost

The main tradeoff of this design is increased structural complexity compared with a single “call translation API directly from one servlet” implementation. However, that simpler design would have been brittle, harder to extend, and much weaker at meeting the caching and provider-switching goals of Task 5. For this project, the added structure is justified because the patterns directly support the feature requirements rather than serving as unnecessary abstraction.

## 7. User Flow

1. A reader opens a public Roller page that includes the shared translation widget.
2. On page load, `translation.js` injects the widget, detects the likely source language, and restores the last saved configuration from local storage.
3. The reader selects a target language and provider, or keeps the saved values.
4. The widget scans the primary visible content, groups text nodes into sections, and sends those sections to `/roller-services/translate`.
5. `TranslationServlet` normalizes the language codes, resolves the provider, and forwards the request to `TranslationCacheService`.
6. `TranslationCacheService` checks the in-memory cache using the normalized content hash of each section.
7. Cached sections are reused immediately, while only cache misses are sent to the chosen translation provider.
8. The backend returns translated sections plus cache metadata to the browser.
9. The widget replaces only the original text nodes in the DOM, leaving page structure, styling, images, and navigation unchanged.
10. The selected configuration is saved locally so the same translation can be auto-applied on the next visit.
11. If the underlying weblog content changes, only the changed sections are retranslated while unchanged sections continue to reuse cached results.

## 8. UML Class Diagram

![Task 5 Translation System Class Diagram](task5-translation-system-class-diagram.png)
