MERGE INTO users (id, username, email, password_hash, role, failed_attempts, locked_until, created_at)
KEY (id)
VALUES (1, 'admin', 'admin@example.com', '$2b$12$84UNQVmtrA1MTmceVLT57uxmQB4w2Ll2q0Zjlk4C7pWqKEHpRegTe', 'ADMIN', 0, NULL, CURRENT_TIMESTAMP);

MERGE INTO sites (id, site_key, display_name, owner_user_id, description, avatar_url, bio, social_links, theme, theme_primary, theme_accent, custom_css, created_at, updated_at)
KEY (id)
VALUES (1, 'admin', 'Admin Site', 1, 'Sample site for admin', NULL, NULL, NULL, 'classic', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO blog_posts (id, user_id, title, slug, content, status, published_at, created_at, updated_at)
KEY (id)
VALUES (1001, 1, 'Welcome to the blog', 'welcome', 'This is a sample post for the public feed.', 'PUBLIC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO blog_tags (id, name) KEY (id) VALUES (2001, 'photography');
MERGE INTO blog_post_tags (id, post_id, tag_id) KEY (id) VALUES (3001, 1001, 2001);

MERGE INTO navigation_links (id, user_id, name, url, icon, group_name, sort_order)
KEY (id)
VALUES (4001, 1, 'Docs', 'https://example.com/docs', 'Document', 'Resources', 1);

MERGE INTO search_engines (id, user_id, name, query_url, is_default)
KEY (id)
VALUES (5001, 1, 'Bing', 'https://www.bing.com/search?q={query}', TRUE);

MERGE INTO custom_pages (id, user_id, title, slug, content, custom_css, visibility, published_at, created_at, updated_at)
KEY (id)
VALUES (6001, 1, 'About', 'about', 'This is a sample custom page.', 'h1 { color: #345; }', 'PUBLIC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO gallery_albums (id, user_id, title, description, cover_url, created_at)
KEY (id)
VALUES (7001, 1, 'City Lights', 'Night city shots collection.', 'https://images.unsplash.com/photo-1469474968028-56623f02e42e', CURRENT_TIMESTAMP);

MERGE INTO gallery_photos (id, album_id, title, image_url, taken_at, created_at)
KEY (id)
VALUES (8001, 7001, 'Night View', 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO tasks (id, user_id, title, due_date, reminder_time, status, created_at, completed_at)
KEY (id)
VALUES (9001, 1, 'Publish weekly update', CURRENT_DATE, CURRENT_TIMESTAMP, 'PENDING', CURRENT_TIMESTAMP, NULL);
