package com.example.job_application_tracker.account;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

final class AuthenticatedAccountRouteFilter extends OncePerRequestFilter {

	private static final Set<String> ACCOUNT_ACCESS_ROUTES =
			Set.of("/login", "/register", "/verify", "/verification/resend");

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String path = request.getRequestURI().substring(request.getContextPath().length());
		if (ACCOUNT_ACCESS_ROUTES.contains(path)
				&& authentication != null
				&& authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken)) {
			response.sendRedirect(request.getContextPath() + "/applications");
			return;
		}
		filterChain.doFilter(request, response);
	}
}
