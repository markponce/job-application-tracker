package com.example.job_application_tracker.account;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class AuthRateLimiterTest {

	@Test
	void doesNotEnforceLimitsWhenThrottlingIsDisabled() {
		JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
		AuthThrottleProperties properties = new AuthThrottleProperties();
		properties.setEnabled(false);
		AuthRateLimiter rateLimiter = new AuthRateLimiter(jdbcTemplate, properties);

		rateLimiter.enforce("login-ip", "127.0.0.1", 1, Duration.ofMinutes(1));

		verifyNoInteractions(jdbcTemplate);
	}
}
