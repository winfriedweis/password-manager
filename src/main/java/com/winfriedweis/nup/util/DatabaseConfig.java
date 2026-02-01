package com.winfriedweis.nup.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DatabaseConfig {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    private static DatabaseConfig instance;
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.nup";
    private static final String CONFIG_FILE = CONFIG_DIR + "/database.properties";

    private DatabaseType type = DatabaseType.POSTGRESQL;
    private String host = "localhost";
    private int port = 5432;
    private String database = "nup_db";
    private String username = "postgres";
    private String password = "";

    private DatabaseConfig() {
        load();
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public void load() {
        Path configPath = Path.of(CONFIG_FILE);
        if (!Files.exists(configPath)) {
            log.info("Keine Konfigurationsdatei gefunden, verwende Standardwerte");
            return;
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(configPath)) {
            props.load(in);

            String typeStr = props.getProperty("type", "POSTGRESQL");
            try {
                type = DatabaseType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Unbekannter Datenbanktyp '{}', verwende POSTGRESQL", typeStr);
                type = DatabaseType.POSTGRESQL;
            }

            host = props.getProperty("host", "localhost");
            port = Integer.parseInt(props.getProperty("port", String.valueOf(type.getDefaultPort())));
            database = props.getProperty("database", "nup_db");
            username = props.getProperty("username", "postgres");
            password = props.getProperty("password", "");

            log.info("Konfiguration geladen: {} @ {}:{}/{}", type, host, port, database);
        } catch (IOException e) {
            log.error("Fehler beim Laden der Datenbank-Konfiguration", e);
        }
    }

    public void save() {
        try {
            Path configDir = Path.of(CONFIG_DIR);
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                log.debug("Konfigurationsverzeichnis erstellt: {}", configDir);
            }

            Properties props = new Properties();
            props.setProperty("type", type.name());
            props.setProperty("host", host);
            props.setProperty("port", String.valueOf(port));
            props.setProperty("database", database);
            props.setProperty("username", username);
            props.setProperty("password", password);

            try (OutputStream out = Files.newOutputStream(Path.of(CONFIG_FILE))) {
                props.store(out, "NUP Database Configuration");
            }
            log.info("Konfiguration gespeichert: {} @ {}:{}/{}", type, host, port, database);
        } catch (IOException e) {
            log.error("Fehler beim Speichern der Datenbank-Konfiguration", e);
        }
    }

    public String getJdbcUrl() {
        return type.buildJdbcUrl(host, port, database);
    }

    public String getDriverClassName() {
        return type.getDriverClassName();
    }

    // Getter und Setter
    public DatabaseType getType() {
        return type;
    }

    public void setType(DatabaseType type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
