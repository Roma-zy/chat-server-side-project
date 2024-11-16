CREATE TABLE roles (
   id SERIAL PRIMARY KEY,
   role_name VARCHAR(50) NOT NULL
);

INSERT INTO roles (role_name) VALUES ('USER'), ('ADMIN');

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) UNIQUE NOT NULL,
    role_id INTEGER NOT NULL,
    is_banned BOOLEAN DEFAULT FALSE,
    ban_until TIMESTAMP,
    last_active TIMESTAMP
);

CREATE TABLE rooms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    owner_id INTEGER NOT NULL,
    password VARCHAR(255),
    last_used TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    room_id INTEGER,
    sender_id INTEGER NOT NULL,
    content VARCHAR(255) NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE bans (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    banned_by_id INTEGER NOT NULL,
    ban_reason VARCHAR(255),
    banned_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (banned_by_id) REFERENCES users(id)
);

CREATE TABLE user_rooms (
    user_id INTEGER PRIMARY KEY,
    current_room_id INTEGER,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (current_room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

CREATE TABLE private_messages (
    id SERIAL PRIMARY KEY,
    sender_id INTEGER NOT NULL,
    receiver_id INTEGER NOT NULL,
    content VARCHAR(255) NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);


DO $$
    DECLARE
        admin_role_id INTEGER;
    BEGIN
        SELECT id INTO admin_role_id FROM roles WHERE role_name = 'ADMIN';

        INSERT INTO users (username, password, nickname, role_id) VALUES ('admin', 'admin', 'Administrator', admin_role_id);
    END $$;