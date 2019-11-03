CREATE TABLE ticket12306_account
(
    id        SERIAL PRIMARY KEY,
    user_id   INT,
    username  VARCHAR(64) UNIQUE NOT NULL,
    password  VARCHAR(64)        NOT NULL,
    create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(id)
)