package com.example.job_application_tracker.account;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityConfigurationTest {

	@Test
	void throttledAuthenticationRendersLoginViewWithTooManyRequestsStatus() throws Exception {
		ViewResolver viewResolver = mock(ViewResolver.class);
		View view = mock(View.class);
		when(viewResolver.resolveViewName(eq("pages/account/login"), any(Locale.class)))
				.thenReturn(view);
		AuthenticationFailureHandler handler =
				new SecurityConfiguration().authenticationFailureHandler(viewResolver);
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setContextPath("/tracker");
		mockRequest.setAttribute(CsrfToken.class.getName(), mock(CsrfToken.class));
		MockHttpServletResponse response = new MockHttpServletResponse();

		handler.onAuthenticationFailure(mockRequest, response,
				new ThrottledAuthenticationException(new RateLimitExceededException(30)));

		assertThat(response.getStatus()).isEqualTo(429);
		assertThat(response.getHeader("Retry-After")).isEqualTo("30");
		assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
		verify(view).render(argThat(model -> Boolean.TRUE.equals(model.get("loginThrottled"))
						&& model.get("csrfToken") instanceof CsrfToken),
				same(mockRequest), same(response));
	}
}
