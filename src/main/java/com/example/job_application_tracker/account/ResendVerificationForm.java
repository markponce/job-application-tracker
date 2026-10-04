package com.example.job_application_tracker.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResendVerificationForm(
		@NotBlank @Email @Size(max = 320) String email) {
}
