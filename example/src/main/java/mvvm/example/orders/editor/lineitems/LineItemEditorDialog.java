package mvvm.example.orders.editor.lineitems;

import javafx.scene.control.Dialog;
import mvvm.example.core.view.Dialogs;

public class LineItemEditorDialog {

    public static Dialog<Runnable> dialog(LineItemEditorViewModel viewModel) {
        return Dialogs.create(
            "Edit Item",
            new LineItemEditorView(viewModel),
            "Confirm",
            viewModel.confirmAction()
        );
    }

    private LineItemEditorDialog() {}
}
