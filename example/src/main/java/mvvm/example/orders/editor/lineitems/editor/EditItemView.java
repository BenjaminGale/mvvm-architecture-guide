package mvvm.example.orders.editor.lineitems.editor;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import mvvm.example.core.view.controls.Buttons;

public class EditItemView extends BorderPane {

    public static Dialog<Runnable> dialog(EditItemViewModel viewModel) {
        var confirmBtn = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);

        var dialog = new Dialog<Runnable>();
        dialog.setTitle("Edit Item");
        dialog.getDialogPane().setContent(new EditItemView(viewModel));
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtn, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == confirmBtn ? viewModel::confirm : null);

        return dialog;
    }

    private EditItemView(EditItemViewModel viewModel) {
        var selectProductBtn = new Button("Select Product...");
        Buttons.bind(selectProductBtn, viewModel.selectProduct);

        var descriptionLabel = new Label();
        descriptionLabel.textProperty().bind(viewModel.descriptionProperty());

        var quantitySpinner = new Spinner<Integer>();
        var quantityFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, viewModel.quantityProperty().get());
        quantitySpinner.setValueFactory(quantityFactory);
        quantitySpinner.setEditable(true);

        var unitPriceLabel = new Label();
        unitPriceLabel.textProperty().bind(viewModel.unitPriceProperty().asString());

        var form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(16));

        form.add(new Label("Product"), 0, 0);
        form.add(selectProductBtn, 1, 0);

        form.add(new Label("Description"), 0, 1);
        form.add(descriptionLabel, 1, 1);

        form.add(new Label("Quantity"), 0, 2);
        form.add(quantitySpinner, 1, 2);

        form.add(new Label("Unit Price"), 0, 3);
        form.add(unitPriceLabel, 1, 3);

        var labelCol = new ColumnConstraints();
        var fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labelCol, fieldCol);

        setCenter(form);

        quantityFactory.valueProperty().bindBidirectional(viewModel.quantityProperty().asObject());
    }
}
