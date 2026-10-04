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
src/main/resources/db/migration/V1__create_applications_table.sql
```

Add the first migration with the initial database entity/schema. Applied versioned migrations should be treated as immutable; create a new migration for subsequent changes. Hibernate is configured with `ddl-auto: validate`, so it checks mappings against the migrated schema rather than creating or updating tables.

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
