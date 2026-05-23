package mvvm.example.orders.editor.lineitems;

import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Button;
import mvvm.example.core.view.controls.Buttons;
import mvvm.example.core.view.controls.Controls;
import mvvm.example.core.view.controls.FormGrid;

public class LineItemEditorView extends FormGrid {

    LineItemEditorView(LineItemEditorViewModel viewModel) {
        addRow("Product", selectProductButton(viewModel));
        addRow("Description", descriptionLabel(viewModel));
        addRow("Quantity", quantitySpinner(viewModel));
        addRow("Unit Price", unitPriceLabel(viewModel));
    }

    private static Button selectProductButton(LineItemEditorViewModel viewModel) {
        var btn = Buttons.button("Select Product...", viewModel.selectProductAction());
        Controls.focusOnShow(btn);
        return btn;
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
