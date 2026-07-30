ALTER TABLE app_info
  ADD COLUMN access_token_minutes INT NOT NULL DEFAULT 30 AFTER allow_avatar_upload,
  ADD COLUMN refresh_token_minutes INT NOT NULL DEFAULT 43200 AFTER access_token_minutes;

CREATE TABLE IF NOT EXISTS user_refresh_token (
  id BIGINT NOT NULL AUTO_INCREMENT,
  token_hash VARCHAR(128) NOT NULL,
  user_id BIGINT NOT NULL,
  app_id VARCHAR(64) NOT NULL,
  device_code VARCHAR(128) NULL,
  token_version BIGINT NOT NULL,
  expires_at DATETIME NOT NULL,
  last_used_at DATETIME NULL,
  revoked_at DATETIME NULL,
  revoke_reason VARCHAR(128) NULL,
  ip_address VARCHAR(64) NULL,
  user_agent VARCHAR(512) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY idx_user_refresh_hash (token_hash),
  KEY idx_user_refresh_user_app (user_id, app_id),
  KEY idx_user_refresh_device (app_id, user_id, device_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
