package com.winfriedweis.nup.controller;

import com.winfriedweis.nup.service.AuthService;
import com.winfriedweis.nup.service.AuthService.LoginResult;
import com.winfriedweis.nup.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
