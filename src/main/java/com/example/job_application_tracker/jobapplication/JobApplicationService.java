package com.example.job_application_tracker.jobapplication;

import com.example.job_application_tracker.jobapplication.model.JobApplication;
import com.example.job_application_tracker.jobapplication.dto.JobApplicationSearchCriteria;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class JobApplicationService {

	private final JobApplicationRepository repository;

	public JobApplicationService(JobApplicationRepository repository) {
		this.repository = repository;
	}

	public JobApplication create(JobApplication application) {
		return repository.save(application);
	}

	@Transactional(readOnly = true)
	public List<JobApplication> getAll() {
		return repository.findAll();
	}

	@Transactional(readOnly = true)
	public Page<JobApplication> search(JobApplicationSearchCriteria criteria, Pageable pageable) {
		return repository.findAll(JobApplicationSpecifications.from(criteria), pageable);
	}

	@Transactional(readOnly = true)
	public JobApplication getById(UUID id) {
		return repository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Job application not found: " + id));
	}

	public JobApplication update(UUID id, JobApplication changes) {
		JobApplication existing = getById(id);
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

	public void delete(UUID id) {
		repository.delete(getById(id));
	}
}
