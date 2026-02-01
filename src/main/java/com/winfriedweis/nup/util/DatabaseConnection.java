package com.winfriedweis.nup.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);

    private static DatabaseConnection instance;
    private HikariDataSource dataSource;

    private DatabaseConnection() {
        initializeDataSource();
    }

    private void initializeDataSource() {
        DatabaseConfig config = DatabaseConfig.getInstance();

        log.info("Initialisiere Datenbankverbindung: {} @ {}:{}",
            config.getType(), config.getHost(), config.getPort());

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName(config.getDriverClassName());

        // Connection Pool Settings
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);

        // Performance
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

        try {
            dataSource = new HikariDataSource(hikariConfig);
            log.info("Datenbankverbindung erfolgreich initialisiert");
        } catch (Exception e) {
            log.error("Fehler bei der Initialisierung der Datenbankverbindung", e);
            throw e;
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.debug("DataSource geschlossen");
        }
    }

    public synchronized void reinitialize() {
        log.info("Reinitialisiere Datenbankverbindung...");
        close();
        initializeDataSource();
    }

    public static boolean testConnection(DatabaseType type, String host, int port,
                                         String database, String username, String password) {
        return testConnectionWithError(type, host, port, database, username, password) == null;
    }

    public static String testConnectionWithError(DatabaseType type, String host, int port,
                                                  String database, String username, String password) {
        String jdbcUrl = type.buildJdbcUrl(host, port, database);
        log.debug("Teste Verbindung zu: {}", jdbcUrl);

        try {
            Class.forName(type.getDriverClassName());
            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
                if (conn != null && !conn.isClosed()) {
                    log.debug("Verbindungstest erfolgreich");
                    return null;
                }
                return "Verbindung konnte nicht hergestellt werden";
            }
        } catch (ClassNotFoundException e) {
            log.error("Treiber nicht gefunden: {}", type.getDriverClassName(), e);
            return "Treiber nicht gefunden: " + e.getMessage();
        } catch (SQLException e) {
            log.error("Verbindungstest fehlgeschlagen", e);
            return e.getMessage();
        }
    }
}
