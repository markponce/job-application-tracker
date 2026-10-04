# Repository guidance

- This is a Java 21 Spring Boot 4 application built with Maven. Use `./mvnw` for Maven commands.
- Check `pom.xml` and the existing code before assuming a feature or dependency is present.
- Keep configuration externalized in `src/main/resources/application.yaml`; do not commit production credentials or secrets.
- Use Flyway migrations under `src/main/resources/db/migration` for database schema changes.
- Put JTE templates under `src/main/jte`.
- Follow test-driven development: write or update a focused test first, run it to confirm it fails for the intended reason, implement the smallest change to pass, then refactor while keeping tests green.
- Keep tests organized one test class/file per production class or component, such as `JobApplicationServiceTest` for `JobApplicationService` and `JobApplicationControllerTest` for `JobApplicationController`. Mirror the production package structure under `src/test/java`; keep shared integration tests separately named and focused.
- Run the narrowest relevant tests after changes; use `./mvnw test` for the full test suite and `./mvnw package` to verify the build.
- Follow the path-specific instructions in `.github/instructions/` when editing Spring Boot code, database migrations, templates, build configuration, or Compose configuration.
