package identicon;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

public class CheckBoxChangeListener implements ChangeListener<Boolean> {

    private ColorPicker colorPicker;
    private Color defaultColor;
    private IUpdateStrategy updateStrategy;

    public CheckBoxChangeListener(ColorPicker colorPicker, Color defaultColor, IUpdateStrategy updateStrategy) {
        this.colorPicker    = colorPicker;
        this.defaultColor   = defaultColor;
        this.updateStrategy = updateStrategy;
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
        updateStrategy.update();
    }

}
