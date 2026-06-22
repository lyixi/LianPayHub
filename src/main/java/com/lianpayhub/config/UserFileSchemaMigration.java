package com.lianpayhub.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserFileSchemaMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserFileSchemaMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public UserFileSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!tableExists("user_file")) {
            return;
        }
        ensureVirtualPathHashColumn();
        backfillVirtualPathHash();
        dropLegacyPathIndex();
        createHashIndex();
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName);
        return count != null && count > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class,
                tableName,
                columnName);
        return count != null && count > 0;
    }

    private boolean indexExists(String tableName, String indexName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?",
                Integer.class,
                tableName,
                indexName);
        return count != null && count > 0;
    }

    private void ensureVirtualPathHashColumn() {
        if (columnExists("user_file", "virtual_path_hash")) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE user_file ADD COLUMN virtual_path_hash VARCHAR(64) NULL AFTER virtual_path");
        log.info("Added column user_file.virtual_path_hash");
    }

    private void backfillVirtualPathHash() {
        jdbcTemplate.execute("UPDATE user_file SET virtual_path_hash = SHA2(virtual_path, 256) WHERE virtual_path_hash IS NULL OR virtual_path_hash = ''");
    }

    private void dropLegacyPathIndex() {
        if (!indexExists("user_file", "idx_user_file_path")) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE user_file DROP INDEX idx_user_file_path");
        log.info("Dropped legacy index idx_user_file_path");
    }

    private void createHashIndex() {
        if (indexExists("user_file", "idx_user_file_path_hash")) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE user_file ADD INDEX idx_user_file_path_hash (user_id, app_id, virtual_path_hash(32))");
        log.info("Created index idx_user_file_path_hash");
    }
}
