CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX job_applications_title_trgm_idx
    ON job_applications USING GIN (lower(title) gin_trgm_ops);

CREATE INDEX job_applications_company_name_trgm_idx
    ON job_applications USING GIN (lower(company_name) gin_trgm_ops);

CREATE INDEX job_applications_url_trgm_idx
    ON job_applications USING GIN (lower(url) gin_trgm_ops);

CREATE INDEX job_applications_company_address_trgm_idx
    ON job_applications USING GIN (lower(company_address) gin_trgm_ops);

CREATE INDEX job_applications_content_trgm_idx
    ON job_applications USING GIN (lower(content) gin_trgm_ops);

CREATE INDEX job_applications_notes_trgm_idx
    ON job_applications USING GIN (lower(notes) gin_trgm_ops);

CREATE INDEX job_applications_created_id_idx
    ON job_applications (created_at DESC, id DESC);

CREATE INDEX job_applications_salary_min_idx
    ON job_applications (salary_min);

CREATE INDEX job_applications_salary_max_idx
    ON job_applications (salary_max);
