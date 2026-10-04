package com.example.job_application_tracker.account;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

class OwnershipMigrationTest {

	@Test
	void ownershipMigrationDeletesLegacyApplicationsBeforeAddingRequiredOwner() {
		try (PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")) {
			postgres.start();
			DriverManagerDataSource dataSource = new DriverManagerDataSource(
					postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());

			Flyway.configure()
					.dataSource(dataSource)
					.locations("classpath:db/migration")
					.target(MigrationVersion.fromVersion("2"))
					.load()
					.migrate();
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			jdbcTemplate.update("""
					INSERT INTO job_applications (company_name, title, url)
					VALUES ('Legacy Co', 'Legacy Engineer', 'https://example.com/legacy')
					""");

			Flyway.configure()
					.dataSource(dataSource)
					.locations("classpath:db/migration")
					.load()
					.migrate();

			assertThat(jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM job_applications", Integer.class)).isZero();
			assertThat(jdbcTemplate.queryForObject("""
					SELECT is_nullable
					FROM information_schema.columns
					WHERE table_name = 'job_applications' AND column_name = 'user_id'
					""", String.class)).isEqualTo("NO");
			assertThat(jdbcTemplate.queryForObject("""
					SELECT COUNT(*)
					FROM information_schema.table_constraints
					WHERE table_name = 'job_applications' AND constraint_type = 'FOREIGN KEY'
					""", Integer.class)).isGreaterThan(0);
		}
	}
}
