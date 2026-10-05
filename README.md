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

`JobApplicationService` provides owner-scoped create, search, retrieve-by-ID, update, and delete operations through `JobApplicationRepository`. The MVC interface is available at `/applications`, with database-backed pagination and optional search/filtering, create, detail, edit, and delete pages. Search covers title, company, URL, address, job description, and notes; optional filters include status, work setup, experience level, salary period, and an overlapping salary range. Supported page sizes are 10, 25, 50, and 100. The root URL redirects to the application list. Missing IDs display a not-found page.

Flyway migration V2 enables PostgreSQL's `pg_trgm` extension and creates GIN trigram indexes for substring search, plus B-tree indexes for stable creation-date ordering and salary bounds. The database role running migrations must be permitted to install `pg_trgm` (normally available from the PostgreSQL `contrib` packages). GIN indexes improve substring lookup but add storage and write overhead; assess them against production data and query plans.

The interface is organized into JTE layouts, partials, and pages under `src/main/jte`. Tailwind CSS is currently loaded from its CDN, so a browser needs internet access to style the pages.

## Accounts and email verification

Users can register at `/register`, verify their email, sign in, sign out, and change their password from the authenticated workspace. Passwords are BCrypt-hashed. Verification links are random, single-use, expire after 24 hours, and are stored as digests. Job applications are owned by the authenticated account; search, detail, update, and delete operations are owner-scoped.

Flyway V3 creates the account and throttling tables and adds required ownership to job applications. **As previously approved, this migration deletes every existing row from `job_applications` before adding non-null user ownership.** Back up or recreate any database you want to preserve before starting the application with this migration.

The Compose file starts Mailpit for local email testing. Open `http://localhost:8025` to view verification messages; the application connects to `localhost:1025` when run from the host. Mailpit stores messages in a named Docker volume, so they survive container restarts. Set `APP_PUBLIC_BASE_URL` to the externally reachable origin used in verification links. For SMTP, configure `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, `MAIL_SMTP_AUTH`, and `MAIL_SMTP_STARTTLS` through the environment or a secrets manager. Set `SESSION_COOKIE_SECURE=true` when serving over HTTPS.

To send real mail through Hostinger, copy `.env.example` to `.env`, comment out the local Mailpit `MAIL_*` values, then uncomment the Hostinger SMTP block and replace the mailbox placeholders. It uses `smtp.hostinger.com` on port `587` with SMTP authentication and STARTTLS. Use the full Hostinger mailbox address for both `MAIL_USERNAME` and `MAIL_FROM`, and use that mailbox's password for `MAIL_PASSWORD`. Keep `.env` private; it is ignored by Git.

Authentication throttles are backed by PostgreSQL so limits are shared across application instances. Per-IP buckets use the servlet request's remote address; configure the trusted reverse proxy so the app receives the real client address, and do not trust arbitrary forwarded headers. Set `AUTH_THROTTLING_ENABLED=false` to disable all authentication throttles; it defaults to `true` and should remain enabled in production. Limits and windows are configurable with `AUTH_LOGIN_PER_IP`, `AUTH_LOGIN_PER_ACCOUNT`, `AUTH_REGISTRATION_PER_IP`, `AUTH_VERIFICATION_PER_IP`, `AUTH_RESEND_PER_IP`, `AUTH_RESEND_PER_ACCOUNT` and their corresponding `*_WINDOW_SECONDS` variables. Requests over a limit receive HTTP 429 and `Retry-After`.

## Deploy to Render

The application can be deployed to Render as a [Docker web service](https://render.com/docs/docker) with a managed [Render Postgres database](https://render.com/docs/postgresql-creating-connecting). Render does not deploy this repository's Docker Compose stack: create the web service and database separately. The local Compose PostgreSQL and Mailpit services are for development only.

1. Push the repository to a Git provider supported by Render. In the Render Dashboard, create a **PostgreSQL** database and a **Web Service** in the same region. Choose the database plan and web-service instance appropriate for your use; note that [Render's free services](https://render.com/docs/free) have limits and are not recommended for production.
2. Create the web service from the repository, choose **Docker** as the runtime, and use the repository-root `Dockerfile`. Enable deploys from the desired branch.
3. From the Render Postgres **Connect** or **Info** page, copy its internal host, database name, username, and password. Keep the web service in the database's region and configure these [environment variables](https://render.com/docs/configure-environment-variables):

   | Key | Value |
   | --- | --- |
   | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<internal-host>:5432/<database-name>` |
   | `SPRING_DATASOURCE_USERNAME` | Render database username |
   | `SPRING_DATASOURCE_PASSWORD` | Render database password |
   | `APP_PUBLIC_BASE_URL` | `https://<your-service>.onrender.com` (or your custom HTTPS domain) |
   | `SESSION_COOKIE_SECURE` | `true` |
   | `GG_JTE_DEVELOPMENT_MODE` | `false` |
   | `GG_JTE_USE_PRECOMPILED_TEMPLATES` | `true` |

   The app binds to `0.0.0.0` and listens on Render's `PORT` environment variable (Render web services default to port `10000`; locally the fallback remains `8080`). `SERVER_ADDRESS` can override the bind address. Keep the database URL internal; don't expose database credentials in source control.
4. Configure an SMTP provider for verification messages. Mailpit is not part of the Render deployment. Set `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, and `MAIL_FROM` to the provider's SMTP values, plus `MAIL_SMTP_AUTH=true` and `MAIL_SMTP_STARTTLS=true` when required by that provider. Add credentials through the Render Dashboard's service environment settings, not in `render.yaml` or the repository. Use an address/domain authorized by the provider.
5. Save environment variables and deploy. Flyway applies migrations during application startup. After deployment, open the `onrender.com` URL, register with a test account, and confirm the verification email arrives through the configured SMTP provider.

**Data warning:** Flyway V3 deletes all rows currently in `job_applications` before adding required account ownership. A new Render database starts empty, but do not point this deployment at a database with application records you need to keep unless those records have been backed up and migrated appropriately. Render Postgres is managed separately from the web service; configure its backup/retention plan for production.

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
