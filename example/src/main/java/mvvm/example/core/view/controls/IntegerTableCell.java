package mvvm.example.core.view.controls;

import javafx.scene.control.TableCell;

public class IntegerTableCell<S> extends TableCell<S, Number> {

    @Override
    protected void updateItem(Number value, boolean empty) {
        super.updateItem(value, empty);
        setText(empty || value == null ? null : String.valueOf(value.intValue()));
    }
}
