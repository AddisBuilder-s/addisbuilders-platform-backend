-- V3__add_enrollments_table.sql
CREATE TABLE IF NOT EXISTS enrollments (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           student_id BIGINT NOT NULL,
                                           course_id BIGINT NOT NULL,
                                           status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_student_course UNIQUE (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
    );