package identicon;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class IdenticonGeneratorController {

    private Identicon identicon;
    private final int PREVIEW_SIZE = 250;

    @FXML public TextField inputTextField;
    @FXML public ComboBox<String> hashingAlgorithmsComboBox;
    @FXML public CheckBox foregroundCheckBox;
    @FXML public ColorPicker foregroundColorPicker;
    @FXML public CheckBox backgroundCheckBox;
    @FXML public ColorPicker backgroundColorPicker;
    @FXML public TextField qualityTextField;
    @FXML public Button saveButton;
    @FXML public ImageView identiconImageView;

    @FXML
    public void initialize() {

        identicon = Identicon.getInstance();

        // Set Preview Dimensions & PlaceHolder Image

        identiconImageView.setFitHeight(PREVIEW_SIZE);
        identiconImageView.setFitWidth(PREVIEW_SIZE);
        identiconImageView.setImage(identicon.getImage());

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

        foregroundCheckBox.selectedProperty()
                .addListener(new CheckBoxChangeListener(foregroundColorPicker, Color.WHITE));
        backgroundCheckBox.selectedProperty()
                .addListener(new CheckBoxChangeListener(backgroundColorPicker, Color.BLACK));

        qualityTextField.textProperty()
                .addListener(new NumbersOnlyChangeListener());

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

        if (file != null) {
            String quality = qualityTextField.getText();
            identicon.setImagePixels(quality.equals("") ? PREVIEW_SIZE : Integer.parseInt(quality));
            identicon.generateImage();
            ImageIO.write(identicon.getBufferedImage(), "png", file);
        }

    }


    private void updatePreview() {

        identicon.setText(inputTextField.getText());
        identicon.setHashingAlgorithm(hashingAlgorithmsComboBox.getValue());
        identicon.setForeground(foregroundColorPicker.getValue());
        identicon.setBackground(backgroundColorPicker.getValue());
        identicon.setIdenticonPixels(5);
        identicon.setBorderPixels(1);
        identicon.generateIdenticon();

        identicon.setImagePixels(PREVIEW_SIZE);
        identicon.generateImage();
        identiconImageView.setImage(identicon.getImage());

    }


    private class CheckBoxChangeListener implements ChangeListener<Boolean> {

        private ColorPicker colorPicker;
        private Color defaultColor;

        CheckBoxChangeListener(ColorPicker colorPicker, Color defaultColor) {
            this.colorPicker  = colorPicker;
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
