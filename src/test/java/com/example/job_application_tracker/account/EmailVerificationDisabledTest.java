package com.example.job_application_tracker.account;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailVerificationDisabledTest {

	@Test
	void registeringWithoutEmailVerificationEnablesAccountWithoutSendingEmail() {
		UserAccountRepository users = mock(UserAccountRepository.class);
		EmailVerificationTokenRepository tokens = mock(EmailVerificationTokenRepository.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
		AuthRateLimiter rateLimiter = mock(AuthRateLimiter.class);
		AuthThrottleProperties throttle = new AuthThrottleProperties();
		when(users.existsByEmail("person@example.com")).thenReturn(false);
		when(users.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(passwordEncoder.encode("strong-password")).thenReturn("encoded-password");

		AccountService accountService = new AccountService(
				users, tokens, passwordEncoder, events, rateLimiter, throttle,
				"http://localhost:8080", "no-reply@example.com", false);

		assertThat(accountService.register(
				"person@example.com", "Jamie", "Rivera", "strong-password", "127.0.0.1")).isTrue();

		var accountCaptor = org.mockito.ArgumentCaptor.forClass(UserAccount.class);
		verify(users).save(accountCaptor.capture());
		UserAccount account = accountCaptor.getValue();
		assertThat(account.isEnabled()).isTrue();
		assertThat(account.getEmailVerifiedAt()).isNotNull();
		verify(tokens, never()).save(any(EmailVerificationToken.class));
		verify(events, never()).publishEvent(any(Object.class));
	}

	@Test
	void disabledVerificationAllowsExistingUnverifiedAccountsToSignIn() {
		UserAccountRepository users = mock(UserAccountRepository.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		AuthRateLimiter rateLimiter = mock(AuthRateLimiter.class);
		UserAccount account = new UserAccount(
				"person@example.com", "encoded-password", "Jamie", "Rivera");
		when(users.findByEmail("person@example.com")).thenReturn(Optional.of(account));
		when(passwordEncoder.matches("strong-password", "encoded-password")).thenReturn(true);

		AccountAuthenticationProvider authenticationProvider = new AccountAuthenticationProvider(
				users, passwordEncoder, rateLimiter, new AuthThrottleProperties(), false);

		var authentication = authenticationProvider.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated("person@example.com", "strong-password"));

		assertThat(authentication.isAuthenticated()).isTrue();
		assertThat(authentication.getPrincipal()).isSameAs(account);
	}
}
