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
| `test/.../pojos/BugReportTest.java` | 14 pure unit tests for POJO logic (no DB) |
| `test/.../business/BugReportCRUDTest.java` | 7 integration tests for manager CRUD (uses JPA + Derby) |
| `test/.../editor/BugReportBeanTest.java` | 4 unit tests for bean mapping |
| `test/.../business/bugreports/BugReportWorkflowServiceNotificationTest.java` | Fast workflow tests proving notification hooks for create/update/delete/status-change, including a combined create->status-change case |
| `test/.../business/bugreports/EmailBugNotificationSmtpSmokeTest.java` | Optional real SMTP smoke test through `EmailBugNotificationChannel` (defaults to `naveenhardik12@gmail.com`) |

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

- **Extensibility**: New notification channels (Slack, Teams) can be added by implementing `BugNotificationChannel` plus one factory registration entry, with no changes to existing channels or workflow orchestration.
- **Testability**: Pure domain logic in POJO enables fast unit tests; the command pattern isolates each mutation for targeted integration testing.
- **Security**: All text inputs sanitized via `HTMLSanitizer`; ownership checks prevent users from editing/deleting others' reports; admin actions require `GlobalPermission.ADMIN`.
- **Consistency**: Follows all existing Roller patterns (Manager interface, Guice DI, Struts2 actions, Tiles, i18n, ORM XML).

## 7. Navigation and Manual Testing Help

This section documents the exact screens and direct URLs that should be used when testing the Bug Report module.

### 7.1 Admin Navigation

Use an account with Roller global admin rights.

1. Sign in as the admin user.
2. Open the Roller admin area.
3. Use the **Server Admin** tab.
4. Click **Bug Reports**.

Direct admin URL:

```text
/roller-ui/admin/bugReportsAdmin.rol
```

What to test on the admin page:

1. The bug report list loads successfully.
2. The status filter works for OPEN, TRIAGED, and RESOLVED reports.
3. An OPEN report can be triaged.
4. A TRIAGED report can be resolved.
5. A RESOLVED report can be reopened to TRIAGED.
6. A report can be deleted by admin.

### 7.2 User Navigation

The user-facing Bug Reports screen is in the authoring area, not the admin area. It requires a weblog context and a user who has at least `EDIT_DRAFT` permission on that weblog.

Navigation through the UI:

1. Sign in as a non-admin user, or any user who has authoring access to a weblog.
2. Select a weblog from the main menu if Roller prompts you to choose one.
3. Open the **Weblog** tab in the editor area.
4. Click **Bug Reports**.

Direct user URLs:

```text
/roller-ui/authoring/bugReports.rol?weblog=<weblogHandle>
/roller-ui/authoring/bugReportAdd.rol?weblog=<weblogHandle>
/roller-ui/authoring/bugReportEdit.rol?weblog=<weblogHandle>&bean.id=<bugReportId>
```

What to test on the user pages:

1. The bug report list shows only the current user's reports.
2. Clicking **Submit New Bug Report** opens the add form.
3. Creating a report returns to the list page and shows the new record.
4. Editing an owned report opens the form with existing values.
5. Updating an owned report saves changes correctly.
6. Deleting an owned report removes it from the list.
7. A user cannot edit or delete another user's report.

### 7.3 Important Testing Note

The admin bug report screen and the user bug report screen are different features:

- Admin dashboard: `/roller-ui/admin/bugReportsAdmin.rol`
- User authoring dashboard: `/roller-ui/authoring/bugReports.rol?weblog=<weblogHandle>`

If you test the user feature from an admin account, you still need a valid weblog context in the URL or in the selected authoring session.

## 8. How to Create a Second User for Testing

If you currently only have the admin account, use one of the two flows below.

### 8.1 Recommended: Create the Second User from Admin UI

This is the simplest and most reliable path.

Navigation through the UI:

1. Sign in as admin.
2. Open **Server Admin**.
3. Click **User Admin**.
4. Click the **create a new user** link.

Direct URLs:

```text
/roller-ui/admin/userAdmin.rol
/roller-ui/admin/createUser.rol
```

Fill at least these fields:

1. Username
2. Screen name
3. Full name
4. Email address
5. Password
6. Locale
7. Time zone
8. Keep **Enabled** checked
9. Leave **Administrator** unchecked if you want a normal user for testing

After saving, log out and log in with the second user account.

### 8.2 Alternative: Enable Public Registration

Roller also supports self-registration, but it is disabled by default in runtime config.

To enable it:

1. Sign in as admin.
2. Open **Server Admin**.
3. Open the global configuration page.
4. In the **User Settings** section, enable **Allow New Users?**
5. Save the configuration.

Then use the public registration page:

```text
/roller-ui/register.rol
```

This flow is useful if you want to test registration plus bug reporting as a normal user.

### 8.3 If the New User Cannot See Bug Reports

The Bug Reports menu only appears for users with authoring permission on a weblog.

To make the second user able to test the feature:

1. Log in as the second user.
2. Create a weblog if the system allows user weblog creation.
3. Or add the second user as a member/editor of an existing weblog.

If user weblog creation is enabled, the direct path is:

```text
/roller-ui/createWeblog.rol
```

Relevant runtime settings:

1. `site.allowUserWeblogCreation` should be enabled if you want the user to create their own weblog.
2. `users.registration.enabled` should be enabled only if you want public registration.

### 8.4 Practical Test Setup

For the cleanest manual test cycle, use this setup:

1. `admin` account for triage, resolve, reopen, delete, and runtime config changes.
2. `tester1` non-admin account for creating and updating bug reports.
3. One weblog owned by `tester1`, or one shared weblog where `tester1` has `EDIT_DRAFT` permission.

## 9. End-to-End Manual Test Checklist

1. Create `tester1` from `/roller-ui/admin/createUser.rol`.
2. Log in as `tester1`.
3. Create a weblog for `tester1` if none exists.
4. Open `/roller-ui/authoring/bugReports.rol?weblog=<weblogHandle>`.
5. Create a new report.
6. Edit the same report.
7. Delete another test report if needed.
8. Log back in as admin.
9. Open `/roller-ui/admin/bugReportsAdmin.rol`.
10. Verify the report appears in the admin dashboard.
11. Change status OPEN -> TRIAGED -> RESOLVED -> TRIAGED.
12. Confirm filters and delete action work.

## 10. PlantUML Class Diagram

The PlantUML source for the implemented class diagram is stored in:

`docs/bug-report-module-class-diagram.puml`

You can render it with any PlantUML-compatible viewer.

## 11. Email Notification Setup

Email notifications fire on every bug report create / update / delete / status-change.
Admins receive every notification. Reporters receive a notification when their report's
status is changed or when it is deleted by an admin.

Three separate things must be configured correctly for emails to arrive.
They are in different places, and missing any one of them causes silent failure.

### 11.1 SMTP credentials (roller-custom.properties)

File: `app/src/main/resources/roller-custom.properties`

These are read once at startup from the classpath. A rebuild + redeploy is required
after every change.

#### Option A — MailerSend (recommended, free tier: 3 000 emails/month)

```properties
mail.configurationType=properties
mail.hostname=smtp.mailersend.net
mail.port=587
mail.username=MS_xxxx@trial-xxxx.mlsender.net   # your SMTP username from MailerSend
mail.password=mssp.xxxx                          # your SMTP password from MailerSend
```

#### Option B — Gmail App Password

```properties
mail.configurationType=properties
mail.hostname=smtp.gmail.com
mail.port=587
mail.username=your-address@gmail.com
mail.password=xxxx xxxx xxxx xxxx    # 16-char App Password, NOT your regular password
```

Generate a Gmail App Password at: https://myaccount.google.com/apppasswords
(requires 2-Step Verification to be enabled on the account first).

### 11.2 MailerSend prerequisites (per SMTP guide)

According to MailerSend's SMTP onboarding guide, the required prerequisites are:

1. Your sending domain must be verified/authenticated in MailerSend.
2. SMTP credentials (username/password) must be generated for that domain.
3. The `From:` address must match that verified domain (or an associated subdomain).
4. Encryption should be TLS/STARTTLS.

If SMTP auth succeeds but mail still does not arrive, check these MailerSend areas:

1. **Activity**: confirm if the message is delivered, hard-bounced, soft-bounced, or rejected.
2. **Suppressions**: ensure recipient is not unsubscribed/suppressed.
3. **Domain status**: ensure the sender domain remains verified.

### 11.3 Admin email address (Roller admin UI)

The notification service resolves admin recipients by looking up all users with the
global ADMIN role in the Roller database and collecting their email addresses.
If the admin's profile email is blank, no recipients are found.

Steps to set the admin's email:

1. Log in as admin.
2. Go to **My Profile** (top-right menu or `/roller-ui/profile.rol`).
3. Enter a real email address in the **Email** field.
4. Save.

The notification service also has two fallback layers if the profile email is blank:

- **Fallback 1**: `site.adminemail` from the Roller runtime config database.
    Set this via Admin → **Global Config** → **Site Settings** → **Admin Email**.
- **Fallback 2**: `mail.username` from `roller-custom.properties`.
    This means if both the profile and `site.adminemail` are blank, the SMTP sender
    address itself is used as the recipient — useful for local dev.

### 11.4 Site admin email (Roller admin UI — for the From: address)

The `From:` address on notification emails is sourced from:

1. `site.adminemail` runtime config (DB) — set via Admin → **Global Config** → **Site Settings**.
2. Fallback: `mail.username` from `roller-custom.properties`.
3. Final fallback: `noreply@localhost` (will be rejected by any real SMTP server).

For MailerSend: the `From:` address **must** be the SMTP username
(`MS_xxxx@trial-xxxx.mlsender.net`), which is exactly what `mail.username` contains.
Because of the fallback added in code, this works automatically without touching the
admin UI — as long as `mail.username` is set in `roller-custom.properties`.

### 11.5 Verifying email is working

After rebuilding and redeploying, check the server log (Tomcat `catalina.out` or
the console when running `mvn jetty:run`). The notification service now logs:

```
INFO  [...] BugReportNotificationService - Resolved admin notification recipients: [you@example.com]
INFO  [...] EmailBugNotificationChannel  - Bug notification email: from=MS_xxxx@..., recipients=[you@example.com], event=CREATED
```

If you see `recipients: []` or the WARN about no recipients, the admin profile email
is blank — fix it via step 11.3 above.

If the INFO lines appear but no email arrives, inspect MailerSend Activity/Suppressions
as described in step 11.2.

### 11.6 Full checklist

| Step | Where | What to do |
|------|-------|-----------|
| 1 | `roller-custom.properties` | Set `mail.hostname`, `mail.port`, `mail.username`, `mail.password` |
| 2 | MailerSend dashboard | Verify domain + check Activity/Suppressions for delivery status |
| 3 | Roller admin UI — My Profile | Set a real email on the admin account |
| 4 | Roller admin UI — Global Config | Set Site Admin Email (optional but recommended) |
| 5 | Terminal | `mvn clean install -pl app -DskipTests` then redeploy the WAR |
| 6 | Server log | Confirm INFO lines appear when a bug report is submitted |

### 11.7 Quick test command (no manual user creation)

To quickly verify that bug-report workflow actions trigger notifications,
run this focused unit test:

```bash
mvn -pl app -Dtest=BugReportWorkflowServiceNotificationTest test
```

What this test verifies in seconds:

1. `create()` notifies admins.
2. `update()` notifies admins.
3. `changeStatus()` notifies both admins and reporter.
4. `delete(..., true)` notifies both admins and reporter.
5. `delete(..., false)` notifies only admins.
6. `create()` then `changeStatus()` in one flow notifies admins twice and reporter once.

This test does not contact MailerSend directly. It verifies notification logic and event wiring.
For real SMTP delivery verification, run one manual action and check the server logs + MailerSend Activity.

### 11.8 Real SMTP smoke test (single command)

If you want one command that attempts an actual email delivery through the configured SMTP provider
(MailerSend or Gmail), use this opt-in smoke test:

```bash
mvn -pl app -Dtest=EmailBugNotificationSmtpSmokeTest \
    -Dbugreport.smtp.smoke=true -Dbugreport.smtp.to=your-recipient@example.com test
```

The test currently defaults to `naveenhardik12@gmail.com` if `bugreport.smtp.to` is not provided.

Notes:

1. The test is disabled by default, so it does not run in normal `mvn test`.
2. It sends exactly one message through `EmailBugNotificationChannel`.
3. It validates SMTP path and auth by failing the test on transport/auth errors.
4. It does not prove inbox placement (spam/quarantine), so also check provider Activity logs.

### 11.9 Normal workflow uses the same email logic

The production UI actions and the SMTP smoke test share the same notification path.

Production flow mapping:

1. User/admin UI actions call `BugReportWorkflowService` (not direct manager save/delete).
2. `BugReportWorkflowService` emits `BugReportEvent` and calls `BugReportNotificationService`.
3. `BugReportNotificationService` resolves recipients and calls `BugReportEventPublisher`.
4. `BugReportEventPublisher` dispatches to `EmailBugNotificationChannel`.
5. `EmailBugNotificationChannel` sends via `MailUtil.sendTextMessage(...)`.

Therefore, when `EmailBugNotificationSmtpSmokeTest` succeeds, it validates the same
final email channel used by normal bug-report operations in the application.

## 12. Requirement Coverage: Modular Notification Channels

This section explicitly answers the assignment requirement:

> "...the system should be modular enough to support adding new notification channels
> (Slack Webhooks, MS Teams, etc.) without impact to remaining channels."

### 12.1 Why the current design satisfies this

1. Channel abstraction exists via `BugNotificationChannel` interface.
2. Dispatch is centralized in `BugReportEventPublisher`, which iterates over channels and isolates failures per channel.
3. Channel selection is runtime-configured (`bugreport.notification.channels`) through `BugNotificationChannelFactory`.
4. Message formatting is separated from transport through `BugNotificationMessageStrategy`, so transport additions do not require message-generation rewrites.

### 12.2 What must change to add a new channel (minimal impact)

To add Slack/Teams, only these localized changes are needed:

1. Add a class implementing `BugNotificationChannel` (e.g., `SlackBugNotificationChannel`).
2. Add one registration branch in `BugNotificationChannelFactory` for the new channel id.
3. Add the channel id to runtime config property `bugreport.notification.channels`.

No changes are required in:

1. `BugReportWorkflowService`
2. Command classes (`Create/Update/Delete/ChangeStatus`)
3. Struts UI actions (`BugReports`, `BugReportsAdmin`)
4. Existing channel implementations (email remains unaffected)

This keeps extension cost and blast radius low while preserving current behavior.
