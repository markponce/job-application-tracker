package com.example.job_application_tracker.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.nio.charset.StandardCharsets;

public record RegistrationForm(
		@NotBlank @Email @Size(max = 320) String email,
		@NotBlank @Size(max = 100) String firstName,
		@NotBlank @Size(max = 100) String lastName,
		@NotBlank @Size(min = 12, max = 72) String password,
		@NotBlank String confirmPassword) {

	@jakarta.validation.constraints.AssertTrue(message = "Password must not exceed 72 UTF-8 bytes.")
	public boolean isPasswordWithinBcryptLimit() {
		return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
	}
}
