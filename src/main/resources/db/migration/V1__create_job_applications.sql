CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE job_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    company_name VARCHAR(255) NOT NULL,
    company_address TEXT,

    title VARCHAR(255) NOT NULL,

    url TEXT NOT NULL,
    content TEXT,

    status VARCHAR(50) NOT NULL DEFAULT 'SAVED',

    work_setup VARCHAR(20) NOT NULL DEFAULT 'ONSITE',

    experience_level VARCHAR(20),

    salary_min NUMERIC(12,2),
    salary_max NUMERIC(12,2),
    salary_period VARCHAR(20),
    salary_currency CHAR(3),

    notes TEXT,

    applied_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT job_applications_status_check
        CHECK (
            status IN (
                'SAVED',
                'APPLIED',
                'UNDER_REVIEW',
                'INTERVIEW',
                'TECHNICAL_EXAM',
                'TECHNICAL_INTERVIEW',
                'FINAL_INTERVIEW',
                'OFFER',
                'ACCEPTED',
                'REJECTED',
                'WITHDRAWN'
            )
        ),

    CONSTRAINT job_applications_work_setup_check
        CHECK (
            work_setup IN (
                'ONSITE',
                'HYBRID',
                'WORK_FROM_HOME'
            )
        ),

    CONSTRAINT job_applications_experience_level_check
        CHECK (
            experience_level IS NULL
            OR experience_level IN (
                'ENTRY',
                'JUNIOR',
                'MID',
                'SENIOR',
                'LEAD',
                'MANAGER',
                'DIRECTOR',
                'EXECUTIVE'
            )
        ),

    CONSTRAINT job_applications_salary_period_check
        CHECK (
            salary_period IS NULL
            OR salary_period IN (
                'MONTHLY',
                'YEARLY'
            )
        ),

    CONSTRAINT job_applications_salary_range_check
        CHECK (
            salary_min IS NULL
            OR salary_max IS NULL
            OR salary_min <= salary_max
        ),

    CONSTRAINT job_applications_salary_positive_check
        CHECK (
            (salary_min IS NULL OR salary_min >= 0)
            AND
            (salary_max IS NULL OR salary_max >= 0)
        )
);
