package com.example.job_application_tracker.jobapplication;

import com.example.job_application_tracker.account.UserAccount;
import com.example.job_application_tracker.account.UserAccountRepository;
import com.example.job_application_tracker.jobapplication.model.JobApplication;
import com.example.job_application_tracker.jobapplication.dto.JobApplicationSearchCriteria;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Service
@Transactional
public class JobApplicationService {

	private final JobApplicationRepository repository;
	private final UserAccountRepository users;

	public JobApplicationService(JobApplicationRepository repository, UserAccountRepository users) {
		this.repository = repository;
		this.users = users;
	}

	public JobApplication create(String ownerEmail, JobApplication application) {
		application.setUser(findOwner(ownerEmail));
		return repository.save(application);
	}

	@Transactional(readOnly = true)
	public Page<JobApplication> search(String ownerEmail, JobApplicationSearchCriteria criteria, Pageable pageable) {
		UserAccount owner = findOwner(ownerEmail);
		return repository.findAll(JobApplicationSpecifications.from(criteria, owner.getId()), pageable);
	}

	@Transactional(readOnly = true)
	public JobApplication getById(String ownerEmail, UUID id) {
		UserAccount owner = findOwner(ownerEmail);
		return repository.findByIdAndUser_Id(id, owner.getId())
				.orElseThrow(() -> new EntityNotFoundException("Job application not found: " + id));
	}

	public JobApplication update(String ownerEmail, UUID id, JobApplication changes) {
		JobApplication existing = getById(ownerEmail, id);
		existing.setCompanyName(changes.getCompanyName());
		existing.setCompanyAddress(changes.getCompanyAddress());
		existing.setTitle(changes.getTitle());
		existing.setUrl(changes.getUrl());
		existing.setContent(changes.getContent());
		existing.setStatus(changes.getStatus());
		existing.setWorkSetup(changes.getWorkSetup());
		existing.setExperienceLevel(changes.getExperienceLevel());
		existing.setSalaryMin(changes.getSalaryMin());
		existing.setSalaryMax(changes.getSalaryMax());
		existing.setSalaryPeriod(changes.getSalaryPeriod());
		existing.setSalaryCurrency(changes.getSalaryCurrency());
		existing.setNotes(changes.getNotes());
		existing.setAppliedAt(changes.getAppliedAt());
		return repository.save(existing);
	}

	public void delete(String ownerEmail, UUID id) {
		repository.delete(getById(ownerEmail, id));
	}

	private UserAccount findOwner(String email) {
		return users.findByEmail(email)
				.orElseThrow(() -> new IllegalStateException("Authenticated account no longer exists"));
	}
}
