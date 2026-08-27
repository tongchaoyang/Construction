ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;

CREATE TABLE user_sso_identities (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    provider VARCHAR(30) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_sso_provider_subject UNIQUE (provider, subject)
);

CREATE INDEX idx_user_sso_identities_user_id ON user_sso_identities (user_id);
