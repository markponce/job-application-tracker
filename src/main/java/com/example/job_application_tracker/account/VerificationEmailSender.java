package com.example.job_application_tracker.account;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class VerificationEmailSender {

	private final JavaMailSender mailSender;

	public VerificationEmailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void send(SimpleMailMessage message) {
		mailSender.send(message);
	}
}
