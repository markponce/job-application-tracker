---
description: 'Project-specific guidance for the frameworks, libraries, and tools configured in pom.xml'
applyTo: '**/*.java, **/*.kt, **/*.sql, **/*.jte, **/pom.xml, **/application*.yaml, **/application*.yml, **/compose*.yaml, **/compose*.yml'
---

# Installed dependency guidance

Use the versions and dependency scopes in `pom.xml` as the source of truth. Consult the upstream documentation linked below when using or changing a dependency; do not assume that a third-party Copilot instruction file is official documentation.

## Spring Boot 4, Spring MVC, and Spring Data JPA

- Use the Spring Boot-managed dependency versions unless a specific override is necessary and documented.
- Keep HTTP handling in MVC controllers and business behavior in services; use Spring Data repositories for persistence.
- Use constructor injection for required collaborators and keep transaction boundaries explicit where behavior requires them.
- Bind related application settings with type-safe configuration properties when configuration grows beyond a few simple values.
- Validate external request data at the boundary and return appropriate HTTP errors; do not expose persistence entities as API contracts by default.
- Official references: [Spring Boot](https://docs.spring.io/spring-boot/reference/), [Spring Framework MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html), [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/).

## PostgreSQL and Spring Boot Docker Compose support

- `compose.yaml` defines the local PostgreSQL service; keep local container configuration there rather than duplicating it unnecessarily in application configuration.
- Spring Boot Docker Compose support can start Compose services during application development and derive connection details. Confirm this integration is active before adding duplicate datasource settings.
- Compose reads optional local overrides from `.env`; `.env` is ignored by Git and `.env.example` documents the local variables. Defaults in `compose.yaml` are development-only, not production credentials.
- Keep the PostgreSQL image pinned to a major version rather than `latest`. If changing Compose port mappings, account for the host port actually published by Compose when configuring or connecting to PostgreSQL.
- Use PostgreSQL-compatible SQL and types in migrations. Official references: [PostgreSQL documentation](https://www.postgresql.org/docs/), [Spring Boot Docker Compose support](https://docs.spring.io/spring-boot/reference/features/dev-services.html#features.dev-services.docker-compose).

## Flyway

- Store versioned migrations in `src/main/resources/db/migration`, named `V<version>__<description>.sql` (for example, `V1__create_applications_table.sql`).
- Treat applied versioned migrations as immutable. Add a new migration to change an existing schema; do not edit a migration that may already have run in another environment.
- Keep migration SQL explicit and PostgreSQL-compatible. Review destructive or data-changing operations carefully.
- This project includes both `flyway-core` and `flyway-database-postgresql`; keep the database-specific module when using Flyway with PostgreSQL.
- Official references: [Flyway migrations](https://documentation.red-gate.com/flyway/flyway-concepts/migrations), [Flyway PostgreSQL support](https://documentation.red-gate.com/flyway/reference/database-driver-reference/postgresql-database).

## Validation and integration tests

- Spring's validation starter is installed. Use Jakarta Bean Validation constraints on request DTOs and `@Valid` at MVC boundaries where input needs validation.
- The Spring Boot Testcontainers integration and PostgreSQL Testcontainers module are test-scoped dependencies. Use PostgreSQL containers for tests that need database behavior and mark them with `@ServiceConnection` so Spring Boot configures the connection.
- Disable Docker Compose integration in a test context that supplies its own Testcontainers database to avoid starting two PostgreSQL services.
- Apply test-driven development when changing behavior: add a focused regression/behavior test before implementation, confirm the test fails for the expected reason, implement the smallest change, and refactor with tests passing. Use MVC or persistence integration tests at the appropriate boundary.
- Official references: [Spring Boot validation](https://docs.spring.io/spring-boot/reference/io/validation.html), [Spring Boot Testcontainers](https://docs.spring.io/spring-boot/reference/testing/testcontainers.html), [Testcontainers PostgreSQL module](https://java.testcontainers.org/modules/databases/postgres/).

## JTE

- Put templates in `src/main/jte`; this project configures the Maven plugin to generate them during `generate-sources`.
- Keep template rendering and presentation logic in templates, and prepare data in application code.
- Preserve the configured HTML content type and use context-appropriate escaping for rendered values.
- `gg.jte.development-mode` is enabled for development; use production-appropriate configuration when deploying.
- Official reference: [JTE documentation](https://jte.gg/).

## Lombok

- Lombok is an optional compile-time dependency used to reduce repetitive entity accessors and provide the protected JPA no-argument constructor.
- Prefer narrow annotations such as `@Getter`, `@Setter`, and `@NoArgsConstructor`; do not use `@Data` on JPA entities because generated equality, hash code, and `toString` can interact badly with generated IDs and lazy associations.
- Do not generate setters for database-generated IDs or timestamps. Keep Lombok out of the packaged runtime artifact.
- Official reference: [Lombok documentation](https://projectlombok.org/features/).

## Tests and Maven

- Use the Spring Boot test dependencies already declared in `pom.xml`; add a test dependency only when a test requires it.
- Keep one focused test class/file per production class or component, following the production package layout under `src/test/java` (for example, `JobApplicationServiceTest` and `JobApplicationControllerTest`). Keep integration tests separate and clearly named.
- Test web behavior at the MVC boundary and persistence behavior against the configured PostgreSQL-compatible schema where practical.
- Use the Maven Wrapper so builds use the project's configured Maven version: `./mvnw test` and `./mvnw package`.
- Official references: [Spring Boot testing](https://docs.spring.io/spring-boot/reference/testing/), [Maven Wrapper](https://maven.apache.org/wrapper/).
