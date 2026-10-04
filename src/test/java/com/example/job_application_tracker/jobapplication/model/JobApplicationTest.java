package com.example.job_application_tracker.jobapplication.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JobApplicationTest {

	@Test
	void appliedAtIsRequiredForStatusesOtherThanSaved() {
		JobApplication application = new JobApplication(
				"Example Co", "Engineer", "https://example.com/jobs/1");
		application.setStatus(ApplicationStatus.APPLIED);

		try (var factory = Validation.buildDefaultValidatorFactory()) {
			Validator validator = factory.getValidator();
			assertThat(validator.validate(application))
					.anyMatch(violation -> violation.getPropertyPath().toString().equals("appliedAtValid"));
		}
	}

	@Test
	void appliedAtIsOptionalWhenStatusIsSaved() {
		JobApplication application = new JobApplication(
				"Example Co", "Engineer", "https://example.com/jobs/1");

		try (var factory = Validation.buildDefaultValidatorFactory()) {
			Validator validator = factory.getValidator();
			assertThat(validator.validate(application))
					.noneMatch(violation -> violation.getPropertyPath().toString().equals("appliedAtValid"));
		}
	}
}
