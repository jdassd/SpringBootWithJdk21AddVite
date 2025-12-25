MERGE INTO blog_posts (id, title, slug, content, status, created_at, updated_at)
KEY (id)
VALUES (1001, '欢迎来到摄影博客', 'welcome', '这是第一篇文章，用于展示博客与摄影展。', 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO users (id, username, email, password_hash, role, failed_attempts, locked_until, created_at)
KEY (id)
VALUES (1, 'admin', 'admin@example.com', '$2b$12$84UNQVmtrA1MTmceVLT57uxmQB4w2Ll2q0Zjlk4C7pWqKEHpRegTe', 'ADMIN', 0, NULL, CURRENT_TIMESTAMP);

MERGE INTO blog_tags (id, name) KEY (id) VALUES (2001, 'photography');
MERGE INTO blog_post_tags (id, post_id, tag_id) KEY (id) VALUES (3001, 1001, 2001);

MERGE INTO navigation_links (id, name, url, icon, group_name, sort_order)
KEY (id)
VALUES (4001, '项目文档', 'https://example.com/docs', 'Document', '资源', 1);

MERGE INTO search_engines (id, name, query_url, is_default)
KEY (id)
VALUES (5001, 'Bing', 'https://www.bing.com/search?q={query}', TRUE);

MERGE INTO custom_pages (id, title, slug, content, custom_css, created_at, updated_at)
KEY (id)
VALUES (6001, '关于我们', 'about', '这是一个自定义页面示例。', 'h1 { color: #345; }', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO gallery_albums (id, title, description, cover_url, created_at)
KEY (id)
VALUES (7001, '城市光影', '记录城市夜景的摄影集。', 'https://images.unsplash.com/photo-1469474968028-56623f02e42e', CURRENT_TIMESTAMP);

MERGE INTO gallery_photos (id, album_id, title, image_url, taken_at, created_at)
KEY (id)
VALUES (8001, 7001, '夜色', 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO tasks (id, title, due_date, reminder_time, status, created_at, completed_at)
KEY (id)
VALUES (9001, '发布本周文章', CURRENT_DATE, CURRENT_TIMESTAMP, 'PENDING', CURRENT_TIMESTAMP, NULL);
