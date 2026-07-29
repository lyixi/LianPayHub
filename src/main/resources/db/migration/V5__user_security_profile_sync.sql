ALTER TABLE user_info
  ADD COLUMN failed_password_attempts INT NOT NULL DEFAULT 0 AFTER last_login_at,
  ADD COLUMN locked_until DATETIME NULL AFTER failed_password_attempts,
  ADD COLUMN token_version BIGINT NOT NULL DEFAULT 1 AFTER locked_until,
  ADD COLUMN must_change_password BIT NOT NULL DEFAULT 0 AFTER token_version;

ALTER TABLE user_config
  ADD COLUMN deleted_at DATETIME NULL AFTER size_bytes;

CREATE TABLE IF NOT EXISTS user_config_version (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  app_id VARCHAR(64) NOT NULL,
  current_version BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_config_version_owner (user_id, app_id),
  KEY idx_user_config_version_owner (user_id, app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
