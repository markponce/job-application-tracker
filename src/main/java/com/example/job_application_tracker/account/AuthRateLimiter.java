package com.example.job_application_tracker.account;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;

@Service
public class AuthRateLimiter {

	private final JdbcTemplate jdbcTemplate;
	private final AuthThrottleProperties throttle;

	public AuthRateLimiter(JdbcTemplate jdbcTemplate, AuthThrottleProperties throttle) {
		this.jdbcTemplate = jdbcTemplate;
		this.throttle = throttle;
	}

	public void enforce(String action, String identity, int limit, Duration window) {
		if (!throttle.isEnabled()) {
			return;
		}
		Instant now = Instant.now();
		long windowSeconds = window.toSeconds();
		long bucketEpoch = now.getEpochSecond() / windowSeconds * windowSeconds;
		OffsetDateTime bucketStart = OffsetDateTime.ofInstant(Instant.ofEpochSecond(bucketEpoch), ZoneOffset.UTC);
		String keyHash = digest(identity);
		Integer count = jdbcTemplate.queryForObject("""
				INSERT INTO auth_rate_limits (action, key_hash, bucket_start, request_count)
				VALUES (?, ?, ?, 1)
				ON CONFLICT (action, key_hash, bucket_start)
				DO UPDATE SET request_count = auth_rate_limits.request_count + 1
				RETURNING request_count
				""", Integer.class, action, keyHash, bucketStart);
		if (count != null && count > limit) {
			long retryAfter = Math.max(1, bucketEpoch + windowSeconds - now.getEpochSecond());
			throw new RateLimitExceededException(retryAfter);
		}
	}

	@Scheduled(cron = "0 17 * * * *")
	public void removeExpiredBuckets() {
		long retentionSeconds = Math.max(
				Math.max(throttle.getLoginWindowSeconds(), throttle.getRegistrationWindowSeconds()),
				Math.max(throttle.getVerificationWindowSeconds(), throttle.getResendWindowSeconds()));
		jdbcTemplate.update("DELETE FROM auth_rate_limits WHERE bucket_start < ?",
				OffsetDateTime.now().minusSeconds(retentionSeconds));
	}

	private String digest(String value) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}
}
