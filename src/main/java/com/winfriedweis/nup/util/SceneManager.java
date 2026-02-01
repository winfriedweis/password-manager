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

    public void switchToLogin() {
        currentUser = null;
        log.debug("Wechsel zu Login-Ansicht");
        loadScene("/fxml/login.fxml", "NUP - Login");
    }

    public void switchToRegister() {
        log.debug("Wechsel zu Registrierungs-Ansicht");
        loadScene("/fxml/register.fxml", "NUP - Registrierung");
    }

    public void switchToDashboard(User user) {
        this.currentUser = user;
        log.info("Benutzer '{}' angemeldet", user.getUsername());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.initData(user);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/css/dark-theme.css").toExternalForm()
            );

            primaryStage.setScene(scene);
            primaryStage.setTitle("NUP - Dashboard - " + user.getUsername());
            primaryStage.setMaximized(true);
            primaryStage.show();

            log.debug("Dashboard geladen für Benutzer '{}'", user.getUsername());
        } catch (IOException e) {
            log.error("Fehler beim Laden des Dashboards", e);
        }
    }

    public void switchToSettings() {
        log.debug("Wechsel zu Einstellungen");
        loadScene("/fxml/settings.fxml", "NUP - Einstellungen");
    }

    public void switchBackFromSettings() {
        if (isLoggedIn()) {
            log.debug("Zurück zum Dashboard");
            switchToDashboard(currentUser);
        } else {
            log.debug("Zurück zum Login");
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
