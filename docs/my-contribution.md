# My contribution

**Izabella Rey** · Backend team (database and backend, focus on security) · 8-person team · 4 weeks

STudo was a team effort. This page describes the parts I was responsible for and how I worked within the team, so that my share of the project is easy to find in the code base and the documentation.

## At a glance

- Designed and implemented the **JWT-based authentication and role-based authorisation** with Spring Security, together with the account and password model.
- Built the **backend services behind the teacher view** of the Android app, including room lists and room schedules.
- Implemented the **assistant function** that proposes substitute teachers and alternative rooms, and the **creation of events and the changes they trigger**.
- Connected the backend with the web client and the Android app and helped bring the **Docker-based deployment** onto the server (CORS, exception handling, container setup).
- Wrote the **first backend tests** and made sure that the main branch always held a **runnable backend**.
- Authored the **product data** and **product usage** chapters of the requirements specification.

## Security and authentication

I owned the security architecture of the backend. Before touching the team repository I built and verified the whole flow in an isolated sandbox project, then integrated it, so that the shared codebase never broke on account of an experiment.

What I built:

- **Login and token issuing.** An `AuthController` with `LoginRequest` and `LoginResponse` DTOs delegates credential checks to Spring Security's `AuthenticationManager`, which loads accounts through a `CustomUserDetailsService` backed by the account repository. On success a `JwtService` issues a signed token carrying username and role.
- **Request authentication.** A `JwtAuthFilter` validates the token on every request and populates the security context.
- **Central, role-based authorisation.** The `SecurityFilterChain` defines access rules per role in one place instead of scattering annotations over methods. I kept the `PasswordEncoder` bean in its own configuration class so that exchanging the user details service later cannot break the wiring.
- **Secrets from the environment.** The signing secret is injected from the environment with a development default, never hard-coded.
- **Staged integration.** Security went into the shared codebase in stages. I deliberately kept the filter chain permissive at first so that colleagues could test their features without fighting authentication.

The account model behind it:

- Three roles (planner, teacher, student) with system-generated usernames and no e-mail login, as required by the customer.
- Two separate password flows: self-service change (requires the old password, identity from the token) and administrative reset (by ID).
- Controllers that modify personal data take the identity from the authenticated principal instead of a client-supplied ID, which closes the classic IDOR hole.

## App views and room plans

For the student and teacher side of the Android app I implemented the backend services and DTOs: the personal timetable of a teacher, the timetable of the student logging in, the list of all rooms and the weekly schedule of a single room.

- The room schedule is computed by walking over every weekday and every non-break time slot of the period grid and looking up the occupancy in a map, which yields a complete grid including free slots in a single pass.
- I removed the real-time occupancy status from the room list DTO on purpose. Deriving it reliably was disproportionately expensive compared with its value, so I cut it and documented the decision instead of shipping something fragile.
- DTOs are organised by feature and reused across controllers where it makes sense, for example the lesson DTO shared by the teacher and the class endpoints. Entities never cross the API boundary.

## Assistant function and change handling

In the third week I implemented the services that turn disruptions into resolved changes:

- **Assistant function.** For an affected lesson it determines which substitute teachers and which alternative rooms are available, so the planner only sees valid options.
- **Events and changes.** Recording an event (teacher absence, room blocking, extra lesson) creates the resulting changes for all affected lessons. The planner then resolves them with the suggestions above, and the change history is built from the persisted changes.

## Data model and persistence

As part of the database and backend team I contributed to the entity model with JPA and Hibernate, including the event hierarchy (room change, teacher absence, extra lesson) mapped with joined-table inheritance, and I maintained SQL test data in step with the entity definitions after each implemented method.

## Integration and deployment

Late in the project the pieces had to work together, and I took part in making that happen:

- Aligned **endpoint URLs and exchanged data types** between the backend and the frontends.
- Brought the application up on the **server VM**, which required a proper **CORS configuration**, consistent **exception handling** and the **Docker** setup.

## Quality and workflow

- Wrote the **first backend tests** (written with AI assistance) and extended the test data alongside the features.
- Kept the **main branch runnable** at all times so that the frontend teams could integrate against a working backend. I used feature branches, committed work in progress with clear messages and merged the main branch into my branches regularly to avoid large conflicts around the shared schema.

## Documentation and teamwork

- Wrote the **Product data** and **Product usage** chapters of the requirements specification in the first week.
- In the backend team I was strongly involved in communication: I thought through application workflows so that frontend and backend teams implemented against the same picture, contributed concept ideas and helped discuss and resolve errors.

## Engineering principles I applied

| Principle | Where it shows |
|---|---|
| The backend is the only trustworthy authorisation layer | Central role rules; identity from the token; frontends hide controls for usability only |
| Never expose entities through the API | DTO layer with feature-based packages |
| Secrets never live in code | Signing secret from the environment |
| Build in isolation, then integrate | Security developed in a sandbox first |
| Keep the shared branch healthy | Runnable main branch, regular merges, feature branches |
| Cut what is disproportionate and say so | Real-time room occupancy removed from the DTO |

## What I learned

I came into the project with a strong database background and learned the rest of the backend stack in four weeks: Spring Boot, Spring Security with JWT, Spring Data JPA, Docker and the practicalities of CORS and exception handling in a real multi-client system. I like to understand a topic holistically before implementing it, then follow it through step by step, and I try to leave the codebase in a state where the next person can continue without me.

On top of that, I ensure the overall team communication is efficient and clear.
