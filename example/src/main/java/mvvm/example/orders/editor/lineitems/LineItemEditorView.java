package mvvm.example.orders.editor.lineitems;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.BorderPane;
import mvvm.example.core.view.controls.Buttons;
import mvvm.example.core.view.controls.FormGrid;

public class LineItemEditorView extends BorderPane {

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
        var selectProductBtn = new Button("Select Product...");
        Buttons.bind(selectProductBtn, viewModel.selectProductAction);

        var descriptionLabel = new Label();
        descriptionLabel.textProperty().bind(viewModel.descriptionProperty());

        var quantitySpinner = new Spinner<Integer>();
        var quantityFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, viewModel.quantityProperty().get());
        quantitySpinner.setValueFactory(quantityFactory);
        quantitySpinner.setEditable(true);

        var unitPriceLabel = new Label();
        unitPriceLabel.textProperty().bind(viewModel.unitPriceProperty().asString());

        var form = new FormGrid();
        form.addRow("Product", selectProductBtn);
        form.addRow("Description", descriptionLabel);
        form.addRow("Quantity", quantitySpinner);
        form.addRow("Unit Price", unitPriceLabel);
        setCenter(form);

        quantityFactory.valueProperty().bindBidirectional(viewModel.quantityProperty().asObject());
    }
}
