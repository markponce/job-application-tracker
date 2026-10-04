package com.example.job_application_tracker.jobapplication;

import com.example.job_application_tracker.jobapplication.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

	@Mock
	private JobApplicationRepository repository;

	private JobApplicationService service;

	@BeforeEach
	void setUp() {
		service = new JobApplicationService(repository);
	}

	@Test
	void createSavesAndReturnsApplication() {
		JobApplication application = new JobApplication("Example Co", "Engineer", "https://example.com/job");
		when(repository.save(application)).thenReturn(application);

		assertSame(application, service.create(application));
		verify(repository).save(application);
	}

	@Test
	void getAllReturnsAllApplications() {
		List<JobApplication> applications = List.of(
				new JobApplication("Example Co", "Engineer", "https://example.com/job"));
		when(repository.findAll()).thenReturn(applications);

		assertSame(applications, service.getAll());
	}

	@Test
	void getByIdReturnsApplicationWhenFound() {
		UUID id = UUID.randomUUID();
		JobApplication application = new JobApplication("Example Co", "Engineer", "https://example.com/job");
		when(repository.findById(id)).thenReturn(Optional.of(application));

		assertSame(application, service.getById(id));
	}

	@Test
	void getByIdThrowsWhenApplicationDoesNotExist() {
		UUID id = UUID.randomUUID();
		when(repository.findById(id)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.getById(id));
	}

	@Test
	void updateCopiesEditableFieldsAndSavesExistingApplication() {
		UUID id = UUID.randomUUID();
		JobApplication existing = new JobApplication("Old Co", "Junior Engineer", "https://example.com/old");
		JobApplication changes = new JobApplication("New Co", "Senior Engineer", "https://example.com/new");
		changes.setCompanyAddress("New address");
		changes.setContent("Job description");
		changes.setStatus(ApplicationStatus.APPLIED);
		changes.setWorkSetup(WorkSetup.HYBRID);
		changes.setExperienceLevel(ExperienceLevel.SENIOR);
		changes.setSalaryMin(new BigDecimal("100000.00"));
		changes.setSalaryMax(new BigDecimal("150000.00"));
		changes.setSalaryPeriod(SalaryPeriod.YEARLY);
		changes.setSalaryCurrency("USD");
		changes.setNotes("Follow up next week");
		when(repository.findById(id)).thenReturn(Optional.of(existing));
		when(repository.save(existing)).thenReturn(existing);

		assertSame(existing, service.update(id, changes));
		assertEquals("New Co", existing.getCompanyName());
		assertEquals("Senior Engineer", existing.getTitle());
		assertEquals("https://example.com/new", existing.getUrl());
		assertEquals("New address", existing.getCompanyAddress());
		assertEquals("Job description", existing.getContent());
		assertEquals(ApplicationStatus.APPLIED, existing.getStatus());
		assertEquals(WorkSetup.HYBRID, existing.getWorkSetup());
		assertEquals(ExperienceLevel.SENIOR, existing.getExperienceLevel());
		assertEquals(new BigDecimal("100000.00"), existing.getSalaryMin());
		assertEquals(new BigDecimal("150000.00"), existing.getSalaryMax());
		assertEquals(SalaryPeriod.YEARLY, existing.getSalaryPeriod());
		assertEquals("USD", existing.getSalaryCurrency());
		assertEquals("Follow up next week", existing.getNotes());
		verify(repository).save(existing);
	}

	@Test
	void updateThrowsWhenApplicationDoesNotExist() {
		UUID id = UUID.randomUUID();
		when(repository.findById(id)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class,
				() -> service.update(id, new JobApplication("Example Co", "Engineer", "https://example.com/job")));
	}

	@Test
	void deleteRemovesExistingApplication() {
		UUID id = UUID.randomUUID();
		JobApplication application = new JobApplication("Example Co", "Engineer", "https://example.com/job");
		when(repository.findById(id)).thenReturn(Optional.of(application));

		service.delete(id);

		verify(repository).delete(application);
	}
}
