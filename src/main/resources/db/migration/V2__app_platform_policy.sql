CREATE TABLE IF NOT EXISTS app_platform_policy (
  id BIGINT NOT NULL AUTO_INCREMENT,
  app_id VARCHAR(64) NOT NULL,
  category VARCHAR(32) NOT NULL,
  enabled BIT NOT NULL,
  provider_code VARCHAR(64) NULL,
  config_json LONGTEXT NULL,
  credential_json LONGTEXT NULL,
  policy_json LONGTEXT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_app_platform_policy_app_category (app_id, category),
  KEY idx_app_platform_policy_category (category, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
