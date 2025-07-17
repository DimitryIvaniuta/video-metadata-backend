-- Users table
CREATE TABLE users
(
    id       BIGINT       NOT NULL PRIMARY KEY DEFAULT nextval('VM_UNIQUE_ID'),
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role     VARCHAR(20)  NOT NULL
);