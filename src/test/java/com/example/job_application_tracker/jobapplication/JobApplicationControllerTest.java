package com.example.job_application_tracker.jobapplication;

import com.example.job_application_tracker.jobapplication.model.JobApplication;
import com.example.job_application_tracker.jobapplication.dto.JobApplicationSearchCriteria;
import com.example.job_application_tracker.jobapplication.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@AutoConfigureMockMvc
@Import(JobApplicationControllerTest.PostgresTestConfiguration.class)
@WithMockUser(username = "owner@example.com")
class JobApplicationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JobApplicationService service;

	@BeforeEach
	void stubEmptySearchResults() {
		when(service.search(any(String.class), any(JobApplicationSearchCriteria.class), any(Pageable.class)))
				.thenAnswer(invocation -> new PageImpl<>(List.of(), invocation.getArgument(2), 0));
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class PostgresTestConfiguration {

		@Bean
		@ServiceConnection
		PostgreSQLContainer postgresContainer() {
			return new PostgreSQLContainer("postgres:17-alpine");
		}
	}

	@Test
	void rootRedirectsToApplications() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/applications"));
	}

	@Test
	void listDisplaysApplications() throws Exception {
		when(service.search(any(String.class), any(JobApplicationSearchCriteria.class), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(
						new JobApplication("Example Co", "Engineer", "https://example.com/jobs/1"))));

		mockMvc.perform(get("/applications"))
				.andExpect(status().isOk())
				.andExpect(view().name("pages/applications/index"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Example Co")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Engineer")));
	}

	@Test
	void searchAndFiltersAreSentToDatabaseWithSupportedPageSize() throws Exception {
		mockMvc.perform(get("/applications")
						.param("q", " remote ")
						.param("status", "APPLIED")
						.param("workSetup", "HYBRID")
						.param("experienceLevel", "SENIOR")
						.param("salaryPeriod", "YEARLY")
						.param("minSalary", "90000")
						.param("maxSalary", "130000")
						.param("page", "3")
						.param("size", "25"))
				.andExpect(status().isOk())
				.andExpect(view().name("pages/applications/index"));

		verify(service).search(eq("owner@example.com"), eq(new JobApplicationSearchCriteria(
				"remote", ApplicationStatus.APPLIED, WorkSetup.HYBRID, ExperienceLevel.SENIOR,
				SalaryPeriod.YEARLY, new java.math.BigDecimal("90000"), new java.math.BigDecimal("130000"))),
				eq(PageRequest.of(2, 25, org.springframework.data.domain.Sort.by(
						org.springframework.data.domain.Sort.Order.desc("createdAt"),
						org.springframework.data.domain.Sort.Order.desc("id")))));
	}

	@Test
	void unsupportedPageSizeFallsBackToDefaultAndLinksPreserveFilters() throws Exception {
		when(service.search(any(String.class), any(JobApplicationSearchCriteria.class), any(Pageable.class)))
				.thenAnswer(invocation -> new PageImpl<>(List.of(), invocation.getArgument(2), 30));

		mockMvc.perform(get("/applications")
						.param("q", "engineer")
						.param("status", "APPLIED")
						.param("page", "2")
						.param("size", "5000"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("size=10")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("q=engineer&amp;status=APPLIED")));

		verify(service).search(eq("owner@example.com"), any(JobApplicationSearchCriteria.class),
				eq(PageRequest.of(1, 10, org.springframework.data.domain.Sort.by(
						org.springframework.data.domain.Sort.Order.desc("createdAt"),
						org.springframework.data.domain.Sort.Order.desc("id")))));
	}

	@Test
	void newApplicationPageShowsForm() throws Exception {
		mockMvc.perform(get("/applications/new"))
				.andExpect(status().isOk())
				.andExpect(view().name("pages/applications/form"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Create application")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"appliedAtField\" hidden")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("<option value=\"USD\">USD — US Dollar</option>")));
	}

	@Test
	void validApplicationSubmissionCreatesAndRedirects() throws Exception {
		mockMvc.perform(post("/applications")
						.with(csrf())
						.param("companyName", "Example Co")
						.param("title", "Engineer")
						.param("url", "https://example.com/jobs/1")
						.param("status", "SAVED")
						.param("workSetup", "ONSITE"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/applications"));

		verify(service).create(eq("owner@example.com"), any(JobApplication.class));
	}

	@Test
	void nonSavedApplicationWithoutAppliedAtRendersValidationError() throws Exception {
		mockMvc.perform(post("/applications")
						.with(csrf())
						.param("companyName", "Example Co")
						.param("title", "Engineer")
						.param("url", "https://example.com/jobs/1")
						.param("status", "APPLIED")
						.param("workSetup", "ONSITE"))
				.andExpect(status().isOk())
				.andExpect(view().name("pages/applications/form"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Application date is required")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"appliedAtField\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("type=\"datetime-local\" required")));
	}

	@Test
	void invalidCurrencyIsRejectedAndDropdownIsRepopulated() throws Exception {
		mockMvc.perform(post("/applications")
						.with(csrf())
						.param("companyName", "Example Co")
						.param("title", "Engineer")
						.param("url", "https://example.com/jobs/1")
						.param("salaryCurrency", "NOT"))
				.andExpect(status().isOk())
				.andExpect(view().name("pages/applications/form"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Select a valid currency")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("<option value=\"USD\">USD — US Dollar</option>")));
	}

	@Test
	void invalidApplicationSubmissionRendersFormWithErrors() throws Exception {
		mockMvc.perform(post("/applications")
						.with(csrf())
						.param("companyName", "")
						.param("title", "")
						.param("url", ""))
				.andExpect(status().isOk())
				.andExpect(view().name("pages/applications/form"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Company name is required")));
	}

	@Test
	void detailPageShowsApplication() throws Exception {
		UUID id = UUID.randomUUID();
		when(service.getById("owner@example.com", id)).thenReturn(
				new JobApplication("Example Co", "Engineer", "https://example.com/jobs/1"));

		mockMvc.perform(get("/applications/{id}", id))
				.andExpect(status().isOk())
				.andExpect(view().name("pages/applications/show"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Example Co")));
	}

	@Test
	void editPageLoadsApplicationIntoForm() throws Exception {
		UUID id = UUID.randomUUID();
		when(service.getById("owner@example.com", id)).thenReturn(
				new JobApplication("Example Co", "Engineer", "https://example.com/jobs/1"));

		mockMvc.perform(get("/applications/{id}/edit", id))
				.andExpect(status().isOk())
				.andExpect(view().name("pages/applications/form"));
	}

	@Test
	void updateSubmissionUpdatesAndRedirects() throws Exception {
		UUID id = UUID.randomUUID();
		mockMvc.perform(post("/applications/{id}", id)
						.with(csrf())
						.param("companyName", "Example Co")
						.param("title", "Engineer")
						.param("url", "https://example.com/jobs/1")
						.param("status", "APPLIED")
						.param("appliedAt", "2026-10-05T12:00")
						.param("workSetup", "HYBRID"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/applications/" + id));

		verify(service).update(eq("owner@example.com"), eq(id), any(JobApplication.class));
	}

	@Test
	void deleteSubmissionDeletesAndRedirects() throws Exception {
		UUID id = UUID.randomUUID();
		mockMvc.perform(post("/applications/{id}/delete", id).with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/applications"));

		verify(service).delete("owner@example.com", id);
	}
}
