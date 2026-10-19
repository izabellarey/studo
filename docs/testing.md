# Testing and technical notes

This document describes how STudo was tested, which technical particularities are worth knowing, and which limitations were known at submission.

**Contents**

1. [Test concept](#test-concept)
2. [Automated tests](#automated-tests)
3. [Acceptance tests](#acceptance-tests)
4. [Manual test walk-through](#manual-test-walk-through)
5. [Technical particularities](#technical-particularities)
6. [Known limitations](#known-limitations)

---

## Test concept

Quality assurance rests on three levels, matched to the risks of the project:

| Level | What it covers | How |
|---|---|---|
| Unit tests | Business logic in the backend, logic and view models of the Android app | Automated with Gradle |
| UI tests | Android screens on an emulator | Automated with Gradle against a running emulator |
| Acceptance tests | Every use case of the requirements specification | Documented test cases, executed manually against the seeded demo school, per role |

In addition, the continuous integration pipeline of the original repository ran the backend tests on every push to the deployment branch, and the team kept a runnable backend on the main branch at all times so that the frontend teams could always integrate against a working server.

## Automated tests

```bash
# Backend: all unit tests
cd backend && ./gradlew test

# Android: unit tests
cd Studo-App && ./gradlew test

# Android: UI tests (an emulator or device must be running)
cd Studo-App && ./gradlew connectedAndroidTest
```

- Backend tests live under `backend/src/test/java/de/cau/sp/stundenplanung`.
- Android unit and UI tests live under `Studo-App/app/src/test/java/com/example/ersteversion`.

## Acceptance tests

Every use case has at least one test case with a fixed structure: ID, linked use case, description, preconditions, test data, steps, expected result and postconditions. Test IDs are derived from the use case ID, for example `T-01-MP-02` for the first test case of use case MP-02.

| Test case | Use case | What is checked |
|---|---|---|
| T-01-ML-01 | Show current changes | A teacher or student taps *Changes* and receives the list of current changes |
| T01-MA-01 | Show timetable (web) | A planner navigates from the class pages to a class and sees its timetable in the weekly view |
| T02-MA-01 | Show timetable (app) | A teacher opens the timetable tab in the app and sees the weekly view |
| T-01-MP-01 | Edit master data | A planner edits existing master data, stays on the master data page and sees the changes |
| T-01-MP-02 | Create timetable | A valid timetable exists for every class; conflicts that remain are shown and must be resolved manually |
| T-01-MP-03 | Edit timetable | A change is made to a class timetable in automatic or manual mode and appears in the change history afterwards |
| T-01-MP-04 | Show change history | A planner opens the change history and sees the recorded changes |
| T-01-MP-05 | Check for conflicts | After creating or editing a timetable, either the changes are saved without conflict, or a list of conflicts is shown |
| T-01-MP-06 | Use assistant function | For each open conflict a suggestion is selected; the result is a valid timetable for the whole school |
| T-01-MP-07 | Record event | A planner records an event on the dashboard and open changes appear in the change history as a consequence |

## Manual test walk-through

The final test pass is a documented, role-based walk-through of the entire product on the seeded demo school:

- **Planner (web):** login and dashboard, class and grade-level pages, generation of the initial timetables with variant comparison and publishing, substitution plan with colour-coded changes, rooms and room schedules, master data configuration, school view with archive and school-year rollover, teacher, student and admin management with deactivation and reactivation, change history and event history, profile and password change, logout
- **Student (web):** login, own timetable with highlighted changes, change list, logout
- **Teacher (web):** login, timetable, change list with filters, rooms and room schedules, logout
- **Android app (teacher and student):** server address configuration, login, weekly and daily timetable, role-dependent room tab, change list with unread counter, persistent login and logout

## Technical particularities

- **Long-running generation.** A full school timetable takes about six minutes to compute. The user interface starts the computation, lets the planner continue working elsewhere and shows a notification when the result is ready, provided the planner stays on the page. Unpublished older drafts are discarded when a new generation starts.
- **Variants instead of a single result.** The generator returns scored variants, and the planner chooses one school-wide variant. Review per class is display-only, and the actual selection is made once for the entire school.
- **Event to change fan-out.** One event, such as a teacher's sickness, can affect many lessons and therefore creates many changes. The dashboard deliberately shows the latest *changes*, not events, because that is the unit planners act on.
- **Conflict-free suggestions.** The assistant function filters candidates against teacher and room availability and existing lessons, so every offered solution is valid.
- **DTO boundary.** The API never exposes entities. DTOs flatten relations and add display-oriented flags such as `geaendert` (changed) and `freistunde` (cancelled without substitute).
- **Security by design.** Stateless JWT, central role-based rules, identity derived from the token, hashed passwords, secrets from the environment.
- **Scoped, read-only mobile client.** The Android app's write-free design removes a whole class of consistency and security concerns.
- **Reproducible environment.** A seed file creates the demo school and a clean reset restores the initial state with three commands.
- **Cross-origin setup.** The web client and the API run on different origins, so the backend has an explicit CORS configuration and a consistent exception-handling strategy.

## Known limitations

Known at submission and partly fixed on the development branch afterwards:

- Push notifications, viewing beyond the current week, weekly alternating courses and electives or working groups are not implemented (see [requirements](requirements.md#implementation-status)).
- The Android app's night mode is prepared (colours and previews exist) but not complete, and the landscape layout is less refined than portrait. State such as the selected day and role survives rotation.
- In the web client, saving grade-level priorities may require pressing the save button twice, and rules for a newly created subject become editable after a page reload.
- Colour and text marking of cancelled lessons in the Android app could be wrong in some cases because the app derived the display from the event type, while a cancellation without a substitute is flagged separately on the change.
- Student web view lists changes but does not highlight them in the timetable grid.
