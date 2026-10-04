package com.example.job_application_tracker.jobapplication;

import com.example.job_application_tracker.jobapplication.dto.JobApplicationSearchCriteria;
import com.example.job_application_tracker.jobapplication.model.JobApplication;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class JobApplicationSpecifications {

	private static final char LIKE_ESCAPE = '\\';

	private JobApplicationSpecifications() {
	}

	public static Specification<JobApplication> from(JobApplicationSearchCriteria criteria, UUID userId) {
		return (root, query, builder) -> {
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(builder.equal(root.get("user").get("id"), userId));
			if (criteria.query() != null) {
				String pattern = "%" + escapeLike(criteria.query().toLowerCase(Locale.ROOT)) + "%";
				predicates.add(builder.or(
						textMatch(root, builder, "title", pattern),
						textMatch(root, builder, "companyName", pattern),
						textMatch(root, builder, "url", pattern),
						textMatch(root, builder, "companyAddress", pattern),
						textMatch(root, builder, "content", pattern),
						textMatch(root, builder, "notes", pattern)));
			}
			if (criteria.status() != null) {
				predicates.add(builder.equal(root.get("status"), criteria.status()));
			}
			if (criteria.workSetup() != null) {
				predicates.add(builder.equal(root.get("workSetup"), criteria.workSetup()));
			}
			if (criteria.experienceLevel() != null) {
				predicates.add(builder.equal(root.get("experienceLevel"), criteria.experienceLevel()));
			}
			if (criteria.salaryPeriod() != null) {
				predicates.add(builder.equal(root.get("salaryPeriod"), criteria.salaryPeriod()));
			}
			if (criteria.minSalary() != null) {
				predicates.add(builder.greaterThanOrEqualTo(
						root.get("salaryMax"), criteria.minSalary()));
			}
			if (criteria.maxSalary() != null) {
				predicates.add(builder.lessThanOrEqualTo(
						root.get("salaryMin"), criteria.maxSalary()));
			}
			return builder.and(predicates.toArray(Predicate[]::new));
		};
	}

	private static String escapeLike(String value) {
		return value.replace(String.valueOf(LIKE_ESCAPE), "\\\\")
				.replace("%", "\\%")
				.replace("_", "\\_");
	}

	private static Predicate textMatch(
			Root<JobApplication> root, CriteriaBuilder builder, String field, String pattern) {
		return builder.like(builder.lower(root.<String>get(field)), pattern, LIKE_ESCAPE);
	}
}
