CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    title_en VARCHAR(255) NOT NULL,
    title_zh VARCHAR(255) NOT NULL,
    description_en TEXT NOT NULL,
    description_zh TEXT NOT NULL,
    category VARCHAR(30) NOT NULL,
    location VARCHAR(255),
    completed_date DATE,
    cover_image_url VARCHAR(1000),
    published BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_projects_category ON projects (category);
CREATE INDEX idx_projects_published ON projects (published);

CREATE TABLE project_images (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    image_url VARCHAR(1000) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_project_images_project_id ON project_images (project_id);

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_reviews_project_user UNIQUE (project_id, user_id)
);

CREATE INDEX idx_reviews_project_id ON reviews (project_id);

CREATE TABLE quote_requests (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    category VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    user_id BIGINT REFERENCES users (id) ON DELETE SET NULL,
    admin_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_quote_requests_status ON quote_requests (status);
CREATE INDEX idx_quote_requests_created_at ON quote_requests (created_at);

CREATE TABLE quote_request_images (
    id BIGSERIAL PRIMARY KEY,
    quote_request_id BIGINT NOT NULL REFERENCES quote_requests (id) ON DELETE CASCADE,
    image_url VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_quote_request_images_quote_request_id ON quote_request_images (quote_request_id);
