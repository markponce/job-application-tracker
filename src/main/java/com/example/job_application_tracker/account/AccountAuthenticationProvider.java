package com.example.job_application_tracker.account;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class AccountAuthenticationProvider implements AuthenticationProvider {

	private static final String DUMMY_HASH = "$2a$10$7EqJtq98hPqEX7fNZaFWoO5u7wT5EU9o5FMd6E7e6s0V6M5RkT8XW";

	private final UserAccountRepository users;
	private final PasswordEncoder passwordEncoder;
	private final AuthRateLimiter rateLimiter;
	private final AuthThrottleProperties throttle;

	public AccountAuthenticationProvider(
			UserAccountRepository users,
			PasswordEncoder passwordEncoder,
			AuthRateLimiter rateLimiter,
			AuthThrottleProperties throttle) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.rateLimiter = rateLimiter;
		this.throttle = throttle;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String email = AccountService.normalizeEmail(authentication.getName());
		String remoteAddress = authentication.getDetails() instanceof WebAuthenticationDetails details
				? details.getRemoteAddress() : "unknown";
		try {
			rateLimiter.enforce("login-ip", remoteAddress, throttle.getLoginPerIp(),
					Duration.ofSeconds(throttle.getLoginWindowSeconds()));
			rateLimiter.enforce("login-account", email, throttle.getLoginPerAccount(),
					Duration.ofSeconds(throttle.getLoginWindowSeconds()));
		} catch (RateLimitExceededException exception) {
			throw new ThrottledAuthenticationException(exception);
		}

		Optional<UserAccount> account = users.findByEmail(email);
		String hash = account.map(UserAccount::getPasswordHash).orElse(DUMMY_HASH);
		String rawPassword = authentication.getCredentials() instanceof String credential ? credential : "";
		boolean passwordMatches = passwordEncoder.matches(rawPassword, hash);
		if (account.isEmpty() || !passwordMatches) {
			throw new BadCredentialsException("Invalid email or password.");
		}
		UserAccount user = account.get();
		if (!user.isEnabled()) {
			throw new DisabledException("Email verification is required.");
		}
		return UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
