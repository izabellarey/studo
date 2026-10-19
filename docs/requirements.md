# Requirements

This document summarises the requirements specification (*Pflichtenheft*) that the team agreed with the customer at the start of the project. It covers context and scope, roles, use cases, the data that the system manages, and the final implementation status of every requirement.

**Contents**

1. [Context and goal](#context-and-goal)
2. [Roles](#roles)
3. [Scope](#scope)
4. [Use cases](#use-cases)
5. [Data requirements](#data-requirements)
6. [Operating conditions](#operating-conditions)
7. [Implementation status](#implementation-status)
8. [Glossary](#glossary)

---

## Context and goal

STudo supports *Gymnasien* in Schleswig-Holstein in three tasks: automatically creating timetables, editing and managing them efficiently, and informing teachers, students and school administration about short-notice changes such as sickness-related substitutions, cancelled lessons or blocked rooms.

The product is hosted on a single server and delivered through a web interface and an Android app. Teachers and students need no special software knowledge because they only read information. Planners (secretariat and school management) work with the web interface, which is designed to be usable with basic experience of administrative software.

## Roles

| Role | Description | Interface |
|---|---|---|
| **Planner** | Secretariat and school management. Configures the system, creates and maintains timetables, records events and resolves changes. | Web |
| **Teacher** | Views the own timetable, changes affecting the own lessons and room schedules. Has no configuration rights. | Android app, web |
| **Student** | Views the timetable of the own class and the relevant changes. No access to room information. | Android app, web |

## Scope

Requirements are prioritised in three tiers. A fourth list defines what the product explicitly does not do.

### Must-have

- Planners can enter and edit the required master data.
- Planners can create timetables automatically.
- Planners can view and edit the created timetables.
- After conflict detection, timetables can be edited with an assistant function that offers solution proposals.
- Planners can display the change history of a timetable.
- Teachers and students can display the timetable relevant to them in the Android app.

### Should-have

- Teachers and students can also see their timetable in the web interface.
- Elective subjects and working groups whose members differ within a class are supported.

### Could-have

- Teachers receive push notifications in the Android app when their timetable changes.
- Timetables and changes can be viewed beyond the current week.
- Weekly alternating courses are supported.

### Out of scope

- No multi-tenancy: one installation serves exactly one school.
- Only the *Gymnasium* school type is covered.
- Only the rule set of Schleswig-Holstein is considered; no rule sets for other federal states.
- Teachers cannot send requests from the Android app to the planners.

## Use cases

Use case IDs follow the convention *M* for must-have, followed by the actor group (*P* planner, *L* teachers and students, *A* all users).

| ID | Use case | Actor | Summary |
|---|---|---|---|
| ML-01 | Show current changes | Teacher, student | The user opens the change list and sees the currently confirmed changes that affect them. |
| MA-01 | Show timetable | All | The user displays the own timetable (planners: any timetable). |
| MP-01 | Edit master data | Planner | The planner changes school profile, period grid, rules, mandatory subjects and quotas. |
| MP-02 | Create timetable | Planner | The system generates timetable variants from the master data. Uses MP-06. |
| MP-03 | Edit timetable | Planner | The planner adjusts a lesson, supported by alternatives from the assistant function. Uses MP-07. |
| MP-04 | Show change history | Planner | The planner browses open and completed changes. |
| MP-05 | Check for conflicts | Planner | The system checks a timetable for rule violations and reports conflicts. |
| MP-06 | Use assistant function | Planner | While creating or editing a timetable, the system proposes solutions for the design and for resolving detected conflicts, such as alternative placements, substitute teachers or rooms. |
| MP-07 | Record event | Planner | The planner records a teacher absence, room blocking or extra lesson, which triggers changes. |

Every use case has a structured description (preconditions, postconditions, main flow, alternatives and exceptions) and at least one acceptance test case, see [testing](testing.md#acceptance-tests).

## Data requirements

The system stores personal data (P), configuration data (K) and school administration data (S). The main data groups are:

| Group | Contents |
|---|---|
| **Users** | Name, system-generated username, hashed password, role (planner, teacher, student) |
| **Teachers** | Abbreviation, employment (full-time or part-time), weekly hours, subjects, availabilities (preferred, not preferred, blocked, with validity period, maintained by planners) |
| **Students** | Entry and exit date, attended learning groups |
| **Learning groups** | Size, group leader, home room, type (class, course, working group), grade level |
| **School structure** | School name, federal state, rooms (abbreviation, capacity, type), period grid with start and end times, offered subjects (abbreviation, required room type), grade levels with a planning priority, constraints |
| **Rules** | Block rules, maximum workloads (per day, per week and consecutive lessons, per grade level or teacher), lesson quotas per learning group and subject, break rules, mandatory subject rules; each with hardness and weight |
| **Timetables** | Lessons (learning group, teacher, room, time, weekday, subject), status (active, archived, draft), validity period, creation time, school year and half year, author |
| **Events** | Type (teacher absence, room blocking, extra lesson), validity period, status, author, type-specific attributes |
| **Changes** | Affected lesson, new teacher, new room, new subject, cancellation flag, date, timestamp, triggering event |

## Operating conditions

- All application data is persisted automatically; operation requires no special skills while running.
- The software is hosted server-side and offered through a web interface and an Android app. Teachers and students use the app to view their timetable.
- Planners need only basic knowledge of administrative software.

## Implementation status

The status is the state at submission of the project.

| Requirement | Tier | Status | Notes |
|---|---|---|---|
| Edit master data | Must | Implemented | Web: school profile, period grid, rules, priorities, subjects and quotas |
| Automatic timetable generation | Must | Implemented | Timefold-based, three school-wide variants, rule set with hard and soft constraints |
| View and edit timetables | Must | Implemented | Class, teacher and room views; editing with suggestions |
| Edit with assistant function after conflict detection | Must | Implemented | Only conflict-free substitute teachers and rooms are offered |
| Change history | Must | Implemented | Open and completed changes, search and filters, separate event history |
| Timetable in the Android app | Must | Implemented | Weekly and daily view, change highlighting, change list, teacher room plans |
| Timetable in the web interface for teachers and students | Should | Implemented | Timetable and change list; students have no room access |
| Elective subjects and working groups | Should | Not implemented | Descoped on the explicit request of the customer during the second-week interview. The data model already has a learning-group concept with types. |
| Push notifications | Could | Not implemented | Dropped when a member of the Android team dropped out |
| Viewing beyond the current week | Could | Not implemented | Backend queries filter by week and could be extended easily |
| Weekly alternating courses | Could | Not implemented | Belonged to the descoped elective feature |
| Teachers accept or decline substitutions (extra feature) | Extra | Replaced | Replaced by an alternative agreed after the Android team member dropped out: teachers can state preferences for classes, maintained in the web client |

Beyond the specification, the web client also provides a school-wide archive view, a school-year rollover that promotes all classes by one grade level, activation and deactivation of users and classes, and a built-in user manual.

## Glossary

| Term | Meaning |
|---|---|
| Planner | Secretariat and school management; synonym for administration staff |
| Learning group | A set of students, for example a class, a course or a working group |
| Lesson | One school lesson with subject, learning group, teacher, weekday and time |
| Timetable | The set of lessons for a school year for the entire school |
| Current timetable | The regular timetable including all confirmed changes |
| Event | Umbrella term for teacher absence, room blocking and extra lesson; triggers changes |
| Change | A substitution, room change or extra lesson that results from an event |
| Change history | The history of a timetable with all recorded changes |
| Constraint / rule | A rule a timetable must satisfy (hard) or should satisfy as far as possible (soft) |
| Block rule | A subject must be taught in double lessons in a grade level |
| Maximum workload | Upper limit for lessons per day, per week or consecutive lessons, per grade level or teacher |
| Break rule | After how many consecutive lessons a break must follow, and how long it must be |
| Mandatory subject rule | A grade level must receive a minimum number of weekly lessons in a subject |
| Lesson quota | Required weekly lessons of a learning group in a subject, optionally with a preferred teacher |
| Priority | A number per grade level that determines which grade levels are planned first during generation |
| Master data | Information the system needs to work: teachers and their subjects, rooms, learning groups, period grid and rule set |
| Assistant function | Provides solution proposals when editing a timetable after a conflict was detected |
| Multi-tenancy | Serving several independent customers (here: schools) with one installation; not supported |
