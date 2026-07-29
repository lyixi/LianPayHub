ALTER TABLE app_info
  ADD COLUMN allow_password_login BIT NOT NULL DEFAULT 0 AFTER need_device_vip,
  ADD COLUMN allow_avatar_upload BIT NOT NULL DEFAULT 1 AFTER allow_password_login;

ALTER TABLE user_info
  ADD COLUMN username VARCHAR(64) NULL AFTER mobile,
  ADD COLUMN password_hash VARCHAR(128) NULL AFTER username,
  ADD COLUMN nickname VARCHAR(128) NULL AFTER password_hash,
  ADD COLUMN avatar_storage_key VARCHAR(512) NULL AFTER nickname,
  ADD COLUMN avatar_url VARCHAR(1024) NULL AFTER avatar_storage_key,
  ADD COLUMN avatar_content_type VARCHAR(128) NULL AFTER avatar_url,
  ADD COLUMN avatar_size_bytes BIGINT NULL AFTER avatar_content_type,
  ADD COLUMN password_set_at DATETIME NULL AFTER avatar_size_bytes,
  ADD COLUMN last_login_at DATETIME NULL AFTER password_set_at;

ALTER TABLE user_info
  ADD UNIQUE KEY uk_user_info_username (username);

CREATE TABLE IF NOT EXISTS user_config (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  app_id VARCHAR(64) NOT NULL,
  config_key VARCHAR(128) NOT NULL,
  content_type VARCHAR(128) NOT NULL,
  content_text MEDIUMTEXT NOT NULL,
  content_hash VARCHAR(64) NOT NULL,
  version BIGINT NOT NULL DEFAULT 1,
  size_bytes BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_config_owner_key (user_id, app_id, config_key),
  KEY idx_user_config_owner (user_id, app_id),
  KEY idx_user_config_version (user_id, app_id, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
