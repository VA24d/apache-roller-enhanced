# Project 2 Report — Team 33

## Table of Contents

1. [Task 1A: Stars](#task-1a-stars)
2. [Task 1B: Trending Blogs](#task-1b-trending-blogs)
3. [Task 2: Transforming Feeds](#task-2-transforming-feeds)
4. [Design Patterns Summary](#design-patterns-summary)
5. [UML Diagrams](#uml-diagrams)
6. [Testing](#testing)

---

## Task 1A: Stars

### Overview

Users can "star" (favourite) both weblogs and individual blog entries. Starred content is accessible from dedicated pages in the user's navigation.

### Requirements Addressed

- Users can visit blog pages and star them
- From their home page, users can view all starred weblogs, sorted by when the most recent blog post was made (most recently updated at top)
- Date and time of last post is displayed
- Users can also star individual blog posts
- Starred entries page uses pagination (not all in a single page)
- Hyperlinks in the home page navigate to starred content

### Design Patterns Used

#### 1. Data Access Object (DAO) Pattern

**Where:** `StarManager` (interface) and `JPAStarManagerImpl` (JPA implementation)

**Rationale:** The DAO pattern separates the persistence logic from the business logic. `StarManager` defines a clean interface with methods like `saveWeblogStar()`, `getStarredWeblogs()`, and `getTrendingWeblogs()`. The JPA implementation (`JPAStarManagerImpl`) encapsulates all database interactions using named JPQL queries.

**Quality attribute improvement:**
- *Maintainability* — Switching from JPA to another persistence framework requires only writing a new implementation of `StarManager`, no changes to actions or UI.
- *Testability* — Actions can be tested against the interface using mocks.

**Trade-off:** Adds an extra layer of abstraction. For a small feature this could be over-engineering, but it is consistent with Roller's existing architecture (e.g., `WeblogManager`, `UserManager`).

#### 2. Dependency Injection (via Guice)

**Where:** `JPAStarManagerImpl` is annotated with `@Singleton` and `@Inject`, bound in `JPAWebloggerModule`, and injected into `WebloggerImpl`.

**Rationale:** Follows Roller's existing DI pattern. The `StarManager` is wired into the `Weblogger` facade so any component in the system can access it via `WebloggerFactory.getWeblogger().getStarManager()`.

**Quality attribute improvement:**
- *Extensibility* — New managers can be added to the DI container without modifying existing code.
- *Loose coupling* — Components depend on the `StarManager` interface, not the concrete implementation.

**Trade-off:** Requires understanding of Guice to add new bindings.

### Changes to Existing Code

| File | Change |
|------|--------|
| `Weblogger.java` (interface) | Added `getStarManager()` method |
| `WebloggerImpl.java` | Added `starManager` field with constructor injection |
| `JPAWebloggerModule.java` | Added Guice binding: `StarManager` → `JPAStarManagerImpl` |
| `UtilitiesModel.java` | Added `isWeblogStarred(weblogId)` and `isEntryStarred(entryId)` methods for Velocity templates |
| `struts.xml` | Added action mappings for `starWeblog`, `starEntry`, `starredWeblogs`, `starredEntries`, `trending` |
| `tiles.xml` | Added tile definitions for `.StarredWeblogs`, `.StarredEntries`, `.Trending` |
| `ApplicationResources.properties` | Added 15 i18n keys for star/trending UI text |
| `createdb.vm` | Added `user_weblog_star` and `user_entry_star` tables with indexes and foreign keys |
| Theme templates (4 themes) | Added star/unstar buttons on sidebar (weblogs) and permalink (entries) |

### New Files Created

| File | Purpose |
|------|---------|
| `pojos/UserWeblogStar.java` | JPA entity — user-to-weblog star with id, user, weblog, starredTime |
| `pojos/UserEntryStar.java` | JPA entity — user-to-entry star with id, user, entry, starredTime |
| `pojos/UserWeblogStar.orm.xml` | ORM mapping with 6 named JPQL queries |
| `pojos/UserEntryStar.orm.xml` | ORM mapping with 6 named JPQL queries |
| `business/StarManager.java` | Interface defining 14 star operations |
| `business/jpa/JPAStarManagerImpl.java` | JPA implementation with `@Singleton`/`@Inject` |
| `ui/struts2/core/StarWeblogAction.java` | Star/unstar weblog (prevents self-starring) |
| `ui/struts2/core/StarEntryAction.java` | Star/unstar entry (redirects back to permalink) |
| `ui/struts2/core/StarredWeblogsAction.java` | Lists user's starred weblogs |
| `ui/struts2/core/StarredEntriesAction.java` | Lists user's starred entries with pagination |
| `ui/struts2/core/TrendingAction.java` | Top 5 trending weblogs and entries |
| `ui/struts2/pagers/StarredEntriesPager.java` | Pagination support for starred entries list |
| `WEB-INF/jsps/core/StarredWeblogs.jsp` | JSP view for starred weblogs |
| `WEB-INF/jsps/core/StarredEntries.jsp` | JSP view for starred entries |
| `WEB-INF/jsps/core/Trending.jsp` | JSP view for trending content |

### Key Implementation Details

- **Sorted by most recent blog post:** The query `UserWeblogStar.getStarredWeblogsByUser` orders by `s.weblog.lastModified DESC` (not by when the user starred it), as required by the spec.
- **Pagination:** Starred entries use `StarredEntriesPager` with configurable page size (default 20). JPQL `setFirstResult()`/`setMaxResults()` handles DB-level pagination.
- **Idempotent starring:** Before creating a star, the system checks if one already exists. Starring twice does not create duplicates.
- **Self-star prevention:** `StarWeblogAction` checks if the authenticated user owns the weblog and prevents self-starring.
- **Star buttons across all themes:** Added to `basic`, `basicmobile`, `fauxcoly`, and `gaurav` themes using Velocity templates with `$utils.isWeblogStarred()` / `$utils.isEntryStarred()` checks.

---

## Task 1B: Trending Blogs

### Overview

Displays the top 5 trending weblogs and blog entries based on the number of stars (user favourites), using efficient aggregate queries.

### Requirements Addressed

- Top 5 trending blogposts and blog pages based on number of stars
- Does NOT iterate over individual articles — uses optimized aggregate queries
- Efficient data fetching

### Design Pattern: Aggregate Query Optimization (via Named Queries)

**Where:** `UserWeblogStar.orm.xml` and `UserEntryStar.orm.xml`

**Rationale:** The trending queries use `GROUP BY` + `COUNT` + `ORDER BY` in JPQL to compute star counts in a single database roundtrip, rather than loading all stars into memory and counting in Java.

**Trending weblogs query:**
```sql
SELECT s.weblog, COUNT(s) AS starCount
FROM UserWeblogStar s
GROUP BY s.weblog
ORDER BY starCount DESC
```

**Trending entries query:**
```sql
SELECT s.entry, COUNT(s) AS starCount
FROM UserEntryStar s
GROUP BY s.entry
ORDER BY starCount DESC
```

**Quality attribute improvement:**
- *Performance* — O(1) database queries regardless of the number of stars; the DB engine handles aggregation natively.
- *Scalability* — Works efficiently even with millions of star records.

**Trade-off:** The queries return `Object[]` arrays (entity + count), which requires index-based access in the JSP/action layer rather than typed getters.

---

## Task 2: Transforming Feeds

### Overview

An admin-side entry processing pipeline that runs when blog entries are saved. It applies configurable processing steps (profanity filter, content summarization, auto tag generation) to entry content before persistence.

### Requirements Addressed

- Posted blogs go through at least 3 processing steps
- Steps include profanity filter, text summarization, and tag generation
- Steps can be added/removed without affecting the remaining process
- Admin-side processing (not user-side)
- Tags added to the body of the blog (no separate storage needed)

### Design Patterns Used

#### 3. Chain of Responsibility Pattern

**Where:** `EntryProcessingStep` (interface), `EntryProcessingPipeline` (orchestrator), and the three step implementations.

**Rationale:** The Chain of Responsibility pattern allows multiple processing steps to be applied sequentially to a blog entry, where each step can modify the entry independently. The pipeline maintains an ordered list of steps and executes each one, passing the same `WeblogEntry` object through. Steps do not know about each other — they only depend on the `EntryProcessingStep` interface.

**How it enables add/remove without breaking:**
- `addStep(EntryProcessingStep)` — appends a new step to the pipeline
- `removeStep(String stepName)` — removes a step by name
- Each step is self-contained; adding or removing one has zero impact on others

**Quality attribute improvement:**
- *Extensibility* — New steps are added by implementing `EntryProcessingStep` and registering in the factory. Zero changes to existing steps or the pipeline orchestrator.
- *Maintainability* — Each step is a separate class with a single responsibility.
- *Fault tolerance* — Each step is wrapped in try-catch; one step's failure doesn't break the pipeline.

**Trade-off:** Steps execute sequentially (no parallelism). Order matters — e.g., the profanity filter should run before the auto-tag generator so that profane words don't become tags.

#### 4. Factory Pattern

**Where:** `EntryProcessingPipelineFactory`

**Rationale:** The factory reads configuration from `roller.properties` and conditionally constructs a pipeline with only the enabled steps. This centralizes creation logic and keeps `EntryEdit` clean — it only calls `EntryProcessingPipelineFactory.createPipeline()` without knowing which steps are active.

**Quality attribute improvement:**
- *Configurability* — Steps are toggled via properties without code changes.
- *Single Responsibility* — Pipeline construction logic is isolated from both the pipeline execution and the Struts action.

**Trade-off:** Adding a new step requires modifying the factory (to add the conditional creation block). This could be further improved with reflection/service loading but would add unnecessary complexity.

#### 5. Strategy Pattern (implicit)

**Where:** Each `EntryProcessingStep` implementation encapsulates a different processing algorithm behind the same interface.

**Rationale:** `ProfanityFilterStep`, `ContentSummarizerStep`, and `AutoTagGeneratorStep` all implement `EntryProcessingStep` but use completely different algorithms:
- Profanity filter: regex-based word boundary matching
- Content summarizer: HTML-aware word counting and truncation
- Auto tag generator: TF-based keyword extraction with stop word filtering

**Quality attribute improvement:**
- *Interchangeability* — Any step can be swapped for a different implementation (e.g., an LLM-based profanity detector) without changing the pipeline.
- *Open/Closed Principle* — The pipeline is open for extension (new steps) but closed for modification.

### Comparison with Existing Plugin System

Roller already has a `WeblogEntryPlugin` interface with plugins like `ConvertLineBreaksPlugin`, `SmileysPlugin`, etc. Our pipeline is **intentionally separate** from this system:

| Aspect | Existing Plugins | New Pipeline |
|--------|-----------------|--------------|
| **When** | Render time (page view) | Save time (entry creation/edit) |
| **Scope** | Text rendering only | Title, text, and summary |
| **Control** | User-side (per-weblog) | Admin-side (global config) |
| **Persistence** | Transforms are not saved | Transforms are persisted |
| **Error handling** | Plugin failure breaks rendering | Step failure is isolated |

This design follows the hint in the assignment: *"the controls mentioned in this feature are admin-side, NOT user-side"*.

### Changes to Existing Code

| File | Change |
|------|--------|
| `EntryEdit.java` | Added 3 lines in `save()`: create pipeline from factory and execute on entry (line 214-218) |
| `roller.properties` | Added 5 pipeline configuration properties |

### New Files Created

| File | Purpose |
|------|---------|
| `business/pipeline/EntryProcessingStep.java` | Interface: `getName()`, `getDescription()`, `process(WeblogEntry)` |
| `business/pipeline/EntryProcessingPipeline.java` | Orchestrator: manages step list, executes sequentially with error isolation |
| `business/pipeline/ProfanityFilterStep.java` | Replaces profane words with asterisks using precompiled regex patterns |
| `business/pipeline/ContentSummarizerStep.java` | Truncates text exceeding configurable word limit, HTML-aware |
| `business/pipeline/AutoTagGeneratorStep.java` | Extracts top-N keywords by frequency, appends as tags to body |
| `business/pipeline/EntryProcessingPipelineFactory.java` | Static factory reading enabled steps from `roller.properties` |

### Key Implementation Details

#### Profanity Filter
- Uses precompiled `Pattern` objects with `\b` (word boundary) matching and `CASE_INSENSITIVE` flag
- Replaces matched words with asterisks of equal length (e.g., "damn" → "****")
- Processes title, text, and summary fields independently
- Whole-word matching prevents false positives (e.g., "hell" in "shell" is NOT filtered)

#### Content Summarizer
- HTML-aware: strips tags for word counting but preserves HTML structure in output
- Configurable word limit (default 500, set via `pipeline.step.contentSummarizer.maxWords`)
- Appends " [...]" when truncation occurs
- Only modifies text if it actually exceeds the word limit

#### Auto Tag Generator
- Strips HTML, tokenizes text, filters stop words and short words (< 4 chars)
- Counts word frequency and picks top-N most frequent words
- Appends tags as `<p class="auto-tags"><em>Tags: #keyword1 #keyword2 ...</em></p>`
- Configurable max tags (default 5, set via `pipeline.step.autoTagGenerator.maxTags`)

### Configuration

In `roller.properties`:
```properties
# Profanity Filter
pipeline.step.profanityFilter.enabled=true

# Content Summarizer
pipeline.step.contentSummarizer.enabled=true
pipeline.step.contentSummarizer.maxWords=500

# Auto Tag Generator
pipeline.step.autoTagGenerator.enabled=true
pipeline.step.autoTagGenerator.maxTags=5
```

To disable any step, set its `enabled` property to `false`. No code changes or recompilation needed — just restart the application.

### How to Add a New Step

1. Create a class implementing `EntryProcessingStep`
2. Add a conditional block in `EntryProcessingPipelineFactory.createPipeline()`
3. Add an `enabled` property in `roller.properties`

No existing steps or the pipeline orchestrator need to be modified.

---

## Design Patterns Summary

| # | Pattern | Location | Task | Justification |
|---|---------|----------|------|---------------|
| 1 | DAO | `StarManager` / `JPAStarManagerImpl` | 1A/1B | Separates persistence from business logic; consistent with Roller's architecture |
| 2 | Dependency Injection | Guice bindings for `StarManager` | 1A/1B | Loose coupling; interface-based dependency; singleton lifecycle management |
| 3 | Chain of Responsibility | `EntryProcessingStep` / `EntryProcessingPipeline` | 2 | Sequential processing with independent, add/removable steps |
| 4 | Factory | `EntryProcessingPipelineFactory` | 2 | Centralizes pipeline construction; reads config to conditionally enable steps |
| 5 | Strategy | Each `EntryProcessingStep` implementation | 2 | Different algorithms behind the same interface; interchangeable steps |

---

## UML Diagrams

### Task 1A/1B — User Highlights

**Before** — No star/favourite mechanism; only `WeblogHitCount` for page hits:

![Task 1 Before](task1_stars_before.png)

**After** — Full starring system with `StarManager`, POJOs, actions, pagination, and trending:

![Task 1 After](task1_stars_after.png)

### Task 2 — Transforming Feeds

**Before** — Only render-time `WeblogEntryPlugin` system; no save-time processing:

![Task 2 Before](task2_pipeline_before.png)

**After** — New admin-side `EntryProcessingPipeline` with Chain of Responsibility pattern:

![Task 2 After](task2_pipeline_after.png)

---

## Testing

### Task 1 — Unit Tests

| Test Class | Tests | Coverage |
|-----------|-------|----------|
| `UserWeblogStarTest` | POJO getters/setters, equals/hashCode, UUID generation | pojos |
| `UserEntryStarTest` | POJO getters/setters, equals/hashCode, UUID generation | pojos |
| `StarManagerTest` | Full CRUD, duplicate prevention, trending queries, star counts | business layer |

### Task 2 — Unit Tests (31 total, all passing)

| Test Class | # Tests | What's Tested |
|-----------|---------|---------------|
| `ProfanityFilterStepTest` | 8 | Word boundary matching, case insensitivity, title/text/summary filtering, null handling |
| `ContentSummarizerStepTest` | 8 | Truncation, exact limits, HTML preservation, null/empty text |
| `AutoTagGeneratorStepTest` | 9 | Tag extraction, stop word exclusion, HTML stripping, max tags limit, null/blank text |
| `EntryProcessingPipelineTest` | 6 | Add/remove steps, execution order, null entry, empty pipeline, unmodifiable list, full integration |

Run all Task 2 tests:
```bash
mvn test -pl app -Dtest="org.apache.roller.weblogger.business.pipeline.*"
```

---

## How to Use

### Feature 1: User Highlights (Stars & Trending)

**Star a weblog:**
1. Log in to Apache Roller
2. Visit any blog page
3. Click "Star this blog" in the sidebar
4. Button changes to "Unstar this blog"

**Star a blog entry:**
1. Navigate to any individual blog post
2. Click "Star this post" below the entry
3. You are redirected back to the same entry

**View starred content:**
- Starred Weblogs: `/roller-ui/starredWeblogs`
- Starred Entries: `/roller-ui/starredEntries` (paginated)

**View trending content:**
- Trending: `/roller-ui/trending` (top 5 weblogs and entries by star count)

### Feature 2: Transforming Feeds Pipeline

The pipeline runs automatically when any blog entry is saved. No user action is required.

**Admin configuration** (in `roller.properties`):
- Enable/disable each step independently
- Adjust word limit for summarizer and max tags for tag generator
- Changes take effect after application restart

**Effects visible in published posts:**
- Profane words replaced with asterisks
- Long posts truncated with "[...]" suffix
- Auto-generated tags appended at the bottom
