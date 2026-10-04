package com.example.job_application_tracker.account;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CsrfModelAdvice {

	@ModelAttribute
	public void addCsrfToken(HttpServletRequest request, Model model) {
		model.addAttribute("csrfToken", request.getAttribute(CsrfToken.class.getName()));
	}

	@ModelAttribute("accountProfile")
	public AccountProfile addAccountProfile(HttpServletRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return AccountProfile.fromPrincipal(request.getUserPrincipal());
		}
		return AccountProfile.fromPrincipal(authentication);
	}
}
