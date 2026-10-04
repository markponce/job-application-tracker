package com.example.job_application_tracker.jobapplication.dto;

import com.example.job_application_tracker.jobapplication.model.ApplicationStatus;
import com.example.job_application_tracker.jobapplication.model.ExperienceLevel;
import com.example.job_application_tracker.jobapplication.model.SalaryPeriod;
import com.example.job_application_tracker.jobapplication.model.WorkSetup;

import java.math.BigDecimal;

public record JobApplicationSearchCriteria(
		String query,
		ApplicationStatus status,
		WorkSetup workSetup,
		ExperienceLevel experienceLevel,
		SalaryPeriod salaryPeriod,
		BigDecimal minSalary,
		BigDecimal maxSalary
) {

	public JobApplicationSearchCriteria {
		query = query == null || query.isBlank() ? null : query.trim();
	}
}
