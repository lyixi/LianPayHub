CREATE TABLE IF NOT EXISTS website_page (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(80) NOT NULL,
    title VARCHAR(160) NOT NULL,
    excerpt VARCHAR(512),
    cover_url VARCHAR(512),
    content TEXT,
    seo_title VARCHAR(160),
    seo_description VARCHAR(320),
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_website_page_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
