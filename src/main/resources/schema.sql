CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    avatar_url VARCHAR(400),
    bio VARCHAR(500),
    failed_attempts INT DEFAULT 0,
    locked_until TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS blog_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(200) NOT NULL,
    content CLOB NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS blog_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS blog_post_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS blog_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    author VARCHAR(120) NOT NULL,
    content CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS navigation_links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    name VARCHAR(120) NOT NULL,
    url VARCHAR(400) NOT NULL,
    icon VARCHAR(120),
    group_name VARCHAR(120),
    sort_order INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS search_engines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    name VARCHAR(120) NOT NULL,
    query_url VARCHAR(400) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS custom_pages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(200) NOT NULL,
    content CLOB NOT NULL,
    custom_css CLOB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS gallery_albums (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    title VARCHAR(200) NOT NULL,
    description CLOB,
    cover_url VARCHAR(400),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS gallery_photos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    album_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    image_url VARCHAR(400) NOT NULL,
    taken_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    title VARCHAR(200) NOT NULL,
    due_date DATE,
    reminder_time TIMESTAMP,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mail_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    direction VARCHAR(32) NOT NULL,
    from_address VARCHAR(150) NOT NULL,
    to_address VARCHAR(150) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    body CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL
);
