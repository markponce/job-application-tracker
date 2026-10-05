package com.example.job_application_tracker.account;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.security.Principal;
import java.util.List;
import java.util.Objects;

@Controller
public class AccountController {

	private final AccountService accountService;
	private final AuthRateLimiter rateLimiter;
	private final AuthThrottleProperties throttle;
	private final SessionRegistry sessionRegistry;

	public AccountController(AccountService accountService, AuthRateLimiter rateLimiter,
			AuthThrottleProperties throttle, SessionRegistry sessionRegistry) {
		this.accountService = accountService;
		this.rateLimiter = rateLimiter;
		this.throttle = throttle;
		this.sessionRegistry = sessionRegistry;
	}

	@GetMapping("/login")
	public String login(
			@RequestParam(required = false) String error,
			@RequestParam(required = false) String logout,
			@RequestParam(required = false) String expired,
			@RequestParam(required = false) String passwordChanged,
			@RequestParam(required = false) String throttled,
			Model model) {
		model.addAttribute("loginError", error != null);
		model.addAttribute("loggedOut", logout != null);
		model.addAttribute("sessionExpired", expired != null);
		model.addAttribute("passwordChanged", passwordChanged != null);
		model.addAttribute("loginThrottled", throttled != null);
		return "pages/account/login";
	}

	@GetMapping("/register")
	public String registerPage(Model model) {
		model.addAttribute("form", new RegistrationForm("", "", "", "", ""));
		model.addAttribute("formErrors", List.of());
		model.addAttribute("emailErrors", List.of());
		model.addAttribute("firstNameErrors", List.of());
		model.addAttribute("lastNameErrors", List.of());
		model.addAttribute("passwordErrors", List.of());
		model.addAttribute("confirmPasswordErrors", List.of());
		if (!model.containsAttribute("registrationSubmitted")) {
			model.addAttribute("registrationSubmitted", false);
		}
		return "pages/account/register";
	}

	@PostMapping("/register")
	public String register(
			@Valid @ModelAttribute("form") RegistrationForm form,
			BindingResult bindingResult,
			HttpServletRequest request,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (!Objects.equals(form.password(), form.confirmPassword())) {
			bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match.");
		}
		if (bindingResult.hasErrors()) {
			model.addAttribute("formErrors", bindingResult.getAllErrors().stream()
					.map(error -> error.getDefaultMessage()).toList());
			model.addAttribute("emailErrors", fieldErrors(bindingResult, "email"));
			model.addAttribute("firstNameErrors", fieldErrors(bindingResult, "firstName"));
			model.addAttribute("lastNameErrors", fieldErrors(bindingResult, "lastName"));
			model.addAttribute("passwordErrors", fieldErrors(bindingResult, "password"));
			model.addAttribute("confirmPasswordErrors", fieldErrors(bindingResult, "confirmPassword"));
			model.addAttribute("registrationSubmitted", false);
			return "pages/account/register";
		}
		accountService.register(form.email(), form.firstName(), form.lastName(),
				form.password(), request.getRemoteAddr());
		redirectAttributes.addFlashAttribute("registrationSubmitted", true);
		return "redirect:/register";
	}

	@GetMapping("/verify")
	public String verifyPage(@RequestParam(required = false) String token, Model model) {
		model.addAttribute("verificationToken", token == null ? "" : token);
		model.addAttribute("verificationComplete", false);
		model.addAttribute("verificationSucceeded", false);
		return "pages/account/verify";
	}

	@PostMapping("/verify")
	public String verify(
			@RequestParam String token,
			HttpServletRequest request,
			Model model) {
		rateLimiter.enforce("verify-ip", request.getRemoteAddr(), throttle.getVerificationPerIp(),
				Duration.ofSeconds(throttle.getVerificationWindowSeconds()));
		model.addAttribute("verificationToken", "");
		model.addAttribute("verificationComplete", true);
		model.addAttribute("verificationSucceeded", accountService.verify(token));
		return "pages/account/verify";
	}

	@GetMapping("/verification/resend")
	public String resendPage(
			@RequestParam(required = false) String sent,
			Model model) {
		model.addAttribute("resendSent", sent != null);
		model.addAttribute("form", new ResendVerificationForm(""));
		model.addAttribute("emailErrors", List.of());
		return "pages/account/resend-verification";
	}

	@PostMapping("/verification/resend")
	public String resend(
			@Valid @ModelAttribute("form") ResendVerificationForm form,
			BindingResult bindingResult,
			HttpServletRequest request,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("resendSent", false);
			model.addAttribute("emailErrors", fieldErrors(bindingResult, "email"));
			return "pages/account/resend-verification";
		}
		accountService.resendVerification(form.email(), request.getRemoteAddr());
		redirectAttributes.addAttribute("sent", "true");
		return "redirect:/verification/resend";
	}

	@GetMapping("/account/password")
	public String passwordPage(Model model) {
		model.addAttribute("form", new PasswordChangeForm("", "", ""));
		model.addAttribute("formErrors", List.of());
		return "pages/account/password";
	}

	@PostMapping("/account/password")
	public String changePassword(
			Principal principal,
			@Valid @ModelAttribute("form") PasswordChangeForm form,
			BindingResult bindingResult,
			Model model,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		if (!Objects.equals(form.newPassword(), form.confirmPassword())) {
			bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match.");
		}
		if (bindingResult.hasErrors()) {
			model.addAttribute("formErrors", bindingResult.getAllErrors().stream()
					.map(error -> error.getDefaultMessage()).toList());
			return "pages/account/password";
		}
		try {
			UserAccount user = accountService.getByEmail(principal.getName());
			accountService.changePassword(user, form.currentPassword(), form.newPassword());
			sessionRegistry.getAllPrincipals().stream()
					.filter(account -> account instanceof UserAccount userAccount
							&& userAccount.getId().equals(user.getId()))
					.flatMap(account -> sessionRegistry.getAllSessions(account, false).stream())
					.forEach(SessionInformation::expireNow);
		} catch (IllegalArgumentException exception) {
			model.addAttribute("formErrors", List.of(exception.getMessage()));
			return "pages/account/password";
		}
		SecurityContextHolder.clearContext();
		if (request.getSession(false) != null) {
			request.getSession(false).invalidate();
		}
		redirectAttributes.addAttribute("passwordChanged", true);
		return "redirect:/login";
	}

	private List<String> fieldErrors(BindingResult bindingResult, String field) {
		return bindingResult.getFieldErrors(field).stream()
				.map(error -> error.getDefaultMessage())
				.toList();
	}
}
