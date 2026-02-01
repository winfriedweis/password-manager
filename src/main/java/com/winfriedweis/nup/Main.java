package com.winfriedweis.nup;

import com.winfriedweis.nup.util.DatabaseConnection;
import com.winfriedweis.nup.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        log.info("=================================");
        log.info("NUP - Version 1.0");
        log.info("Noch Unbekanntes Programm");
        log.info("=================================");

        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.setPrimaryStage(primaryStage);

        primaryStage.setTitle("NUP - Login");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

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
        try {
            DatabaseConnection.getInstance().close();
            log.info("Datenbankverbindung geschlossen");
        } catch (Exception e) {
            log.error("Fehler beim Schließen der Datenbankverbindung", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
