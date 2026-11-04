ALTER TABLE users
    ADD COLUMN phone VARCHAR(40),
    ADD COLUMN bio TEXT,
    ADD COLUMN university VARCHAR(200),
    ADD COLUMN department VARCHAR(200),
    ADD COLUMN student_id VARCHAR(100),
    ADD COLUMN cv_url VARCHAR(1000),
    ADD COLUMN avatar_url VARCHAR(1000),
    ADD COLUMN employee_id VARCHAR(100),
    ADD COLUMN proof_document_url VARCHAR(1000),
    ADD COLUMN coordinator_decision_notes TEXT,
    ADD COLUMN coordinator_decision_at TIMESTAMP NULL;

ALTER TABLE organizations
    ADD COLUMN industry VARCHAR(150),
    ADD COLUMN location VARCHAR(255),
    ADD COLUMN contact_email VARCHAR(255),
    ADD COLUMN contact_phone VARCHAR(40),
    ADD COLUMN registration_number VARCHAR(150),
    ADD COLUMN tax_id VARCHAR(150),
    ADD COLUMN verification_notes TEXT,
    ADD COLUMN verification_document_name VARCHAR(255),
    ADD COLUMN verification_document_url VARCHAR(1000);

ALTER TABLE internship_listings
    ADD COLUMN location VARCHAR(255),
    ADD COLUMN category VARCHAR(150),
    ADD COLUMN responsibilities TEXT,
    ADD COLUMN eligibility TEXT,
    ADD COLUMN stipend VARCHAR(150),
    ADD COLUMN duration_months INT NOT NULL DEFAULT 6;

ALTER TABLE internship_applications
    ADD COLUMN notes TEXT;

CREATE TABLE saved_internships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    internship_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_saved_internship_user UNIQUE (user_id, internship_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (internship_id) REFERENCES internship_listings(id) ON DELETE CASCADE
);

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'INFO',
    link VARCHAR(1000),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notifications_user_created (user_id, created_at)
);

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id BIGINT,
    actor_name VARCHAR(150) NOT NULL,
    actor_role VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    target_id VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_logs_created (created_at)
);
