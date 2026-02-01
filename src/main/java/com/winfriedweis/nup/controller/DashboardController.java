package com.winfriedweis.nup.controller;

import com.winfriedweis.nup.dao.UserDAO;
import com.winfriedweis.nup.dao.UserDAOImpl;
import com.winfriedweis.nup.model.User;
import com.winfriedweis.nup.util.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
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
    private StackPane previewOverlay;

    public void initData(User user) {
        this.currentUser = user;
        usernameLabel.setText(user.getUsername());
        emailLabel.setText(user.getEmail());
        loadProfileImage();

        profileImageView.setOnMouseClicked(this::handleProfileImageClick);
        profileImageView.setCursor(javafx.scene.Cursor.HAND);
    }

    private void loadProfileImage() {
        Optional<byte[]> imageDataOpt = userDAO.getProfileImage(currentUser.getId());

        if (imageDataOpt.isPresent()) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(imageDataOpt.get());
                Image image = new Image(bis);
                profileImageView.setImage(image);
                log.debug("Profilbild geladen für User {}", currentUser.getUsername());
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
            InputStream defaultImage = getClass()
                .getResourceAsStream("/images/default-avatar.png");
            if (defaultImage != null) {
                profileImageView.setImage(new Image(defaultImage));
                log.debug("Standard-Profilbild gesetzt");
            }
        } catch (Exception e) {
            log.error("Fehler beim Laden des Standard-Profilbilds", e);
        }
    }

    private void handleProfileImageClick(MouseEvent event) {
        Image currentImage = profileImageView.getImage();
        if (currentImage == null) {
            return;
        }

        Stage stage = (Stage) profileImageView.getScene().getWindow();
        javafx.scene.Parent currentRoot = stage.getScene().getRoot();

        StackPane wrapper;
        if (currentRoot instanceof StackPane) {
            wrapper = (StackPane) currentRoot;
        } else {
            wrapper = new StackPane();
            wrapper.getChildren().add(currentRoot);
            stage.getScene().setRoot(wrapper);
        }

        previewOverlay = new StackPane();
        previewOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        previewOverlay.setAlignment(Pos.CENTER);

        double maxSize = Math.min(stage.getWidth(), stage.getHeight()) * 0.6;
        ImageView largeImageView = new ImageView(currentImage);
        largeImageView.setFitWidth(maxSize);
        largeImageView.setFitHeight(maxSize);
        largeImageView.setPreserveRatio(true);

        Circle clipCircle = new Circle(maxSize / 2);
        clipCircle.setCenterX(maxSize / 2);
        clipCircle.setCenterY(maxSize / 2);

        StackPane imageContainer = new StackPane(largeImageView);
        imageContainer.setMaxSize(maxSize, maxSize);
        imageContainer.setClip(clipCircle);

        Button closeButton = new Button("X");
        closeButton.setStyle(
            "-fx-background-color: #FF6B68; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-min-width: 40px; " +
            "-fx-min-height: 40px; " +
            "-fx-background-radius: 20px;"
        );

        StackPane finalWrapper = wrapper;
        closeButton.setOnAction(e -> closePreview(finalWrapper));

        VBox content = new VBox(20, imageContainer, closeButton);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        previewOverlay.getChildren().add(content);
        previewOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == previewOverlay) {
                closePreview(finalWrapper);
            }
        });

        wrapper.getChildren().add(previewOverlay);
    }

    private void closePreview(StackPane wrapper) {
        if (previewOverlay != null && wrapper.getChildren().contains(previewOverlay)) {
            wrapper.getChildren().remove(previewOverlay);
            previewOverlay = null;
        }
    }

    @FXML
    private void handleOpenSettings() {
        SceneManager.getInstance().switchToSettings();
    }

    @FXML
    private void handleLogout() {
        log.info("Benutzer '{}' abgemeldet", currentUser.getUsername());
        SceneManager.getInstance().switchToLogin();
    }
}
