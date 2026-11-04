CREATE TABLE uploaded_files (
    id VARCHAR(36) PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    purpose VARCHAR(30) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    storage_filename VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(150) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_uploaded_files_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_uploaded_files_owner (owner_id)
);
