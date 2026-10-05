package com.example.job_application_tracker.account;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;


@Configuration
@EnableScheduling
@EnableConfigurationProperties(AuthThrottleProperties.class)
public class SecurityConfiguration {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			AccountAuthenticationProvider authenticationProvider,
			SessionRegistry sessionRegistry) throws Exception {
		http
				.authenticationManager(new ProviderManager(authenticationProvider))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/", "/login", "/register", "/verify", "/verification/resend",
								"/error", "/favicon.ico", "/css/**").permitAll()
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						.loginProcessingUrl("/login")
						.usernameParameter("email")
						.failureHandler(authenticationFailureHandler())
						.defaultSuccessUrl("/applications", true)
						.permitAll())
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout")
						.invalidateHttpSession(true)
						.clearAuthentication(true)
						.permitAll())
				.headers(headers -> headers
						.referrerPolicy(policy -> policy.policy(
								ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
				.sessionManagement(session -> session
						.sessionFixation(fixation -> fixation.migrateSession())
						.maximumSessions(-1)
						.expiredUrl("/login?expired")
						.sessionRegistry(sessionRegistry));
		http.addFilterBefore(new AuthenticatedAccountRouteFilter(), UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	AuthenticationFailureHandler authenticationFailureHandler() {
		return (request, response, exception) -> {
			ThrottledAuthenticationException throttled = throttledException(exception);
			if (throttled != null) {
				response.setHeader("Retry-After", Long.toString(throttled.getRetryAfterSeconds()));
				response.setHeader("Cache-Control", "no-store");
				response.setStatus(429);
				response.setContentType("text/plain;charset=UTF-8");
				response.getWriter().write("Too many sign-in attempts. Please wait before trying again. Sign in at "
						+ request.getContextPath() + "/login.");
				return;
			}
			response.sendRedirect(request.getContextPath() + "/login?error");
		};
	}

	@Bean
	HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

	@Bean
	SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

	private ThrottledAuthenticationException throttledException(AuthenticationException exception) {
		Throwable cause = exception;
		while (cause != null) {
			if (cause instanceof ThrottledAuthenticationException throttled) {
				return throttled;
			}
			cause = cause.getCause();
		}
		return null;
	}
}
