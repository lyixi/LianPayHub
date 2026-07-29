CREATE TABLE IF NOT EXISTS search_platform_config (
  id BIGINT NOT NULL AUTO_INCREMENT,
  provider_code VARCHAR(64) NOT NULL,
  display_name VARCHAR(128) NOT NULL,
  base_url VARCHAR(512) NULL,
  console_base_url VARCHAR(512) NULL,
  config_json LONGTEXT NULL,
  credential_json LONGTEXT NULL,
  enabled BIT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_search_platform_code (provider_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
