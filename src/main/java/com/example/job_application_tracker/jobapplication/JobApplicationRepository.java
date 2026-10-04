package com.example.job_application_tracker.jobapplication;

import com.example.job_application_tracker.jobapplication.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID>,
		JpaSpecificationExecutor<JobApplication> {

	java.util.Optional<JobApplication> findByIdAndUser_Id(UUID id, UUID userId);
}
