# Task 3: Bug Report

## 1. Feature Description

The Bug Report module extends Apache Roller with a complete workflow for submitting, tracking, and managing software issue reports inside the existing application. Regular users can create, view, edit, and delete their own bug reports from the authoring area, while administrators can review all reports from a centralized dashboard, filter them by status, add admin notes, change status, and delete reports when needed.

The feature is designed to fit Roller conventions rather than introduce a parallel subsystem. It reuses the Struts 2 UI flow, JPA-backed manager pattern, Roller runtime/static configuration, i18n resources, Tiles views, and existing security hooks. In addition to CRUD support, the module also includes an extensible notification pipeline so the system can notify admins and reporters through email and can later be extended to Slack, Microsoft Teams, or other channels.

## 2. Implementation

### Backend

The backend is split into four cohesive layers:

1. Domain model
   The `BugReport` entity stores report metadata such as title, description, severity, type, owner, timestamps, status, page URL, repro steps, expected/actual behavior, and admin notes.
2. Data access
   The `BugReportManager` interface and `JPABugReportManagerImpl` provide persistence and query operations using Roller's existing manager style.
3. Workflow/business layer
   `BugReportWorkflowService` validates operations, executes commands, creates events, and triggers notifications.
4. Notification layer
   `BugReportNotificationService`, `BugReportEventPublisher`, channel abstractions, and message strategies handle modular notification dispatch.

Implemented backend behavior:

- `create()` validates the report, sets audit metadata, persists it, and notifies admins.
- `update()` validates ownership-sensitive edits, persists changes, and notifies admins.
- `changeStatus()` validates transitions through command logic, persists the new state, notifies admins, and optionally notifies the reporter.
- `delete()` removes the report and notifies admins; reporter notification is controlled by the caller.
- Admin recipient resolution first checks admin user profiles, then `site.adminemail`, then `mail.username`.
- Notification transport is currently email-based, but transport selection is isolated behind `BugNotificationChannel`.

Status lifecycle implemented:

- `OPEN -> TRIAGED`
- `TRIAGED -> RESOLVED`
- `RESOLVED -> TRIAGED` for reopen

The following architecture diagram summarizes the implemented module structure:

![Bug Report Module Architecture](bug-report-diagram.png)

### Frontend (screenshots)

The frontend is implemented using Struts 2 actions and JSP views under the existing Roller UI.

User-facing screens:

- Bug report listing page for the logged-in user
- Add/edit bug report form
- Delete action for owned reports

Admin-facing screens:

- Bug report admin dashboard
- Status filter view
- Change status action with admin notes
- Delete action with reporter notification support

Suggested screenshot slots for the final report:

1. User bug report list page
   Insert screenshot of `/roller-ui/authoring/bugReports.rol?weblog=<weblogHandle>`
2. User create/edit form
   Insert screenshot of `/roller-ui/authoring/bugReportAdd.rol?weblog=<weblogHandle>`
3. Admin bug report dashboard
   Insert screenshot of `/roller-ui/admin/bugReportsAdmin.rol`
4. Admin status-change flow
   Insert screenshot showing OPEN/TRIAGED/RESOLVED actions and notes field

If you want to keep the file fully report-ready even before adding screenshots, you can replace each slot above with pasted UI captures from your local run.

## 3. Assumptions / Constraints

- A normal user must have authoring access to a weblog with at least `EDIT_DRAFT` permission to access the user-side bug report pages.
- The admin dashboard is restricted to users with global `ADMIN` permission.
- Users can only edit or delete their own reports from the user interface.
- Reporter notifications are only sent for status change and delete events, not for report creation.
- Admin notifications are sent for create, update, delete, and status change events.
- Email delivery depends on valid SMTP configuration in `roller-custom.properties` and, if used, runtime config values such as `site.adminemail`.
- The current implementation includes one notification channel, `email`, but the design supports adding more channels without changing the workflow layer.
- The current status model is intentionally small: `OPEN`, `TRIAGED`, and `RESOLVED`.
- The bug report screens are split between the authoring UI and the admin UI; both flows must be tested separately.

## 4. Files Modified / Added

### Domain and Persistence

- `app/src/main/java/org/apache/roller/weblogger/pojos/BugReport.java`
- `app/src/main/resources/org/apache/roller/weblogger/pojos/BugReport.orm.xml`
- `app/src/main/resources/sql/createdb.vm`
- `app/src/main/resources/META-INF/persistence.xml`

### Business and Workflow

- `app/src/main/java/org/apache/roller/weblogger/business/BugReportManager.java`
- `app/src/main/java/org/apache/roller/weblogger/business/jpa/JPABugReportManagerImpl.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/BugReportCommand.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/CreateBugReportCommand.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/UpdateBugReportCommand.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/DeleteBugReportCommand.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/ChangeStatusBugReportCommand.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/BugReportWorkflowService.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/BugReportServiceFactory.java`

### Notifications

- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/BugReportEvent.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/BugReportEventType.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/BugReportAudience.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/BugReportEventPublisher.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/BugReportNotificationService.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/BugNotificationChannel.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/EmailBugNotificationChannel.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/BugNotificationChannelFactory.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/BugNotificationMessage.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/BugNotificationMessageStrategy.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/AdminBugNotificationMessageStrategy.java`
- `app/src/main/java/org/apache/roller/weblogger/business/bugreports/ReporterStatusNotificationMessageStrategy.java`

### UI and Views

- `app/src/main/java/org/apache/roller/weblogger/ui/struts2/editor/BugReportBean.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/struts2/editor/BugReports.java`
- `app/src/main/java/org/apache/roller/weblogger/ui/struts2/admin/BugReportsAdmin.java`
- `app/src/main/webapp/WEB-INF/jsps/editor/BugReports.jsp`
- `app/src/main/webapp/WEB-INF/jsps/editor/BugReportEdit.jsp`
- `app/src/main/webapp/WEB-INF/jsps/admin/BugReportsAdmin.jsp`

### Framework and Configuration Integration

- `app/src/main/resources/struts.xml`
- `app/src/main/webapp/WEB-INF/tiles.xml`
- `app/src/main/resources/org/apache/roller/weblogger/ui/struts2/editor/editor-menu.xml`
- `app/src/main/resources/org/apache/roller/weblogger/ui/struts2/admin/admin-menu.xml`
- `app/src/main/resources/ApplicationResources.properties`
- `app/src/main/resources/org/apache/roller/weblogger/config/runtimeConfigDefs.xml`
- `app/src/main/java/org/apache/roller/weblogger/business/Weblogger.java`
- `app/src/main/java/org/apache/roller/weblogger/business/WebloggerImpl.java`
- `app/src/main/java/org/apache/roller/weblogger/business/jpa/JPAWebloggerImpl.java`
- `app/src/main/java/org/apache/roller/weblogger/business/jpa/JPAWebloggerModule.java`

### Tests and Documentation

- `app/src/test/java/org/apache/roller/weblogger/pojos/BugReportTest.java`
- `app/src/test/java/org/apache/roller/weblogger/business/BugReportCRUDTest.java`
- `app/src/test/java/org/apache/roller/weblogger/ui/struts2/editor/BugReportBeanTest.java`
- `app/src/test/java/org/apache/roller/weblogger/business/bugreports/BugReportWorkflowServiceNotificationTest.java`
- `app/src/test/java/org/apache/roller/weblogger/business/bugreports/EmailBugNotificationSmtpSmokeTest.java`
- `docs/project2_bug-report-task.md`
- `docs/bug-report-module-class-diagram.puml`

## 5. Design Patterns

- Repository Pattern
  Used through `BugReportManager` and `JPABugReportManagerImpl` to isolate persistence logic from the workflow and UI layers.
- Command Pattern
  Used through `BugReportCommand` and the concrete command classes for create, update, delete, and status change operations.
- Strategy Pattern
  Used through `BugNotificationMessageStrategy` with separate implementations for admin and reporter messaging.
- Observer/Event Pattern
  Used through `BugReportEvent` and `BugReportEventPublisher` so workflow execution can trigger notifications without direct coupling to transport details.
- Factory Pattern
  Used through `BugNotificationChannelFactory` and `BugReportServiceFactory` to centralize object creation.
- Template Method Pattern
  Inherited from Roller's `UIAction` base class, which standardizes request handling, security checks, and action behavior for the UI layer.

## 6. Rationale Behind Pattern

- Repository was chosen because Roller already uses manager interfaces for persistence; this keeps the new feature aligned with the existing codebase and simplifies testing.
- Command was chosen because each mutation has different validation and side effects. Encapsulating them separately makes the workflow easier to test and extend.
- Strategy was chosen because admin notifications and reporter notifications require different message formats and are likely to evolve independently.
- Observer/Event was chosen to separate business operations from notification delivery. A report can be created or updated without the workflow needing to know how many channels exist.
- Factory was chosen to keep channel/service construction in one place and support runtime-driven extensibility.
- Template Method was retained through `UIAction` so the new screens automatically follow Roller conventions for permissions, menus, and lifecycle hooks.

## 7. User Flow

### Normal User Flow

1. User logs into Roller and opens a weblog they can author.
2. User navigates to the authoring bug report page.
3. User creates a new bug report with title, description, type, severity, and optional reproduction details.
4. The system validates required fields and saves the report through `BugReportWorkflowService`.
5. The workflow emits a `CREATED` event and notifies admins.
6. User can later open the same report from the list and edit it.
7. The system saves changes, emits an `UPDATED` event, and again notifies admins.
8. User can delete their own report from the list page.

### Admin Flow

1. Admin logs into Roller and opens the bug report admin dashboard.
2. Admin views all reports or filters by `OPEN`, `TRIAGED`, or `RESOLVED`.
3. Admin selects a report and changes its status.
4. The workflow executes the status-change command, persists the new status, emits a `STATUS_CHANGED` event, and notifies both admins and the original reporter.
5. Admin may add internal notes while changing status.
6. Admin can also delete a report if required.
7. On admin deletion, the workflow emits a `DELETED` event, notifies admins, and can notify the reporter as well.

### Notification Flow

1. A workflow action completes successfully.
2. `BugReportWorkflowService` creates a `BugReportEvent`.
3. `BugReportNotificationService` resolves recipients based on audience.
4. A message strategy builds the subject/body for that audience.
5. `BugReportEventPublisher` dispatches the message to all enabled channels.
6. `EmailBugNotificationChannel` sends the final email via Roller mail utilities.

## 8. UML Class Diagram

![Bug Report Module Class Diagram](bug-report-diagram.png)