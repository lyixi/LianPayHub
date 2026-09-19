CREATE TABLE IF NOT EXISTS website_media (
 id BIGINT NOT NULL AUTO_INCREMENT, storage_key VARCHAR(512) NOT NULL, url VARCHAR(512) NOT NULL,
 file_name VARCHAR(255) NOT NULL, content_type VARCHAR(100) NOT NULL, size_bytes BIGINT NOT NULL,
 title VARCHAR(180) NULL, alt_text VARCHAR(255) NULL, description VARCHAR(1000) NULL,
 created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, PRIMARY KEY(id),
 UNIQUE KEY uk_website_media_storage_key(storage_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
