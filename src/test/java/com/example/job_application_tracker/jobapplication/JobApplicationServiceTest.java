package com.example.job_application_tracker.jobapplication;

import com.example.job_application_tracker.account.UserAccount;
import com.example.job_application_tracker.account.UserAccountRepository;
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

	@Mock
	private UserAccountRepository users;

	private JobApplicationService service;
	private UserAccount owner;

	@BeforeEach
	void setUp() {
		service = new JobApplicationService(repository, users);
		owner = new UserAccount("owner@example.com", "hash");
		when(users.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
	}

	@Test
	void createSavesAndReturnsApplication() {
		JobApplication application = new JobApplication("Example Co", "Engineer", "https://example.com/job");
		when(repository.save(application)).thenReturn(application);

		assertSame(application, service.create("owner@example.com", application));
		assertSame(owner, application.getUser());
		verify(repository).save(application);
	}

	@Test
	void getByIdReturnsApplicationWhenFound() {
		UUID id = UUID.randomUUID();
		JobApplication application = new JobApplication("Example Co", "Engineer", "https://example.com/job");
		when(repository.findByIdAndUser_Id(id, null)).thenReturn(Optional.of(application));

		assertSame(application, service.getById("owner@example.com", id));
	}

	@Test
	void getByIdThrowsWhenApplicationDoesNotExist() {
		UUID id = UUID.randomUUID();
		when(repository.findByIdAndUser_Id(id, null)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.getById("owner@example.com", id));
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
		when(repository.findByIdAndUser_Id(id, null)).thenReturn(Optional.of(existing));
		when(repository.save(existing)).thenReturn(existing);

		assertSame(existing, service.update("owner@example.com", id, changes));
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
		when(repository.findByIdAndUser_Id(id, null)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class,
				() -> service.update("owner@example.com", id,
						new JobApplication("Example Co", "Engineer", "https://example.com/job")));
	}

	@Test
	void deleteRemovesExistingApplication() {
		UUID id = UUID.randomUUID();
		JobApplication application = new JobApplication("Example Co", "Engineer", "https://example.com/job");
		when(repository.findByIdAndUser_Id(id, null)).thenReturn(Optional.of(application));

		service.delete("owner@example.com", id);

		verify(repository).delete(application);
	}
}
