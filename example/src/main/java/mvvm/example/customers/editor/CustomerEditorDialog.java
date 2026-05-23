package mvvm.example.customers.editor;

import javafx.scene.control.Dialog;
import mvvm.example.core.view.Dialogs;

public class CustomerEditorDialog {

    public static Dialog<Runnable> dialog(CustomerEditorViewModel viewModel) {
        return Dialogs.create(
            viewModel.isNew() ? "Add Customer" : "Edit Customer",
            new CustomerEditorView(viewModel),
            "Save",
            viewModel.confirmAction()
        );
    }

    private CustomerEditorDialog() {}
}
