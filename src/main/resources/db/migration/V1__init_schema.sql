CREATE TABLE IF NOT EXISTS app_info (
  id BIGINT NOT NULL AUTO_INCREMENT,
  app_id VARCHAR(64) NOT NULL,
  app_name VARCHAR(128) NOT NULL,
  app_secret_hash VARCHAR(128) NOT NULL,
  app_secret_version INT NOT NULL DEFAULT 1,
  app_type VARCHAR(32) NOT NULL,
  need_mobile_login BIT NOT NULL,
  need_device_vip BIT NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_app_info_app_id (app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payment_channel_config (
  id BIGINT NOT NULL AUTO_INCREMENT,
  app_id VARCHAR(64) NOT NULL,
  pay_channel VARCHAR(32) NOT NULL,
  provider_code VARCHAR(64) NOT NULL,
  merchant_id VARCHAR(128) NULL,
  channel_app_id VARCHAR(128) NULL,
  notify_url VARCHAR(512) NULL,
  config_json LONGTEXT NULL,
  credential_json LONGTEXT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_payment_channel_config_app_channel (app_id, pay_channel),
  KEY idx_payment_channel_config_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notification_channel_config (
  id BIGINT NOT NULL AUTO_INCREMENT,
  channel_type VARCHAR(32) NOT NULL,
  provider_code VARCHAR(64) NOT NULL,
  display_name VARCHAR(128) NOT NULL,
  sender_name VARCHAR(128) NULL,
  sender_address VARCHAR(256) NULL,
  endpoint VARCHAR(512) NULL,
  template_code VARCHAR(128) NULL,
  access_key_id VARCHAR(256) NULL,
  access_key_secret VARCHAR(512) NULL,
  secret_id VARCHAR(256) NULL,
  secret_key VARCHAR(512) NULL,
  sdk_app_id VARCHAR(128) NULL,
  region VARCHAR(128) NULL,
  config_json LONGTEXT NULL,
  credential_json LONGTEXT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_notification_channel_provider (channel_type, provider_code),
  KEY idx_notification_channel_status (channel_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sms_send_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  config_id BIGINT NULL,
  channel_type VARCHAR(32) NOT NULL,
  provider_code VARCHAR(64) NULL,
  app_id VARCHAR(64) NULL,
  mobile VARCHAR(32) NOT NULL,
  template_code VARCHAR(128) NULL,
  params_json LONGTEXT NULL,
  message_id VARCHAR(128) NULL,
  success BIT NOT NULL,
  result_message VARCHAR(512) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_sms_send_log_type_time (channel_type, created_at),
  KEY idx_sms_send_log_config_time (config_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS admin_user (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(128) NOT NULL,
  display_name VARCHAR(128) NOT NULL,
  status VARCHAR(32) NOT NULL,
  last_login_at DATETIME NULL,
  password_version INT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_admin_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_info (
  id BIGINT NOT NULL AUTO_INCREMENT,
  mobile VARCHAR(32) NOT NULL,
  user_type VARCHAR(32) NOT NULL,
  open_id VARCHAR(128) NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_info_mobile (mobile)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_app_binding (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  app_id VARCHAR(64) NOT NULL,
  bind_type VARCHAR(32) NOT NULL,
  bind_at DATETIME NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_app_binding_user_app (user_id, app_id),
  KEY idx_user_app_binding_app (app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS device_info (
  id BIGINT NOT NULL AUTO_INCREMENT,
  app_id VARCHAR(64) NOT NULL,
  user_id BIGINT NULL,
  device_code VARCHAR(128) NOT NULL,
  device_name VARCHAR(128) NULL,
  device_type VARCHAR(64) NULL,
  device_fingerprint VARCHAR(256) NULL,
  bind_status VARCHAR(32) NOT NULL,
  bind_at DATETIME NULL,
  last_launch_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_device_info_app_device (app_id, device_code),
  KEY idx_device_info_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS device_code_change_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  device_id BIGINT NOT NULL,
  app_id VARCHAR(64) NOT NULL,
  old_device_code VARCHAR(128) NOT NULL,
  new_device_code VARCHAR(128) NOT NULL,
  reason VARCHAR(512) NULL,
  admin_id BIGINT NULL,
  admin_username VARCHAR(64) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_device_code_change_device (device_id),
  KEY idx_device_code_change_app (app_id),
  KEY idx_device_code_change_old (old_device_code),
  KEY idx_device_code_change_new (new_device_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS package_info (
  id BIGINT NOT NULL AUTO_INCREMENT,
  app_id VARCHAR(64) NOT NULL,
  package_name VARCHAR(128) NOT NULL,
  package_type VARCHAR(32) NOT NULL,
  price_cents INT NOT NULL,
  duration_days INT NOT NULL,
  benefits_text VARCHAR(1024) NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_package_info_app (app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payment_order (
  id BIGINT NOT NULL AUTO_INCREMENT,
  app_id VARCHAR(64) NOT NULL,
  user_id BIGINT NULL,
  device_id BIGINT NULL,
  package_id BIGINT NOT NULL,
  order_no VARCHAR(64) NOT NULL,
  amount_cents INT NOT NULL,
  pay_channel VARCHAR(32) NOT NULL,
  pay_provider VARCHAR(64) NOT NULL,
  pay_status VARCHAR(32) NOT NULL,
  trade_no VARCHAR(128) NULL,
  channel_order_no VARCHAR(128) NULL,
  callback_data LONGTEXT NULL,
  callback_count INT NOT NULL DEFAULT 0,
  refunded_amount_cents INT NOT NULL DEFAULT 0,
  paid_at DATETIME NULL,
  expire_at DATETIME NULL,
  closed_at DATETIME NULL,
  close_reason VARCHAR(512) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_payment_order_no (order_no),
  KEY idx_payment_order_app (app_id),
  KEY idx_payment_order_user (user_id),
  KEY idx_payment_order_device (device_id),
  KEY idx_payment_order_status_expire (pay_status, expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payment_refund (
  id BIGINT NOT NULL AUTO_INCREMENT,
  app_id VARCHAR(64) NOT NULL,
  order_id BIGINT NOT NULL,
  refund_no VARCHAR(64) NOT NULL,
  amount_cents INT NOT NULL,
  reason VARCHAR(512) NULL,
  status VARCHAR(32) NOT NULL,
  channel_refund_no VARCHAR(128) NULL,
  processed_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_payment_refund_no (refund_no),
  KEY idx_payment_refund_order (order_id),
  KEY idx_payment_refund_app (app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS member_info (
  id BIGINT NOT NULL AUTO_INCREMENT,
  app_id VARCHAR(64) NOT NULL,
  member_subject_type VARCHAR(32) NOT NULL,
  user_id BIGINT NULL,
  device_id BIGINT NULL,
  package_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL,
  start_at DATETIME NOT NULL,
  expire_at DATETIME NOT NULL,
  order_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_member_info_user (app_id, user_id),
  KEY idx_member_info_device (app_id, device_id),
  KEY idx_member_info_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS launch_record (
  id BIGINT NOT NULL AUTO_INCREMENT,
  app_id VARCHAR(64) NOT NULL,
  device_id BIGINT NULL,
  user_id BIGINT NULL,
  platform VARCHAR(64) NULL,
  version VARCHAR(64) NULL,
  network_type VARCHAR(64) NULL,
  ip_address VARCHAR(64) NULL,
  event_type VARCHAR(32) NOT NULL,
  session_id VARCHAR(64) NULL,
  session_start_at DATETIME NULL,
  session_end_at DATETIME NULL,
  duration_seconds BIGINT NULL,
  event_data LONGTEXT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_launch_record_app_time (app_id, created_at),
  KEY idx_launch_record_device (device_id),
  KEY idx_launch_record_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payment_callback_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  app_id VARCHAR(64) NOT NULL,
  order_id BIGINT NULL,
  pay_channel VARCHAR(32) NOT NULL,
  pay_provider VARCHAR(64) NOT NULL,
  trade_no VARCHAR(128) NULL,
  raw_payload LONGTEXT NOT NULL,
  verify_status VARCHAR(32) NOT NULL,
  process_status VARCHAR(32) NOT NULL,
  error_message VARCHAR(512) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_payment_callback_order (order_id),
  KEY idx_payment_callback_app (app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payment_event_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  app_id VARCHAR(64) NOT NULL,
  order_id BIGINT NULL,
  event_type VARCHAR(32) NOT NULL,
  pay_channel VARCHAR(32) NULL,
  pay_provider VARCHAR(64) NULL,
  amount_cents INT NULL,
  trade_no VARCHAR(128) NULL,
  operator_type VARCHAR(32) NOT NULL,
  operator_id VARCHAR(64) NULL,
  event_data LONGTEXT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_payment_event_app_time (app_id, created_at),
  KEY idx_payment_event_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS admin_operation_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  admin_id BIGINT NULL,
  username VARCHAR(64) NULL,
  operation_type VARCHAR(64) NOT NULL,
  target_type VARCHAR(64) NULL,
  target_id VARCHAR(128) NULL,
  request_method VARCHAR(16) NULL,
  request_uri VARCHAR(512) NULL,
  ip_address VARCHAR(64) NULL,
  user_agent VARCHAR(512) NULL,
  request_body LONGTEXT NULL,
  confirm_reason VARCHAR(512) NULL,
  result_status VARCHAR(32) NOT NULL,
  error_message VARCHAR(512) NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_admin_operation_admin (admin_id),
  KEY idx_admin_operation_time (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS app_login_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  app_id VARCHAR(64) NOT NULL,
  user_id BIGINT NULL,
  mobile VARCHAR(32) NULL,
  login_type VARCHAR(32) NOT NULL,
  device_id BIGINT NULL,
  device_code VARCHAR(128) NULL,
  ip_address VARCHAR(64) NULL,
  user_agent VARCHAR(512) NULL,
  result_status VARCHAR(32) NOT NULL,
  error_message VARCHAR(512) NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_app_login_app_time (app_id, created_at),
  KEY idx_app_login_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS adapter_report (
  id BIGINT NOT NULL AUTO_INCREMENT,
  app_id VARCHAR(64) NOT NULL,
  source_id VARCHAR(128) NOT NULL,
  report_type VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL,
  payload LONGTEXT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_adapter_report_app_source (app_id, source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 云同步文件系统
-- ============================================================

CREATE TABLE IF NOT EXISTS user_file (
  id            BIGINT NOT NULL AUTO_INCREMENT,
  user_id       BIGINT NOT NULL,
  app_id        VARCHAR(64) NOT NULL,
  virtual_path  VARCHAR(1024) NOT NULL COMMENT '用户视角路径，如 /settings/config.json',
  virtual_path_hash VARCHAR(64) NULL,
  file_name     VARCHAR(256) NOT NULL,
  storage_key   VARCHAR(512) NOT NULL COMMENT '存储后端 key，格式：sync/{appId}/{userId}/{UUID}.{ext}',
  content_type  VARCHAR(128) NULL,
  size_bytes    BIGINT NOT NULL DEFAULT 0,
  checksum      VARCHAR(64) NULL COMMENT 'SHA-256',
  file_category VARCHAR(16) NOT NULL COMMENT 'CONFIG / IMAGE',
  version       BIGINT NOT NULL DEFAULT 1,
  deleted_at    DATETIME NULL COMMENT '软删除时间，NULL 表示未删除',
  created_at    DATETIME NOT NULL,
  updated_at    DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_user_file_owner (user_id, app_id),
  KEY idx_user_file_path_hash (user_id, app_id, virtual_path_hash(32)),
  KEY idx_user_file_updated (user_id, app_id, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_storage_quota (
  user_id     BIGINT NOT NULL,
  app_id      VARCHAR(64) NOT NULL,
  used_bytes  BIGINT NOT NULL DEFAULT 0,
  quota_bytes BIGINT NOT NULL DEFAULT 52428800 COMMENT '默认 50 MB',
  file_count  INT NOT NULL DEFAULT 0,
  PRIMARY KEY (user_id, app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
