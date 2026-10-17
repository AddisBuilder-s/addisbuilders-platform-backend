CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       full_name VARCHAR(150) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL,
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       coordinator_status VARCHAR(20) DEFAULT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE organizations (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               user_id INT NOT NULL UNIQUE,
                               name VARCHAR(200) NOT NULL,
                               description TEXT,
                               website VARCHAR(255),
                               verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                               logo_url VARCHAR(500),
                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE course_categories (
                                   id INT AUTO_INCREMENT PRIMARY KEY,
                                   name VARCHAR(100) NOT NULL UNIQUE,
                                   description TEXT
);

CREATE TABLE courses (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         mentor_id INT NOT NULL,
                         category_id INT,
                         title VARCHAR(200) NOT NULL,
                         description TEXT,
                         level VARCHAR(50) NOT NULL,
                         status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
                         thumbnail_url VARCHAR(500),
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         FOREIGN KEY (mentor_id) REFERENCES users(id),
                         FOREIGN KEY (category_id) REFERENCES course_categories(id)
);

CREATE TABLE modules (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         course_id INT NOT NULL,
                         title VARCHAR(200) NOT NULL,
                         module_order INT NOT NULL,
                         UNIQUE KEY idx_course_module_order (course_id, module_order),
                         FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

CREATE TABLE lessons (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         module_id INT NOT NULL,
                         title VARCHAR(200) NOT NULL,
                         type VARCHAR(50) NOT NULL,
                         body TEXT,
                         content_url VARCHAR(1000),
                         lesson_order INT NOT NULL,
                         UNIQUE KEY idx_module_lesson_order (module_id, lesson_order),
                         FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE
);

CREATE TABLE assignments (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             course_id INT NOT NULL,
                             title VARCHAR(200) NOT NULL,
                             instructions TEXT,
                             due_at TIMESTAMP,
                             points INT NOT NULL DEFAULT 100,
                             FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

CREATE TABLE submissions (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             assignment_id INT NOT NULL,
                             learner_id INT NOT NULL,
                             text_answer TEXT,
                             file_url VARCHAR(1000),
                             status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
                             grade DECIMAL(5,2),
                             feedback TEXT,
                             submitted_at TIMESTAMP,
                             graded_at TIMESTAMP,
                             UNIQUE KEY idx_assignment_learner (assignment_id, learner_id),
                             FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
                             FOREIGN KEY (learner_id) REFERENCES users(id)
);

CREATE TABLE student_progress (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  student_id INT NOT NULL,
                                  lesson_id INT NOT NULL,
                                  is_completed BOOLEAN NOT NULL DEFAULT FALSE,
                                  completed_at TIMESTAMP,
                                  UNIQUE KEY idx_student_lesson (student_id, lesson_id),
                                  FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
                                  FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
);

CREATE TABLE internship_listings (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     organization_id INT NOT NULL,
                                     coordinator_id INT,
                                     title VARCHAR(200) NOT NULL,
                                     description TEXT NOT NULL,
                                     requirements TEXT,
                                     work_mode VARCHAR(50) NOT NULL,
                                     deadline TIMESTAMP,
                                     status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
                                     FOREIGN KEY (coordinator_id) REFERENCES users(id)
);

CREATE TABLE internship_applications (
                                         id INT AUTO_INCREMENT PRIMARY KEY,
                                         internship_id INT NOT NULL,
                                         learner_id INT NOT NULL,
                                         cover_letter TEXT,
                                         resume_url VARCHAR(1000),
                                         status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
                                         applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         UNIQUE KEY idx_internship_learner (internship_id, learner_id),
                                         FOREIGN KEY (internship_id) REFERENCES internship_listings(id) ON DELETE CASCADE,
                                         FOREIGN KEY (learner_id) REFERENCES users(id)
);