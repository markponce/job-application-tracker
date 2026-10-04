package com.example.job_application_tracker.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.nio.charset.StandardCharsets;

public record PasswordChangeForm(
		@NotBlank String currentPassword,
		@NotBlank @Size(min = 12, max = 72) String newPassword,
		@NotBlank String confirmPassword) {

	@jakarta.validation.constraints.AssertTrue(message = "Password must not exceed 72 UTF-8 bytes.")
	public boolean isPasswordWithinBcryptLimit() {
		return newPassword == null || newPassword.getBytes(StandardCharsets.UTF_8).length <= 72;
	}
}
