ALTER TABLE user_accounts
    ADD COLUMN first_name VARCHAR(100),
    ADD COLUMN last_name VARCHAR(100);

UPDATE user_accounts
SET first_name = 'Account',
    last_name = 'User';

ALTER TABLE user_accounts
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name SET NOT NULL;
