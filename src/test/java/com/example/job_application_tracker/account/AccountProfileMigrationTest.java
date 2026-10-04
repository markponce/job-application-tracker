package com.example.job_application_tracker.account;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

class AccountProfileMigrationTest {

	@Test
	void addsRequiredNameColumnsAndBackfillsExistingAccounts() {
		try (PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")) {
			postgres.start();
			DriverManagerDataSource dataSource = new DriverManagerDataSource(
					postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
			Flyway.configure()
					.dataSource(dataSource)
					.locations("classpath:db/migration")
					.target(MigrationVersion.fromVersion("3"))
					.load()
					.migrate();
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			jdbcTemplate.update("""
					INSERT INTO user_accounts (email, password_hash)
					VALUES ('existing@example.com', 'encoded-password')
					""");

			Flyway.configure()
					.dataSource(dataSource)
					.locations("classpath:db/migration")
					.load()
					.migrate();

			assertThat(jdbcTemplate.queryForMap("""
					SELECT first_name, last_name FROM user_accounts
					WHERE email = 'existing@example.com'
					"""))
					.containsEntry("first_name", "Account")
					.containsEntry("last_name", "User");
			assertThat(jdbcTemplate.queryForObject("""
					SELECT COUNT(*) FROM information_schema.columns
					WHERE table_name = 'user_accounts'
						AND column_name IN ('first_name', 'last_name')
						AND is_nullable = 'NO'
					""", Integer.class)).isEqualTo(2);
		}
	}
}
