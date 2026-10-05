package com.example.job_application_tracker.account;

import com.example.job_application_tracker.jobapplication.JobApplicationService;
import com.example.job_application_tracker.jobapplication.model.JobApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
		properties = {
				"spring.docker.compose.enabled=false",
				"app.auth.throttling.registration-per-ip=1",
				"app.auth.throttling.login-per-ip=4",
				"app.auth.throttling.login-per-account=2",
				"app.auth.throttling.verification-per-ip=1",
				"app.auth.throttling.resend-per-ip=5",
				"app.auth.throttling.resend-per-account=1"
		})
@AutoConfigureMockMvc
@Import(AccountIntegrationTest.PostgresTestConfiguration.class)
class AccountIntegrationTest {

	private static final String EMAIL = "person@example.com";
	private static final String PASSWORD = "correct-horse-battery";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAccountRepository users;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private JobApplicationService applications;

	@Autowired
	private SessionRegistry sessionRegistry;

	@MockitoBean
	private JavaMailSender mailSender;

	@BeforeEach
	void clearAccountData() {
		jdbcTemplate.update("DELETE FROM job_applications");
		users.deleteAll();
		jdbcTemplate.update("DELETE FROM auth_rate_limits");
		reset(mailSender);
	}

	@Test
	void registrationRequiresVerificationAndOnlyThenAllowsLogin() throws Exception {
		MvcResult registration = mockMvc.perform(post("/register")
						.with(csrf())
						.param("email", "Person@Example.com")
						.param("firstName", "Jamie")
						.param("lastName", "Rivera")
						.param("password", PASSWORD)
						.param("confirmPassword", PASSWORD))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/register"))
				.andReturn();

		mockMvc.perform(get("/register").flashAttrs(registration.getFlashMap()))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"verification email will be sent shortly.")));

		UserAccount user = users.findByEmail(EMAIL).orElseThrow();
		assertThat(user.isEnabled()).isFalse();
		assertThat(user.getFirstName()).isEqualTo("Jamie");
		assertThat(user.getLastName()).isEqualTo("Rivera");
		assertThat(user.getPasswordHash()).isNotEqualTo(PASSWORD);
		assertThat(passwordEncoder.matches(PASSWORD, user.getPasswordHash())).isTrue();
		verify(mailSender, timeout(1_000)).send(any(SimpleMailMessage.class));
		String token = verificationTokenFromEmail();
		String storedDigest = jdbcTemplate.queryForObject(
				"SELECT token_digest FROM email_verification_tokens WHERE user_id = ?",
				String.class, user.getId());
		assertThat(storedDigest).hasSize(64).isNotEqualTo(token);

		mockMvc.perform(post("/login")
						.with(csrf())
						.param("email", EMAIL)
						.param("password", PASSWORD))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?error"));

		mockMvc.perform(post("/verify").with(csrf()).param("token", token))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Your email is verified")));
		assertThat(users.findByEmail(EMAIL).orElseThrow().isEnabled()).isTrue();

		jdbcTemplate.update("DELETE FROM auth_rate_limits WHERE action LIKE 'verify-%'");
		mockMvc.perform(post("/verify").with(csrf()).param("token", token))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("invalid, expired, or has already been used")));

		MvcResult login = mockMvc.perform(post("/login")
						.with(csrf())
						.param("email", EMAIL)
						.param("password", PASSWORD))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/applications"))
				.andReturn();

		mockMvc.perform(post("/account/password")
						.session((org.springframework.mock.web.MockHttpSession) login.getRequest().getSession(false))
						.with(csrf())
						.param("currentPassword", "incorrect-current")
						.param("newPassword", "new-correct-horse-password")
						.param("confirmPassword", "new-correct-horse-password"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Current password is incorrect")));

		mockMvc.perform(post("/account/password")
						.session((org.springframework.mock.web.MockHttpSession) login.getRequest().getSession(false))
						.with(csrf())
						.param("currentPassword", PASSWORD)
						.param("newPassword", "new-correct-horse-password")
						.param("confirmPassword", "new-correct-horse-password"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?passwordChanged=true"));
		assertThat(passwordEncoder.matches("new-correct-horse-password",
				users.findByEmail(EMAIL).orElseThrow().getPasswordHash())).isTrue();

		jdbcTemplate.update("DELETE FROM auth_rate_limits");
		mockMvc.perform(post("/login")
						.with(csrf())
						.param("email", EMAIL)
						.param("password", PASSWORD))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?error"));
	}

	@Test
	void registrationDoesNotWaitForVerificationEmail() throws Exception {
		CountDownLatch mailStarted = new CountDownLatch(1);
		CountDownLatch releaseMail = new CountDownLatch(1);
		doAnswer(invocation -> {
			mailStarted.countDown();
			assertThat(releaseMail.await(5, TimeUnit.SECONDS)).isTrue();
			return null;
		}).when(mailSender).send(any(SimpleMailMessage.class));

		var requestExecutor = Executors.newSingleThreadExecutor();
		try {
			var registration = requestExecutor.submit(() -> mockMvc.perform(post("/register")
							.with(csrf())
							.param("email", EMAIL)
							.param("firstName", "Taylor")
							.param("lastName", "Morgan")
							.param("password", PASSWORD)
							.param("confirmPassword", PASSWORD))
					.andReturn());

			assertThat(mailStarted.await(2, TimeUnit.SECONDS)).isTrue();
			assertThat(registration.get(1, TimeUnit.SECONDS).getResponse().getStatus()).isEqualTo(302);
		} finally {
			releaseMail.countDown();
			requestExecutor.shutdownNow();
		}
	}

	@Test
	void registrationRequiresCsrfAndRateLimitsWithRetryAfter() throws Exception {
		mockMvc.perform(post("/register")
						.param("email", EMAIL)
						.param("password", PASSWORD)
						.param("confirmPassword", PASSWORD))
				.andExpect(status().isForbidden());

		MockHttpServletRequestBuilder registration = post("/register")
				.with(csrf())
				.param("email", EMAIL)
				.param("firstName", "Taylor")
				.param("lastName", "Morgan")
				.param("password", PASSWORD)
				.param("confirmPassword", PASSWORD);
		mockMvc.perform(registration).andExpect(status().is3xxRedirection());
		mockMvc.perform(post("/register")
						.with(csrf())
						.param("email", "another@example.com")
						.param("firstName", "Taylor")
						.param("lastName", "Morgan")
						.param("password", PASSWORD)
						.param("confirmPassword", PASSWORD))
				.andExpect(status().isTooManyRequests())
				.andExpect(header().exists("Retry-After"));
	}

	@Test
	void registrationDisplaysFieldSpecificValidationErrors() throws Exception {
		mockMvc.perform(post("/register")
						.with(csrf())
						.param("email", "not-an-email")
						.param("firstName", "")
						.param("lastName", "")
						.param("password", "short")
						.param("confirmPassword", "different"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(
						"verification email will be sent shortly."))))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"data-field-error=\"email\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"data-field-error=\"firstName\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"data-field-error=\"lastName\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"id=\"firstName\" name=\"firstName\" type=\"text\" required maxlength=\"100\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"id=\"lastName\" name=\"lastName\" type=\"text\" required maxlength=\"100\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("must be a well-formed email address")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"data-field-error=\"password\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"data-field-error=\"confirmPassword\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"Passwords do not match.")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"type=\"password\" required minlength=\"12\" maxlength=\"72\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"id=\"confirmPassword\" name=\"confirmPassword\" type=\"password\" required minlength=\"12\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"maxlength=\"72\" autocomplete=\"new-password\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"aria-invalid=\"true\"")));
		assertThat(users.findByEmail("not-an-email")).isEmpty();
	}

	@Test
	void applicationRoutesRequireAuthenticationAndLogoutRequiresCsrf() throws Exception {
		mockMvc.perform(get("/applications"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
		mockMvc.perform(get("/register")).andExpect(status().isOk());
		mockMvc.perform(post("/logout").with(user(EMAIL)))
				.andExpect(status().isForbidden());
		mockMvc.perform(post("/logout").with(user(EMAIL)).with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?logout"));
	}

	@Test
	void authenticatedUsersAreRedirectedAwayFromAccountAccessRoutes() throws Exception {
		mockMvc.perform(get("/login").with(user(EMAIL)))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/applications"));
		mockMvc.perform(get("/register").with(user(EMAIL)))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/applications"));
		mockMvc.perform(get("/verify").with(user(EMAIL)))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/applications"));
		mockMvc.perform(get("/verification/resend").with(user(EMAIL)))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/applications"));
		mockMvc.perform(post("/login").with(user(EMAIL)).with(csrf())
						.param("email", EMAIL).param("password", PASSWORD))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/applications"));
		mockMvc.perform(post("/register").with(user(EMAIL)).with(csrf())
						.param("email", EMAIL).param("firstName", "Taylor").param("lastName", "Morgan")
						.param("password", PASSWORD).param("confirmPassword", PASSWORD))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/applications"));
		mockMvc.perform(post("/verify").with(user(EMAIL)).with(csrf()).param("token", "token"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/applications"));
		mockMvc.perform(post("/verification/resend").with(user(EMAIL)).with(csrf())
						.param("email", EMAIL))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/applications"));
	}

	@Test
	void applicationTopbarShowsProfileMenuAndStableAvatarColor() throws Exception {
		UserAccount profileAccount = saveVerifiedUser(EMAIL, "Jamie", "Rivera");

		String firstResponse = mockMvc.perform(get("/applications").with(user(profileAccount)))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Jamie Rivera")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("data-initials=\"JR\"")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Change password")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Log out")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")))
				.andReturn().getResponse().getContentAsString();
		String secondResponse = mockMvc.perform(get("/applications").with(user(profileAccount)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Matcher firstColor = Pattern.compile("data-avatar-color=\"([^\"]+)\"").matcher(firstResponse);
		Matcher secondColor = Pattern.compile("data-avatar-color=\"([^\"]+)\"").matcher(secondResponse);
		assertThat(firstColor.find()).isTrue();
		assertThat(secondColor.find()).isTrue();
		assertThat(firstColor.group(1)).isEqualTo(secondColor.group(1));
	}

	@Test
	void rateLimitsRepeatedLoginAttempts() throws Exception {
		saveVerifiedUser(EMAIL);
		mockMvc.perform(login(EMAIL, "wrong-password"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?error"));
		assertThat(jdbcTemplate.queryForObject(
				"SELECT MAX(request_count) FROM auth_rate_limits WHERE action = 'login-account'", Integer.class))
				.isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject(
				"SELECT MAX(request_count) FROM auth_rate_limits WHERE action = 'login-ip'", Integer.class))
				.isEqualTo(1);
		mockMvc.perform(login(EMAIL, "wrong-password"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?error"));
		assertThat(jdbcTemplate.queryForObject(
				"SELECT MAX(request_count) FROM auth_rate_limits WHERE action = 'login-account'", Integer.class))
				.isEqualTo(2);
		mockMvc.perform(login(EMAIL, PASSWORD))
				.andExpect(status().isTooManyRequests())
				.andExpect(header().exists("Retry-After"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"Too many sign-in attempts. Please wait before trying again.")));
	}

	@Test
	void verificationAndResendEndpointsAreThrottled() throws Exception {
		mockMvc.perform(post("/register")
						.with(csrf())
						.param("email", EMAIL)
						.param("firstName", "Taylor")
						.param("lastName", "Morgan")
						.param("password", PASSWORD)
						.param("confirmPassword", PASSWORD))
				.andExpect(status().is3xxRedirection());
		String token = verificationTokenFromEmail();

		mockMvc.perform(post("/verify").with(csrf()).param("token", token))
				.andExpect(status().isOk());
		mockMvc.perform(post("/verify").with(csrf()).param("token", token))
				.andExpect(status().isTooManyRequests())
				.andExpect(header().exists("Retry-After"));

		jdbcTemplate.update("DELETE FROM auth_rate_limits WHERE action LIKE 'verify-%'");
		users.save(new UserAccount("pending@example.com", passwordEncoder.encode(PASSWORD)));
		mockMvc.perform(post("/verification/resend").with(csrf()).param("email", "pending@example.com"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/verification/resend?sent=true"));
		mockMvc.perform(post("/verification/resend").with(csrf()).param("email", "pending@example.com"))
				.andExpect(status().isTooManyRequests())
				.andExpect(header().exists("Retry-After"));
	}

	@Test
	void unknownAccountLoginUsesTheGenericFailurePath() throws Exception {
		mockMvc.perform(login("unknown@example.com", "invalid-password"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?error"));
	}

	@Test
	void changingPasswordExpiresOtherAuthenticatedSessions() throws Exception {
		saveVerifiedUser(EMAIL);
		org.springframework.mock.web.MockHttpSession firstSession =
				(org.springframework.mock.web.MockHttpSession) mockMvc.perform(login(EMAIL, PASSWORD))
						.andExpect(status().is3xxRedirection())
						.andReturn().getRequest().getSession(false);
		org.springframework.mock.web.MockHttpSession secondSession =
				(org.springframework.mock.web.MockHttpSession) mockMvc.perform(login(EMAIL, PASSWORD))
						.andExpect(status().is3xxRedirection())
						.andReturn().getRequest().getSession(false);
		assertThat(sessionRegistry.getAllPrincipals()).hasSize(2);

		mockMvc.perform(post("/account/password")
						.session(firstSession)
						.with(csrf())
						.param("currentPassword", PASSWORD)
						.param("newPassword", "updated-account-password")
						.param("confirmPassword", "updated-account-password"))
				.andExpect(status().is3xxRedirection());

		mockMvc.perform(get("/applications").session(secondSession))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?expired"));
	}

	@Test
	void applicationQueriesAndMutationsAreScopedToAuthenticatedOwner() throws Exception {
		UserAccount first = saveVerifiedUser("first@example.com");
		saveVerifiedUser("second@example.com");
		JobApplication application = applications.create(first.getEmail(),
				new JobApplication("Owned Co", "Engineer", "https://example.com/job"));

		mockMvc.perform(get("/applications")
						.with(user("second@example.com")))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.not(
						org.hamcrest.Matchers.containsString("Owned Co"))));

		mockMvc.perform(get("/applications/{id}", application.getId())
						.with(user("second@example.com")))
				.andExpect(status().isNotFound());

		mockMvc.perform(post("/applications/{id}/delete", application.getId())
						.with(user("second@example.com"))
						.with(csrf()))
				.andExpect(status().isNotFound());
		assertThat(applications.getById(first.getEmail(), application.getId()).getCompanyName())
				.isEqualTo("Owned Co");
	}

	private MockHttpServletRequestBuilder login(String email, String password) {
		return post("/login").with(csrf()).param("email", email).param("password", password);
	}

	private UserAccount saveVerifiedUser(String email) {
		return saveVerifiedUser(email, "Account", "User");
	}

	private UserAccount saveVerifiedUser(String email, String firstName, String lastName) {
		UserAccount user = new UserAccount(email, passwordEncoder.encode(PASSWORD), firstName, lastName);
		user.verifyEmail(java.time.OffsetDateTime.now());
		return users.save(user);
	}

	private String verificationTokenFromEmail() {
		org.mockito.ArgumentCaptor<SimpleMailMessage> messages =
				org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(messages.capture());
		Matcher matcher = Pattern.compile("token=([A-Za-z0-9_-]+)")
				.matcher(messages.getValue().getText());
		assertThat(matcher.find()).isTrue();
		return matcher.group(1);
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class PostgresTestConfiguration {

		@Bean
		@ServiceConnection
		PostgreSQLContainer postgresContainer() {
			return new PostgreSQLContainer("postgres:17-alpine");
		}
	}
}
