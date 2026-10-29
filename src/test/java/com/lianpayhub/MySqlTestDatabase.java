package com.lianpayhub;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.UUID;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/** Each test context owns a fresh schema, never the configured application schema. */
public class MySqlTestDatabase implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        Environment env = context.getEnvironment();
        String configuredUrl = env.getProperty("TEST_MYSQL_URL", env.getProperty("spring.datasource.url"));
        if (configuredUrl == null || !configuredUrl.matches("jdbc:mysql://[^/]+/[^?]*(\\?.*)?")) {
            throw new IllegalStateException("Tests require a jdbc:mysql://host:port/database URL (TEST_MYSQL_URL)");
        }
        String username = env.getProperty("TEST_MYSQL_USERNAME", env.getProperty("spring.datasource.username"));
        String password = env.getProperty("TEST_MYSQL_PASSWORD", env.getProperty("spring.datasource.password", ""));
        int schemaStart = configuredUrl.indexOf('/', "jdbc:mysql://".length());
        int queryStart = configuredUrl.indexOf('?', schemaStart);
        String serverUrl = configuredUrl.substring(0, schemaStart + 1)
                + (queryStart < 0 ? "" : configuredUrl.substring(queryStart));
        String schema = "lph_test_" + UUID.randomUUID().toString().replace("-", "");
        execute(serverUrl, username, password, "CREATE DATABASE `" + schema + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) context.getBeanFactory();
        factory.registerDisposableBean("mysqlTestDatabaseCleanup", () ->
                execute(serverUrl, username, password, "DROP DATABASE IF EXISTS `" + schema + "`"));
        // Close the pool (including Hibernate's create-drop) before removing its database.
        factory.registerDependentBean("mysqlTestDatabaseCleanup", "dataSource");
        String testUrl = configuredUrl.substring(0, schemaStart + 1) + schema
                + (queryStart < 0 ? "" : configuredUrl.substring(queryStart));
        TestPropertyValues.of(
                "spring.datasource.url=" + testUrl,
                "spring.datasource.username=" + username,
                "spring.datasource.password=" + password,
                "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL57Dialect",
                "spring.flyway.enabled=false",
                "lianpayhub.admin.force-default-password-change=false",
                "lianpayhub.admin.password-complexity-required=false"
        ).applyTo(context);
    }

    private static void execute(String url, String username, String password, String sql) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException exception) {
            throw new IllegalStateException("Cannot create/clean the isolated MySQL test database; check MySQL and CREATE/DROP DATABASE permissions", exception);
        }
    }
}
