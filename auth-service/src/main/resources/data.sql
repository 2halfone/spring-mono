-- Initial user data for development and testing
-- Passwords are BCrypt-hashed versions of simple passwords

-- Test users with hashed passwords:
-- admin / admin123 -> $2a$10$N.cZJWdI4WbZuRGp.1hTlONJPGf4.LZHkV9.WK5vB2QFgExN5Y/2.
-- user / user123 -> $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.
-- test / test123 -> $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.

-- Insert users (without roles - those go in separate table)
INSERT INTO users (username, email, password, enabled, account_non_expired, account_non_locked, credentials_non_expired, email_verified, created_date, last_modified_date) VALUES
('admin', 'admin@example.com', '$2a$10$N.cZJWdI4WbZuRGp.1hTlONJPGf4.LZHkV9.WK5vB2QFgExN5Y/2.', true, true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user', 'user@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', true, true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('test', 'test@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', true, true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert user roles (separate table for @ElementCollection mapping)
INSERT INTO user_roles (user_id, role) VALUES
(1, 'ADMIN'),    -- admin user gets ADMIN role
(1, 'USER'),     -- admin user also gets USER role (typical hierarchy)
(2, 'USER'),     -- user gets USER role
(3, 'USER');     -- test user gets USER role

-- Note: These are the BCrypt hashes for:
-- admin123 -> $2a$10$N.cZJWdI4WbZuRGp.1hTlONJPGf4.LZHkV9.WK5vB2QFgExN5Y/2.
-- user123 -> $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.
-- test123 -> $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.
