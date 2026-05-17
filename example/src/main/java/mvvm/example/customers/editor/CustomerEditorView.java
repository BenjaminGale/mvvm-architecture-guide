package mvvm.example.customers.editor;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import mvvm.example.customers.domain.CustomerStatus;

public class CustomerEditorView extends GridPane {

    public static Dialog<Runnable> dialog(CustomerEditorViewModel viewModel) {
        var saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        var dialog = new Dialog<Runnable>();
        dialog.setTitle("Customer");
        dialog.getDialogPane().setContent(new CustomerEditorView(viewModel));
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == saveBtn ? viewModel::confirm : null);
        return dialog;
    }

    private CustomerEditorView(CustomerEditorViewModel viewModel) {
        setHgap(8);
        setVgap(8);
        setPadding(new Insets(16));

        var nameField = new TextField();
        nameField.textProperty().bindBidirectional(viewModel.nameProperty());

        var emailField = new TextField();
        emailField.textProperty().bindBidirectional(viewModel.emailProperty());

        var statusCombo = new ComboBox<CustomerStatus>();
        statusCombo.getItems().addAll(CustomerStatus.values());
        statusCombo.valueProperty().bindBidirectional(viewModel.statusProperty());
        statusCombo.setConverter(new StringConverter<>() {
            @Override public String toString(CustomerStatus s) {
                return s == null ? "" : s.displayName();
            }
            @Override public CustomerStatus fromString(String s) {
                return null;
            }
        });

        add(new Label("Name"), 0, 0);
        add(nameField, 1, 0);
        add(new Label("Email"), 0, 1);
        add(emailField, 1, 1);
        add(new Label("Status"), 0, 2);
        add(statusCombo, 1, 2);

        Platform.runLater(nameField::requestFocus);
    }
}
