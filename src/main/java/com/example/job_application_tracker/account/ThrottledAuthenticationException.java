package com.example.job_application_tracker.account;

import org.springframework.security.core.AuthenticationException;

public class ThrottledAuthenticationException extends AuthenticationException {

	private final long retryAfterSeconds;

	public ThrottledAuthenticationException(RateLimitExceededException cause) {
		super("Too many authentication attempts", cause);
		this.retryAfterSeconds = cause.getRetryAfterSeconds();
	}

	public long getRetryAfterSeconds() {
		return retryAfterSeconds;
	}
}
