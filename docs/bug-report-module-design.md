# Bug Report Module — Design Document

## 1. Overview

The Bug Report module enables users of the Apache Roller weblogger to submit, view, edit, and delete bug reports. Administrators can triage, resolve, reopen, and delete reports from a centralized dashboard. The module follows all existing Roller conventions and patterns.

## 2. Architecture

### 2.1 Layer Diagram

```
┌─────────────────────────────────────────────────────┐
│                   UI Layer (Struts 2)                │
│  BugReports.java (user)  │  BugReportsAdmin.java    │
│  BugReportBean.java      │                          │
│  BugReports.jsp           │  BugReportsAdmin.jsp     │
│  BugReportEdit.jsp        │                          │
├─────────────────────────────────────────────────────┤
│              Business / Service Layer                │
│  BugReportWorkflowService (Command pattern)          │
│  BugReportNotificationService                        │
│  BugReportEventPublisher                             │
├─────────────────────────────────────────────────────┤
│              Data Access Layer                       │
│  BugReportManager (interface)                        │
│  JPABugReportManagerImpl (JPA / ORM XML)             │
├─────────────────────────────────────────────────────┤
│              Domain Model                            │
│  BugReport (POJO entity)                             │
│  Enums: ReportType, Severity, Status                 │
└─────────────────────────────────────────────────────┘
```

### 2.2 Key Design Patterns

| Pattern | Where Used | Rationale |
|---------|-----------|-----------|
| **Repository** | `BugReportManager` / `JPABugReportManagerImpl` | Follows existing Roller Manager pattern for data access |
| **Command** | `CreateBugReportCommand`, `UpdateBugReportCommand`, `DeleteBugReportCommand`, `ChangeStatusBugReportCommand` | Encapsulates each mutation as a validated, testable unit |
| **Strategy** | `BugNotificationMessageStrategy` → `AdminBugNotificationMessageStrategy`, `ReporterStatusNotificationMessageStrategy` | Different message formats for different audiences |
| **Observer / Event** | `BugReportEvent`, `BugReportEventPublisher` | Decouples mutation logic from notification dispatch |
| **Factory** | `BugNotificationChannelFactory`, `BugReportServiceFactory` | Creates notification channels from runtime config; creates workflow service from static context |
| **Template Method** | `UIAction` base class | All actions inherit standard security, menu, and session handling |

### 2.3 Notification Architecture

```
BugReportWorkflowService
   └── executes Command
   └── creates BugReportEvent
   └── BugReportNotificationService
         ├── resolves recipients (admins / reporter)
         ├── selects MessageStrategy
         └── BugReportEventPublisher
               └── BugNotificationChannelFactory
                     └── EmailBugNotificationChannel (MailUtil)
                     └── (future: SlackChannel, TeamsChannel)
```

The channel list is configurable at runtime via `bugreport.notification.channels` in the admin config page. Currently only `email` is implemented; adding Slack/Teams only requires implementing `BugNotificationChannel` and registering it in the factory.

## 3. Files Created

### 3.1 Domain Model
| File | Purpose |
|------|---------|
| `pojos/BugReport.java` | Entity POJO with enums, status transition validation, sanitization |
| `pojos/BugReport.orm.xml` | JPA ORM mapping (table, columns, named queries) |

### 3.2 Data Access
| File | Purpose |
|------|---------|
| `business/BugReportManager.java` | Repository interface |
| `business/jpa/JPABugReportManagerImpl.java` | JPA implementation using JPAPersistenceStrategy |

### 3.3 Business / Notification (19 files in `business/bugreports/`)
| File | Purpose |
|------|---------|
| `BugReportCommand.java` | Command interface |
| `CreateBugReportCommand.java` | Creates and saves a new bug report |
| `UpdateBugReportCommand.java` | Updates an existing bug report |
| `DeleteBugReportCommand.java` | Removes a bug report |
| `ChangeStatusBugReportCommand.java` | Validates and changes status |
| `BugReportWorkflowService.java` | Orchestrates command execution + notification |
| `BugReportServiceFactory.java` | Static factory for workflow service |
| `BugReportEvent.java` | Immutable event payload |
| `BugReportEventType.java` | Enum: CREATED, UPDATED, DELETED, STATUS_CHANGED |
| `BugReportAudience.java` | Enum: ADMINS, REPORTER |
| `BugReportEventPublisher.java` | Fans out events to channels |
| `BugReportNotificationService.java` | Resolves recipients, selects strategy, dispatches |
| `BugNotificationChannel.java` | Channel interface |
| `EmailBugNotificationChannel.java` | Email channel (uses MailUtil) |
| `BugNotificationChannelFactory.java` | Reads runtime config, creates channels |
| `BugNotificationMessage.java` | Canonical message (subject + body) |
| `BugNotificationMessageStrategy.java` | Strategy interface |
| `AdminBugNotificationMessageStrategy.java` | Formats messages for admins |
| `ReporterStatusNotificationMessageStrategy.java` | Formats messages for reporters |

### 3.4 UI Layer
| File | Purpose |
|------|---------|
| `ui/struts2/editor/BugReportBean.java` | Form bean (copyFrom/copyTo) |
| `ui/struts2/editor/BugReports.java` | User action: list, add, edit, delete own reports |
| `ui/struts2/admin/BugReportsAdmin.java` | Admin action: list all, filter, change status, delete |
| `jsps/editor/BugReports.jsp` | User list view |
| `jsps/editor/BugReportEdit.jsp` | User add/edit form |
| `jsps/admin/BugReportsAdmin.jsp` | Admin dashboard with status filters and action buttons |

### 3.5 Configuration
| File | Change |
|------|--------|
| `struts.xml` | Added action mappings for bugReports, bugReportAdd, bugReportEdit, bugReportsAdmin |
| `tiles.xml` | Added tile definitions: .BugReports, .BugReportEdit, .BugReportsAdmin |
| `editor-menu.xml` | Added Bug Reports menu item under Weblog tab |
| `admin-menu.xml` | Added Bug Reports menu item under Server Admin tab |
| `ApplicationResources.properties` | Added 40+ i18n keys for all labels/messages |
| `runtimeConfigDefs.xml` | Added bugReportSettings display-group with notification properties |
| `persistence.xml` | Added BugReport.orm.xml mapping-file |
| `createdb.vm` | Added roller_bugreport table DDL with indexes |
| `Weblogger.java` | Added getBugReportManager() to interface |
| `WebloggerImpl.java` | Added bugReportManager field, constructor, getter |
| `JPAWebloggerImpl.java` | Wired bugReportManager via constructor |
| `JPAWebloggerModule.java` | Added Guice binding |

### 3.6 Tests
| File | Purpose |
|------|---------|
| `test/.../pojos/BugReportTest.java` | 11 pure unit tests for POJO logic (no DB) |
| `test/.../business/BugReportCRUDTest.java` | 7 integration tests for manager CRUD (uses JPA + Derby) |
| `test/.../editor/BugReportBeanTest.java` | 4 unit tests for bean mapping |

## 4. Database Schema

```sql
CREATE TABLE roller_bugreport (
    id              VARCHAR(48)   NOT NULL PRIMARY KEY,
    title           VARCHAR(255)  NOT NULL,
    description     TEXT          NOT NULL,
    report_type     VARCHAR(32)   NOT NULL,
    severity        VARCHAR(16)   NOT NULL,
    status          VARCHAR(16)   NOT NULL,
    page_url        VARCHAR(512),
    steps_to_reproduce TEXT,
    expected_behavior  TEXT,
    actual_behavior    TEXT,
    admin_notes     TEXT,
    reporter_username VARCHAR(255) NOT NULL,
    last_modified_by  VARCHAR(255),
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP     NOT NULL,
    resolved_at     TIMESTAMP
);
-- Indexes for common query patterns
CREATE INDEX idx_bugreport_reporter ON roller_bugreport(reporter_username);
CREATE INDEX idx_bugreport_status   ON roller_bugreport(status);
CREATE INDEX idx_bugreport_updated  ON roller_bugreport(updated_at);
```

## 5. Status State Machine

```
        ┌──────┐
   ┌────│ OPEN │────┐
   │    └──────┘    │
   │        ▲       │
   │        │       ▼
   │    ┌───────┐   │
   │    │TRIAGED│   │
   │    └───────┘   │
   │        │       │
   │        ▼       │
   │   ┌────────┐   │
   └──▶│RESOLVED│◀──┘
       └────────┘
       (RESOLVED → TRIAGED allowed for reopen)
       (RESOLVED → OPEN NOT allowed — must go through TRIAGED)
```

## 6. Quality Attributes

- **Extensibility**: New notification channels (Slack, Teams) can be added by implementing `BugNotificationChannel` and registering in the factory — zero changes to existing code (OCP).
- **Testability**: Pure domain logic in POJO enables fast unit tests; the command pattern isolates each mutation for targeted integration testing.
- **Security**: All text inputs sanitized via `HTMLSanitizer`; ownership checks prevent users from editing/deleting others' reports; admin actions require `GlobalPermission.ADMIN`.
- **Consistency**: Follows all existing Roller patterns (Manager interface, Guice DI, Struts2 actions, Tiles, i18n, ORM XML).
