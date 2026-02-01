package com.winfriedweis.nup.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

public class ImageCropDialog {
    private static final double MAX_DISPLAY_SIZE = 500;
    private static final double MIN_CROP_SIZE = 50;

    private final Stage dialogStage;
    private final Image originalImage;
    private final Canvas canvas;
    private final double displayScale;

    private double cropX, cropY, cropSize;
    private boolean isDragging = false;
    private boolean isResizing = false;
    private double dragStartX, dragStartY;
    private double initialCropX, initialCropY, initialCropSize;

    private WritableImage croppedImage = null;

    public ImageCropDialog(Stage owner, Image image) {
        this.originalImage = image;
        this.dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setTitle("Profilbild zuschneiden");

        double imgWidth = image.getWidth();
        double imgHeight = image.getHeight();
        double maxDim = Math.max(imgWidth, imgHeight);
        this.displayScale = maxDim > MAX_DISPLAY_SIZE ? MAX_DISPLAY_SIZE / maxDim : 1.0;

        double canvasWidth = imgWidth * displayScale;
        double canvasHeight = imgHeight * displayScale;

        this.canvas = new Canvas(canvasWidth, canvasHeight);

        double initialSize = Math.min(imgWidth, imgHeight) * 0.8;
        cropX = (imgWidth - initialSize) / 2;
        cropY = (imgHeight - initialSize) / 2;
        cropSize = initialSize;

        setupMouseHandlers();
        drawCanvas();

        Label infoLabel = new Label("Ziehe das Quadrat um den gewünschten Ausschnitt zu wählen.\nEcke ziehen zum Vergrößern/Verkleinern.");
        infoLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 12px;");
        infoLabel.setWrapText(true);

        Button confirmButton = new Button("Übernehmen");
        confirmButton.setOnAction(e -> {
            cropImage();
            dialogStage.close();
        });

        Button cancelButton = new Button("Abbrechen");
        cancelButton.setOnAction(e -> {
            croppedImage = null;
            dialogStage.close();
        });

        HBox buttonBox = new HBox(15, confirmButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        StackPane canvasPane = new StackPane(canvas);
        canvasPane.setStyle("-fx-background-color: #1E1E1E;");

        VBox root = new VBox(15, infoLabel, canvasPane, buttonBox);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2B2B2B;");

        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);
    }

    private void setupMouseHandlers() {
        canvas.setOnMousePressed(e -> {
            double mx = e.getX() / displayScale;
            double my = e.getY() / displayScale;

            double handleSize = 20 / displayScale;
            double cornerX = cropX + cropSize - handleSize;
            double cornerY = cropY + cropSize - handleSize;

            if (mx >= cornerX && mx <= cropX + cropSize && my >= cornerY && my <= cropY + cropSize) {
                isResizing = true;
                isDragging = false;
            } else if (mx >= cropX && mx <= cropX + cropSize && my >= cropY && my <= cropY + cropSize) {
                isDragging = true;
                isResizing = false;
            }

            dragStartX = mx;
            dragStartY = my;
            initialCropX = cropX;
            initialCropY = cropY;
            initialCropSize = cropSize;
        });

        canvas.setOnMouseDragged(e -> {
            double mx = e.getX() / displayScale;
            double my = e.getY() / displayScale;

            if (isResizing) {
                double dx = mx - dragStartX;
                double dy = my - dragStartY;
                double delta = Math.max(dx, dy);
                double newSize = Math.max(MIN_CROP_SIZE, initialCropSize + delta);

                if (cropX + newSize <= originalImage.getWidth() && cropY + newSize <= originalImage.getHeight()) {
                    cropSize = newSize;
                } else {
                    double maxSize = Math.min(originalImage.getWidth() - cropX, originalImage.getHeight() - cropY);
                    cropSize = Math.min(newSize, maxSize);
                }
            } else if (isDragging) {
                double dx = mx - dragStartX;
                double dy = my - dragStartY;

                double newX = initialCropX + dx;
                double newY = initialCropY + dy;

                cropX = Math.max(0, Math.min(newX, originalImage.getWidth() - cropSize));
                cropY = Math.max(0, Math.min(newY, originalImage.getHeight() - cropSize));
            }

            drawCanvas();
        });

        canvas.setOnMouseReleased(e -> {
            isDragging = false;
            isResizing = false;
        });

        canvas.setOnMouseMoved(e -> {
            double mx = e.getX() / displayScale;
            double my = e.getY() / displayScale;

            double handleSize = 20 / displayScale;
            double cornerX = cropX + cropSize - handleSize;
            double cornerY = cropY + cropSize - handleSize;

            if (mx >= cornerX && mx <= cropX + cropSize && my >= cornerY && my <= cropY + cropSize) {
                canvas.setCursor(Cursor.SE_RESIZE);
            } else if (mx >= cropX && mx <= cropX + cropSize && my >= cropY && my <= cropY + cropSize) {
                canvas.setCursor(Cursor.MOVE);
            } else {
                canvas.setCursor(Cursor.DEFAULT);
            }
        });
    }

    private void drawCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.drawImage(originalImage, 0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.rgb(0, 0, 0, 0.6));

        double cx = cropX * displayScale;
        double cy = cropY * displayScale;
        double cs = cropSize * displayScale;

        gc.fillRect(0, 0, canvas.getWidth(), cy);
        gc.fillRect(0, cy + cs, canvas.getWidth(), canvas.getHeight() - cy - cs);
        gc.fillRect(0, cy, cx, cs);
        gc.fillRect(cx + cs, cy, canvas.getWidth() - cx - cs, cs);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(cx, cy, cs, cs);

        double handleSize = 15;
        gc.setFill(Color.WHITE);
        gc.fillRect(cx + cs - handleSize, cy + cs - handleSize, handleSize, handleSize);

        gc.setStroke(Color.rgb(255, 255, 255, 0.3));
        gc.setLineWidth(1);
        gc.strokeLine(cx + cs/3, cy, cx + cs/3, cy + cs);
        gc.strokeLine(cx + 2*cs/3, cy, cx + 2*cs/3, cy + cs);
        gc.strokeLine(cx, cy + cs/3, cx + cs, cy + cs/3);
        gc.strokeLine(cx, cy + 2*cs/3, cx + cs, cy + 2*cs/3);
    }

    private void cropImage() {
        PixelReader reader = originalImage.getPixelReader();

        int x = (int) Math.round(cropX);
        int y = (int) Math.round(cropY);
        int size = (int) Math.round(cropSize);

        x = Math.max(0, Math.min(x, (int) originalImage.getWidth() - 1));
        y = Math.max(0, Math.min(y, (int) originalImage.getHeight() - 1));
        size = Math.min(size, (int) Math.min(originalImage.getWidth() - x, originalImage.getHeight() - y));

        croppedImage = new WritableImage(reader, x, y, size, size);
    }

    public Optional<WritableImage> showAndWait() {
        dialogStage.showAndWait();
        return Optional.ofNullable(croppedImage);
    }
}
