ALTER TABLE users
    ADD COLUMN mentor_status VARCHAR(20),
    ADD COLUMN mentor_decision_notes TEXT,
    ADD COLUMN mentor_decision_at TIMESTAMP NULL;

-- Keep existing mentor accounts available while requiring approval for new registrations.
UPDATE users
SET mentor_status = 'APPROVED'
WHERE role = 'MENTOR';
