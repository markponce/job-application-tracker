package com.example.job_application_tracker.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

	Optional<EmailVerificationToken> findByTokenDigest(String tokenDigest);

	@Modifying
	@Query("update EmailVerificationToken t set t.usedAt = :now "
			+ "where t.tokenDigest = :digest and t.usedAt is null and t.expiresAt > :now")
	int consume(@Param("digest") String digest, @Param("now") OffsetDateTime now);

	void deleteByUserId(UUID userId);
}
