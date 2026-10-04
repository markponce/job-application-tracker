package com.example.job_application_tracker.jobapplication.dto;

import com.example.job_application_tracker.jobapplication.*;
import com.example.job_application_tracker.jobapplication.CurrencyCatalog;
import com.example.job_application_tracker.jobapplication.model.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Locale;

public record JobApplicationForm(
		@NotBlank(message = "Company name is required")
		@Size(max = 255)
		String companyName,
		String companyAddress,
		@NotBlank(message = "Job title is required")
		@Size(max = 255)
		String title,
		@NotBlank(message = "Job URL is required")
		String url,
		String content,
		@NotNull ApplicationStatus status,
		@NotNull WorkSetup workSetup,
		ExperienceLevel experienceLevel,
		@Digits(integer = 10, fraction = 2)
		@DecimalMin(value = "0.00", message = "Minimum salary must be zero or greater")
		BigDecimal salaryMin,
		@Digits(integer = 10, fraction = 2)
		@DecimalMin(value = "0.00", message = "Maximum salary must be zero or greater")
		BigDecimal salaryMax,
		SalaryPeriod salaryPeriod,
		@Size(max = 3)
		String salaryCurrency,
		String notes,
		LocalDateTime appliedAt
) {

	public JobApplicationForm {
		status = status == null ? ApplicationStatus.SAVED : status;
		workSetup = workSetup == null ? WorkSetup.ONSITE : workSetup;
		salaryCurrency = salaryCurrency == null || salaryCurrency.isBlank()
				? null
				: salaryCurrency.trim().toUpperCase(Locale.ROOT);
	}

	public static JobApplicationForm empty() {
		return new JobApplicationForm(null, null, null, null, null, null, null, null,
				null, null, null, null, null, null);
	}

	@AssertTrue(message = "Minimum salary must not exceed maximum salary")
	public boolean isSalaryRangeValid() {
		return salaryMin == null || salaryMax == null || salaryMin.compareTo(salaryMax) <= 0;
	}

	@AssertTrue(message = "Application date is required")
	public boolean isAppliedAtValid() {
		return status == ApplicationStatus.SAVED || appliedAt != null;
	}

	@AssertTrue(message = "Select a valid currency")
	public boolean isSalaryCurrencyValid() {
		return salaryCurrency == null || CurrencyCatalog.contains(salaryCurrency);
	}

	public JobApplication toEntity() {
		JobApplication application = new JobApplication(companyName, title, url);
		application.setCompanyAddress(companyAddress);
		application.setContent(content);
		application.setStatus(status);
		application.setWorkSetup(workSetup);
		application.setExperienceLevel(experienceLevel);
		application.setSalaryMin(salaryMin);
		application.setSalaryMax(salaryMax);
		application.setSalaryPeriod(salaryPeriod);
		application.setSalaryCurrency(salaryCurrency);
		application.setNotes(notes);
		application.setAppliedAt(appliedAt == null ? null
				: appliedAt.atZone(ZoneId.systemDefault()).toOffsetDateTime());
		return application;
	}

	public static JobApplicationForm from(JobApplication application) {
		OffsetDateTime appliedAt = application.getAppliedAt();
		return new JobApplicationForm(
				application.getCompanyName(),
				application.getCompanyAddress(),
				application.getTitle(),
				application.getUrl(),
				application.getContent(),
				application.getStatus(),
				application.getWorkSetup(),
				application.getExperienceLevel(),
				application.getSalaryMin(),
				application.getSalaryMax(),
				application.getSalaryPeriod(),
				application.getSalaryCurrency(),
				application.getNotes(),
				appliedAt == null ? null : appliedAt.toLocalDateTime());
	}
}
