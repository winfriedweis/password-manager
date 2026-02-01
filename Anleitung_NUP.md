# JavaFX NUP - Vollständige Schritt-für-Schritt Anleitung

## Authentifizierungs-App mit Multi-Datenbank-Support, Brute-Force-Schutz & Profilbild-Editor

> **Version 2.0** | Java 21 | JavaFX 21 | PostgreSQL/MySQL

---

## Inhaltsverzeichnis

1. [Einführung & Konzepte](#-einführung--konzepte)
2. [Voraussetzungen](#-voraussetzungen)
3. [Projektstruktur](#-projektstruktur)
4. [Schritt 1: Datenbank Setup](#️-schritt-1-datenbank-setup)
5. [Schritt 2: Maven Configuration](#-schritt-2-maven-configuration-pomxml)
6. [Schritt 3: Logging Configuration](#-schritt-3-logging-configuration)
7. [Schritt 4: Model Layer](#-schritt-4-model-layer)
8. [Schritt 5: Utility Classes](#-schritt-5-utility-classes)
9. [Schritt 6: DAO Layer](#-schritt-6-dao-layer)
10. [Schritt 7: Service Layer](#️-schritt-7-service-layer)
11. [Schritt 8: CSS Dark Theme](#-schritt-8-dark-theme-css)
12. [Schritt 9: FXML Views](#️-schritt-9-fxml-views)
13. [Schritt 10: Controller](#-schritt-10-controller)
14. [Schritt 11: Main Class](#-schritt-11-main-class)
15. [Schritt 12: Projekt starten](#-schritt-12-projekt-starten)
16. [Troubleshooting](#-troubleshooting)
17. [Nächste Schritte](#-nächste-schritte)

---

## Einführung & Konzepte

Diese Anleitung führt dich durch die Erstellung einer **Desktop-Authentifizierungs-App** mit JavaFX. Bevor wir starten, hier eine Erklärung der wichtigsten Konzepte:

### Was ist JavaFX?

[JavaFX](https://openjfx.io/) ist ein modernes Framework zur Erstellung von Desktop-Anwendungen mit Java. Es ersetzt das ältere Swing-Framework und bietet:

- **FXML**: XML-basierte UI-Beschreibung (ähnlich wie HTML für Webseiten)
- **CSS-Styling**: Gestaltung der Oberfläche mit CSS
- **Properties**: Reaktive Datenbindung zwischen UI und Daten

### Architektur-Patterns

| Pattern | Beschreibung | Warum verwenden wir es? |
|---------|--------------|-------------------------|
| **MVC** | Model-View-Controller trennt Daten, Darstellung und Logik | Bessere Wartbarkeit und Testbarkeit |
| **DAO** | Data Access Object kapselt alle Datenbankzugriffe | Austauschbare Datenbankschicht |
| **Singleton** | Eine einzige Instanz einer Klasse | Für geteilte Ressourcen wie DB-Verbindungen |

> **Weiterführende Links:**
> - [MVC Pattern erklärt](https://www.baeldung.com/mvc-and-mvp-patterns)
> - [DAO Pattern](https://www.baeldung.com/java-dao-pattern)

### Sicherheitskonzepte

| Konzept | Was ist das? | Warum wichtig? |
|---------|--------------|----------------|
| **BCrypt** | Passwort-Hashing-Algorithmus | Speichert Passwörter sicher (nicht im Klartext) |
| **Connection Pool** | Wiederverwendbare DB-Verbindungen | Performance-Optimierung |
| **Prepared Statements** | SQL mit Platzhaltern | Schutz vor SQL-Injection |
| **Brute-Force-Schutz** | Sperrung nach X Fehlversuchen | Verhindert Passwort-Erraten |

> **Weiterführende Links:**
> - [BCrypt erklärt](https://www.baeldung.com/java-password-hashing)
> - [SQL Injection Prevention](https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html)

---

## Voraussetzungen

### 1. Java 21 installieren

Java 21 ist eine **LTS-Version** (Long Term Support) mit vielen modernen Features.

**Download:** [Oracle JDK 21](https://www.oracle.com/java/technologies/downloads/#java21) oder [OpenJDK 21](https://adoptium.net/)

**Installation prüfen:**
```bash
java -version
# Sollte "21.x.x" anzeigen
```

### 2. Maven installieren

[Maven](https://maven.apache.org/) ist ein Build-Tool, das Dependencies (Bibliotheken) automatisch herunterlädt.

**macOS:**
```bash
brew install maven
```

**Windows:** [Download](https://maven.apache.org/download.cgi) und PATH-Variable setzen

**Linux:**
```bash
sudo apt install maven
```

**Installation prüfen:**
```bash
mvn -version
```

### 3. IDE installieren (empfohlen)

- **[IntelliJ IDEA Community](https://www.jetbrains.com/idea/download/)** (kostenlos, empfohlen)
- Alternativ: [VS Code](https://code.visualstudio.com/) mit Java-Extension

### 4. Datenbank installieren

Du brauchst **PostgreSQL** (empfohlen) oder **MySQL**. Siehe [Schritt 1](#️-schritt-1-datenbank-setup).

---

## Projektstruktur

So sieht das fertige Projekt aus:

```
nup-noch-unbekanntes-programm/
├── src/main/java/com/winfriedweis/nup/
│   ├── Main.java                        # Einstiegspunkt der App
│   ├── model/
│   │   └── User.java                    # Datenmodell für Benutzer
│   ├── dao/
│   │   ├── UserDAO.java                 # Interface für DB-Zugriffe
│   │   └── UserDAOImpl.java             # Implementierung der DB-Zugriffe
│   ├── service/
│   │   ├── AuthService.java             # Authentifizierungslogik + Brute-Force-Schutz
│   │   └── ValidationService.java       # Eingabe-Validierung
│   ├── controller/
│   │   ├── LoginController.java         # Login-Seite Logik
│   │   ├── RegisterController.java      # Registrierung Logik
│   │   ├── DashboardController.java     # Dashboard nach Login
│   │   └── SettingsController.java      # Einstellungen-Seite
│   └── util/
│       ├── DatabaseConnection.java      # Verbindung zur Datenbank
│       ├── DatabaseConfig.java          # Konfiguration speichern/laden
│       ├── DatabaseType.java            # Enum: MySQL oder PostgreSQL
│       ├── ImageCropDialog.java         # Profilbild zuschneiden
│       ├── PasswordUtil.java            # BCrypt Passwort-Hashing
│       └── SceneManager.java            # Navigation zwischen Seiten
│
├── src/main/resources/
│   ├── fxml/
│   │   ├── login.fxml                   # Login-Seite UI
│   │   ├── register.fxml                # Registrierung UI
│   │   ├── dashboard.fxml               # Dashboard UI
│   │   └── settings.fxml                # Einstellungen UI
│   ├── css/
│   │   └── dark-theme.css               # Dunkles Design
│   ├── images/
│   │   └── default-avatar.png           # Standard-Profilbild
│   ├── db/
│   │   └── schema.sql                   # Datenbank-Schema
│   └── logback.xml                      # Logging-Konfiguration
│
└── pom.xml                              # Maven-Konfiguration
```

---

## Schritt 1: Datenbank Setup

### Option A: PostgreSQL (Empfohlen)

[PostgreSQL](https://www.postgresql.org/) ist eine leistungsstarke Open-Source-Datenbank.

#### 1.1 PostgreSQL installieren

**macOS:**
```bash
brew install postgresql@16
brew services start postgresql@16
```

**Windows:**
1. Download: [PostgreSQL Installer](https://www.postgresql.org/download/windows/)
2. Installer ausführen (merke dir das Passwort für den `postgres` User!)

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

#### 1.2 Datenbank erstellen

```bash
# Als postgres User einloggen
sudo -u postgres psql     # Linux/macOS
# Oder auf Windows: psql -U postgres

# In der PostgreSQL-Konsole:
CREATE DATABASE nup_db;
\q
```

#### 1.3 Schema erstellen

Erstelle die Datei `src/main/resources/db/schema.sql`:

```sql
-- PostgreSQL Schema für NUP

-- Datenbank erstellen (manuell in psql ausführen oder pgAdmin)
-- CREATE DATABASE nup_db;

-- Dann mit der Datenbank verbinden und folgendes ausführen:

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    profile_image BYTEA,
    profile_image_type VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indizes erstellen
CREATE INDEX IF NOT EXISTS idx_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_email ON users(email);

-- Trigger für automatisches updated_at Update
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

**Schema ausführen:**
```bash
psql -U postgres -d nup_db -f src/main/resources/db/schema.sql
```

> **Was macht dieses Schema?**
> - `SERIAL PRIMARY KEY`: Automatisch hochzählende ID
> - `BYTEA`: Binärdaten für Profilbilder speichern
> - `INDEX`: Schnellere Suche nach Username/Email
> - `TRIGGER`: Automatisches Update von `updated_at`

---

### Option B: MySQL

Falls du MySQL bevorzugst:

**Schema für MySQL:**
```sql
CREATE DATABASE IF NOT EXISTS nup_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE nup_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    profile_image MEDIUMBLOB,
    profile_image_type VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB;
```

> **Unterschiede zu PostgreSQL:**
> - `INT AUTO_INCREMENT` statt `SERIAL`
> - `MEDIUMBLOB` statt `BYTEA` (bis 16 MB Bilder)
> - `ON UPDATE CURRENT_TIMESTAMP` statt Trigger

---

## Schritt 2: Maven Configuration (pom.xml)

Maven verwaltet alle Abhängigkeiten (Libraries) des Projekts.

Erstelle `pom.xml` im Projektroot:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.winfriedweis.nup</groupId>
    <artifactId>nup-noch-unbekanntes-programm</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <javafx.version>21.0.1</javafx.version>
        <slf4j.version>2.0.9</slf4j.version>
        <logback.version>1.4.14</logback.version>
    </properties>

    <dependencies>
        <!-- JavaFX Controls (Buttons, Labels etc.) -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <!-- JavaFX FXML (für deine .fxml Dateien) -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <!-- JavaFX Swing (für SwingFXUtils - Bildkonvertierung) -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-swing</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <!-- PostgreSQL Connector -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.1</version>
        </dependency>

        <!-- MySQL Connector -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.2.0</version>
        </dependency>

        <!-- HikariCP (Connection Pool) -->
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>5.1.0</version>
        </dependency>

        <!-- BCrypt (Passwort Hashing) -->
        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>0.4</version>
        </dependency>

        <!-- SLF4J API -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>${slf4j.version}</version>
        </dependency>

        <!-- Logback (SLF4J Implementation) -->
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>${logback.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>com.winfriedweis.nup.Main</mainClass>
                    <options>
                        <option>--add-opens</option>
                        <option>java.base/java.lang=ALL-UNNAMED</option>
                    </options>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

> **Was bedeuten die Dependencies?**
>
> | Dependency | Zweck |
> |------------|-------|
> | `javafx-controls` | UI-Elemente wie Buttons, Labels, TextFields |
> | `javafx-fxml` | Lädt FXML-Dateien (UI-Beschreibungen) |
> | `javafx-swing` | Konvertiert Bilder zwischen JavaFX und Java AWT |
> | `postgresql` | Treiber für PostgreSQL-Verbindung |
> | `mysql-connector-j` | Treiber für MySQL-Verbindung |
> | `HikariCP` | Schnellster Connection Pool für Java |
> | `jbcrypt` | BCrypt-Implementierung für Passwort-Hashing |
> | `slf4j-api` + `logback` | Professionelles Logging-Framework |

---

## Schritt 3: Logging Configuration

Erstelle `src/main/resources/logback.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- Console Appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- File Appender -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${user.home}/.nup/logs/nup.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${user.home}/.nup/logs/nup.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Application Logging -->
    <logger name="com.winfriedweis.nup" level="DEBUG"/>

    <!-- HikariCP Logging - nur Warnungen -->
    <logger name="com.zaxxer.hikari" level="WARN"/>

    <!-- Root Logger -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

> **Was macht diese Konfiguration?**
> - Logs werden in die Konsole UND in eine Datei geschrieben
> - Log-Dateien werden 30 Tage aufbewahrt
> - Speicherort: `~/.nup/logs/nup.log`

---

## Schritt 4: Model Layer

Das **Model** repräsentiert die Daten unserer Anwendung.

### User.java

Erstelle `src/main/java/com/winfriedweis/nup/model/User.java`:

```java
package com.winfriedweis.nup.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Repräsentiert einen Benutzer in der Datenbank.
 *
 * Verwendet JavaFX Properties für bidirektionales Binding mit der UI.
 * Das bedeutet: Ändert sich ein Wert hier, aktualisiert sich automatisch die UI.
 *
 * @see <a href="https://openjfx.io/javadoc/21/javafx.base/javafx/beans/property/Property.html">JavaFX Properties</a>
 */
public class User {
    // JavaFX Properties ermöglichen automatische UI-Updates
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();

    // Normale Felder für Daten, die nicht in der UI gebunden werden
    private String passwordHash;
    private byte[] profileImage;
    private String profileImageType;

    // Leerer Konstruktor (benötigt für FXML)
    public User() {}

    // Konstruktor für neue Benutzer
    public User(String username, String email, String passwordHash) {
        setUsername(username);
        setEmail(email);
        this.passwordHash = passwordHash;
    }

    // === ID ===
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // === Username ===
    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public StringProperty usernameProperty() {
        return username;
    }

    // === Email ===
    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public StringProperty emailProperty() {
        return email;
    }

    // === Password Hash ===
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // === Profile Image ===
    public byte[] getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }

    public String getProfileImageType() {
        return profileImageType;
    }

    public void setProfileImageType(String type) {
        this.profileImageType = type;
    }
}
```

> **Warum JavaFX Properties?**
>
> Normale Java-Felder aktualisieren die UI nicht automatisch. Mit Properties kannst du schreiben:
> ```java
> label.textProperty().bind(user.usernameProperty());
> ```
> Jetzt aktualisiert sich das Label automatisch, wenn sich der Username ändert!

---

## Schritt 5: Utility Classes

### 5.1 DatabaseType.java

Ein [Enum](https://www.baeldung.com/a-guide-to-java-enums), das die unterstützten Datenbanktypen definiert.

Erstelle `src/main/java/com/winfriedweis/nup/util/DatabaseType.java`:

```java
package com.winfriedweis.nup.util;

/**
 * Unterstützte Datenbanktypen mit ihren Konfigurationen.
 *
 * Ein Enum ist perfekt, wenn du eine feste Anzahl von Optionen hast.
 */
public enum DatabaseType {
    MYSQL("com.mysql.cj.jdbc.Driver", 3306, "jdbc:mysql://"),
    POSTGRESQL("org.postgresql.Driver", 5432, "jdbc:postgresql://");

    private final String driverClassName;
    private final int defaultPort;
    private final String jdbcPrefix;

    DatabaseType(String driverClassName, int defaultPort, String jdbcPrefix) {
        this.driverClassName = driverClassName;
        this.defaultPort = defaultPort;
        this.jdbcPrefix = jdbcPrefix;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public String getJdbcPrefix() {
        return jdbcPrefix;
    }

    /**
     * Erstellt eine JDBC-URL aus den Verbindungsdaten.
     * Beispiel: jdbc:postgresql://localhost:5432/nup_db
     */
    public String buildJdbcUrl(String host, int port, String database) {
        return jdbcPrefix + host + ":" + port + "/" + database;
    }
}
```

---

### 5.2 DatabaseConfig.java

Speichert die Datenbank-Einstellungen in `~/.nup/database.properties`.

Erstelle `src/main/java/com/winfriedweis/nup/util/DatabaseConfig.java`:

```java
package com.winfriedweis.nup.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Singleton für Datenbank-Konfiguration.
 *
 * Speichert Einstellungen in ~/.nup/database.properties
 *
 * Was ist ein Singleton?
 * Eine Klasse, von der nur EINE Instanz existieren kann.
 * Aufruf: DatabaseConfig.getInstance()
 *
 * @see <a href="https://www.baeldung.com/java-singleton">Singleton Pattern</a>
 */
public class DatabaseConfig {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    // Singleton-Instanz
    private static DatabaseConfig instance;

    // Konfigurationspfade
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.nup";
    private static final String CONFIG_FILE = CONFIG_DIR + "/database.properties";

    // Standardwerte
    private DatabaseType type = DatabaseType.POSTGRESQL;
    private String host = "localhost";
    private int port = 5432;
    private String database = "nup_db";
    private String username = "postgres";
    private String password = "";

    // Private Konstruktor (Singleton!)
    private DatabaseConfig() {
        load();
    }

    /**
     * Gibt die einzige Instanz zurück (erstellt sie bei Bedarf).
     * synchronized = Thread-sicher
     */
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    /**
     * Lädt die Konfiguration aus der Datei (falls vorhanden).
     */
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

    /**
     * Speichert die aktuelle Konfiguration in die Datei.
     */
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

    // === Getter und Setter ===

    public String getJdbcUrl() {
        return type.buildJdbcUrl(host, port, database);
    }

    public String getDriverClassName() {
        return type.getDriverClassName();
    }

    public DatabaseType getType() { return type; }
    public void setType(DatabaseType type) { this.type = type; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

---

### 5.3 DatabaseConnection.java

Erstellt `src/main/java/com/winfriedweis/nup/util/DatabaseConnection.java`:

```java
package com.winfriedweis.nup.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Verwaltet Datenbankverbindungen mit HikariCP Connection Pool.
 *
 * Was ist ein Connection Pool?
 * Statt für jede Abfrage eine neue Verbindung zu erstellen (langsam!),
 * werden Verbindungen wiederverwendet (schnell!).
 *
 * HikariCP ist der schnellste Connection Pool für Java.
 *
 * @see <a href="https://github.com/brettwooldridge/HikariCP">HikariCP GitHub</a>
 */
public class DatabaseConnection {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);

    private static DatabaseConnection instance;
    private HikariDataSource dataSource;

    private DatabaseConnection() {
        initializeDataSource();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private void initializeDataSource() {
        DatabaseConfig config = DatabaseConfig.getInstance();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName(config.getDriverClassName());

        // Pool-Einstellungen
        hikariConfig.setMaximumPoolSize(10);    // Max. 10 Verbindungen
        hikariConfig.setMinimumIdle(2);         // Min. 2 offene Verbindungen
        hikariConfig.setConnectionTimeout(30000); // 30 Sek. Timeout
        hikariConfig.setIdleTimeout(600000);    // 10 Min. Idle-Timeout
        hikariConfig.setMaxLifetime(1800000);   // 30 Min. max. Lebensdauer

        // Performance-Optimierungen
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(hikariConfig);
        log.info("Datenbankverbindung initialisiert: {}", config.getJdbcUrl());
    }

    /**
     * Holt eine Verbindung aus dem Pool.
     * WICHTIG: Immer mit try-with-resources verwenden!
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Initialisiert den Pool neu (nach Konfigurationsänderung).
     */
    public void reinitialize() {
        log.info("Datenbankverbindung wird neu initialisiert...");
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        initializeDataSource();
    }

    /**
     * Testet eine Verbindung ohne den Pool zu nutzen.
     * Gibt null zurück bei Erfolg, sonst die Fehlermeldung.
     */
    public static String testConnectionWithError(DatabaseType type, String host,
                                                  int port, String database,
                                                  String username, String password) {
        String jdbcUrl = type.buildJdbcUrl(host, port, database);

        try {
            Class.forName(type.getDriverClassName());
            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
                if (conn != null && !conn.isClosed()) {
                    return null; // Erfolg!
                }
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return "Unbekannter Fehler";
    }

    /**
     * Schließt den Connection Pool (bei App-Ende).
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("Datenbankverbindung geschlossen");
        }
    }
}
```

---

### 5.4 PasswordUtil.java

Erstelle `src/main/java/com/winfriedweis/nup/util/PasswordUtil.java`:

```java
package com.winfriedweis.nup.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility-Klasse für sicheres Passwort-Hashing mit BCrypt.
 *
 * Was ist Hashing?
 * Eine Einweg-Funktion, die aus einem Passwort einen Hash erstellt.
 * Der Hash kann NICHT zurück in das Passwort umgewandelt werden.
 *
 * Warum BCrypt?
 * - Speziell für Passwörter entwickelt
 * - Enthält automatisch einen "Salt" (Zufallswert)
 * - Langsam genug, um Brute-Force-Angriffe zu erschweren
 *
 * @see <a href="https://www.baeldung.com/java-password-hashing">BCrypt in Java</a>
 */
public class PasswordUtil {
    // 12 Log Rounds = 2^12 = 4096 Iterationen
    // Höher = sicherer, aber langsamer
    private static final int LOG_ROUNDS = 12;

    /**
     * Erstellt einen BCrypt-Hash aus dem Klartext-Passwort.
     *
     * @param plainPassword Das Passwort im Klartext
     * @return Der BCrypt-Hash (enthält Salt und Algorithmus-Info)
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Überprüft, ob ein Passwort zum Hash passt.
     *
     * @param plainPassword Das eingegebene Passwort
     * @param hashedPassword Der gespeicherte Hash
     * @return true wenn das Passwort korrekt ist
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
```

---

### 5.5 SceneManager.java

Erstelle `src/main/java/com/winfriedweis/nup/util/SceneManager.java`:

```java
package com.winfriedweis.nup.util;

import com.winfriedweis.nup.controller.DashboardController;
import com.winfriedweis.nup.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Singleton für die Navigation zwischen Seiten (Scenes).
 *
 * Verwaltet:
 * - Das Hauptfenster (Stage)
 * - Den aktuell eingeloggten Benutzer
 * - Das Wechseln zwischen FXML-Seiten
 */
public class SceneManager {
    private static final Logger log = LoggerFactory.getLogger(SceneManager.class);

    private static SceneManager instance;
    private Stage primaryStage;
    private User currentUser;

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // === Navigation ===

    public void switchToLogin() {
        currentUser = null;
        loadScene("/fxml/login.fxml", "NUP - Login");
    }

    public void switchToRegister() {
        loadScene("/fxml/register.fxml", "NUP - Registrierung");
    }

    public void switchToDashboard(User user) {
        this.currentUser = user;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            // Controller manuell initialisieren (mit User-Daten)
            DashboardController controller = loader.getController();
            controller.initData(user);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/css/dark-theme.css").toExternalForm()
            );

            primaryStage.setScene(scene);
            primaryStage.setTitle("NUP - Dashboard - " + user.getUsername());
            primaryStage.setMaximized(true);  // Maximiert starten
            primaryStage.show();
        } catch (IOException e) {
            log.error("Fehler beim Laden des Dashboards", e);
        }
    }

    public void switchToSettings() {
        loadScene("/fxml/settings.fxml", "NUP - Einstellungen");
    }

    /**
     * Intelligente Rückkehr: Zum Dashboard wenn eingeloggt, sonst zum Login.
     */
    public void switchBackFromSettings() {
        if (isLoggedIn()) {
            switchToDashboard(currentUser);
        } else {
            switchToLogin();
        }
    }

    private void loadScene(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/css/dark-theme.css").toExternalForm()
            );

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.show();
        } catch (IOException e) {
            log.error("Fehler beim Laden der Szene: {}", fxmlPath, e);
        }
    }
}
```

---

### 5.6 ImageCropDialog.java

Erstelle `src/main/java/com/winfriedweis/nup/util/ImageCropDialog.java`:

```java
package com.winfriedweis.nup.util;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Dialog zum Zuschneiden von Profilbildern.
 *
 * Features:
 * - Ziehbares Auswahlrechteck
 * - Drittel-Raster (Rule of Thirds)
 * - Quadratischer Ausschnitt für Profilbilder
 */
public class ImageCropDialog {

    /**
     * Zeigt den Crop-Dialog und gibt das zugeschnittene Bild zurück.
     *
     * @param image Das Originalbild
     * @param owner Das Eltern-Fenster (für modale Anzeige)
     * @return Das zugeschnittene Bild oder empty() bei Abbruch
     */
    public static Optional<WritableImage> show(Image image, Window owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Bildausschnitt wählen");

        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();

        // Canvas-Größe berechnen (max 600px)
        double maxCanvasSize = 600;
        double scale = Math.min(maxCanvasSize / imageWidth, maxCanvasSize / imageHeight);
        double canvasWidth = imageWidth * scale;
        double canvasHeight = imageHeight * scale;

        Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Auswahlrechteck (quadratisch, 70% der kleineren Seite)
        double[] cropSize = {Math.min(canvasWidth, canvasHeight) * 0.7};
        double[] cropX = {(canvasWidth - cropSize[0]) / 2};
        double[] cropY = {(canvasHeight - cropSize[0]) / 2};

        // Drag-State
        final boolean[] isDragging = {false};
        final double[] dragStartX = {0};
        final double[] dragStartY = {0};

        // Zeichenfunktion
        Runnable redraw = () -> {
            gc.clearRect(0, 0, canvasWidth, canvasHeight);
            gc.drawImage(image, 0, 0, canvasWidth, canvasHeight);

            // Dunkler Overlay außerhalb der Auswahl
            gc.setFill(Color.rgb(0, 0, 0, 0.5));
            gc.fillRect(0, 0, canvasWidth, cropY[0]);
            gc.fillRect(0, cropY[0], cropX[0], cropSize[0]);
            gc.fillRect(cropX[0] + cropSize[0], cropY[0],
                       canvasWidth - (cropX[0] + cropSize[0]), cropSize[0]);
            gc.fillRect(0, cropY[0] + cropSize[0], canvasWidth,
                       canvasHeight - (cropY[0] + cropSize[0]));

            // Auswahlrahmen
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeRect(cropX[0], cropY[0], cropSize[0], cropSize[0]);

            // Drittel-Raster
            gc.setStroke(Color.rgb(255, 255, 255, 0.3));
            gc.setLineWidth(1);
            double third = cropSize[0] / 3;
            gc.strokeLine(cropX[0] + third, cropY[0], cropX[0] + third, cropY[0] + cropSize[0]);
            gc.strokeLine(cropX[0] + 2 * third, cropY[0], cropX[0] + 2 * third, cropY[0] + cropSize[0]);
            gc.strokeLine(cropX[0], cropY[0] + third, cropX[0] + cropSize[0], cropY[0] + third);
            gc.strokeLine(cropX[0], cropY[0] + 2 * third, cropX[0] + cropSize[0], cropY[0] + 2 * third);
        };

        redraw.run();

        // Maus-Events für Drag
        canvas.setOnMousePressed(e -> {
            if (e.getX() >= cropX[0] && e.getX() <= cropX[0] + cropSize[0] &&
                e.getY() >= cropY[0] && e.getY() <= cropY[0] + cropSize[0]) {
                isDragging[0] = true;
                dragStartX[0] = e.getX() - cropX[0];
                dragStartY[0] = e.getY() - cropY[0];
            }
        });

        canvas.setOnMouseDragged(e -> {
            if (isDragging[0]) {
                cropX[0] = Math.max(0, Math.min(canvasWidth - cropSize[0], e.getX() - dragStartX[0]));
                cropY[0] = Math.max(0, Math.min(canvasHeight - cropSize[0], e.getY() - dragStartY[0]));
                redraw.run();
            }
        });

        canvas.setOnMouseReleased(e -> isDragging[0] = false);

        // Buttons
        Button okButton = new Button("Übernehmen");
        Button cancelButton = new Button("Abbrechen");

        final WritableImage[] result = {null};

        okButton.setOnAction(e -> {
            // Zurück auf Original-Koordinaten skalieren
            double scaleBack = 1.0 / scale;
            int x = (int) (cropX[0] * scaleBack);
            int y = (int) (cropY[0] * scaleBack);
            int size = (int) (cropSize[0] * scaleBack);

            // Crop ausführen
            PixelReader reader = image.getPixelReader();
            result[0] = new WritableImage(reader, x, y, size, size);
            dialog.close();
        });

        cancelButton.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(10, okButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(15,
            new Label("Ziehe das Auswahlrechteck auf den gewünschten Bereich"),
            canvas,
            buttonBox);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20; -fx-background-color: #3C3F41;");

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
            ImageCropDialog.class.getResource("/css/dark-theme.css").toExternalForm()
        );

        dialog.setScene(scene);
        dialog.showAndWait();

        return Optional.ofNullable(result[0]);
    }
}
```

---

## Schritt 6: DAO Layer

Das **DAO-Pattern** (Data Access Object) trennt die Datenbanklogik vom Rest der Anwendung.

### 6.1 UserDAO.java (Interface)

Erstelle `src/main/java/com/winfriedweis/nup/dao/UserDAO.java`:

```java
package com.winfriedweis.nup.dao;

import com.winfriedweis.nup.model.User;
import java.util.Optional;

/**
 * Interface für User-Datenbankoperationen.
 *
 * Warum ein Interface?
 * - Austauschbare Implementierungen (z.B. für Tests)
 * - Klare Trennung von Vertrag und Implementierung
 *
 * @see <a href="https://www.baeldung.com/java-dao-pattern">DAO Pattern</a>
 */
public interface UserDAO {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String identifier);
    boolean createUser(User user);
    boolean updateProfileImage(int userId, byte[] imageData, String imageType);
    Optional<byte[]> getProfileImage(int userId);
}
```

---

### 6.2 UserDAOImpl.java (Implementierung)

Erstelle `src/main/java/com/winfriedweis/nup/dao/UserDAOImpl.java`:

```java
package com.winfriedweis.nup.dao;

import com.winfriedweis.nup.model.User;
import com.winfriedweis.nup.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Optional;

/**
 * Implementierung der User-Datenbankoperationen.
 *
 * Verwendet:
 * - PreparedStatements (gegen SQL-Injection)
 * - try-with-resources (automatisches Schließen von Ressourcen)
 * - Optional (statt null)
 */
public class UserDAOImpl implements UserDAO {
    private static final Logger log = LoggerFactory.getLogger(UserDAOImpl.class);

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, email, password_hash, profile_image_type " +
                     "FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            log.error("Fehler bei findByUsername: {}", username, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, username, email, password_hash, profile_image_type " +
                     "FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            log.error("Fehler bei findByEmail: {}", email, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String identifier) {
        String sql = "SELECT id, username, email, password_hash, profile_image_type " +
                     "FROM users WHERE username = ? OR email = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, identifier);
            pstmt.setString(2, identifier);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            log.error("Fehler bei findByUsernameOrEmail: {}", identifier, e);
        }
        return Optional.empty();
    }

    @Override
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPasswordHash());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
                log.info("Neuer Benutzer erstellt: {}", user.getUsername());
                return true;
            }
        } catch (SQLException e) {
            log.error("Fehler bei createUser: {}", user.getUsername(), e);
        }
        return false;
    }

    @Override
    public boolean updateProfileImage(int userId, byte[] imageData, String imageType) {
        String sql = "UPDATE users SET profile_image = ?, profile_image_type = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBytes(1, imageData);
            pstmt.setString(2, imageType);
            pstmt.setInt(3, userId);

            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                log.info("Profilbild aktualisiert für User-ID: {}", userId);
            }
            return success;
        } catch (SQLException e) {
            log.error("Fehler bei updateProfileImage für User-ID: {}", userId, e);
        }
        return false;
    }

    @Override
    public Optional<byte[]> getProfileImage(int userId) {
        String sql = "SELECT profile_image FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                byte[] imageData = rs.getBytes("profile_image");
                if (imageData != null) {
                    return Optional.of(imageData);
                }
            }
        } catch (SQLException e) {
            log.error("Fehler bei getProfileImage für User-ID: {}", userId, e);
        }
        return Optional.empty();
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setProfileImageType(rs.getString("profile_image_type"));
        return user;
    }
}
```

---

## Schritt 7: Service Layer

Die **Service-Schicht** enthält die Geschäftslogik.

### 7.1 ValidationService.java

Erstelle `src/main/java/com/winfriedweis/nup/service/ValidationService.java`:

```java
package com.winfriedweis.nup.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validierung von Benutzereingaben.
 *
 * Alle Methoden sind static, da kein Zustand gespeichert wird.
 */
public class ValidationService {
    // Email-Pattern (vereinfacht, für die meisten Fälle ausreichend)
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Username: 3-20 Zeichen, nur Buchstaben, Zahlen, Unterstrich
    private static final Pattern USERNAME_PATTERN =
        Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Prüft, ob das Passwort alle Anforderungen erfüllt:
     * - Mindestens 8 Zeichen
     * - Mindestens ein Großbuchstabe
     * - Mindestens ein Kleinbuchstabe
     * - Mindestens eine Ziffer
     * - Mindestens ein Sonderzeichen
     */
    public static boolean isValidPassword(String password) {
        return getPasswordErrors(password).isEmpty();
    }

    /**
     * Gibt eine Liste aller nicht erfüllten Passwort-Anforderungen zurück.
     */
    public static List<String> getPasswordErrors(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("Passwort darf nicht leer sein");
            return errors;
        }

        if (password.length() < 8) {
            errors.add("Mindestens 8 Zeichen");
        }

        if (!password.chars().anyMatch(Character::isUpperCase)) {
            errors.add("Mindestens ein Großbuchstabe");
        }

        if (!password.chars().anyMatch(Character::isLowerCase)) {
            errors.add("Mindestens ein Kleinbuchstabe");
        }

        if (!password.chars().anyMatch(Character::isDigit)) {
            errors.add("Mindestens eine Zahl");
        }

        if (!password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0)) {
            errors.add("Mindestens ein Sonderzeichen (!@#$%^&*...)");
        }

        return errors;
    }
}
```

---

### 7.2 AuthService.java

Erstelle `src/main/java/com/winfriedweis/nup/service/AuthService.java`:

```java
package com.winfriedweis.nup.service;

import com.winfriedweis.nup.dao.UserDAO;
import com.winfriedweis.nup.dao.UserDAOImpl;
import com.winfriedweis.nup.model.User;
import com.winfriedweis.nup.util.PasswordUtil;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service für Authentifizierung mit Brute-Force-Schutz.
 *
 * Brute-Force-Schutz:
 * - Max. 5 Fehlversuche pro Account
 * - 5 Minuten Sperre nach Erreichen des Limits
 * - ConcurrentHashMap für Thread-Sicherheit
 */
public class AuthService {
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_SECONDS = 300; // 5 Minuten

    private final UserDAO userDAO;

    // Thread-sichere Map für Login-Versuche
    private final Map<String, LoginAttemptInfo> loginAttempts = new ConcurrentHashMap<>();

    public AuthService() {
        this.userDAO = new UserDAOImpl();
    }

    /**
     * Record für Login-Ergebnis (Java 16+ Feature).
     * Immutable und automatisch mit equals/hashCode/toString.
     */
    public record LoginResult(boolean success, User user, String errorMessage) {
        public static LoginResult success(User user) {
            return new LoginResult(true, user, null);
        }
        public static LoginResult failure(String message) {
            return new LoginResult(false, null, message);
        }
    }

    /**
     * Führt den Login durch mit Brute-Force-Schutz.
     */
    public LoginResult login(String identifier, String password) {
        if (identifier == null || identifier.isBlank()) {
            return LoginResult.failure("Benutzername/Email darf nicht leer sein");
        }

        String normalizedIdentifier = identifier.toLowerCase().trim();

        // Prüfe Lockout
        if (isLockedOut(normalizedIdentifier)) {
            long remainingSeconds = getRemainingLockoutSeconds(normalizedIdentifier);
            return LoginResult.failure("Account gesperrt. Versuche es in " +
                formatDuration(remainingSeconds) + " erneut.");
        }

        Optional<User> userOpt = userDAO.findByUsernameOrEmail(identifier);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                resetLoginAttempts(normalizedIdentifier);
                return LoginResult.success(user);
            }
        }

        // Fehlgeschlagener Versuch
        recordFailedAttempt(normalizedIdentifier);
        int remainingAttempts = MAX_LOGIN_ATTEMPTS - getFailedAttempts(normalizedIdentifier);

        if (remainingAttempts <= 0) {
            return LoginResult.failure("Account gesperrt für " +
                formatDuration(LOCKOUT_DURATION_SECONDS) + " wegen zu vieler Fehlversuche.");
        }

        return LoginResult.failure("Ungültige Anmeldedaten. Noch " + remainingAttempts + " Versuche.");
    }

    /**
     * Registriert einen neuen Benutzer.
     */
    public boolean register(String username, String email, String password,
                           StringBuilder errorMessage) {
        if (!ValidationService.isValidUsername(username)) {
            errorMessage.append("Username muss 3-20 Zeichen lang sein (nur Buchstaben, Zahlen, _)");
            return false;
        }

        if (!ValidationService.isValidEmail(email)) {
            errorMessage.append("Ungültige Email-Adresse");
            return false;
        }

        List<String> passwordErrors = ValidationService.getPasswordErrors(password);
        if (!passwordErrors.isEmpty()) {
            errorMessage.append("Passwort-Anforderungen:\n");
            for (String error : passwordErrors) {
                errorMessage.append("- ").append(error).append("\n");
            }
            return false;
        }

        if (userDAO.findByUsername(username).isPresent()) {
            errorMessage.append("Username bereits vergeben");
            return false;
        }

        if (userDAO.findByEmail(email).isPresent()) {
            errorMessage.append("Email bereits registriert");
            return false;
        }

        String passwordHash = PasswordUtil.hashPassword(password);
        User newUser = new User(username, email, passwordHash);

        return userDAO.createUser(newUser);
    }

    // === Brute-Force-Schutz Hilfsmethoden ===

    private boolean isLockedOut(String identifier) {
        LoginAttemptInfo info = loginAttempts.get(identifier);
        if (info == null) {
            return false;
        }

        if (info.attempts >= MAX_LOGIN_ATTEMPTS) {
            long elapsedSeconds = Instant.now().getEpochSecond() - info.lastAttemptTime;
            if (elapsedSeconds < LOCKOUT_DURATION_SECONDS) {
                return true;
            }
            loginAttempts.remove(identifier);
        }
        return false;
    }

    private long getRemainingLockoutSeconds(String identifier) {
        LoginAttemptInfo info = loginAttempts.get(identifier);
        if (info == null) {
            return 0;
        }
        long elapsedSeconds = Instant.now().getEpochSecond() - info.lastAttemptTime;
        return Math.max(0, LOCKOUT_DURATION_SECONDS - elapsedSeconds);
    }

    private void recordFailedAttempt(String identifier) {
        loginAttempts.compute(identifier, (key, info) -> {
            if (info == null) {
                return new LoginAttemptInfo(1, Instant.now().getEpochSecond());
            }
            return new LoginAttemptInfo(info.attempts + 1, Instant.now().getEpochSecond());
        });
    }

    private int getFailedAttempts(String identifier) {
        LoginAttemptInfo info = loginAttempts.get(identifier);
        return info != null ? info.attempts : 0;
    }

    private void resetLoginAttempts(String identifier) {
        loginAttempts.remove(identifier);
    }

    private String formatDuration(long seconds) {
        if (seconds >= 60) {
            long minutes = seconds / 60;
            return minutes + " Minute" + (minutes > 1 ? "n" : "");
        }
        return seconds + " Sekunden";
    }

    // Record für Login-Versuchs-Info
    private record LoginAttemptInfo(int attempts, long lastAttemptTime) {}
}
```

---

## Schritt 8: Dark Theme CSS

Erstelle `src/main/resources/css/dark-theme.css`:

```css
/* ============================================
   NUP Dark Theme - IntelliJ IDEA inspiriert
   ============================================ */

/* Basis-Farben als CSS-Variablen */
.root {
    -fx-base: #2B2B2B;
    -fx-background: #3C3F41;
    -fx-control-inner-background: #2B2B2B;
    -fx-accent: #4A88C7;
    -fx-focus-color: #4A88C7;
    -fx-faint-focus-color: #4A88C722;
    -fx-text-base-color: #BBBBBB;
    -fx-text-fill: #BBBBBB;
    -fx-prompt-text-fill: #787878;
}

/* Hauptcontainer */
.container {
    -fx-background-color: #3C3F41;
}

/* Karten für Inhaltsblöcke */
.card {
    -fx-background-color: #2B2B2B;
    -fx-background-radius: 8px;
    -fx-padding: 30px;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);
}

/* === Eingabefelder === */
.text-field, .password-field {
    -fx-background-color: #45494A;
    -fx-text-fill: #BBBBBB;
    -fx-prompt-text-fill: #787878;
    -fx-background-radius: 4px;
    -fx-border-color: #555555;
    -fx-border-radius: 4px;
    -fx-padding: 10px 14px;
    -fx-font-size: 14px;
    -fx-min-height: 38px;
}

.text-field:focused, .password-field:focused {
    -fx-border-color: #4A88C7;
    -fx-border-width: 2px;
}

/* === Labels === */
.label {
    -fx-text-fill: #BBBBBB;
    -fx-font-size: 14px;
}

.title-label {
    -fx-font-size: 28px;
    -fx-font-weight: bold;
    -fx-text-fill: #FFFFFF;
}

.subtitle-label {
    -fx-font-size: 14px;
    -fx-text-fill: #787878;
}

/* === Buttons === */
.button {
    -fx-background-color: #365880;
    -fx-text-fill: white;
    -fx-font-size: 14px;
    -fx-padding: 12px 24px;
    -fx-background-radius: 4px;
    -fx-cursor: hand;
    -fx-min-height: 40px;
}

.button:hover {
    -fx-background-color: #4A88C7;
}

.button:pressed {
    -fx-background-color: #2B5F8D;
}

.secondary-button {
    -fx-background-color: transparent;
    -fx-text-fill: #4A88C7;
    -fx-border-color: #4A88C7;
    -fx-border-width: 1px;
    -fx-border-radius: 4px;
    -fx-padding: 8px 16px;
}

.secondary-button:hover {
    -fx-background-color: #4A88C722;
}

.icon-button {
    -fx-background-color: #2B2B2B;
    -fx-text-fill: #BBBBBB;
    -fx-font-size: 18px;
    -fx-background-radius: 20px;
    -fx-cursor: hand;
}

.icon-button:hover {
    -fx-background-color: #365880;
}

/* === Fehler/Erfolg Meldungen === */
.error-label {
    -fx-text-fill: #FF6B68;
    -fx-font-size: 13px;
}

.success-label {
    -fx-text-fill: #6A9955;
    -fx-font-size: 13px;
}

/* === Profilbild === */
.profile-image-view {
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);
}

/* === Links === */
.hyperlink {
    -fx-text-fill: #4A88C7;
    -fx-underline: false;
}

.hyperlink:hover {
    -fx-underline: true;
}

/* === ScrollPane === */
.scroll-pane {
    -fx-background-color: transparent;
    -fx-background: transparent;
}

.scroll-pane .viewport {
    -fx-background-color: transparent;
}

.scroll-pane .content {
    -fx-background-color: transparent;
}

/* === ComboBox === */
.combo-box {
    -fx-background-color: #45494A;
    -fx-border-color: #555555;
    -fx-border-radius: 4px;
}

.combo-box .list-cell {
    -fx-background-color: #45494A;
    -fx-text-fill: #BBBBBB;
}

.combo-box-popup .list-view {
    -fx-background-color: #2B2B2B;
}

.combo-box-popup .list-cell:hover {
    -fx-background-color: #365880;
}
```

---

## Schritt 9: FXML Views

### 9.1 login.fxml

Erstelle `src/main/resources/fxml/login.fxml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<BorderPane xmlns="http://javafx.com/javafx"
            xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.winfriedweis.nup.controller.LoginController"
            styleClass="container"
            minWidth="800" minHeight="600">

    <!-- Settings Button oben rechts -->
    <top>
        <HBox alignment="CENTER_RIGHT" BorderPane.alignment="CENTER_RIGHT"
              style="-fx-padding: 20px;">
            <Button onAction="#handleOpenSettings"
                    text="⚙"
                    styleClass="icon-button"
                    style="-fx-min-width: 40px; -fx-min-height: 40px;"/>
        </HBox>
    </top>

    <!-- Login Card mittig -->
    <center>
        <StackPane>
            <VBox alignment="CENTER" spacing="20"
                  StackPane.alignment="CENTER"
                  style="-fx-padding: 40px;">

                <Label text="NUP - Login"
                       styleClass="title-label"/>

                <VBox spacing="20" maxWidth="420"
                      styleClass="card">

                    <Label text="Bitte melde dich mit deinem Konto an."
                           styleClass="subtitle-label"/>

                    <VBox spacing="8">
                        <Label text="E-Mail oder Username"/>
                        <TextField fx:id="emailField"
                                   promptText="E-Mail oder Username eingeben"/>
                    </VBox>

                    <VBox spacing="8">
                        <Label text="Passwort"/>
                        <PasswordField fx:id="passwordField"
                                       promptText="Passwort eingeben"/>
                    </VBox>

                    <Label fx:id="errorLabel"
                           styleClass="error-label"
                           visible="false"
                           wrapText="true"/>

                    <Button text="Anmelden"
                            onAction="#handleLogin"
                            maxWidth="Infinity"
                            defaultButton="true"/>

                    <Hyperlink text="Noch kein Konto? Hier registrieren."
                               onAction="#handleOpenRegister"
                               style="-fx-padding: 10px 0 0 0;"/>
                </VBox>
            </VBox>
        </StackPane>
    </center>

</BorderPane>
```

---

### 9.2 register.fxml

Erstelle `src/main/resources/fxml/register.fxml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<BorderPane xmlns="http://javafx.com/javafx"
            xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.winfriedweis.nup.controller.RegisterController"
            styleClass="container"
            minWidth="800" minHeight="650">

    <center>
        <StackPane>
            <VBox alignment="CENTER" spacing="20"
                  StackPane.alignment="CENTER"
                  style="-fx-padding: 40px;">

                <Label text="Registrierung"
                       styleClass="title-label"/>

                <VBox spacing="18" maxWidth="450"
                      styleClass="card">

                    <Label text="Erstelle dein kostenloses Konto"
                           styleClass="subtitle-label"/>

                    <VBox spacing="8">
                        <Label text="Benutzername"/>
                        <TextField fx:id="usernameField"
                                   promptText="3-20 Zeichen (a-z, 0-9, _)"/>
                    </VBox>

                    <VBox spacing="8">
                        <Label text="E-Mail"/>
                        <TextField fx:id="emailField"
                                   promptText="deine@email.de"/>
                    </VBox>

                    <VBox spacing="8">
                        <Label text="Passwort"/>
                        <PasswordField fx:id="passwordField"
                                       promptText="Min. 8 Zeichen, Groß-/Kleinbuchstaben, Zahl, Sonderzeichen"/>
                    </VBox>

                    <VBox spacing="8">
                        <Label text="Passwort bestätigen"/>
                        <PasswordField fx:id="confirmPasswordField"
                                       promptText="Passwort wiederholen"/>
                    </VBox>

                    <Label fx:id="errorLabel"
                           styleClass="error-label"
                           visible="false"
                           wrapText="true"/>

                    <Label fx:id="successLabel"
                           styleClass="success-label"
                           visible="false"
                           wrapText="true"/>

                    <Button text="Registrieren"
                            onAction="#handleRegister"
                            maxWidth="Infinity"
                            defaultButton="true"/>

                    <Hyperlink text="Schon ein Konto? Zurück zum Login."
                               onAction="#handleBackToLogin"
                               style="-fx-padding: 10px 0 0 0;"/>
                </VBox>
            </VBox>
        </StackPane>
    </center>

</BorderPane>
```

---

### 9.3 dashboard.fxml

Erstelle `src/main/resources/fxml/dashboard.fxml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.*?>
<?import javafx.scene.image.ImageView?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.shape.Circle?>

<BorderPane xmlns="http://javafx.com/javafx"
            xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.winfriedweis.nup.controller.DashboardController"
            styleClass="container"
            minWidth="1024" minHeight="768">

    <!-- Top Bar -->
    <top>
        <HBox spacing="20"
              alignment="CENTER_LEFT"
              styleClass="card"
              style="-fx-padding: 20px;">

            <!-- Profilbild (klickbar für Vorschau) -->
            <StackPane onMouseClicked="#handleProfileImageClick"
                       style="-fx-cursor: hand;">
                <Circle radius="35" fill="#555555"/>
                <ImageView fx:id="profileImageView"
                           fitWidth="70" fitHeight="70"
                           preserveRatio="true"
                           styleClass="profile-image-view">
                    <clip>
                        <Circle radius="35" centerX="35" centerY="35"/>
                    </clip>
                </ImageView>
            </StackPane>

            <!-- User Info -->
            <VBox spacing="5">
                <Label fx:id="usernameLabel"
                       style="-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;"/>
                <Label fx:id="emailLabel"
                       styleClass="subtitle-label"/>
            </VBox>

            <!-- Spacer -->
            <Region HBox.hgrow="ALWAYS"/>

            <!-- Action Buttons -->
            <Button text="⚙ Einstellungen"
                    onAction="#handleOpenSettings"
                    styleClass="secondary-button"/>

            <Button text="Abmelden"
                    onAction="#handleLogout"
                    styleClass="secondary-button"/>
        </HBox>
    </top>

    <!-- Center Content -->
    <center>
        <ScrollPane fitToWidth="true" fitToHeight="true"
                    style="-fx-background-color: transparent;">
            <VBox alignment="CENTER" spacing="40"
                  style="-fx-padding: 60px;">

                <Label text="Willkommen in NUP!"
                       styleClass="title-label"
                       style="-fx-font-size: 48px;"/>

                <Label text="Deine Authentifizierung war erfolgreich!"
                       styleClass="subtitle-label"
                       style="-fx-font-size: 20px;"/>

                <!-- Feature Grid -->
                <GridPane hgap="30" vgap="30"
                          alignment="CENTER"
                          maxWidth="1200">

                    <!-- Feature 1: Sicherheit -->
                    <VBox GridPane.columnIndex="0" GridPane.rowIndex="0"
                          alignment="CENTER" spacing="15"
                          styleClass="card"
                          style="-fx-padding: 40px; -fx-min-width: 350px; -fx-min-height: 250px;">
                        <Label text="🔒" style="-fx-font-size: 48px;"/>
                        <Label text="Sicherheit"
                               style="-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;"/>
                        <Label text="BCrypt Passwort-Hashing"
                               style="-fx-font-size: 14px; -fx-text-fill: #6A9955;"/>
                        <Label text="Sichere Authentifizierung mit modernsten Standards"
                               wrapText="true" textAlignment="CENTER"
                               style="-fx-font-size: 13px; -fx-text-fill: #787878;"/>
                    </VBox>

                    <!-- Feature 2: Konfigurierbar -->
                    <VBox GridPane.columnIndex="1" GridPane.rowIndex="0"
                          alignment="CENTER" spacing="15"
                          styleClass="card"
                          style="-fx-padding: 40px; -fx-min-width: 350px; -fx-min-height: 250px;">
                        <Label text="⚙" style="-fx-font-size: 48px;"/>
                        <Label text="Konfigurierbar"
                               style="-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;"/>
                        <Label text="PostgreSQL + MySQL Support"
                               style="-fx-font-size: 14px; -fx-text-fill: #4A88C7;"/>
                        <Label text="Wähle deine bevorzugte Datenbank"
                               wrapText="true" textAlignment="CENTER"
                               style="-fx-font-size: 13px; -fx-text-fill: #787878;"/>
                    </VBox>

                    <!-- Feature 3: Benutzerprofile -->
                    <VBox GridPane.columnIndex="0" GridPane.rowIndex="1"
                          alignment="CENTER" spacing="15"
                          styleClass="card"
                          style="-fx-padding: 40px; -fx-min-width: 350px; -fx-min-height: 250px;">
                        <Label text="👤" style="-fx-font-size: 48px;"/>
                        <Label text="Benutzerprofile"
                               style="-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;"/>
                        <Label text="Profilbild mit Crop-Editor"
                               style="-fx-font-size: 14px; -fx-text-fill: #6A9955;"/>
                        <Label text="Personalisiere dein Profil mit einem eigenen Bild"
                               wrapText="true" textAlignment="CENTER"
                               style="-fx-font-size: 13px; -fx-text-fill: #787878;"/>
                    </VBox>

                    <!-- Feature 4: Erweiterbar -->
                    <VBox GridPane.columnIndex="1" GridPane.rowIndex="1"
                          alignment="CENTER" spacing="15"
                          styleClass="card"
                          style="-fx-padding: 40px; -fx-min-width: 350px; -fx-min-height: 250px;">
                        <Label text="🚀" style="-fx-font-size: 48px;"/>
                        <Label text="Brute-Force-Schutz"
                               style="-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;"/>
                        <Label text="5 Versuche, dann 5 Min. Sperre"
                               style="-fx-font-size: 14px; -fx-text-fill: #4A88C7;"/>
                        <Label text="Automatischer Schutz vor Passwort-Attacken"
                               wrapText="true" textAlignment="CENTER"
                               style="-fx-font-size: 13px; -fx-text-fill: #787878;"/>
                    </VBox>
                </GridPane>

                <!-- Status Info -->
                <VBox alignment="CENTER" spacing="15"
                      styleClass="card"
                      style="-fx-padding: 30px; -fx-max-width: 800px;">
                    <Label text="Projekt-Status: Version 2.0"
                           style="-fx-font-size: 18px; -fx-text-fill: #6A9955; -fx-font-weight: bold;"/>
                    <Separator maxWidth="600"/>
                    <HBox spacing="40" alignment="CENTER">
                        <VBox alignment="CENTER" spacing="5">
                            <Label text="✓" style="-fx-font-size: 24px; -fx-text-fill: #6A9955;"/>
                            <Label text="Login" style="-fx-font-size: 14px;"/>
                        </VBox>
                        <VBox alignment="CENTER" spacing="5">
                            <Label text="✓" style="-fx-font-size: 24px; -fx-text-fill: #6A9955;"/>
                            <Label text="Registrierung" style="-fx-font-size: 14px;"/>
                        </VBox>
                        <VBox alignment="CENTER" spacing="5">
                            <Label text="✓" style="-fx-font-size: 24px; -fx-text-fill: #6A9955;"/>
                            <Label text="Multi-DB" style="-fx-font-size: 14px;"/>
                        </VBox>
                        <VBox alignment="CENTER" spacing="5">
                            <Label text="✓" style="-fx-font-size: 24px; -fx-text-fill: #6A9955;"/>
                            <Label text="Sicherheit" style="-fx-font-size: 14px;"/>
                        </VBox>
                    </HBox>
                </VBox>
            </VBox>
        </ScrollPane>
    </center>

    <!-- Bottom Bar -->
    <bottom>
        <HBox alignment="CENTER" spacing="20"
              style="-fx-padding: 15px; -fx-background-color: #2B2B2B;">
            <Label text="NUP Version 2.0" styleClass="subtitle-label"/>
            <Separator orientation="VERTICAL"/>
            <Label text="Java 21 + JavaFX 21" styleClass="subtitle-label"/>
        </HBox>
    </bottom>

</BorderPane>
```

---

### 9.4 settings.fxml

Erstelle `src/main/resources/fxml/settings.fxml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.*?>
<?import javafx.scene.image.ImageView?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.shape.Circle?>

<BorderPane xmlns="http://javafx.com/javafx"
            xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.winfriedweis.nup.controller.SettingsController"
            styleClass="container"
            minWidth="800" minHeight="700">

    <center>
        <ScrollPane fitToWidth="true" fitToHeight="true"
                    style="-fx-background-color: transparent;">
            <VBox alignment="CENTER" spacing="30"
                  style="-fx-padding: 60px;">

                <Label text="⚙ Einstellungen"
                       styleClass="title-label"
                       style="-fx-font-size: 36px;"/>

                <!-- Profil-Sektion (nur sichtbar wenn eingeloggt) -->
                <VBox fx:id="profileSection" spacing="20" maxWidth="600"
                      styleClass="card"
                      managed="false" visible="false">

                    <Label text="Profil"
                           style="-fx-font-size: 20px; -fx-font-weight: bold;"/>

                    <HBox spacing="20" alignment="CENTER_LEFT">
                        <!-- Profilbild -->
                        <StackPane>
                            <Circle radius="50" fill="#555555"/>
                            <ImageView fx:id="profileImageView"
                                       fitWidth="100" fitHeight="100"
                                       preserveRatio="true"
                                       styleClass="profile-image-view">
                                <clip>
                                    <Circle radius="50" centerX="50" centerY="50"/>
                                </clip>
                            </ImageView>
                        </StackPane>

                        <VBox spacing="10">
                            <Label fx:id="profileUsernameLabel"
                                   style="-fx-font-size: 18px; -fx-font-weight: bold;"/>
                            <Button text="Profilbild ändern"
                                    onAction="#handleChangeProfileImage"
                                    styleClass="secondary-button"/>
                        </VBox>
                    </HBox>
                </VBox>

                <!-- Datenbank-Konfiguration -->
                <VBox spacing="20" maxWidth="600"
                      styleClass="card">

                    <Label text="Datenbankverbindung"
                           style="-fx-font-size: 20px; -fx-font-weight: bold;"/>

                    <VBox spacing="10">
                        <Label text="Datenbanktyp"/>
                        <ComboBox fx:id="dbTypeComboBox" maxWidth="Infinity"/>
                    </VBox>

                    <VBox spacing="10">
                        <Label text="Host"/>
                        <TextField fx:id="hostField" promptText="localhost"/>
                    </VBox>

                    <VBox spacing="10">
                        <Label text="Port"/>
                        <TextField fx:id="portField" promptText="5432"/>
                    </VBox>

                    <VBox spacing="10">
                        <Label text="Datenbank-Name"/>
                        <TextField fx:id="databaseField" promptText="nup_db"/>
                    </VBox>

                    <VBox spacing="10">
                        <Label text="Benutzername"/>
                        <TextField fx:id="dbUsernameField" promptText="postgres"/>
                    </VBox>

                    <VBox spacing="10">
                        <Label text="Passwort"/>
                        <PasswordField fx:id="dbPasswordField"
                                       promptText="Datenbank-Passwort"/>
                    </VBox>

                    <Label fx:id="statusLabel"
                           styleClass="subtitle-label"
                           wrapText="true"/>

                    <HBox spacing="15">
                        <Button text="Verbindung testen"
                                onAction="#handleTestConnection"
                                styleClass="secondary-button"/>

                        <Button text="Speichern"
                                onAction="#handleSaveSettings"/>

                        <Region HBox.hgrow="ALWAYS"/>

                        <Button text="Zurück"
                                onAction="#handleBack"
                                styleClass="secondary-button"/>
                    </HBox>
                </VBox>
            </VBox>
        </ScrollPane>
    </center>

</BorderPane>
```

---

## Schritt 10: Controller

### 10.1 LoginController.java

Erstelle `src/main/java/com/winfriedweis/nup/controller/LoginController.java`:

```java
package com.winfriedweis.nup.controller;

import com.winfriedweis.nup.service.AuthService;
import com.winfriedweis.nup.service.AuthService.LoginResult;
import com.winfriedweis.nup.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String identifier = emailField.getText().trim();
        String password = passwordField.getText();

        if (identifier.isEmpty() || password.isEmpty()) {
            showError("Bitte alle Felder ausfüllen");
            return;
        }

        LoginResult result = authService.login(identifier, password);

        if (result.success()) {
            SceneManager.getInstance().switchToDashboard(result.user());
        } else {
            showError(result.errorMessage());
        }
    }

    @FXML
    private void handleOpenRegister() {
        SceneManager.getInstance().switchToRegister();
    }

    @FXML
    private void handleOpenSettings() {
        SceneManager.getInstance().switchToSettings();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
```

---

### 10.2 RegisterController.java

Erstelle `src/main/java/com/winfriedweis/nup/controller/RegisterController.java`:

```java
package com.winfriedweis.nup.controller;

import com.winfriedweis.nup.service.AuthService;
import com.winfriedweis.nup.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() ||
            password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Bitte alle Felder ausfüllen");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwörter stimmen nicht überein");
            return;
        }

        StringBuilder errorMessage = new StringBuilder();
        boolean success = authService.register(username, email, password, errorMessage);

        if (success) {
            showSuccess("Registrierung erfolgreich! Du wirst weitergeleitet...");

            // Nach 2 Sekunden zum Login wechseln
            CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS)
                .execute(() -> Platform.runLater(() ->
                    SceneManager.getInstance().switchToLogin()
                ));
        } else {
            showError(errorMessage.toString());
        }
    }

    @FXML
    private void handleBackToLogin() {
        SceneManager.getInstance().switchToLogin();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }
}
```

---

### 10.3 DashboardController.java

Erstelle `src/main/java/com/winfriedweis/nup/controller/DashboardController.java`:

```java
package com.winfriedweis.nup.controller;

import com.winfriedweis.nup.dao.UserDAO;
import com.winfriedweis.nup.dao.UserDAOImpl;
import com.winfriedweis.nup.model.User;
import com.winfriedweis.nup.util.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

public class DashboardController {
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @FXML private ImageView profileImageView;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;

    private User currentUser;
    private final UserDAO userDAO = new UserDAOImpl();

    public void initData(User user) {
        this.currentUser = user;
        usernameLabel.setText(user.getUsername());
        emailLabel.setText(user.getEmail());
        loadProfileImage();
    }

    private void loadProfileImage() {
        Optional<byte[]> imageDataOpt = userDAO.getProfileImage(currentUser.getId());

        if (imageDataOpt.isPresent()) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(imageDataOpt.get());
                Image image = new Image(bis);
                profileImageView.setImage(image);
            } catch (Exception e) {
                log.error("Fehler beim Laden des Profilbilds", e);
                setDefaultProfileImage();
            }
        } else {
            setDefaultProfileImage();
        }
    }

    private void setDefaultProfileImage() {
        try {
            InputStream defaultImage = getClass().getResourceAsStream("/images/default-avatar.png");
            if (defaultImage != null) {
                profileImageView.setImage(new Image(defaultImage));
            }
        } catch (Exception e) {
            log.error("Fehler beim Laden des Default-Profilbilds", e);
        }
    }

    @FXML
    private void handleProfileImageClick() {
        // Modal-Vorschau des Profilbilds
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initOwner(profileImageView.getScene().getWindow());
        modal.setTitle("Profilbild - " + currentUser.getUsername());

        ImageView largeImageView = new ImageView(profileImageView.getImage());
        largeImageView.setFitWidth(400);
        largeImageView.setFitHeight(400);
        largeImageView.setPreserveRatio(true);

        Circle clip = new Circle(200, 200, 200);
        largeImageView.setClip(clip);

        StackPane pane = new StackPane(largeImageView);
        pane.setAlignment(Pos.CENTER);
        pane.setStyle("-fx-padding: 30; -fx-background-color: #3C3F41;");

        Scene scene = new Scene(pane);
        scene.getStylesheets().add(
            getClass().getResource("/css/dark-theme.css").toExternalForm()
        );

        modal.setScene(scene);
        modal.show();
    }

    @FXML
    private void handleOpenSettings() {
        SceneManager.getInstance().switchToSettings();
    }

    @FXML
    private void handleLogout() {
        SceneManager.getInstance().switchToLogin();
    }
}
```

---

### 10.4 SettingsController.java

Erstelle `src/main/java/com/winfriedweis/nup/controller/SettingsController.java`:

```java
package com.winfriedweis.nup.controller;

import com.winfriedweis.nup.dao.UserDAO;
import com.winfriedweis.nup.dao.UserDAOImpl;
import com.winfriedweis.nup.model.User;
import com.winfriedweis.nup.util.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;

public class SettingsController {
    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);

    // Profil-Sektion
    @FXML private VBox profileSection;
    @FXML private ImageView profileImageView;
    @FXML private Label profileUsernameLabel;

    // Datenbank-Konfiguration
    @FXML private ComboBox<DatabaseType> dbTypeComboBox;
    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private TextField databaseField;
    @FXML private TextField dbUsernameField;
    @FXML private PasswordField dbPasswordField;
    @FXML private Label statusLabel;

    private final UserDAO userDAO = new UserDAOImpl();

    @FXML
    public void initialize() {
        // DB-Typ ComboBox initialisieren
        dbTypeComboBox.getItems().addAll(DatabaseType.values());

        DatabaseConfig config = DatabaseConfig.getInstance();
        dbTypeComboBox.setValue(config.getType());

        // Bei Typ-Wechsel Port automatisch anpassen
        dbTypeComboBox.setOnAction(e -> {
            DatabaseType selected = dbTypeComboBox.getValue();
            if (selected != null) {
                portField.setText(String.valueOf(selected.getDefaultPort()));
            }
        });

        // Felder mit aktueller Konfiguration füllen
        loadCurrentConfig();

        // Profil-Sektion nur anzeigen wenn eingeloggt
        User currentUser = SceneManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            profileSection.setManaged(true);
            profileSection.setVisible(true);
            loadUserProfile(currentUser);
        } else {
            profileSection.setManaged(false);
            profileSection.setVisible(false);
        }
    }

    private void loadCurrentConfig() {
        DatabaseConfig config = DatabaseConfig.getInstance();
        hostField.setText(config.getHost());
        portField.setText(String.valueOf(config.getPort()));
        databaseField.setText(config.getDatabase());
        dbUsernameField.setText(config.getUsername());
        dbPasswordField.setText(config.getPassword());
    }

    private void loadUserProfile(User user) {
        profileUsernameLabel.setText(user.getUsername());

        Optional<byte[]> imageDataOpt = userDAO.getProfileImage(user.getId());

        if (imageDataOpt.isPresent()) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(imageDataOpt.get());
                profileImageView.setImage(new Image(bis));
            } catch (Exception e) {
                log.error("Fehler beim Laden des Profilbilds", e);
                setDefaultProfileImage();
            }
        } else {
            setDefaultProfileImage();
        }
    }

    private void setDefaultProfileImage() {
        try {
            InputStream defaultImage = getClass().getResourceAsStream("/images/default-avatar.png");
            if (defaultImage != null) {
                profileImageView.setImage(new Image(defaultImage));
            }
        } catch (Exception e) {
            log.error("Fehler beim Laden des Default-Profilbilds", e);
        }
    }

    @FXML
    private void handleChangeProfileImage() {
        User currentUser = SceneManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Profilbild auswählen");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Bilder", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());

        if (selectedFile != null) {
            try {
                Image originalImage = new Image(new FileInputStream(selectedFile));

                // Crop-Dialog öffnen
                Optional<WritableImage> croppedOpt = ImageCropDialog.show(
                    originalImage, profileImageView.getScene().getWindow()
                );

                if (croppedOpt.isPresent()) {
                    WritableImage croppedImage = croppedOpt.get();

                    // In byte[] konvertieren
                    BufferedImage buffered = SwingFXUtils.fromFXImage(croppedImage, null);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(buffered, "png", baos);
                    byte[] imageData = baos.toByteArray();

                    // In Datenbank speichern
                    boolean success = userDAO.updateProfileImage(
                        currentUser.getId(), imageData, "image/png"
                    );

                    if (success) {
                        profileImageView.setImage(croppedImage);
                        log.info("Profilbild erfolgreich aktualisiert");
                    }
                }
            } catch (Exception e) {
                log.error("Fehler beim Profilbild-Upload", e);
                showStatus("Fehler beim Upload: " + e.getMessage(), false);
            }
        }
    }

    @FXML
    private void handleTestConnection() {
        statusLabel.setText("Teste Verbindung...");
        statusLabel.setStyle("-fx-text-fill: #4A88C7;");

        try {
            DatabaseType type = dbTypeComboBox.getValue();
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            String database = databaseField.getText().trim();
            String username = dbUsernameField.getText().trim();
            String password = dbPasswordField.getText();

            String error = DatabaseConnection.testConnectionWithError(
                type, host, port, database, username, password
            );

            if (error == null) {
                showStatus("✓ Verbindung erfolgreich!", true);
            } else {
                showStatus("✗ Verbindung fehlgeschlagen: " + error, false);
            }
        } catch (NumberFormatException e) {
            showStatus("✗ Ungültiger Port", false);
        }
    }

    @FXML
    private void handleSaveSettings() {
        try {
            DatabaseConfig config = DatabaseConfig.getInstance();

            config.setType(dbTypeComboBox.getValue());
            config.setHost(hostField.getText().trim());
            config.setPort(Integer.parseInt(portField.getText().trim()));
            config.setDatabase(databaseField.getText().trim());
            config.setUsername(dbUsernameField.getText().trim());
            config.setPassword(dbPasswordField.getText());

            config.save();

            // Verbindung neu initialisieren
            DatabaseConnection.getInstance().reinitialize();

            showStatus("✓ Einstellungen gespeichert!", true);
            log.info("Datenbankeinstellungen gespeichert");
        } catch (Exception e) {
            showStatus("✗ Fehler beim Speichern: " + e.getMessage(), false);
            log.error("Fehler beim Speichern der Einstellungen", e);
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.getInstance().switchBackFromSettings();
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + (success ? "#6A9955" : "#FF6B68") + ";");
    }
}
```

---

## Schritt 11: Main Class

Erstelle `src/main/java/com/winfriedweis/nup/Main.java`:

```java
package com.winfriedweis.nup;

import com.winfriedweis.nup.util.DatabaseConnection;
import com.winfriedweis.nup.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hauptklasse und Einstiegspunkt der NUP-Anwendung.
 */
public class Main extends Application {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        log.info("=================================");
        log.info("NUP - Version 2.0");
        log.info("Noch Unbekanntes Programm");
        log.info("=================================");

        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.setPrimaryStage(primaryStage);

        // Window Settings
        primaryStage.setTitle("NUP - Login");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // Login-Seite laden
        sceneManager.switchToLogin();

        primaryStage.centerOnScreen();
        primaryStage.setOnCloseRequest(event -> cleanup());
    }

    @Override
    public void stop() {
        cleanup();
    }

    private void cleanup() {
        log.info("Anwendung wird beendet...");
        DatabaseConnection.getInstance().close();
        log.info("Datenbankverbindung geschlossen");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

---

## Schritt 12: Projekt starten

### 1. Default-Avatar hinzufügen

Erstelle einen Ordner `src/main/resources/images/` und füge ein Bild namens `default-avatar.png` hinzu (ein einfaches Platzhalter-Bild, z.B. 200x200 Pixel).

### 2. Dependencies laden

```bash
mvn clean install
```

### 3. Anwendung starten

```bash
mvn javafx:run
```

**Oder in IntelliJ IDEA:**
1. Maven-Seitenleiste öffnen (rechts)
2. `Plugins` → `javafx` → `javafx:run` doppelklicken

---

## Troubleshooting

### Problem: "Connection refused"

**Ursachen und Lösungen:**

| Ursache | Lösung |
|---------|--------|
| PostgreSQL läuft nicht | `sudo systemctl start postgresql` (Linux) oder Services-App (Windows) |
| Falscher Port | Standard: PostgreSQL = 5432, MySQL = 3306 |
| Firewall blockiert | Port in Firewall freigeben |
| Falsche Credentials | In Settings überprüfen |

### Problem: "Zu viele fehlgeschlagene Versuche"

Die Sperre ist im RAM gespeichert. **Lösungen:**
- 5 Minuten warten
- Anwendung neu starten (setzt Sperre zurück)

### Problem: Profilbild wird nicht angezeigt

**Checkliste:**
- [ ] `default-avatar.png` in `src/main/resources/images/` vorhanden?
- [ ] PostgreSQL: `BYTEA` Datentyp verwendet?
- [ ] MySQL: `MEDIUMBLOB` verwendet?

### Problem: "ClassNotFoundException: com.mysql.cj.jdbc.Driver"

**Lösung:**
```bash
mvn clean install -U   # Force-Update der Dependencies
```

---

## Nächste Schritte

Mögliche Erweiterungen:

1. **Passwort-Reset** - E-Mail-basierter Reset-Link
2. **"Angemeldet bleiben"** - Session-Speicherung
3. **2FA** - Zwei-Faktor-Authentifizierung mit TOTP
4. **Theme-Switcher** - Light/Dark Mode Toggle
5. **Internationalisierung** - Mehrsprachigkeit (DE/EN)
6. **Admin-Panel** - Benutzerverwaltung

---

## Verwendete Technologien

| Technologie | Version | Zweck |
|-------------|---------|-------|
| Java | 21 | Programmiersprache |
| JavaFX | 21.0.1 | Desktop GUI Framework |
| PostgreSQL | 16+ | Primäre Datenbank |
| MySQL | 8+ | Alternative Datenbank |
| HikariCP | 5.1.0 | Connection Pool |
| BCrypt | 0.4 | Passwort-Hashing |
| SLF4J + Logback | 2.0.9 / 1.4.14 | Logging |
| Maven | 3.9+ | Build-Tool |

---

## Weiterführende Links

- [JavaFX Dokumentation](https://openjfx.io/javadoc/21/)
- [PostgreSQL Tutorial](https://www.postgresqltutorial.com/)
- [BCrypt erklärt](https://www.baeldung.com/java-password-hashing)
- [HikariCP GitHub](https://github.com/brettwooldridge/HikariCP)
- [OWASP Security Cheatsheet](https://cheatsheetseries.owasp.org/)

---

**Viel Erfolg mit deinem NUP-Projekt!**
