CREATE TABLE user_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    email_verified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT user_accounts_email_unique UNIQUE (email)
);

CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    token_digest VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ
);

CREATE INDEX email_verification_tokens_user_id_idx
    ON email_verification_tokens(user_id);
CREATE INDEX email_verification_tokens_expiry_idx
    ON email_verification_tokens(expires_at);

CREATE TABLE auth_rate_limits (
    action VARCHAR(40) NOT NULL,
    key_hash VARCHAR(64) NOT NULL,
    bucket_start TIMESTAMPTZ NOT NULL,
    request_count INTEGER NOT NULL,
    PRIMARY KEY (action, key_hash, bucket_start)
);

-- Existing application records are intentionally disposable per the approved migration policy.
DELETE FROM job_applications;

ALTER TABLE job_applications
    ADD COLUMN user_id UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE;

CREATE INDEX job_applications_user_created_idx
    ON job_applications(user_id, created_at DESC, id DESC);
