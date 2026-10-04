package com.example.job_application_tracker.jobapplication;

import com.example.job_application_tracker.jobapplication.dto.JobApplicationSearchCriteria;
import com.example.job_application_tracker.jobapplication.model.ApplicationStatus;
import com.example.job_application_tracker.jobapplication.model.ExperienceLevel;
import com.example.job_application_tracker.jobapplication.model.JobApplication;
import com.example.job_application_tracker.jobapplication.model.SalaryPeriod;
import com.example.job_application_tracker.jobapplication.model.WorkSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Import(JobApplicationRepositoryTest.PostgresTestConfiguration.class)
@Transactional
class JobApplicationRepositoryTest {

	@Autowired
	private JobApplicationRepository repository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void clearApplications() {
		repository.deleteAll();
	}

	@Test
	void searchesAllTextColumnsCaseInsensitivelyAndEscapesWildcards() {
		JobApplication title = application("First Co", "Needle Role", null, null, null, null);
		JobApplication company = application("Needle Company", "Developer", null, null, null, null);
		JobApplication url = application("Third Co", "Developer", null, null, null, null);
		url.setUrl("https://example.com/NEEDLE-role");
		JobApplication address = application("Fourth Co", "Developer", null, null, null, null);
		address.setCompanyAddress("Needle Street");
		JobApplication content = application("Fifth Co", "Developer", null, null, null, null);
		content.setContent("Needle description");
		JobApplication notes = application("Sixth Co", "Developer", null, null, null, null);
		notes.setNotes("Needle follow-up");
		JobApplication wildcard = application("Seventh Co", "Developer", null, null, null, null);
		wildcard.setNotes("Role is 100% remote");
		repository.saveAll(List.of(title, company, url, address, content, notes, wildcard));

		Page<JobApplication> matches = repository.findAll(
				JobApplicationSpecifications.from(criteria("needle")), PageRequest.of(0, 20));
		Page<JobApplication> literalWildcardMatches = repository.findAll(
				JobApplicationSpecifications.from(criteria("%")), PageRequest.of(0, 20));

		assertThat(matches.getContent()).hasSize(6);
		assertThat(literalWildcardMatches.getContent()).containsExactly(wildcard);
	}

	@Test
	void omitsBlankSearchAndAppliesOnlySuppliedFilters() {
		JobApplication matching = application("Example Co", "Engineer", ApplicationStatus.APPLIED,
				WorkSetup.HYBRID, ExperienceLevel.SENIOR, SalaryPeriod.YEARLY);
		matching.setSalaryMin(new BigDecimal("100000"));
		matching.setSalaryMax(new BigDecimal("150000"));
		JobApplication other = application("Other Co", "Analyst", ApplicationStatus.SAVED,
				WorkSetup.ONSITE, ExperienceLevel.ENTRY, SalaryPeriod.MONTHLY);
		repository.saveAll(List.of(matching, other));

		JobApplicationSearchCriteria criteria = new JobApplicationSearchCriteria(
				"   ", ApplicationStatus.APPLIED, WorkSetup.HYBRID, ExperienceLevel.SENIOR,
				SalaryPeriod.YEARLY, new BigDecimal("120000"), new BigDecimal("130000"));

		Page<JobApplication> results = repository.findAll(
				JobApplicationSpecifications.from(criteria), PageRequest.of(0, 20));

		assertThat(results.getContent()).containsExactly(matching);
	}

	@Test
	void appliesSalaryRangeOverlapAndSupportsOpenEndedBounds() {
		JobApplication overlap = application("Overlap", "Engineer", null, null, null, null);
		overlap.setSalaryMin(new BigDecimal("90000"));
		overlap.setSalaryMax(new BigDecimal("120000"));
		JobApplication belowRange = application("Below", "Engineer", null, null, null, null);
		belowRange.setSalaryMin(new BigDecimal("50000"));
		belowRange.setSalaryMax(new BigDecimal("80000"));
		JobApplication openEnded = application("Open", "Engineer", null, null, null, null);
		openEnded.setSalaryMin(new BigDecimal("100000"));
		repository.saveAll(List.of(overlap, belowRange, openEnded));

		Page<JobApplication> bounded = repository.findAll(
				JobApplicationSpecifications.from(criteria(null, new BigDecimal("110000"), new BigDecimal("140000"))),
				PageRequest.of(0, 20));
		Page<JobApplication> minimumOnly = repository.findAll(
				JobApplicationSpecifications.from(criteria(null, new BigDecimal("85000"), null)),
				PageRequest.of(0, 20));
		Page<JobApplication> maximumOnly = repository.findAll(
				JobApplicationSpecifications.from(criteria(null, null, new BigDecimal("85000"))),
				PageRequest.of(0, 20));

		assertThat(bounded.getContent()).containsExactly(overlap);
		assertThat(minimumOnly.getContent()).containsExactly(overlap);
		assertThat(maximumOnly.getContent()).containsExactly(belowRange);
	}

	@Test
	void paginatesWithDeterministicOrderingAndTotalCount() {
		repository.saveAll(List.of(
				application("One", "Engineer", null, null, null, null),
				application("Two", "Engineer", null, null, null, null),
				application("Three", "Engineer", null, null, null, null)));

		Page<JobApplication> firstPage = repository.findAll(
				JobApplicationSpecifications.from(criteria(null)),
				PageRequest.of(0, 2, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));
		Page<JobApplication> secondPage = repository.findAll(
				JobApplicationSpecifications.from(criteria(null)),
				PageRequest.of(1, 2, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));

		assertThat(firstPage.getTotalElements()).isEqualTo(3);
		assertThat(firstPage.getContent()).hasSize(2);
		assertThat(secondPage.getContent()).hasSize(1);
		assertThat(firstPage.getContent()).doesNotContainAnyElementsOf(secondPage.getContent());
	}

	@Test
	void trigramIndexesCanServeSubstringPredicates() {
		jdbcTemplate.execute("SET LOCAL enable_seqscan = off");
		List<String> plan = jdbcTemplate.query(
				"""
				EXPLAIN SELECT id FROM job_applications
				WHERE lower(title) LIKE '%needle%' ESCAPE '\\'
				   OR lower(company_name) LIKE '%needle%' ESCAPE '\\'
				   OR lower(url) LIKE '%needle%' ESCAPE '\\'
				   OR lower(company_address) LIKE '%needle%' ESCAPE '\\'
				   OR lower(content) LIKE '%needle%' ESCAPE '\\'
				   OR lower(notes) LIKE '%needle%' ESCAPE '\\'
				""",
				(resultSet, rowNumber) -> resultSet.getString(1));

		String explainPlan = String.join("\n", plan);
		assertThat(explainPlan)
				.contains("BitmapOr")
				.contains("job_applications_title_trgm_idx")
				.contains("job_applications_notes_trgm_idx");
	}

	private JobApplication application(String company, String title, ApplicationStatus status,
			WorkSetup setup, ExperienceLevel experience, SalaryPeriod period) {
		JobApplication application = new JobApplication(company, title, "https://example.com/jobs/1");
		application.setStatus(status == null ? ApplicationStatus.SAVED : status);
		if (application.getStatus() != ApplicationStatus.SAVED) {
			application.setAppliedAt(OffsetDateTime.now());
		}
		application.setWorkSetup(setup == null ? WorkSetup.ONSITE : setup);
		application.setExperienceLevel(experience);
		application.setSalaryPeriod(period);
		return application;
	}

	private JobApplicationSearchCriteria criteria(String query) {
		return criteria(query, null, null);
	}

	private JobApplicationSearchCriteria criteria(String query, BigDecimal minSalary, BigDecimal maxSalary) {
		return new JobApplicationSearchCriteria(query, null, null, null, null, minSalary, maxSalary);
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
