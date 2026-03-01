# Feature 1: User Highlights — Design Document

**Feature**: User Highlights (Star & Trending)  
**Branch**: `project-2-feat-1-user-highlights`  
**Date**: 2026-03-01  
**Status**: Implementation

---

## 1. Feature Requirements

### 1A — Stars (Favourite)

Users can:
- Visit blog pages (weblogs) and **star** (favourite) them
- Visit individual blog posts (entries) and **star** them
- View all their starred **weblogs** from the home page, sorted by most recent blog post time (recently updated weblogs at the top), with the date/time of the last post displayed
- View all their starred **blog entries** from the home page via a dedicated page, with **pagination** (not all displayed on one page)
- **Unstar** previously starred weblogs and entries

### 1B — Trending Blogs

- Display the **top 5 trending blog posts** and **blog pages** (weblogs) based on the number of users who have starred them
- **Efficiency requirement**: Must NOT iterate over individual articles — use aggregate SQL queries (`GROUP BY` + `COUNT`) for optimal performance
- Trending data must be fetched using optimized database queries (`SELECT ... GROUP BY ... ORDER BY COUNT(*)`)

---

## 2. Entity Design

### 2.1 New Entities

Two new JPA entities are introduced. Using two separate tables (rather than a single polymorphic table) enables:
- Proper foreign key constraints to different target tables (`weblog` vs `weblogentry`)
- Cleaner JPQL named queries per type
- Efficient per-type aggregation for trending calculations

#### UserWeblogStar

| Field | Type | DB Column | Constraints |
|-------|------|-----------|-------------|
| id | String (varchar 48) | id | PK, generated UUID |
| user | User (many-to-one) | userid | FK → roller_user(id), NOT NULL |
| weblog | Weblog (many-to-one) | weblogid | FK → weblog(id), NOT NULL |
| starredTime | Timestamp | starred_time | NOT NULL |

**Unique constraint**: (userid, weblogid) — a user can star a weblog at most once.

#### UserEntryStar

| Field | Type | DB Column | Constraints |
|-------|------|-----------|-------------|
| id | String (varchar 48) | id | PK, generated UUID |
| user | User (many-to-one) | userid | FK → roller_user(id), NOT NULL |
| entry | WeblogEntry (many-to-one) | entryid | FK → weblogentry(id), NOT NULL |
| starredTime | Timestamp | starred_time | NOT NULL |

**Unique constraint**: (userid, entryid) — a user can star an entry at most once.

### 2.2 ER Diagram

```
roller_user (User)
    ├──< user_weblog_star (UserWeblogStar) >──┤ weblog (Weblog)
    └──< user_entry_star  (UserEntryStar)  >──┤ weblogentry (WeblogEntry)
```

Both star tables form a many-to-many relationship between User and Weblog/WeblogEntry, with additional metadata (starredTime).

---

## 3. Database Schema

### 3.1 New Tables

```sql
create table user_weblog_star (
    id            varchar(48) not null primary key,
    userid        varchar(48) not null,
    weblogid      varchar(48) not null,
    starred_time  timestamp   not null
);
create unique index uws_user_weblog_idx on user_weblog_star(userid, weblogid);
create index uws_weblogid_idx on user_weblog_star(weblogid);

alter table user_weblog_star add constraint uws_userid_fk
    foreign key (userid) references roller_user(id);
alter table user_weblog_star add constraint uws_weblogid_fk
    foreign key (weblogid) references weblog(id);

create table user_entry_star (
    id            varchar(48) not null primary key,
    userid        varchar(48) not null,
    entryid       varchar(48) not null,
    starred_time  timestamp   not null
);
create unique index ues_user_entry_idx on user_entry_star(userid, entryid);
create index ues_entryid_idx on user_entry_star(entryid);

alter table user_entry_star add constraint ues_userid_fk
    foreign key (userid) references roller_user(id);
alter table user_entry_star add constraint ues_entryid_fk
    foreign key (entryid) references weblogentry(id);
```

### 3.2 Index Strategy

- **Unique composite index** on (userid, weblogid) / (userid, entryid) — enforces idempotency and speeds up lookup/toggle operations
- **Index on target FK** (weblogid / entryid) — speeds up `GROUP BY` aggregation for trending queries

---

## 4. Service Layer API

### StarManager Interface

New service interface `org.apache.roller.weblogger.business.StarManager`:

| Method | Description |
|--------|-------------|
| `void saveWeblogStar(UserWeblogStar star)` | Persist a weblog star |
| `void removeWeblogStar(UserWeblogStar star)` | Remove a weblog star |
| `UserWeblogStar getWeblogStar(String id)` | Look up star by ID |
| `UserWeblogStar getWeblogStarByUserAndWeblog(User user, Weblog weblog)` | Look up existing star |
| `List<UserWeblogStar> getStarredWeblogs(User user, int offset, int length)` | User's starred weblogs sorted by most recent entry pubTime |
| `void saveEntryStar(UserEntryStar star)` | Persist an entry star |
| `void removeEntryStar(UserEntryStar star)` | Remove an entry star |
| `UserEntryStar getEntryStar(String id)` | Look up star by ID |
| `UserEntryStar getEntryStarByUserAndEntry(User user, WeblogEntry entry)` | Look up existing star |
| `List<UserEntryStar> getStarredEntries(User user, int offset, int length)` | User's starred entries, paginated, newest star first |
| `List<Object[]> getTrendingWeblogs(int limit)` | Top N weblogs by star count: returns [Weblog, starCount] |
| `List<Object[]> getTrendingEntries(int limit)` | Top N entries by star count: returns [WeblogEntry, starCount] |
| `long getWeblogStarCount(Weblog weblog)` | Count of stars for a weblog |
| `long getEntryStarCount(WeblogEntry entry)` | Count of stars for an entry |
| `void release()` | Release resources |

### Trending Query Strategy (Efficiency)

The trending queries use aggregate JPQL with `GROUP BY` and `COUNT`, avoiding any iteration over individual articles:

```sql
-- Trending weblogs (top N by star count)
SELECT s.weblog, COUNT(s) AS cnt
FROM UserWeblogStar s
GROUP BY s.weblog
ORDER BY cnt DESC

-- Trending entries (top N by star count)
SELECT s.entry, COUNT(s) AS cnt
FROM UserEntryStar s
GROUP BY s.entry
ORDER BY cnt DESC
```

These queries are executed as single SQL statements by the database engine, using index scans on the FK columns.

---

## 5. Dependency Injection

- New Guice binding: `StarManager → JPAStarManagerImpl`
- Added to `Weblogger` interface: `StarManager getStarManager()`
- Added to `WebloggerImpl` constructor chain → `JPAWebloggerImpl`

---

## 6. Web Layer

### 6.1 New Struts Actions

| Action Class | URL Mapping | Methods | Purpose |
|-------------|-------------|---------|---------|
| `StarredWeblogsAction` | `/roller-ui/starredWeblogs` | `execute()` | List starred weblogs sorted by recent post |
| `StarredEntriesAction` | `/roller-ui/starredEntries` | `execute()` | Paginated list of starred entries |
| `TrendingAction` | `/roller-ui/trending` | `execute()` | Top 5 trending weblogs and entries |
| `StarWeblogAction` | `/roller-ui/starWeblog` | `star()`, `unstar()` | Toggle star on a weblog |
| `StarEntryAction` | `/roller-ui/starEntry` | `star()`, `unstar()` | Toggle star on an entry |

### 6.2 New JSP Views

| JSP | Description |
|-----|-------------|
| `StarredWeblogs.jsp` | Table of starred weblogs: name (linked), last post date, unstar button |
| `StarredEntries.jsp` | Paginated starred entries: title, weblog, date, unstar, prev/next |
| `Trending.jsp` | Top 5 trending weblogs and entries with star counts |

### 6.3 Navigation

Links added to `MainMenu.jsp` and `MainMenuSidebar.jsp`:
- "My Starred Weblogs" → `/roller-ui/starredWeblogs`
- "My Starred Entries" → `/roller-ui/starredEntries`
- "Trending" → `/roller-ui/trending`

### 6.4 Pagination

`StarredEntriesPager` follows the existing `EntriesPager` pattern:
- Fetch `COUNT + 1` items; if result > COUNT, hasMore = true
- Pager provides `getNextLink()` / `getPrevLink()` with `bean.page` parameter
- Default page size: 30 entries

---

## 7. Before / After UML

See:
- `docs/user_highlights_before.puml` — subsystem before the feature
- `docs/user_highlights_after.puml` — subsystem after the feature (new classes highlighted)

---

## 8. Testing Strategy

### Integration Tests (`StarManagerTest.java`)

| Test | Verifies |
|------|----------|
| `testWeblogStarCRUD` | Create, read, delete weblog star; idempotency |
| `testEntryStarCRUD` | Create, read, delete entry star; idempotency |
| `testStarredWeblogsOrdering` | Starred weblogs sorted by most recent entry pubTime |
| `testStarredEntriesPagination` | Paginated retrieval with offset/limit |
| `testTrendingWeblogs` | Top N by aggregate count, correct ordering |
| `testTrendingEntries` | Top N by aggregate count, correct ordering |
| `testStarCounts` | `getWeblogStarCount` / `getEntryStarCount` accuracy |

### POJO Unit Tests

| Test | Verifies |
|------|----------|
| `UserWeblogStarTest` | Constructor, getters/setters, equals, hashCode |
| `UserEntryStarTest` | Constructor, getters/setters, equals, hashCode |
