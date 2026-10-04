package com.example.job_application_tracker.jobapplication;

import com.example.job_application_tracker.jobapplication.dto.JobApplicationForm;
import com.example.job_application_tracker.jobapplication.dto.JobApplicationForm;
import com.example.job_application_tracker.jobapplication.model.*;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JobApplicationFormTest {

	@Test
	void appliesDefaultsWhenStatusAndWorkSetupAreNotProvided() {
		JobApplicationForm form = new JobApplicationForm(
				"Example Co", null, "Engineer", "https://example.com/jobs/1", null,
				null, null, null, null, null, null, null, null, null);

		assertThat(form.status()).isEqualTo(ApplicationStatus.SAVED);
		assertThat(form.workSetup()).isEqualTo(WorkSetup.ONSITE);
	}

	@Test
	void convertsFormValuesToEntity() {
		JobApplicationForm form = new JobApplicationForm(
				"Example Co", "Somewhere", "Engineer", "https://example.com/jobs/1", "Description",
				ApplicationStatus.APPLIED, WorkSetup.HYBRID, ExperienceLevel.SENIOR,
				new BigDecimal("100000.00"), new BigDecimal("150000.00"), SalaryPeriod.YEARLY,
				"USD", "Follow up", LocalDateTime.of(2026, 10, 4, 12, 30));

		JobApplication entity = form.toEntity();

		assertThat(entity.getCompanyName()).isEqualTo("Example Co");
		assertThat(entity.getCompanyAddress()).isEqualTo("Somewhere");
		assertThat(entity.getTitle()).isEqualTo("Engineer");
		assertThat(entity.getUrl()).isEqualTo("https://example.com/jobs/1");
		assertThat(entity.getContent()).isEqualTo("Description");
		assertThat(entity.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
		assertThat(entity.getWorkSetup()).isEqualTo(WorkSetup.HYBRID);
		assertThat(entity.getExperienceLevel()).isEqualTo(ExperienceLevel.SENIOR);
		assertThat(entity.getSalaryMin()).isEqualByComparingTo("100000.00");
		assertThat(entity.getSalaryMax()).isEqualByComparingTo("150000.00");
		assertThat(entity.getSalaryPeriod()).isEqualTo(SalaryPeriod.YEARLY);
		assertThat(entity.getSalaryCurrency()).isEqualTo("USD");
		assertThat(entity.getNotes()).isEqualTo("Follow up");
		assertThat(entity.getAppliedAt()).isNotNull();
	}

	@Test
	void createsFormFromEntity() {
		JobApplication entity = new JobApplication("Example Co", "Engineer", "https://example.com/jobs/1");
		entity.setStatus(ApplicationStatus.APPLIED);
		entity.setWorkSetup(WorkSetup.HYBRID);
		entity.setSalaryMin(new BigDecimal("100000.00"));
		entity.setNotes("Follow up");

		JobApplicationForm form = JobApplicationForm.from(entity);

		assertThat(form.companyName()).isEqualTo("Example Co");
		assertThat(form.status()).isEqualTo(ApplicationStatus.APPLIED);
		assertThat(form.workSetup()).isEqualTo(WorkSetup.HYBRID);
		assertThat(form.salaryMin()).isEqualByComparingTo("100000.00");
		assertThat(form.notes()).isEqualTo("Follow up");
	}

	@Test
	void appliedAtIsRequiredForStatusesOtherThanSaved() {
		JobApplicationForm form = formWithStatus(ApplicationStatus.APPLIED, null);

		try (var factory = Validation.buildDefaultValidatorFactory()) {
			Validator validator = factory.getValidator();
			assertThat(validator.validate(form))
					.anyMatch(violation -> violation.getPropertyPath().toString().equals("appliedAtValid"));
		}
	}

	@Test
	void appliedAtIsOptionalWhenStatusIsSaved() {
		JobApplicationForm form = formWithStatus(ApplicationStatus.SAVED, null);

		try (var factory = Validation.buildDefaultValidatorFactory()) {
			Validator validator = factory.getValidator();
			assertThat(validator.validate(form))
					.noneMatch(violation -> violation.getPropertyPath().toString().equals("appliedAtValid"));
		}
	}

	@Test
	void acceptsOnlyCurrenciesSupportedByTheBackendCatalog() {
		JobApplicationForm form = new JobApplicationForm(
				"Example Co", null, "Engineer", "https://example.com/jobs/1", null,
				ApplicationStatus.SAVED, WorkSetup.ONSITE, null, null, null, null,
				"NOT", null, null);

		try (var factory = Validation.buildDefaultValidatorFactory()) {
			Validator validator = factory.getValidator();
			assertThat(validator.validate(form))
					.anyMatch(violation -> violation.getPropertyPath().toString().equals("salaryCurrencyValid"));
		}
	}

	@Test
	void normalizesCurrencyAndTreatsBlankAsUnspecified() {
		JobApplicationForm usd = formWithCurrency("usd");
		JobApplicationForm blank = formWithCurrency(" ");

		assertThat(usd.salaryCurrency()).isEqualTo("USD");
		assertThat(blank.salaryCurrency()).isNull();
	}

	private JobApplicationForm formWithStatus(ApplicationStatus status, LocalDateTime appliedAt) {
		return new JobApplicationForm(
				"Example Co", null, "Engineer", "https://example.com/jobs/1", null,
				status, WorkSetup.ONSITE, null, null, null, null, null, null, appliedAt);
	}

	private JobApplicationForm formWithCurrency(String currency) {
		return new JobApplicationForm(
				"Example Co", null, "Engineer", "https://example.com/jobs/1", null,
				ApplicationStatus.SAVED, WorkSetup.ONSITE, null, null, null, null,
				currency, null, null);
	}
}
