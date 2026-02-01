# 🔐 NUP - Noch Unbekanntes Programm

> Eine moderne JavaFX Desktop-Authentifizierungs-App mit Multi-Datenbank-Support, Brute-Force-Schutz und Profilbild-Editor. Bereit für Erweiterungen. Das Ziel ist es einen lokalen Passwort Manager zu programmieren mit zahlreichen Zusatzfunktionen. Der Titel der Anwendung ist aktuell NUP - Noch Unbekanntes Programm. Da es sich um ein Lernprojekt handelt kommen noch unterschiedliche Features dazu. In diesem Projekt wird getestet wie im Zeitalter der KI, LLMs als Lernpartner fungieren können. Nicht im Stil von Vibecoding sondern als Lernunterstützung mit Cross-Resource Prüfung zum erlenen sicherer Code Strukturen. 
> In Entwicklung - Viele Features noch nicht vorhanden. 

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.1-4A90D9?style=flat-square&logo=java&logoColor=white)](https://openjfx.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=flat-square&logo=apachemaven&logoColor=white)](https://maven.apache.org/)

---

## 📱 Projektübersicht

NUP soll eine vollständige Authentifizierungs-Lösung für Desktop-Anwendungen werden, entwickelt mit JavaFX und modernen Java 21 Features. Das Projekt demonstriert Best Practices in Sachen Sicherheit, Architektur und User Experience.

### ✨ Highlights

* **🔒 Sicher:** BCrypt Passwort-Hashing mit 12 Log Rounds + Brute-Force-Schutz
* **🗄️ Multi-DB:** PostgreSQL und MySQL out-of-the-box unterstützt
* **⚡ Performant:** HikariCP Connection Pool für optimierte DB-Zugriffe
* **🎨 Modern UI:** Dark Theme im IntelliJ IDEA-Stil mit CSS
* **👤 Profilbilder:** Upload mit integriertem Crop-Editor
* **📝 Logging:** Professionelles Logging mit SLF4J + Logback
* **🏗️ Clean Architecture:** MVC + DAO + Service Layer Pattern

---

## 🛠️ Tech Stack

| Kategorie | Technologie | Beschreibung |
|:----------|:------------|:-------------|
| **Sprache** | [Java 21](https://openjdk.org/) | LTS mit Records, Pattern Matching, Virtual Threads |
| **GUI Framework** | [JavaFX 21](https://openjfx.io/) | Modernes Desktop UI Framework |
| **Build Tool** | [Maven](https://maven.apache.org/) | Dependency Management & Build |
| **Datenbank** | [PostgreSQL](https://www.postgresql.org/) / [MySQL](https://www.mysql.com/) | Multi-DB Support |
| **Connection Pool** | [HikariCP 5.1](https://github.com/brettwooldridge/HikariCP) | Schnellster JDBC Connection Pool |
| **Security** | [BCrypt](https://www.mindrot.org/projects/jBCrypt/) | Sicheres Passwort-Hashing |
| **Logging** | [SLF4J](https://www.slf4j.org/) + [Logback](https://logback.qos.ch/) | Enterprise-grade Logging |

---

## 🖼️ Screenshots

| Login | Dashboard | Settings |
|:-----:|:---------:|:--------:|
| ![Login](docs/screenshots/login.png) | ![Dashboard](docs/screenshots/dashboard.png) | ![Settings](docs/screenshots/settings.png) |

> *Screenshots folgen nach Fertigstellung der UI*

---

## 🔥 Features

### Authentifizierung
- ✅ Login mit Username oder E-Mail
- ✅ Registrierung mit Validierung
- ✅ BCrypt Passwort-Hashing (12 Rounds)
- ✅ Brute-Force-Schutz (5 Versuche → 5 Min. Sperre)

### Benutzerprofile
- ✅ Profilbild-Upload
- ✅ Crop-Editor mit Drittel-Raster
- ✅ Klick-Vorschau im Dashboard

### Datenbank
- ✅ PostgreSQL Support
- ✅ MySQL Support
- ✅ Dynamische Konfiguration in `~/.nup/`
- ✅ Verbindungstest in Settings
- ✅ HikariCP Connection Pooling

### Security
- ✅ Prepared Statements (SQL Injection Prevention)
- ✅ Passwort-Komplexitätsanforderungen
- ✅ Thread-sichere Login-Tracking

---

## 📦 Installation

### Voraussetzungen

- **Java 21** oder höher ([Download](https://adoptium.net/))
- **Maven 3.9+** ([Download](https://maven.apache.org/download.cgi))
- **PostgreSQL 16** oder **MySQL 8** ([PostgreSQL](https://www.postgresql.org/download/) | [MySQL](https://dev.mysql.com/downloads/))

### 1. Repository klonen

```bash
git clone https://github.com/winfriedweis/nup-noch-unbekanntes-programm.git
cd nup-noch-unbekanntes-programm
```

### 2. Datenbank einrichten

**PostgreSQL:**
```bash
# Datenbank erstellen
sudo -u postgres psql -c "CREATE DATABASE nup_db;"

# Schema importieren
psql -U postgres -d nup_db -f src/main/resources/db/schema.sql
```

**MySQL:**
```bash
mysql -u root -p < src/main/resources/db/schema-mysql.sql
```

### 3. Dependencies installieren

```bash
mvn clean install
```

### 4. Anwendung starten

```bash
mvn javafx:run
```

Beim ersten Start öffnet sich der Login-Screen. Klicke auf ⚙️ um die Datenbankverbindung zu konfigurieren.

---

## 📂 Projektstruktur

```text
nup-noch-unbekanntes-programm/
├── src/main/java/com/winfriedweis/nup/
│   ├── Main.java                    # Einstiegspunkt
│   ├── model/
│   │   └── User.java                # Datenmodell
│   ├── dao/
│   │   ├── UserDAO.java             # Interface
│   │   └── UserDAOImpl.java         # Implementierung
│   ├── service/
│   │   ├── AuthService.java         # Auth + Brute-Force
│   │   └── ValidationService.java   # Input Validation
│   ├── controller/
│   │   ├── LoginController.java
│   │   ├── RegisterController.java
│   │   ├── DashboardController.java
│   │   └── SettingsController.java
│   └── util/
│       ├── DatabaseConnection.java  # HikariCP Pool
│       ├── DatabaseConfig.java      # Config Management
│       ├── DatabaseType.java        # DB Enum
│       ├── ImageCropDialog.java     # Profilbild Editor
│       ├── PasswordUtil.java        # BCrypt Wrapper
│       └── SceneManager.java        # Navigation
│
├── src/main/resources/
│   ├── fxml/                        # UI Layouts
│   ├── css/dark-theme.css           # Styling
│   ├── images/                      # Assets
│   ├── db/schema.sql                # DB Schema
│   └── logback.xml                  # Logging Config
│
├── pom.xml                          # Maven Config
├── README.md
└── NUP-Anleitung-Neu.md             # Ausführliche Anleitung
```

---

## ⚙️ Konfiguration

Die App speichert Einstellungen in `~/.nup/`:

| Datei | Inhalt |
|:------|:-------|
| `database.properties` | DB-Verbindungsdaten |
| `logs/nup.log` | Anwendungs-Logs (30 Tage) |

**Beispiel `database.properties`:**
```properties
type=POSTGRESQL
host=localhost
port=5432
database=nup_db
username=postgres
password=
```

---

## 🧪 Passwort-Anforderungen

Bei der Registrierung muss das Passwort folgende Kriterien erfüllen:

| Anforderung | Beispiel |
|:------------|:---------|
| Mindestens 8 Zeichen | `Password1!` ✅ |
| Ein Großbuchstabe | `password1!` ❌ |
| Ein Kleinbuchstabe | `PASSWORD1!` ❌ |
| Eine Ziffer | `Password!!` ❌ |
| Ein Sonderzeichen | `Password11` ❌ |

---

## 🚀 Roadmap

- [ ] Passwort-Reset via E-Mail
- [ ] "Angemeldet bleiben" Checkbox
- [ ] Zwei-Faktor-Authentifizierung (2FA)
- [ ] Light/Dark Theme Toggle
- [ ] Mehrsprachigkeit (DE/EN)
- [ ] Admin-Panel für Benutzerverwaltung

---

## 📚 Dokumentation

Eine **ausführliche Schritt-für-Schritt Anleitung** mit Erklärungen aller Konzepte findest du in:

📖 **[Anleitung_NUP.md](Anleitung_NUP.md)**

Diese Anleitung ist anfängerfreundlich und erklärt:
- JavaFX Grundlagen
- MVC/DAO Pattern
- BCrypt & Sicherheit
- Connection Pooling
- Und vieles mehr...

---

## 🤝 Contributing

Beiträge sind willkommen! Bitte erstelle einen Fork und einen Pull Request.

1. Fork das Repository
2. Erstelle einen Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Committe deine Änderungen (`git commit -m 'Add AmazingFeature'`)
4. Push zum Branch (`git push origin feature/AmazingFeature`)
5. Öffne einen Pull Request

---

## 📝 Lizenz

Noch nicht festgelegt.

---

## 👨‍💻 Autor

**Winfried Weis**

[![GitHub](https://img.shields.io/badge/GitHub-winfriedweis-181717?style=flat-square&logo=github)](https://github.com/winfriedweis)

---

<p align="center">
  <sub>Built with ❤️ and ☕ using Java 21 & JavaFX</sub>
</p>
