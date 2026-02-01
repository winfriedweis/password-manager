package com.winfriedweis.nup.controller;

import com.winfriedweis.nup.service.AuthService;
import com.winfriedweis.nup.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
