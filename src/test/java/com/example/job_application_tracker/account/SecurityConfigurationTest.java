package com.example.job_application_tracker.account;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class SecurityConfigurationTest {

	@Test
	void throttledAuthenticationReturnsAResponseWithoutForwardingTheRequest() throws Exception {
		AuthenticationFailureHandler handler = new SecurityConfiguration().authenticationFailureHandler();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setContextPath("/tracker");
		HttpServletRequest request = spy(mockRequest);
		MockHttpServletResponse response = new MockHttpServletResponse();

		handler.onAuthenticationFailure(request, response,
				new ThrottledAuthenticationException(new RateLimitExceededException(30)));

		verify(request, never()).getRequestDispatcher("/login?throttled");
		assertThat(response.getStatus()).isEqualTo(429);
		assertThat(response.getHeader("Retry-After")).isEqualTo("30");
		assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
		assertThat(response.getContentType()).isEqualTo("text/plain;charset=UTF-8");
		assertThat(response.getContentAsString())
				.contains("Too many sign-in attempts. Please wait before trying again.")
				.contains("/tracker/login");
	}
}
