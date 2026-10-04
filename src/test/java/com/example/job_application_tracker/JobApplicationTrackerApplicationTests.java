package com.example.job_application_tracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.example.job_application_tracker.jobapplication.model.JobApplication;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Import(JobApplicationTrackerApplicationTests.PostgresTestConfiguration.class)
class JobApplicationTrackerApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private Validator validator;

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void contextLoads() {
	}

	@Test
	void migrationCreatesJobApplicationsWithUuidAndDefaultValues() {
		Map<String, Object> application = jdbcTemplate.queryForMap("""
				INSERT INTO job_applications (company_name, title, url)
				VALUES ('Example Co', 'Engineer', 'https://example.com/jobs/1')
				RETURNING id, status, work_setup
				""");

		assertNotNull(UUID.fromString(application.get("id").toString()));
		assertEquals("SAVED", application.get("status"));
		assertEquals("ONSITE", application.get("work_setup"));
	}

	@Test
	void migrationRejectsUnknownApplicationStatus() {
		assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update("""
				INSERT INTO job_applications (company_name, title, url, status)
				VALUES ('Example Co', 'Engineer', 'https://example.com/jobs/2', 'INVALID')
				"""));
	}

	@Test
	void migrationRejectsInvalidWorkSetupExperienceAndSalaryValues() {
		assertRejected("""
				INSERT INTO job_applications (company_name, title, url, work_setup)
				VALUES ('Example Co', 'Engineer', 'https://example.com/jobs/3', 'INVALID')
				""");
		assertRejected("""
				INSERT INTO job_applications (company_name, title, url, experience_level)
				VALUES ('Example Co', 'Engineer', 'https://example.com/jobs/4', 'INVALID')
				""");
		assertRejected("""
				INSERT INTO job_applications (company_name, title, url, salary_period)
				VALUES ('Example Co', 'Engineer', 'https://example.com/jobs/5', 'WEEKLY')
				""");
		assertRejected("""
				INSERT INTO job_applications (company_name, title, url, salary_min, salary_max)
				VALUES ('Example Co', 'Engineer', 'https://example.com/jobs/6', 100, 99)
				""");
		assertRejected("""
				INSERT INTO job_applications (company_name, title, url, salary_min)
				VALUES ('Example Co', 'Engineer', 'https://example.com/jobs/7', -1)
				""");
	}

	@Test
	void jobApplicationValidatesRequiredFieldsAndDatabaseLengths() {
		JobApplication application = new JobApplication(null, null, null);
		assertEquals(Set.of("companyName", "title", "url"),
				validator.validate(application).stream()
						.map(violation -> violation.getPropertyPath().toString())
						.collect(Collectors.toSet()));

		application.setCompanyName("x".repeat(256));
		application.setTitle("Engineer");
		application.setUrl("https://example.com/jobs/1");
		Set<ConstraintViolation<JobApplication>> violations = validator.validate(application);
		assertTrue(violations.stream().anyMatch(violation ->
				violation.getPropertyPath().toString().equals("companyName")));
	}

	@Test
	void jobApplicationValidatesSalaryRangePrecisionAndNonNegativeValues() {
		JobApplication application = new JobApplication("Example Co", "Engineer",
				"https://example.com/jobs/1");

		application.setSalaryMin(new BigDecimal("100.00"));
		application.setSalaryMax(new BigDecimal("99.00"));
		assertTrue(validator.validate(application).stream()
				.anyMatch(violation -> violation.getPropertyPath().toString().equals("salaryRangeValid")));

		application.setSalaryMax(new BigDecimal("200.00"));
		application.setSalaryMin(new BigDecimal("-1.00"));
		assertTrue(validator.validate(application).stream()
				.anyMatch(violation -> violation.getPropertyPath().toString().equals("salaryMin")));

		application.setSalaryMin(new BigDecimal("12345678901.00"));
		assertTrue(validator.validate(application).stream()
				.anyMatch(violation -> violation.getPropertyPath().toString().equals("salaryMin")));
	}

	@Test
	@Transactional
	void jobApplicationPersistsWithGeneratedIdAndTimestamps() {
		JobApplication application = new JobApplication("Example Co", "Engineer",
				"https://example.com/jobs/8");

		entityManager.persist(application);
		entityManager.flush();

		assertNotNull(application.getId());
		assertNotNull(application.getCreatedAt());
		assertNotNull(application.getUpdatedAt());
	}

	private void assertRejected(String sql) {
		assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update(sql));
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class PostgresTestConfiguration {

		@Bean
		@ServiceConnection
		PostgreSQLContainer postgresContainer() {
			return new PostgreSQLContainer("postgres:17-alpine");
		}
	}
}
