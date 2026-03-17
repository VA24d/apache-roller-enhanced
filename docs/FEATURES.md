# Apache Roller Weblogger - New Features Documentation

## Table of Contents
1. [Feature 1: User Highlights (Stars & Trending)](#feature-1-user-highlights-stars--trending)
2. [Feature 2: Transforming Feeds Pipeline](#feature-2-transforming-feeds-pipeline)

---

## Feature 1: User Highlights (Stars & Trending)

### Overview
User Highlights allows authenticated users to "star" (favourite) weblogs and individual blog entries. The system also tracks trending content based on star counts.

### Components

**Data Model:**
- `UserWeblogStar` — stores a user-to-weblog star relationship with timestamp
- `UserEntryStar` — stores a user-to-entry star relationship with timestamp

**Business Layer:**
- `StarManager` (interface) — defines operations for starring/unstarring and querying stars
- `JPAStarManagerImpl` — JPA implementation using named queries

**UI Actions (Struts2):**
- `StarWeblogAction` — handles starring/unstarring a weblog
- `StarEntryAction` — handles starring/unstarring an entry (redirects back to entry permalink)
- `StarredWeblogsAction` — displays user's starred weblogs
- `StarredEntriesAction` — displays user's starred entries with pagination
- `TrendingAction` — displays top 5 trending weblogs and entries by star count

### How to Use

#### Starring a Weblog
1. Log in to Apache Roller
2. Navigate to any blog's page
3. Click the **"Star this blog"** button in the sidebar
4. The button changes to **"Unstar this blog"** — click again to remove the star

The star button appears on all themes: basic, basicmobile, fauxcoly, and gaurav.

#### Starring a Blog Entry
1. Log in to Apache Roller
2. Navigate to any individual blog post (permalink page)
3. Click the **"Star this post"** button below the entry
4. The button changes to **"Unstar this post"** — click again to remove the star

After starring/unstarring, you are redirected back to the same entry.

#### Viewing Your Starred Content
- **Starred Weblogs:** Navigate to `/roller-ui/starredWeblogs` — shows all weblogs you have starred, sorted by most recently updated blog first
- **Starred Entries:** Navigate to `/roller-ui/starredEntries` — shows all entries you have starred, with pagination (20 per page)

#### Viewing Trending Content
Navigate to `/roller-ui/trending` to see:
- **Top 5 Trending Weblogs** — weblogs with the most stars
- **Top 5 Trending Entries** — entries with the most stars

Each trending item shows its star count.

### Database Tables
Two tables are created automatically:
- `user_weblog_star` (id, userid, weblogid, starred_time)
- `user_entry_star` (id, userid, entryid, starred_time)

---

## Feature 2: Transforming Feeds Pipeline

### Overview
The Transforming Feeds Pipeline processes blog entries through a configurable sequence of steps when an entry is saved. It uses the Chain of Responsibility design pattern, allowing steps to be added or removed without modifying existing code.

### Components

**Core Interface:**
- `EntryProcessingStep` — interface that all pipeline steps implement, with `getName()`, `getDescription()`, and `process(WeblogEntry)` methods

**Pipeline Orchestrator:**
- `EntryProcessingPipeline` — manages an ordered list of steps and executes them sequentially on a `WeblogEntry`. Supports `addStep()`, `removeStep(String)`, and `execute(WeblogEntry)`

**Built-in Steps:**

| Step | Class | Description |
|------|-------|-------------|
| Profanity Filter | `ProfanityFilterStep` | Replaces profane words with asterisks in title, text, and summary |
| Content Summarizer | `ContentSummarizerStep` | Truncates text exceeding a word limit, appending "[...]" |
| Auto Tag Generator | `AutoTagGeneratorStep` | Extracts top keywords from text and appends them as auto-generated tags |

**Factory:**
- `EntryProcessingPipelineFactory` — creates a pipeline configured from `roller.properties`

### How It Works

When a user saves a blog entry (draft, publish, or update) through the EntryEdit action, the pipeline executes automatically:

1. The entry text passes through the **Profanity Filter** — offensive words are replaced with asterisks (e.g., "damn" becomes "****")
2. The entry text passes through the **Content Summarizer** — if the text exceeds the configured word limit, it is truncated with a "[...]" suffix
3. The entry text passes through the **Auto Tag Generator** — the most frequent meaningful words are extracted and appended as tags at the bottom of the entry

Each step operates independently and handles errors gracefully — if one step fails, the pipeline continues with the remaining steps.

### Configuration

All pipeline settings are in `app/src/main/resources/org/apache/roller/weblogger/config/roller.properties`:

```properties
# --- Entry Processing Pipeline ---

# Profanity Filter: replaces offensive words with asterisks
pipeline.step.profanityFilter.enabled=true

# Content Summarizer: truncates entries exceeding word limit
pipeline.step.contentSummarizer.enabled=true
pipeline.step.contentSummarizer.maxWords=500

# Auto Tag Generator: extracts top keywords as tags
pipeline.step.autoTagGenerator.enabled=true
pipeline.step.autoTagGenerator.maxTags=5
```

#### Disabling a Step
Set the step's `enabled` property to `false`:
```properties
pipeline.step.profanityFilter.enabled=false
```

#### Adjusting Parameters
- **Content Summarizer word limit:** Change `pipeline.step.contentSummarizer.maxWords` (default: 500)
- **Auto Tag Generator max tags:** Change `pipeline.step.autoTagGenerator.maxTags` (default: 5)

### Adding a Custom Step

To add a new processing step:

1. Create a class implementing `EntryProcessingStep`:

```java
package org.apache.roller.weblogger.business.pipeline;

import org.apache.roller.weblogger.pojos.WeblogEntry;

public class MyCustomStep implements EntryProcessingStep {

    @Override
    public String getName() {
        return "MyCustomStep";
    }

    @Override
    public String getDescription() {
        return "Description of what this step does";
    }

    @Override
    public void process(WeblogEntry entry) {
        if (entry == null || entry.getText() == null) return;
        // Your processing logic here
        entry.setText(processedText);
    }
}
```

2. Register the step in `EntryProcessingPipelineFactory.createPipeline()`:

```java
if ("true".equals(WebloggerConfig.getProperty("pipeline.step.myCustom.enabled"))) {
    pipeline.addStep(new MyCustomStep());
}
```

3. Add configuration to `roller.properties`:

```properties
pipeline.step.myCustom.enabled=true
```

### Removing a Step at Runtime

The pipeline supports removing steps by name:

```java
EntryProcessingPipeline pipeline = EntryProcessingPipelineFactory.createPipeline();
pipeline.removeStep("ProfanityFilter");  // removes by step name
pipeline.execute(entry);
```

### Design Decisions
- **Pipeline pattern:** Steps execute in order, each modifying the entry in place. This allows composable, reusable transformations.
- **Error isolation:** Each step is wrapped in try-catch so one failing step doesn't break the entire pipeline.
- **Admin-side processing:** The pipeline runs when entries are saved (in `EntryEdit`), not at render time. This means content is transformed once at save time rather than on every page view.
- **Independent from existing plugins:** The pipeline is separate from Roller's existing `WeblogEntryPlugin` system (which operates at render time). Both systems can coexist.

### Testing
31 unit tests cover the pipeline:
- `ProfanityFilterStepTest` — 8 tests (word boundary matching, case insensitivity, title/text/summary filtering, null handling)
- `ContentSummarizerStepTest` — 8 tests (truncation, exact limits, HTML handling, null/empty text)
- `AutoTagGeneratorStepTest` — 9 tests (tag extraction, stop word exclusion, HTML stripping, max tags limit, null/blank text)
- `EntryProcessingPipelineTest` — 6 tests (add/remove steps, execution order, null entry, empty pipeline, unmodifiable steps list, full pipeline integration)

Run tests with:
```bash
mvn test -pl app -Dtest="org.apache.roller.weblogger.business.pipeline.*"
```
