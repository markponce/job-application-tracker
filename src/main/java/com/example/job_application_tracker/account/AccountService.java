package com.example.job_application_tracker.account;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class AccountService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final long VERIFICATION_EXPIRY_HOURS = 24;

    private final UserAccountRepository users;
    private final EmailVerificationTokenRepository tokens;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;
    private final AuthRateLimiter rateLimiter;
    private final AuthThrottleProperties throttle;
    private final String publicBaseUrl;
    private final String mailFrom;
    private final boolean emailVerificationEnabled;

    public AccountService(
            UserAccountRepository users,
            EmailVerificationTokenRepository tokens,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher events,
            AuthRateLimiter rateLimiter,
            AuthThrottleProperties throttle,
            @Value("${app.public-base-url:http://localhost:8080}") String publicBaseUrl,
            @Value("${app.mail.from:no-reply@localhost}") String mailFrom,
            @Value("${app.auth.email-verification.enabled:true}") boolean emailVerificationEnabled) {
        this.users = users;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
        this.events = events;
        this.rateLimiter = rateLimiter;
        this.throttle = throttle;
        this.publicBaseUrl = publicBaseUrl;
        this.mailFrom = mailFrom;
        this.emailVerificationEnabled = emailVerificationEnabled;
    }

    public static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public boolean register(String emailInput, String firstName, String lastName,
                            String password, String remoteAddress) {
        rateLimiter.enforce("register-ip", remoteAddress, throttle.getRegistrationPerIp(),
                java.time.Duration.ofSeconds(throttle.getRegistrationWindowSeconds()));
        String email = normalizeEmail(emailInput);
        if (users.existsByEmail(email)) {
            return false;
        }

        UserAccount user = users.save(new UserAccount(
                email, passwordEncoder.encode(password), firstName, lastName));
        if (emailVerificationEnabled) {
            sendVerification(user);
        } else {
            user.verifyEmail(OffsetDateTime.now());
        }
        return true;
    }

    @Transactional
    public boolean verify(String rawToken) {
        if (rawToken == null || rawToken.isBlank() || rawToken.length() > 128) {
            return false;
        }
        String digest = digest(rawToken);
        OffsetDateTime now = OffsetDateTime.now();
        EmailVerificationToken token = tokens.findByTokenDigest(digest).orElse(null);
        if (token == null || token.getUsedAt() != null || !token.getExpiresAt().isAfter(now)) {
            return false;
        }
        rateLimiter.enforce("verify-account", token.getUser().getEmail(), throttle.getVerificationPerAccount(),
                java.time.Duration.ofSeconds(throttle.getVerificationWindowSeconds()));
        if (tokens.consume(digest, now) != 1) {
            return false;
        }
        token.getUser().verifyEmail(now);
        return true;
    }

    @Transactional
    public void resendVerification(String emailInput, String remoteAddress) {
        if (!emailVerificationEnabled) {
            return;
        }
        rateLimiter.enforce(
                "resend-ip",
                remoteAddress,
                throttle.getResendPerIp(),
                java.time.Duration.ofSeconds(throttle.getResendWindowSeconds())
        );

        String email = normalizeEmail(emailInput);
        rateLimiter.enforce(
                "resend-account",
                email,
                throttle.getResendPerAccount(),
                java.time.Duration.ofSeconds(throttle.getResendWindowSeconds())
        );

        users.findByEmail(email).filter(user -> !user.isEnabled()).ifPresent(this::sendVerification);
    }

    @Transactional
    public void changePassword(UserAccount user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        users.save(user);
    }

    @Transactional(readOnly = true)
    public UserAccount getByEmail(String email) {
        return users.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new IllegalStateException("Authenticated account no longer exists"));
    }

    private void sendVerification(UserAccount user) {
        tokens.deleteByUserId(user.getId());
        byte[] randomToken = new byte[32];
        SECURE_RANDOM.nextBytes(randomToken);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomToken);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(VERIFICATION_EXPIRY_HOURS);
        tokens.save(new EmailVerificationToken(user, digest(rawToken), expiresAt));
        String link = UriComponentsBuilder.fromUriString(publicBaseUrl)
                .path("/verify")
                .queryParam("token", rawToken)
                .build()
                .encode()
                .toUriString();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getEmail());
        message.setSubject("Verify your ApplyTrack account");
        message.setText("Verify your email within 24 hours using this link:\n\n" + link);
        events.publishEvent(message);
    }

    private String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
