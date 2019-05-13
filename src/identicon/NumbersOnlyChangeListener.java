package identicon;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class NumbersOnlyChangeListener implements ChangeListener<String> {

    private TextField textField;
    private boolean updateOnChange;
    private IUpdateStrategy updateStrategy;

    public NumbersOnlyChangeListener(TextField textField, boolean updateOnChange, IUpdateStrategy updateStrategy) {
        this.textField      = textField;
        this.updateOnChange = updateOnChange;
        this.updateStrategy = updateStrategy;
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (!newValue.matches("\\d*")) {
            textField.setText(newValue.replaceAll("[^\\d]", ""));
        }
        if (updateOnChange) {
            updateStrategy.update();
        }
    }

}