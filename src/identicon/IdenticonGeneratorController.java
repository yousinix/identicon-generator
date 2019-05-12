package identicon;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IdenticonGeneratorController {

    private final int BORDER_SIZE   = 1;
    private final int ORIGINAL_SIZE = 5;
    private final int TOTAL_SIZE    = ORIGINAL_SIZE + 2 * BORDER_SIZE;
    private final int PREVIEW_SIZE  = 250;

    private BufferedImage identicon;

    @FXML public TextField inputTextField;
    @FXML public ComboBox<String> hashingAlgorithmsComboBox;
    @FXML public CheckBox foregroundCheckBox;
    @FXML public ColorPicker foregroundColorPicker;
    @FXML public CheckBox backgroundCheckBox;
    @FXML public ColorPicker backgroundColorPicker;
    @FXML public TextField qualityTextField;
    @FXML public Button saveButton;
    @FXML public VBox previewVBox;
    @FXML public ImageView identiconImageView;

    @FXML
    public void initialize() {

        // Set Preview Dimensions
        previewVBox.setPrefHeight(PREVIEW_SIZE);
        previewVBox.setPrefWidth(PREVIEW_SIZE);

        // Initial Values
        hashingAlgorithmsComboBox.setItems(FXCollections.observableArrayList("MD5", "SHA-1", "SHA-256"));
        hashingAlgorithmsComboBox.getSelectionModel().select(0);
        foregroundColorPicker.setValue(null);
        backgroundColorPicker.setValue(null);
        foregroundColorPicker.setDisable(true);
        backgroundColorPicker.setDisable(true);

        // Sync Changes

        hashingAlgorithmsComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> updatePreview());

        inputTextField.textProperty()
                .addListener((observable, oldValue, newValue) -> updatePreview());

        foregroundColorPicker.setOnAction(event -> updatePreview());
        backgroundColorPicker.setOnAction(event -> updatePreview());

        foregroundCheckBox.selectedProperty().addListener(new CheckBoxChangeListener(foregroundColorPicker, Color.WHITE));
        backgroundCheckBox.selectedProperty().addListener(new CheckBoxChangeListener(backgroundColorPicker, Color.BLACK));

        qualityTextField.textProperty().addListener(new NumbersOnlyChangeListener());

    }

    @FXML
    public void handleSaveButtonOnAction(ActionEvent event) throws IOException {

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG", "*.png");

        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName("Identicon");
        fileChooser.setTitle("Save your Identicon");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File file = fileChooser.showSaveDialog(null);

        if (identicon != null && file != null) {

            int quality = qualityTextField.getText() == null || qualityTextField.getText().equals("") ?
                    PREVIEW_SIZE : Integer.parseInt(qualityTextField.getText());

            Image image = scaleImage(identicon, quality);

            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);

        }
    }

    private void updatePreview() {
        byte[] hashedText = encrypt(inputTextField.getText(), hashingAlgorithmsComboBox.getValue());
        Color foreground = foregroundColorPicker.getValue();
        Color background = backgroundColorPicker.getValue();
        identicon = generateIdenticon(hashedText, foreground, background);
        identiconImageView.setImage(scaleImage(identicon, PREVIEW_SIZE));
    }

    private byte[] encrypt(String textToEncrypt, String hashingAlgorithm) {

        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance(hashingAlgorithm);
        } catch(NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());

        }
        return messageDigest.digest(textToEncrypt.getBytes());

    }

    private BufferedImage generateIdenticon(byte[] hashedText, Color foregroundColor, Color backgroundColor) {

        // Determine Colors

        int r, g, b, a;

        r = foregroundColor == null ? hashedText[0] & 255 : (int) (foregroundColor.getRed() * 255);
        g = foregroundColor == null ? hashedText[1] & 255 : (int) (foregroundColor.getGreen() * 255);
        b = foregroundColor == null ? hashedText[2] & 255 : (int) (foregroundColor.getBlue() * 255);
        a = foregroundColor == null ? 255 : (int) (foregroundColor.getOpacity() * 255);
        int[] foreground = new int[]{r, g, b, a};

        r = backgroundColor == null ? 255 : (int) (backgroundColor.getRed() * 255);
        g = backgroundColor == null ? 255 : (int) (backgroundColor.getGreen() * 255);
        b = backgroundColor == null ? 255 : (int) (backgroundColor.getBlue() * 255);
        a = backgroundColor == null ? 255 : (int) (backgroundColor.getOpacity() * 255);
        int[] background = new int[]{r, g, b, a};


        BufferedImage identicon = new BufferedImage(TOTAL_SIZE, TOTAL_SIZE, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = identicon.getRaster();

        // Draw Border
        for (int x = 0; x < TOTAL_SIZE; x++) {
            for (int y = 0; y < TOTAL_SIZE; y++) {
                if (x == 0 || x == TOTAL_SIZE - 1 ||
                    y == 0 || y == TOTAL_SIZE - 1) {
                    raster.setPixel(x, y, background);
                }
            }
        }

        // Draw Identicon
        for (int x = 0; x < ORIGINAL_SIZE; x++) {
            int i = x < 3 ? x : 4 - x;
            for (int y = 0; y < ORIGINAL_SIZE; y++) {
                if ((hashedText[i] >> y & 1) == 1) {
                    raster.setPixel(x + 1, y + 1, foreground);
                } else {
                    raster.setPixel(x + 1, y + 1, background);
                }
            }
        }

        return identicon;

    }

    private Image scaleImage(BufferedImage image, int scaleSize) {

        BufferedImage scaledImage = new BufferedImage(scaleSize, scaleSize, BufferedImage.TYPE_INT_ARGB);

        AffineTransform transform = new AffineTransform();
        int scale = scaleSize / TOTAL_SIZE;
        transform.scale(scale, scale);

        AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        scaledImage = transformOp.filter(image, scaledImage);

        return SwingFXUtils.toFXImage(scaledImage, null);
    }

    private class CheckBoxChangeListener implements ChangeListener<Boolean> {

        ColorPicker colorPicker;
        Color defaultColor;

        CheckBoxChangeListener(ColorPicker colorPicker, Color defaultColor) {
            this.colorPicker = colorPicker;
            this.defaultColor = defaultColor;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (newValue) {
                colorPicker.setValue(defaultColor);
                colorPicker.setDisable(false);
            } else {
                colorPicker.setValue(null);
                colorPicker.setDisable(true);
            }
            updatePreview();
        }

    }

    private class NumbersOnlyChangeListener implements ChangeListener<String> {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (!newValue.matches("\\d*")) {
                qualityTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        }
    }

}
