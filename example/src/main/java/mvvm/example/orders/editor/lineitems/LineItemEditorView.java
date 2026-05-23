package mvvm.example.orders.editor.lineitems;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Button;
import mvvm.example.core.view.controls.Buttons;
import mvvm.example.core.view.controls.FormGrid;

public class LineItemEditorView extends FormGrid {

    public static Dialog<Runnable> dialog(LineItemEditorViewModel viewModel) {
        var confirmBtn = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        var dialog = new Dialog<Runnable>();
        dialog.setTitle("Edit Item");
        dialog.getDialogPane().setContent(new LineItemEditorView(viewModel));
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtn, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == confirmBtn ? viewModel::confirm : null);
        return dialog;
    }

    private LineItemEditorView(LineItemEditorViewModel viewModel) {
        addRow("Product", selectProductButton(viewModel));
        addRow("Description", descriptionLabel(viewModel));
        addRow("Quantity", quantitySpinner(viewModel));
        addRow("Unit Price", unitPriceLabel(viewModel));
    }

    private static Button selectProductButton(LineItemEditorViewModel viewModel) {
        return Buttons.button("Select Product...", viewModel.selectProductAction);
    }

    private static Label descriptionLabel(LineItemEditorViewModel viewModel) {
        var label = new Label();
        label.textProperty().bind(viewModel.descriptionProperty());
        return label;
    }

    private static Spinner<Integer> quantitySpinner(LineItemEditorViewModel viewModel) {
        var factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, viewModel.quantityProperty().get());
        factory.valueProperty().bindBidirectional(viewModel.quantityProperty().asObject());
        var spinner = new Spinner<Integer>();
        spinner.setValueFactory(factory);
        spinner.setEditable(true);
        return spinner;
    }

    private static Label unitPriceLabel(LineItemEditorViewModel viewModel) {
        var label = new Label();
        label.textProperty().bind(viewModel.unitPriceProperty().asString());
        return label;
    }
}
