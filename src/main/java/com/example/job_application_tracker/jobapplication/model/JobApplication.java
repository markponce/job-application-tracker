package com.example.job_application_tracker.jobapplication.model;

import com.example.job_application_tracker.jobapplication.model.ApplicationStatus;
import com.example.job_application_tracker.jobapplication.model.ExperienceLevel;
import com.example.job_application_tracker.jobapplication.model.SalaryPeriod;
import com.example.job_application_tracker.jobapplication.model.WorkSetup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.example.job_application_tracker.account.UserAccount;

@Entity
@Table(name = "job_applications")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobApplication {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Setter(AccessLevel.NONE)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserAccount user;

	@NotNull
	@Size(max = 255)
	@Column(nullable = false, length = 255)
	private String companyName;

	@Column(columnDefinition = "text")
	private String companyAddress;

	@NotNull
	@Size(max = 255)
	@Column(nullable = false, length = 255)
	private String title;

	@NotNull
	@Column(nullable = false, columnDefinition = "text")
	private String url;

	@Column(columnDefinition = "text")
	private String content;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private ApplicationStatus status = ApplicationStatus.SAVED;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private WorkSetup workSetup = WorkSetup.ONSITE;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ExperienceLevel experienceLevel;

	@Digits(integer = 10, fraction = 2)
	@DecimalMin("0.00")
	@Column(precision = 12, scale = 2)
	private BigDecimal salaryMin;

	@Digits(integer = 10, fraction = 2)
	@DecimalMin("0.00")
	@Column(precision = 12, scale = 2)
	private BigDecimal salaryMax;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private SalaryPeriod salaryPeriod;

	@Size(max = 3)
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(columnDefinition = "char(3)", length = 3)
	private String salaryCurrency;

	@Column(columnDefinition = "text")
	private String notes;

	private OffsetDateTime appliedAt;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	@Setter(AccessLevel.NONE)
	private OffsetDateTime createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	@Setter(AccessLevel.NONE)
	private OffsetDateTime updatedAt;

	public JobApplication(String companyName, String title, String url) {
		this.companyName = companyName;
		this.title = title;
		this.url = url;
	}

	@AssertTrue(message = "salaryMin must be less than or equal to salaryMax")
	public boolean isSalaryRangeValid() {
		return salaryMin == null || salaryMax == null || salaryMin.compareTo(salaryMax) <= 0;
	}

	@AssertTrue(message = "Application date is required")
	public boolean isAppliedAtValid() {
		return status == ApplicationStatus.SAVED || appliedAt != null;
	}
}
