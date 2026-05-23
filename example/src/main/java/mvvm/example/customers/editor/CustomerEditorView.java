package mvvm.example.customers.editor;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import mvvm.example.core.view.controls.Controls;
import mvvm.example.core.view.controls.FormGrid;
import mvvm.example.customers.domain.CustomerStatus;

public class CustomerEditorView extends FormGrid {

    public CustomerEditorView(CustomerEditorViewModel viewModel) {
        addRow("Name", nameField(viewModel));
        addRow("Email", emailField(viewModel));
        addRow("Status", statusCombo(viewModel));
    }

    private static TextField nameField(CustomerEditorViewModel viewModel) {
        var field = new TextField();
        field.textProperty().bindBidirectional(viewModel.nameProperty());
        Controls.focusOnShow(field);
        return field;
    }

    private static TextField emailField(CustomerEditorViewModel viewModel) {
        var field = new TextField();
        field.textProperty().bindBidirectional(viewModel.emailProperty());
        return field;
    }

    private static ComboBox<CustomerStatus> statusCombo(CustomerEditorViewModel viewModel) {
        var combo = new ComboBox<CustomerStatus>();
        combo.getItems().addAll(CustomerStatus.values());
        combo.valueProperty().bindBidirectional(viewModel.statusProperty());
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(CustomerStatus s) { return s == null ? "" : s.displayName(); }
            @Override public CustomerStatus fromString(String s) { return null; }
        });
        return combo;
    }
}
