CREATE TABLE IF NOT EXISTS website_product (
 id BIGINT NOT NULL AUTO_INCREMENT, slug VARCHAR(80) NOT NULL, app_id VARCHAR(64) NULL, name VARCHAR(128) NOT NULL, tagline VARCHAR(128) NOT NULL, summary VARCHAR(512) NULL, accent_color VARCHAR(16) NULL, cover_url VARCHAR(512) NULL, download_url VARCHAR(512) NULL, purchase_url VARCHAR(512) NULL, content TEXT NULL, sort_order INT NOT NULL DEFAULT 0, status VARCHAR(16) NOT NULL, created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, PRIMARY KEY(id), UNIQUE KEY uk_website_product_slug(slug), KEY idx_website_product_status_sort(status,sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS website_article (
 id BIGINT NOT NULL AUTO_INCREMENT, slug VARCHAR(80) NOT NULL, category VARCHAR(48) NOT NULL, title VARCHAR(160) NOT NULL, excerpt VARCHAR(512) NULL, cover_url VARCHAR(512) NULL, content TEXT NULL, seo_title VARCHAR(160) NULL, seo_description VARCHAR(320) NULL, published_at DATETIME NULL, status VARCHAR(16) NOT NULL, created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, PRIMARY KEY(id), UNIQUE KEY uk_website_article_slug(slug), KEY idx_website_article_category_status(category,status), KEY idx_website_article_status_published(status,published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS website_lead (
 id BIGINT NOT NULL AUTO_INCREMENT, name VARCHAR(80) NOT NULL, contact VARCHAR(128) NOT NULL, subject VARCHAR(160) NULL, message VARCHAR(2000) NOT NULL, status VARCHAR(24) NOT NULL DEFAULT 'NEW', created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, PRIMARY KEY(id), KEY idx_website_lead_status_created(status,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
