-- Inserts two hardcoded users: admin / user
-- Passwords are BCrypt hashes of "adminpass" and "userpass" respectively.

INSERT INTO users (username, password, role)
    VALUES ('admin',
        '$2a$10$7QJ1kV8b1hN/EdHKQh8kjuv0/L3X5RtingmM1S1d1z6fPKUrKfI3a', -- BCrypt("adminpass")
        'ADMIN'),
       ('user',
        '$2a$10$uL5KpQrZnGd7rF8XzYl9..3MKlOy5JH0Yx1BcA2Dv6eW9kAbCdEfG', -- BCrypt("userpass")
        'USER');