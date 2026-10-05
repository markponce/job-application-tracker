package com.example.job_application_tracker;

import com.example.job_application_tracker.account.AccountProfile;
import com.example.job_application_tracker.account.RateLimitExceededException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalWebExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalWebExceptionHandler.class);

	@ExceptionHandler(EntityNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String notFound(
			EntityNotFoundException exception,
			HttpServletRequest request,
			Model model) {
		logHandledException(request, HttpStatus.NOT_FOUND, exception);
		Object csrfToken = request.getAttribute(CsrfToken.class.getName());
		model.addAttribute("csrfToken", csrfToken != null ? csrfToken : request.getAttribute("_csrf"));
		model.addAttribute("accountProfile", AccountProfile.fromPrincipal(request.getUserPrincipal()));
		return "pages/errors/404";
	}

	@ExceptionHandler(RateLimitExceededException.class)
	@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
	public String rateLimited(
			RateLimitExceededException exception,
			HttpServletRequest request,
			HttpServletResponse response,
			Model model) {
		logHandledException(request, HttpStatus.TOO_MANY_REQUESTS, exception);
		response.setHeader("Retry-After", Long.toString(exception.getRetryAfterSeconds()));
		model.addAttribute("retryAfterSeconds", exception.getRetryAfterSeconds());
		return "pages/account/rate-limited";
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String internalServerError(HttpServletRequest request, Model model, Exception exception) {
		logHandledException(request, HttpStatus.INTERNAL_SERVER_ERROR, exception);
		Object csrfToken = request.getAttribute(CsrfToken.class.getName());
		model.addAttribute("csrfToken", csrfToken != null ? csrfToken : request.getAttribute("_csrf"));
		model.addAttribute("accountProfile", AccountProfile.fromPrincipal(request.getUserPrincipal()));
		return "pages/errors/500";
	}

	private void logHandledException(HttpServletRequest request, HttpStatus status, Exception exception) {
		if (status.is5xxServerError()) {
			logger.error("Unhandled exception while processing request {}", request.getRequestURI(), exception);
		} else if (status.is4xxClientError()) {
			logger.warn("Request {} handled with HTTP {}: {}",
					request.getRequestURI(), status.value(), exception.getMessage());
		}
	}
}
