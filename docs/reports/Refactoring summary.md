# Refactoring Summary — `MetaWeblogAPIHandler.java`

**File:** `app/src/main/java/org/apache/roller/weblogger/webservices/xmlrpc/MetaWeblogAPIHandler.java`  
**Date:** February 11, 2026  
**Scope:** MetaWeblog XML-RPC handler and surrounding xmlrpc package

---

## 1. New Files Created

| File | Purpose |
|------|---------|
| `PostRequestAdapter.java` | DTO / adapter that wraps the raw XML-RPC `Hashtable` struct with type-safe getters (`getTitle()`, `getDescription()`, `getDateCreated()`, `getCategories()`). Eliminates magic-string lookups and unsafe casts scattered across handler methods. |
| `PostResponseBuilder.java` | Builds XML-RPC response structs (`Map`) from domain objects (`WeblogEntry`, `WeblogCategory`). Extracted from the private `createPostStruct()` and `createCategoryStruct()` methods that were previously inlined in the handler. |
| `NewPostRequest.java` | Parameter object that encapsulates the 8 arguments previously passed to `populateEntryFromRequest()` (title, description, website, user, dateCreated, updateTime, publish). Replaces a long parameter list with a single cohesive object. |
| `MediaUploadRequest.java` | Parameter object for binary media uploads. Wraps the `name`, `type`, and `bits` fields from the XML-RPC struct with typed access and input sanitization (path separator replacement). Includes a `fromStruct()` factory method. |

All new files are in the same package:  
`org.apache.roller.weblogger.webservices.xmlrpc`

---

## 2. Refactorings Applied

### 2.1 Dependency Injection (Testability)

- **Before:** Every method called `WebloggerFactory.getWeblogger()` inline (static coupling).
- **After:** `Weblogger` is injected via a new constructor `MetaWeblogAPIHandler(Weblogger)`. A backward-compatible no-arg constructor still exists for the XML-RPC framework, delegating to the static factory internally.
- **Benefit:** The handler can now be unit-tested by injecting a mock `Weblogger` instance without PowerMock or container startup.

### 2.2 Parameter Objects (Replace Long Parameter Lists)

- **Before:** `populateEntryFromRequest()` accepted 8 separate parameters; `newMediaObject()` unpacked `Hashtable` fields inline with raw string keys.
- **After:** `NewPostRequest` encapsulates entry-creation parameters; `MediaUploadRequest` encapsulates media upload fields.
- **Benefit:** Method signatures are cleaner, related data travels together, and each parameter object is independently testable.

### 2.3 Extract Method (Decompose Large Methods)

The monolithic `editPost()` and `newPost()` methods were broken into focused, single-responsibility helpers:

| Extracted Method | Responsibility |
|------------------|----------------|
| `lookupEntryOrThrow(postid)` | Centralised entry lookup with null-check (was inconsistent across methods) |
| `resolveNewPostTitle(request)` | Title resolution with auto-truncation fallback |
| `resolveNewPostDate(request)` | Date resolution with "default to now" logic |
| `applyEditFields(entry, request, publish)` | Applies edit-specific fields to an existing entry |
| `populateEntry(entry, NewPostRequest)` | Populates a new entry from the parameter object |
| `saveAndFlush(entry)` | Persistence + cache invalidation in one place |
| `saveMediaFile(website, MediaUploadRequest)` | Full media upload workflow extracted from `newMediaObject()` |
| `resolveFirstValidCategory(...)` | Iterates category names to find the first valid one |
| `resolveAndSetCategory(...)` | Sets category with fallback to default |

### 2.4 Extract String Literals into Constants

- **`PostRequestAdapter`** — 6 constants: `FIELD_TITLE`, `FIELD_DESCRIPTION`, `FIELD_DATE_CREATED`, `FIELD_PUB_DATE`, `FIELD_CATEGORIES`, `FIELD_LINK`
- **`PostResponseBuilder`** — 15 constants: `RESP_TITLE`, `RESP_LINK`, `RESP_DESCRIPTION`, `RESP_PUB_DATE`, `RESP_DATE_CREATED`, `RESP_GUID`, `RESP_PERMA_LINK`, `RESP_POST_ID`, `RESP_USER_ID`, `RESP_AUTHOR`, `RESP_CATEGORIES`, `RESP_HTML_URL`, `RESP_RSS_URL`, `FEED_TYPE_ENTRIES`, `FEED_FORMAT_RSS`
- **`MediaUploadRequest`** — 3 constants: `FIELD_NAME`, `FIELD_TYPE`, `FIELD_BITS`
- **`MetaWeblogAPIHandler`** — `TITLE_TRUNCATE_MAX`, `TITLE_TRUNCATE_SUFFIX`, `RESP_URL`

### 2.5 Replace Magic Numbers with Named Constants

| Before | After |
|--------|-------|
| `Utilities.truncateNicely(description, 15, 15, "...")` | `Utilities.truncateNicely(description, TITLE_TRUNCATE_MAX, TITLE_TRUNCATE_MAX, TITLE_TRUNCATE_SUFFIX)` |

### 2.6 Replace Generic Exceptions with Specific Types

- **Before:** All catch blocks used `catch (Exception e)`.
- **After:** Catch blocks now use `catch (WebloggerException e)` — the actual declared exception type of every manager method in the codebase. `saveMediaFile()` additionally catches `java.io.IOException` for stream failures. `XmlRpcException` is re-thrown directly instead of being re-wrapped.
- **Benefit:** Finer-grained error handling; unexpected `RuntimeException`s are no longer silently swallowed and re-wrapped.

### 2.7 Remove `Thread.sleep` (Performance)

- **Before:** Both `editPost()` and `newPost()` ended with `Thread.sleep(RollerConstants.SEC_IN_MS)` — an artificial 1-second delay per request to work around second-precision timestamps.
- **After:** Removed entirely. Timestamp uniqueness is a persistence-layer concern (ID generation / DB schema), not a handler concern.
- **Benefit:** Throughput is no longer artificially capped to 1 request/second/thread.

### 2.8 Replace Legacy Collections

- **Before:** `Hashtable` and `Vector` used internally (synchronized, legacy).
- **After:** Internal code uses `HashMap` and `ArrayList`. Boundary method signatures still accept `Hashtable` for XML-RPC framework compatibility.

### 2.9 Null-Safety Guards

- **Before:** `editPost()` called `entry.getWebsite().getHandle()` without null-checking `entry`, risking `NullPointerException`.
- **After:** All entry-lookup paths go through `lookupEntryOrThrow()` which throws a proper `XmlRpcException(INVALID_POSTID)` if the entry is not found.

### 2.10 Resource Leak Fix

- **Before:** `newMediaObject()` created a `ByteArrayInputStream` that was never closed.
- **After:** Wrapped in try-with-resources (`try (InputStream inputStream = ...)`).

### 2.11 Improved Error Specificity in Media Upload

- **Before:** Upload validation errors were thrown as `new Exception(errors.toString())`, then re-wrapped.
- **After:** Thrown directly as `new XmlRpcException(UPLOAD_DENIED_EXCEPTION, errors.toString())`.

---

## 3. Files Modified

| File | Changes |
|------|---------|
| `MetaWeblogAPIHandler.java` | Full rewrite applying all refactorings listed above (556 lines) |

## 4. Files Created

### Production Sources

| File | Lines |
|------|-------|
| `PostRequestAdapter.java` | 95 |
| `PostResponseBuilder.java` | 119 |
| `NewPostRequest.java` | 83 |
| `MediaUploadRequest.java` | 71 |

### Unit Tests

| File | Lines | Test Count |
|------|-------|------------|
| `PostRequestAdapterTest.java` | 151 | 13 |
| `PostResponseBuilderTest.java` | 149 | 3 |
| `MediaUploadRequestTest.java` | 77 | 5 |
| `NewPostRequestTest.java` | 66 | 2 |

All test files are in:  
`app/src/test/java/org/apache/roller/weblogger/webservices/xmlrpc/`

## 5. Files Not Modified

The following files in the same package were **not** touched as they are outside the scope of this refactoring:

- `BaseAPIHandler.java` — contains commented-out dead code and static coupling, but is the shared base for all XML-RPC handlers
- `BloggerAPIHandler.java` — parent class with similar patterns; a separate refactoring pass is recommended

---

## 6. Test Coverage

**Framework:** JUnit Jupiter 5 + Mockito 5.15.2  
**Total tests:** 23 &nbsp;|&nbsp; **Failures:** 0 &nbsp;|&nbsp; **Errors:** 0  
**Build command:**
```
mvn test -pl app -am \
  -Dtest=org.apache.roller.weblogger.webservices.xmlrpc.*Test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

### What is tested

| Class Under Test | Scenarios |
|------------------|----------|
| `PostRequestAdapter` | Title extraction (present / missing), description (present / null), `dateCreated` fallback to `pubDate`, categories (array / empty / missing), `getFirstCategory`, null-struct rejection |
| `PostResponseBuilder` | Full post-struct creation (all fields populated), null-link / null-pubTime / null-category omission, category struct with mocked `URLStrategy`. Uses `Mockito.mockStatic` for `WebloggerRuntimeConfig.getAbsoluteContextURL()` |
| `MediaUploadRequest` | `fromStruct()` field extraction, slash sanitization in filenames, null-name and null-bits rejection (`IllegalArgumentException`), null `contentType` tolerance |
| `NewPostRequest` | Constructor / getter round-trip, publish-flag true vs false |

---

## 7. Design Principles Applied

| Principle | How Applied |
|-----------|-------------|
| **Single Responsibility (SRP)** | Handler orchestrates; `PostRequestAdapter` parses; `PostResponseBuilder` serialises; parameter objects carry data |
| **Open/Closed** | New behaviour (e.g. additional struct fields) can be added to adapter/builder without modifying the handler |
| **Dependency Inversion** | Handler depends on `Weblogger` interface, not `WebloggerFactory` static |
| **DRY** | Shared entry-population logic extracted; string constants centralised |
| **Law of Demeter** | `saveAndFlush()` encapsulates the `manager → save → flush → cache` chain |
