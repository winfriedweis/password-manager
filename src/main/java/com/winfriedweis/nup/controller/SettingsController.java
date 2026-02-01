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
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class SettingsController {
    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);

    // Profil-Elemente
    @FXML private VBox profileSection;
    @FXML private ImageView profileImageView;
    @FXML private Label profileUsernameLabel;

    // Datenbank-Elemente
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
        // ComboBox mit Datenbanktypen befüllen
        dbTypeComboBox.getItems().addAll(DatabaseType.values());

        // Listener für automatische Port-Anpassung
        dbTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                portField.setText(String.valueOf(newVal.getDefaultPort()));
            }
        });

        // Gespeicherte Konfiguration laden
        loadConfig();

        // Profil-Bereich anzeigen wenn eingeloggt
        if (SceneManager.getInstance().isLoggedIn()) {
            User currentUser = SceneManager.getInstance().getCurrentUser();
            profileSection.setVisible(true);
            profileSection.setManaged(true);
            profileUsernameLabel.setText(currentUser.getUsername());
            loadProfileImage(currentUser);
        }
    }

    private void loadConfig() {
        DatabaseConfig config = DatabaseConfig.getInstance();
        dbTypeComboBox.setValue(config.getType());
        hostField.setText(config.getHost());
        portField.setText(String.valueOf(config.getPort()));
        databaseField.setText(config.getDatabase());
        dbUsernameField.setText(config.getUsername());
        dbPasswordField.setText(config.getPassword());
    }

    private void loadProfileImage(User user) {
        Optional<byte[]> imageDataOpt = userDAO.getProfileImage(user.getId());

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
            InputStream defaultImage = getClass()
                .getResourceAsStream("/images/default-avatar.png");
            if (defaultImage != null) {
                profileImageView.setImage(new Image(defaultImage));
            }
        } catch (Exception e) {
            log.error("Fehler beim Laden des Standard-Profilbilds", e);
        }
    }

    @FXML
    private void handleChangeProfileImage() {
        if (!SceneManager.getInstance().isLoggedIn()) {
            return;
        }

        User currentUser = SceneManager.getInstance().getCurrentUser();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Profilbild auswählen");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Bilder", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) profileImageView.getScene().getWindow();
        var selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            log.debug("Bild ausgewählt: {}", selectedFile.getName());

            try {
                Image originalImage = new Image(selectedFile.toURI().toString());

                ImageCropDialog cropDialog = new ImageCropDialog(stage, originalImage);
                Optional<WritableImage> croppedOpt = cropDialog.showAndWait();

                if (croppedOpt.isPresent()) {
                    WritableImage croppedImage = croppedOpt.get();

                    byte[] imageData = writableImageToBytes(croppedImage);
                    String mimeType = "image/png";

                    boolean success = userDAO.updateProfileImage(
                        currentUser.getId(), imageData, mimeType
                    );

                    if (success) {
                        loadProfileImage(currentUser);
                        statusLabel.setText("Profilbild aktualisiert!");
                        statusLabel.setStyle("-fx-text-fill: #6A9955;");
                        log.info("Profilbild aktualisiert für User {}", currentUser.getUsername());
                    } else {
                        statusLabel.setText("Fehler beim Speichern des Profilbilds");
                        statusLabel.setStyle("-fx-text-fill: #FF6B68;");
                    }
                }
            } catch (IOException e) {
                log.error("Fehler beim Verarbeiten des Bildes", e);
                statusLabel.setText("Fehler beim Verarbeiten des Bildes");
                statusLabel.setStyle("-fx-text-fill: #FF6B68;");
            }
        }
    }

    private byte[] writableImageToBytes(WritableImage image) throws IOException {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

        BufferedImage rgbImage = new BufferedImage(
            bufferedImage.getWidth(),
            bufferedImage.getHeight(),
            BufferedImage.TYPE_INT_RGB
        );

        java.awt.Graphics2D g2d = rgbImage.createGraphics();
        g2d.setColor(java.awt.Color.WHITE);
        g2d.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
        g2d.drawImage(bufferedImage, 0, 0, null);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean written = ImageIO.write(rgbImage, "png", baos);

        if (!written) {
            throw new IOException("Bild konnte nicht geschrieben werden");
        }

        byte[] result = baos.toByteArray();
        if (result.length == 0) {
            throw new IOException("Bild-Daten sind leer");
        }

        return result;
    }

    @FXML
    private void handleTestConnection() {
        statusLabel.setText("Teste Verbindung...");
        statusLabel.setStyle("-fx-text-fill: #4A88C7;");

        DatabaseType type = dbTypeComboBox.getValue();
        if (type == null) {
            statusLabel.setText("Bitte Datenbanktyp auswählen");
            statusLabel.setStyle("-fx-text-fill: #FF6B68;");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Ungültiger Port");
            statusLabel.setStyle("-fx-text-fill: #FF6B68;");
            return;
        }

        String error = DatabaseConnection.testConnectionWithError(
                type,
                hostField.getText().trim(),
                port,
                databaseField.getText().trim(),
                dbUsernameField.getText().trim(),
                dbPasswordField.getText()
        );

        if (error == null) {
            statusLabel.setText("Verbindung erfolgreich!");
            statusLabel.setStyle("-fx-text-fill: #6A9955;");
        } else {
            statusLabel.setText("Verbindung fehlgeschlagen: " + error);
            statusLabel.setStyle("-fx-text-fill: #FF6B68;");
        }
    }

    @FXML
    private void handleSaveSettings() {
        DatabaseType type = dbTypeComboBox.getValue();
        if (type == null) {
            statusLabel.setText("Bitte Datenbanktyp auswählen");
            statusLabel.setStyle("-fx-text-fill: #FF6B68;");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Ungültiger Port");
            statusLabel.setStyle("-fx-text-fill: #FF6B68;");
            return;
        }

        // Konfiguration speichern
        DatabaseConfig config = DatabaseConfig.getInstance();
        config.setType(type);
        config.setHost(hostField.getText().trim());
        config.setPort(port);
        config.setDatabase(databaseField.getText().trim());
        config.setUsername(dbUsernameField.getText().trim());
        config.setPassword(dbPasswordField.getText());
        config.save();

        // DatabaseConnection neu initialisieren
        try {
            DatabaseConnection.getInstance().reinitialize();
            statusLabel.setText("Einstellungen gespeichert!");
            statusLabel.setStyle("-fx-text-fill: #6A9955;");
        } catch (Exception e) {
            statusLabel.setText("Gespeichert, aber Verbindung fehlgeschlagen: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #FF6B68;");
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.getInstance().switchBackFromSettings();
    }
}
