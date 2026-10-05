package com.example.job_application_tracker.account;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


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
			SessionRegistry sessionRegistry,
			AuthenticationFailureHandler authenticationFailureHandler) throws Exception {
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
						.failureHandler(authenticationFailureHandler)
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
	AuthenticationFailureHandler authenticationFailureHandler(ViewResolver viewResolver) {
		return (request, response, exception) -> {
			ThrottledAuthenticationException throttled = throttledException(exception);
			if (throttled != null) {
				response.setHeader("Retry-After", Long.toString(throttled.getRetryAfterSeconds()));
				response.setHeader("Cache-Control", "no-store");
				response.setStatus(429);
				renderThrottledLogin(request, response, viewResolver);
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

	private void renderThrottledLogin(
			HttpServletRequest request,
			HttpServletResponse response,
			ViewResolver viewResolver) throws IOException, ServletException {
		Object csrfToken = request.getAttribute(CsrfToken.class.getName());
		if (csrfToken == null) {
			csrfToken = request.getAttribute("_csrf");
		}
		if (!(csrfToken instanceof CsrfToken)) {
			throw new ServletException("CSRF token is unavailable for the throttled login view");
		}

		Map<String, Object> model = new HashMap<>();
		model.put("loginError", false);
		model.put("loggedOut", false);
		model.put("sessionExpired", false);
		model.put("passwordChanged", false);
		model.put("loginThrottled", true);
		model.put("csrfToken", csrfToken);

		Locale locale = LocaleContextHolder.getLocale();
		View view;
		try {
			view = viewResolver.resolveViewName("pages/account/login", locale);
			if (view == null) {
				throw new ServletException("Login view could not be resolved");
			}
			view.render(model, request, response);
		} catch (ServletException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new ServletException("Failed to render throttled login view", exception);
		}
	}
}
