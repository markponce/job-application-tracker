# Job Application Tracker

A Java 21 and Spring Boot application using Maven, PostgreSQL, Flyway, Spring Data JPA, Spring MVC, and JTE.

## Prerequisites

- Java 21
- Docker with Docker Compose

## Run locally

1. Optionally create a local Compose environment file:

   ```bash
   cp .env.example .env
   ```

   The Compose file has local-only defaults, so this step is only needed to override them. `.env` is ignored by Git.
2. Start the application:

   ```bash
   ./mvnw spring-boot:run
   ```

   Spring Boot's Docker Compose support starts the PostgreSQL service from `compose.yaml` and configures the connection. You can also start the database separately with `docker compose up -d postgres`.

The Compose credentials and defaults are for local development only. Configure production credentials through the deployment environment or a secrets manager, not this file.

## Database schema and migrations

Flyway owns schema changes. Add versioned PostgreSQL SQL migrations under `src/main/resources/db/migration`, for example:

```text
src/main/resources/db/migration/V1__create_job_applications.sql
```

The initial `job_applications` table is created by `V1__create_job_applications.sql`. Applied versioned migrations should be treated as immutable; create a new migration for subsequent changes. Hibernate is configured with `ddl-auto: validate`, so it checks entity mappings against the migrated schema rather than creating or updating tables.

The `JobApplication` JPA model and its status, work setup, experience level, and salary period enums are in `com.example.job_application_tracker.jobapplication`. Bean Validation constraints mirror the migration's required fields, lengths, salary precision/range, and non-negative salary rules.

`JobApplicationService` provides create, list, retrieve-by-ID, update, and delete operations through `JobApplicationRepository`. The MVC interface is available at `/applications`, with database-backed pagination and optional search/filtering, create, detail, edit, and delete pages. Search covers title, company, URL, address, job description, and notes; optional filters include status, work setup, experience level, salary period, and an overlapping salary range. Supported page sizes are 10, 25, 50, and 100. The root URL redirects to the application list. Missing IDs display a not-found page.

Flyway migration V2 enables PostgreSQL's `pg_trgm` extension and creates GIN trigram indexes for substring search, plus B-tree indexes for stable creation-date ordering and salary bounds. The database role running migrations must be permitted to install `pg_trgm` (normally available from the PostgreSQL `contrib` packages). GIN indexes improve substring lookup but add storage and write overhead; assess them against production data and query plans.

The interface is organized into JTE layouts, partials, and pages under `src/main/jte`. Tailwind CSS is currently loaded from its CDN, so a browser needs internet access to style the pages. Authentication and user ownership are not implemented yet; application records are not user-scoped.

## Tests and build

The Spring context integration test uses a PostgreSQL Testcontainers instance and requires Docker:

```bash
./mvnw test
```

Build and package the application with:

```bash
./mvnw package
```

## Templates

JTE templates belong in `src/main/jte`. The Maven plugin generates template classes during the build.
