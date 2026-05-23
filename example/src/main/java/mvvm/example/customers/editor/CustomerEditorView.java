package mvvm.example.customers.editor;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import mvvm.example.core.view.controls.Controls;
import mvvm.example.core.view.controls.FormGrid;
import mvvm.example.customers.domain.CustomerStatus;

public class CustomerEditorView extends FormGrid {

    public static Dialog<Runnable> dialog(CustomerEditorViewModel viewModel) {
        var saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        var dialog = new Dialog<Runnable>();
        dialog.setTitle(viewModel.isNew() ? "Add Customer" : "Edit Customer");
        dialog.getDialogPane().setContent(new CustomerEditorView(viewModel));
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == saveBtn ? viewModel::confirm : null);
        return dialog;
    }

    private CustomerEditorView(CustomerEditorViewModel viewModel) {
        var nameField = new TextField();
        nameField.textProperty().bindBidirectional(viewModel.nameProperty());

        var emailField = new TextField();
        emailField.textProperty().bindBidirectional(viewModel.emailProperty());

        var statusCombo = new ComboBox<CustomerStatus>();
        statusCombo.getItems().addAll(CustomerStatus.values());
        statusCombo.valueProperty().bindBidirectional(viewModel.statusProperty());
        statusCombo.setConverter(new StringConverter<>() {
            @Override public String toString(CustomerStatus s) { return s == null ? "" : s.displayName(); }
            @Override public CustomerStatus fromString(String s) { return null; }
        });

        addRow("Name", nameField);
        addRow("Email", emailField);
        addRow("Status", statusCombo);

        Controls.focusOnShow(nameField);
    }
}
