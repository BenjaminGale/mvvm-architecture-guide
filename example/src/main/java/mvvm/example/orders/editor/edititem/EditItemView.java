package mvvm.example.orders.editor.edititem;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.converter.BigDecimalStringConverter;

public class EditItemView extends BorderPane {

    public EditItemView(EditItemViewModel viewModel) {
        var descriptionField = new TextField();

        var quantitySpinner = new Spinner<Integer>();
        var quantityFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, viewModel.quantityProperty().get());
        quantitySpinner.setValueFactory(quantityFactory);
        quantitySpinner.setEditable(true);

        var unitPriceField = new TextField();
        var priceFormatter = new TextFormatter<>(new BigDecimalStringConverter(), viewModel.unitPriceProperty().get());
        unitPriceField.setTextFormatter(priceFormatter);

        var form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(16));

        form.add(new Label("Description"), 0, 0);
        form.add(descriptionField,         1, 0);
        form.add(new Label("Quantity"),    0, 1);
        form.add(quantitySpinner,          1, 1);
        form.add(new Label("Unit Price"),  0, 2);
        form.add(unitPriceField,           1, 2);

        var labelCol = new ColumnConstraints();
        var fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labelCol, fieldCol);

        var confirmBtn = new Button("Confirm");
        var cancelBtn  = new Button("Cancel");
        var buttons = new HBox(8, confirmBtn, cancelBtn);
        buttons.setPadding(new Insets(0, 16, 16, 16));

        setCenter(form);
        setBottom(buttons);

        descriptionField.textProperty().bindBidirectional(viewModel.descriptionProperty());
        quantityFactory.valueProperty().bindBidirectional(viewModel.quantityProperty().asObject());
        priceFormatter.valueProperty().bindBidirectional(viewModel.unitPriceProperty());

        confirmBtn.setOnAction(e -> {
            viewModel.confirm();
            ((Stage) getScene().getWindow()).close();
        });

        cancelBtn.setOnAction(e -> ((Stage) getScene().getWindow()).close());
    }
}
