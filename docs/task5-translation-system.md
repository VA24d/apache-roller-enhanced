# Task 5 - Web Translation and Caching

This document explains our implementation for Task 5A and Task 5B: weblog translation, provider switching, Marathi support, and section-level caching with selective invalidation.

## Scope

- Implemented for public Roller webpage views that render weblog content through the theme layer.
- Translation is currently mounted on weblog-facing pages such as weblog, permalink, search, tags, archives, and frontpage views where the shared translation widget is included.
- The backend caching and translation endpoint are generic and can support additional webpage types if those views also use the same translation flow.

## Task 5A - Web Translation

### What we implemented

- Application-side translation for webpage text already present at page load.
- Translation changes only text nodes; layout, images, links, and structure remain intact.
- Easily switchable translation providers:
  - `Sarvam AI`
  - `MyMemory`
- Supported languages:
  - English (`en`)
  - Hindi (`hi`)
  - Bengali (`bn`)
  - Tamil (`ta`)
  - Telugu (`te`)
  - Kannada (`kn`)
  - Marathi (`mr`)
- Source language selection in the widget, including `Auto Detect`.
- Translation support on public weblog views through the shared theme script.

### Design

The browser-side widget:
- extracts primary content sections from the rendered page
- sends only the relevant text content to the translation endpoint
- replaces only the translated text in the DOM
- preserves the original text for restore/re-translate flows
- stores the user's last used translation configuration in local storage

The backend:
- exposes a single translation servlet endpoint
- normalizes supported language codes
- delegates translation to the selected provider

### Important implementation notes

- Provider switching is centralized through `TranslationProviderFactory`.
- Marathi support was added across frontend selection and backend language normalization.
- The page's language metadata is now exposed through `lang` attributes on translation-enabled theme templates to improve source auto-detection.

## Task 5B - Caching

### Goal

Avoid repeated translation API calls for unchanged weblog content and reuse previous translations when a page is revisited.

### Caching strategy

We implemented **section-level caching** instead of whole-page-only caching.

Each page is split into primary content sections such as:
- headings
- paragraphs
- list items
- entry text blocks
- other text-bearing content containers in the main weblog content area

Each section is translated independently and cached on the server.

### Significant change definition

A **significant change** is defined as a change to a section's **normalized primary text content**.

Normalization rules:
- trim leading and trailing whitespace
- collapse repeated whitespace

This means:
- cosmetic spacing-only edits do **not** invalidate the cache
- real content edits to headings/body text **do** invalidate the cache

### Selective invalidation

When a page is translated again:
- unchanged sections reuse cached translations
- changed sections alone are retranslated
- full-page retranslation is avoided

### Revisit behavior

When the user revisits a translation-enabled page:
- the browser restores the user's last used translation configuration
- the widget auto-applies translation using that saved configuration
- the backend cache reuses unchanged section translations

### Why this is efficient

- reduces API usage
- reduces latency on repeated visits
- avoids reprocessing entire pages for small edits
- keeps the design extensible because caching is not hard-coded to one specific theme file

## Key Files

### Backend

- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/TranslationServlet.java`
  - accepts both legacy text translation requests and section-based translation requests
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/TranslationCacheService.java`
  - performs section-level cache lookup, reuse, and selective retranslation
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/TranslationLanguageSupport.java`
  - language normalization and Sarvam language mapping, including Marathi
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/MyMemoryTranslationProvider.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/rendering/servlets/SarvamTranslationProvider.java`

### Frontend

- `app/src/main/webapp/theme/scripts/translation.js`
  - translation widget
  - source auto-detection
  - section extraction
  - auto-apply on revisit
  - restore behavior
- `app/src/main/webapp/theme/styles/translation.css`
  - widget styling

### Tests

- `app/src/test/java/org/apache/roller/weblogger/ui/rendering/servlets/TranslationCacheServiceTest.java`
- `app/src/test/java/org/apache/roller/weblogger/ui/rendering/servlets/TranslationLanguageSupportTest.java`

## Design Patterns Used in Task 5

The Task 5 implementation was designed with modularity, maintainability, and extensibility in mind. The following design patterns are used directly in this feature.

### 1. Strategy Pattern

**Where used**
- `TranslationProvider`
- `MyMemoryTranslationProvider`
- `SarvamTranslationProvider`

**Why it was used**

Translation is the variable behavior in this subsystem. Different providers have different APIs, credentials, request formats, and language mappings, but the rest of the system should not need to care about those differences.

**Quality attributes improved**
- **Modularity:** provider-specific logic is isolated from servlet and UI logic.
- **Maintainability:** provider implementations can be changed independently.
- **Extensibility:** adding a new translation provider only requires implementing the shared interface and plugging it into the factory.
- **Testability:** provider behavior can be stubbed or mocked behind the same interface.

**Trade-offs**
- introduces one more abstraction layer
- requires all providers to conform to a common contract even if their APIs differ internally

### 2. Factory Pattern

**Where used**
- `TranslationProviderFactory`

**Why it was used**

Object creation for translation providers is centralized in one place so that callers do not directly instantiate `SarvamTranslationProvider` or `MyMemoryTranslationProvider`.

**Quality attributes improved**
- **Maintainability:** construction logic is not duplicated across the codebase.
- **Extensibility:** new providers can be added in one place.
- **Coupling reduction:** the servlet depends on the provider abstraction, not on concrete classes.

**Trade-offs**
- the factory must be updated when new providers are introduced
- a simple conditional factory is easy to maintain now, but may need refactoring if the number of providers grows significantly

### 3. Cache Pattern

**Where used**
- `TranslationCacheService`

**Why it was used**

Caching is the main optimization required by Task 5B. Instead of repeatedly translating the same unchanged content, the service stores section-level translations and reuses them across revisits.

**Quality attributes improved**
- **Performance:** fewer repeated translation API calls
- **Scalability:** lower provider/API usage under repeated access
- **Cost efficiency:** reduces external translation credit consumption
- **Responsiveness:** revisited pages translate faster when content is unchanged

**Trade-offs**
- adds cache-state management complexity
- requires careful invalidation logic
- consumes memory to store cached translations

### 4. Template Method Style Workflow

**Where used**
- `TranslationServlet` orchestrates a fixed flow:
  - parse request
  - normalize languages
  - select provider
  - apply cache lookup/translation
  - return response

**Why it was used**

Although not implemented through inheritance, the servlet follows a stable algorithmic skeleton where some steps vary indirectly through helper classes such as the provider strategy and cache service.

**Quality attributes improved**
- **Maintainability:** request handling remains structured and predictable
- **Readability:** the translation pipeline is easier to follow end-to-end
- **Separation of concerns:** parsing, provider selection, language normalization, and caching are delegated instead of being mixed together

**Trade-offs**
- the orchestration layer becomes a central integration point that must be kept clean
- if too many special cases are added later, the servlet may need further refactoring

### 5. Adapter-Like Normalization Layer

**Where used**
- `TranslationLanguageSupport`

**Why it was used**

Different parts of the system speak slightly different "language formats":
- browser/UI language values
- generic normalized internal language codes
- provider-specific codes such as Sarvam's `en-IN`, `mr-IN`, etc.

`TranslationLanguageSupport` acts as a normalization bridge between these representations.

**Quality attributes improved**
- **Interoperability:** frontend and provider-specific formats are kept compatible
- **Maintainability:** normalization rules are centralized
- **Extensibility:** adding new languages or provider-specific mappings does not require changing the whole flow

**Trade-offs**
- adds another helper layer to the subsystem
- the normalization logic must be kept in sync with provider capabilities

## Why this design is maintainable and extensible

- Provider-specific logic is separated from translation orchestration.
- Caching is implemented in a standalone service instead of being embedded into the servlet.
- Language normalization is centralized instead of repeated in multiple files.
- The browser-side widget and server-side translation backend communicate through a small, stable JSON contract.
- Additional providers, languages, or translation-enabled views can be added with localized changes instead of broad rewrites.

## Verification

### Compile

```powershell
mvn -pl app "-Dmaven.repo.local=.m2/repository" -DskipTests compile
```

### Existing tests

```powershell
mvn -pl app "-Dmaven.repo.local=.m2/repository" test
```

### Run locally

```powershell
mvn "-Dmaven.repo.local=.m2/repository" -DskipTests install
mvn -pl app "-Dmaven.repo.local=.m2/repository" jetty:run
```

Open:

```text
http://localhost:8080/roller
```

### Manual verification checklist

1. Open a public weblog page.
2. Use the translation widget to translate the page.
3. Confirm layout and links remain unchanged while text changes.
4. Switch between `MyMemory` and `Sarvam`.
5. Confirm Marathi appears as a supported language.
6. Refresh the same page and confirm the previous config auto-applies.
7. Check the widget status message for cached section reuse.
8. Edit only one part of the weblog entry, reload, and verify that unchanged sections are reused while changed sections are retranslated.

## Bonus-scope note

The backend caching and translation endpoint are generic, but the current frontend integration is still mounted on selected theme-driven public views rather than every webpage type in Roller. So the current implementation is stronger than a weblog-only prototype, but the "all webpage types in Roller" bonus should be presented carefully and justified honestly.
