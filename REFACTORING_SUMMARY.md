# Refactoring Summary: WeblogEntry.java

## Overview
This document summarizes the refactoring changes applied to `WeblogEntry.java` and the supporting classes extracted from it to improve code quality, maintainability, testability, and modernize the Java implementation.

The refactoring follows Martin Fowler's catalog and targets four axes of improvement:
1. **Separation of Concerns** — Extract presentation logic and value objects
2. **Dependency Inversion** — Replace Service Locator calls with injectable parameters
3. **Strengthening Encapsulation** — Protect mutable collections, remove dead setters
4. **Identity & Modernization** — Fix equals/hashCode, replace legacy APIs

---

## 1. Refactored File: `WeblogEntry.java`

We performed a "Rich Domain Model" refactoring on the `WeblogEntry` class. We kept business logic inside the class but cleaned it up significantly, extracted presentation/view concerns, and introduced value objects.

### Key Changes & Justifications

#### Phase 1 — Separation of Concerns

*   **Extracted `WeblogEntryPresenter` (Extract Class)**
    *   **What we did:** Moved all presentation/view methods out of `WeblogEntry` into a new `WeblogEntryPresenter` class. The affected methods include `formatPubTime`, `formatUpdateTime`, `getDisplayTitle`, `getRss09xDescription`, `getTransformedText`, `getTransformedSummary`, `displayContent`, `getDisplayContent`, and the private `render` helper. The original methods in `WeblogEntry` are now `@Deprecated` bridges that delegate to `new WeblogEntryPresenter(this)`. A convenience method `presenter()` was added.
    *   **Why:** A domain entity should not contain HTML rendering, date formatting, or RSS serialization logic. This violates the Single Responsibility Principle. Extracting to a separate presenter class means the entity focuses on data/rules while the presenter focuses on display.

*   **Removed No-Op "Form Bean" Setters (Remove Dead Code)**
    *   **What we did:** Deleted `setPermalink()`, `setPermaLink()`, `setDisplayTitle()`, `setRss09xDescription()`, and `setCommentsStillAllowed()`. These were empty setter methods with no backing field — they existed only to satisfy a form-binding framework convention.
    *   **Why:** Empty setters pollute the API, confuse maintainers, and imply mutability that doesn't exist. Their purpose was silently discarding data from form submissions, coupling the domain entity to the web framework.

#### Phase 2 — Dependency Inversion (Replace Service Locator with Injection)

*   **Isolated All `WebloggerFactory` Dependencies**
    *   **What we did:** For every method that called `WebloggerFactory.getWeblogger()` (the Service Locator), we:
        1. Marked the original method `@Deprecated` with `// LEGACY ADAPTER` comment
        2. Created a new DI-friendly overload that accepts the dependency as a parameter
    *   **Methods refactored:**
        | Original (Deprecated) | DI Overload | Injected Dependency |
        |:---|:---|:---|
        | `getCreator()` | `setCreator(User)` + fallback | `User` |
        | `hasWritePermissions(User)` | `hasWritePermissions(User, UserManager)` | `UserManager` |
        | `getCommentsStillAllowed()` | `getCommentsStillAllowed(boolean)` | site-wide flag |
        | `getPermalink()` | `getPermalink(URLStrategy)` | `URLStrategy` |
        | `getComments(...)` | `getComments(WeblogEntryManager, boolean)` | `WeblogEntryManager` |
        | `createAnchor()` | `createAnchor(WeblogEntryManager)` | `WeblogEntryManager` |
    *   **Why:** Service Locators make testing hard because the class secretly reaches out to global state. By allowing dependencies to be passed in, we can easily pass mock objects during testing without bootstrapping the entire container.

*   **Injectable Title Separator**
    *   **What we did:** Replaced the static call `WebloggerConfig.getProperty("title.separator")` with an injectable `transient char titleSeparator` field (default `'-'`), settable via `setTitleSeparator(char)`.
    *   **Why:** Eliminates hidden coupling to the static configuration singleton and makes `createAnchorBase()` unit-testable without an application context.

#### Phase 3 — Strengthening Encapsulation

*   **Encapsulated Entry Attributes Collection (Encapsulate Collection)**
    *   **What we did:** Changed `getEntryAttributes()` to return `Collections.unmodifiableSet(attSet)`. Added `putEntryAttribute(name, value)` and `removeEntryAttribute(name)` as the sole mutation points.
    *   **Why:** Exposing a mutable `Set` lets any caller silently add/remove attributes bypassing validation. The entity should own its invariants. The `setEntryAttributes()` mutator is retained for JPA/ORM framework use only.

*   **Extracted `PluginConfiguration` Value Object (Replace Primitive with Object)**
    *   **What we did:** Created `PluginConfiguration.java` — an immutable value object that wraps the raw comma-delimited `plugins` string. Added `getPluginConfiguration()` to `WeblogEntry` and deprecated `getPluginsList()`.
    *   **Why:** The raw `String plugins` field was parsed via `StringUtils.split()` in multiple places (entry, presenter, rendering). A value object centralizes parsing, is immutable, and provides structured access (`containsPlugin()`, `getPluginNames()`, `isEmpty()`).

*   **Extracted `TagManifest` Value Object (Encapsulate Collection)**
    *   **What we did:** Created `TagManifest.java` to encapsulate all tag manipulation logic — add, remove, diff tracking (`addedTags`, `removedTags`), string↔set conversion. `WeblogEntry` delegates `addTag()`, `setTagsAsString()`, `getTagsAsString()`, `getAddedTags()`, `getRemovedTags()` to it. A private `syncTagsFromManifest()` method keeps the raw JPA-visible sets in sync.
    *   **Why:** Tag management was scattered inline in `WeblogEntry` with complex mutable state (three separate `Set` fields, locale-aware lowercasing, diff tracking). Extracting to a dedicated class makes the logic testable in isolation and gives `WeblogEntry` a single method call instead of 30+ lines of inline manipulation.

#### Phase 4 — Identity Stabilization & Modernization

*   **Fixed `equals()` / `hashCode()` (Replace Mutable Identity)**
    *   **What we did:** Changed `equals()` and `hashCode()` to use the immutable `id` field via `Objects.equals()` / `Objects.hashCode()`. Previously they used `EqualsBuilder`/`HashCodeBuilder` on the mutable `anchor` + `website` fields.
    *   **Why:** Using mutable fields in identity methods means an entry's hash can change while it sits in a `HashSet` or `HashMap`, causing ghost entries and lookup failures. The `id` is assigned at construction (via `UUIDGenerator`) and never changes, making it a safe identity key.

*   **Implemented Builder Pattern**
    *   **What we did:** Added a static inner `Builder` class.
    *   **Why:** The constructor for `WeblogEntry` took **11 arguments**! This is a "Long Parameter List" code smell. It's easy to mix up arguments (like passing `title` where `text` should go). The builder lets us write readable code like `.title("My Post").status(PUBLISHED).build()`.

*   **Removed Legacy `StringTokenizer`**
    *   **What we did:** Replaced the old `StringTokenizer` class with modern `String.split()` in the `createAnchorBase` method.
    *   **Why:** `StringTokenizer` is a "legacy" class (from Java 1.0!) that is discouraged in modern Java. Using `split()` and loops is clearer and more standard.

*   **Modern Date/Time API (`java.time`)**
    *   **What we did:** Replaced `java.util.Calendar` with `java.time.Instant` and `ChronoUnit` in the `getCommentsStillAllowed` method.
    *   **Why:** The old `Calendar` API is notoriously difficult to work with (months start at 0, not thread-safe). The new API (introduced in Java 8) is cleaner, safer, and makes date math (like "is today before expiration date?") much easier to read.

#### Phase 5 — Code Smells & Integrity Fixes

*   **Serialization Fix (`WeblogEntryAttribute`)**
    *   **What we did:** Made `WeblogEntryAttribute` implement `Serializable` and added `serialVersionUID`.
    *   **Why:** `WeblogEntry` is `Serializable` but contained a collection of `WeblogEntryAttribute` objects. Since the attribute class wasn't serializable, this caused serialization failures (Critical Code Smell).

*   **Renamed Confusing Method (`getPermaLink` → `getRelativePermalink`)**
    *   **What we did:** Renamed the legacy method `getPermaLink()` to `getRelativePermalink()` across the entity, wrappers, and templates.
    *   **Why:** The distinction between `getPermalink()` (absolute) and `getPermaLink()` (relative) relied solely on the capitalization of the letter 'L', which is a "Blocker" severity code smell due to high confusion risk.

*   **Cleaned Exception Handling & Constructor**
    *   **What we did:** Replaced generic `catch (Exception)` with specific `catch (WebloggerException)` and removed the unused `id` parameter from the main constructor.
    *   **Why:** Generic catches mask runtime errors, and unused parameters add noise. Both improvements increase maintainability.

---

## 2. Created File: `PluginConfiguration.java`

**Path:** `app/src/main/java/org/apache/roller/weblogger/pojos/PluginConfiguration.java`

An immutable value object that wraps the comma-delimited plugin string.

**Key API:**
| Method | Description |
|:---|:---|
| `PluginConfiguration(String)` | Parse from raw comma-delimited string |
| `of(List<String>)` | Factory from explicit list |
| `EMPTY` | Singleton for no-plugins case |
| `getPluginNames()` | Returns `List<String>` of plugin names |
| `containsPlugin(String)` | Check if a specific plugin is enabled |
| `isEmpty()` | True if no plugins configured |
| `getRaw()` | Returns original raw string |

---

## 3. Created File: `TagManifest.java`

**Path:** `app/src/main/java/org/apache/roller/weblogger/pojos/TagManifest.java`

Encapsulates tag management with add/remove tracking for JPA persistence synchronization.

**Key API:**
| Method | Description |
|:---|:---|
| `addTag(name, creator, weblog, entry, locale, timestamp)` | Add a single tag (locale-aware lowercasing) |
| `setTagsFromString(tags, ...)` | Parse space-delimited tag string, compute added/removed diffs |
| `toSortedString()` | Returns tags as sorted space-delimited string |
| `getTags()` | Returns unmodifiable view of current tags |
| `getAddedTags()` / `getRemovedTags()` | Diff sets for persistence layer sync |

---

## 4. Created File: `WeblogEntryPresenter.java`

**Path:** `app/src/main/java/org/apache/roller/weblogger/pojos/WeblogEntryPresenter.java`

Extracted view/presentation logic from `WeblogEntry`. All HTML rendering, date formatting, RSS description generation, and plugin-based text transformation live here.

**Key API:**
| Method | Description |
|:---|:---|
| `formatPubTime(pattern)` | Format publish time with timezone |
| `formatUpdateTime(pattern)` | Format update time with timezone |
| `getDisplayTitle()` | Title or first 255 chars of text |
| `getRss09xDescription(int)` | Escaped HTML for RSS 0.9x feeds |
| `getTransformedText()` | Entry text after plugin transformation |
| `getTransformedSummary()` | Entry summary after plugin transformation |
| `displayContent(readMoreLink)` | Right display content based on summary/text availability |
| `render(String)` | Internal: apply configured plugins to content |

---

## 5. Created File: `WeblogEntryTest.java`

**Path:** `app/src/test/java/org/apache/roller/weblogger/pojos/WeblogEntryTest.java`

### Why did we create this?
The existing tests were integration tests that required a database and the entire application to be running (which is slow). We needed **Unit Tests** that run instantly and verify our specific logic changes isolated from the rest of the system.

### What does it test?
*   **Builder Pattern:** Verifies fluent builder constructs entries correctly.
*   **Anchor Generation:** Proves that titles like "My Cool Post" correctly become "my-cool-post", with 5-word truncation and text fallback.
*   **Comment Expiration:** Proves that the new `java.time` logic correctly allows or blocks comments based on the number of days, site-wide setting, website setting, and entry-level setting.
*   **Permissions:** Proves the DI-friendly `hasWritePermissions(User, UserManager)` works for Admins, Authors, and Limited users (draft vs. published) using Mockito mocks — no real database required.

---

## 6. ORM Compatibility

**File:** `app/src/main/resources/org/apache/roller/weblogger/pojos/WeblogEntry.orm.xml`

The JPA mapping uses `access="PROPERTY"` and `metadata-complete="true"`, meaning all persistence is driven by getter/setter names in the XML — not by annotations or field names. All refactoring changes were verified compatible:
- No persisted property getter/setter signatures were changed
- New transient fields (`creator`, `titleSeparator`, `tagManifest`) are not mapped and correctly invisible to JPA
- `setEntryAttributes()` retained for ORM framework write-back
- `setTags()` retained with `tagManifest` sync on load

---

## Summary Table

| File | Status | Description |
|:---|:---|:---|
| `app/.../pojos/WeblogEntry.java` | **Refactored** | Fixed identity, encapsulated collections, removed dead setters, extracted presentation/tags/plugins, added DI overloads, modernized APIs, added Builder, fixed code smells. |
| `app/.../pojos/WeblogEntryAttribute.java`| **Refactored** | Added `Serializable` interface to fix serialization critical smell. |
| `app/.../pojos/PluginConfiguration.java` | **Created** | Immutable value object replacing raw plugin string (Primitive Obsession fix). |
| `app/.../pojos/TagManifest.java` | **Created** | Encapsulated tag management with add/remove diff tracking. |
| `app/.../pojos/WeblogEntryPresenter.java` | **Created** | Extracted all view/presentation logic from WeblogEntry (SRP). |
| `app/.../pojos/WeblogEntryTest.java` | **Created** | Unit tests for Builder, anchors, comment expiration, permissions (Mockito-based). |
| `app/.../pojos/WeblogEntry.orm.xml` | **Verified** | No changes needed — all refactoring is ORM-compatible. |

---

## Refactoring Techniques Applied

| Technique | Where Applied |
|:---|:---|
| **Extract Class** | `WeblogEntryPresenter`, `TagManifest`, `PluginConfiguration` |
| **Replace Primitive with Object** | `PluginConfiguration` for raw plugin string |
| **Encapsulate Collection** | `getEntryAttributes()` → unmodifiable; `TagManifest` |
| **Remove Dead Code** | No-op form bean setters removed |
| **Replace Query with Parameter** (DI) | All `WebloggerFactory` call sites |
| **Replace Constructor with Builder** | `WeblogEntry.Builder` inner class |
| **Modernize API** | `StringTokenizer` → `String.split()`, `Calendar` → `java.time` |
| **Fix Mutable Identity** | `equals()`/`hashCode()` now use immutable `id` |
