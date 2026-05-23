package mvvm.example.orders.editor.header;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import mvvm.example.core.view.controls.Controls;

import java.time.format.DateTimeFormatter;

public class OrderHeaderView extends BorderPane {

    public OrderHeaderView(OrderHeaderViewModel viewModel) {
        var customerNameLabel = new Label();
        customerNameLabel.textProperty().bind(
            Bindings.createStringBinding(
                () -> {
                    var customer = viewModel.selectedCustomerProperty().get();
                    return customer != null ? customer.name() : "No customer selected";
                },
                viewModel.selectedCustomerProperty()
            )
        );
        customerNameLabel.styleProperty().bind(
            Bindings.when(viewModel.selectedCustomerProperty().isNull())
                .then("-fx-text-fill: -fx-mid-text-color;")
                .otherwise("")
        );

        var selectBtn = new Button("Select…");
        selectBtn.setOnAction(e -> viewModel.selectCustomerAction.execute());
        selectBtn.disableProperty().bind(viewModel.selectCustomerAction.canExecuteProperty().not());

        var customerRow = new HBox(8, customerNameLabel, selectBtn);
        customerRow.setAlignment(Pos.CENTER_LEFT);

        var orderDatePicker = new DatePicker();
        var referenceField = new TextField();

        var createdDateLabel = new Label(viewModel.createdDate() != null
            ? viewModel.createdDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            : "");
        var statusLabel = new Label(viewModel.status().displayName());

        var form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(8));
        form.add(new Label("Customer"), 0, 0);
        form.add(customerRow, 1, 0);
        form.add(new Label("Created"), 0, 1);
        form.add(createdDateLabel, 1, 1);
        form.add(new Label("Status"), 0, 2);
        form.add(statusLabel, 1, 2);
        form.add(new Label("Ship By"), 0, 3);
        form.add(orderDatePicker, 1, 3);
        form.add(new Label("Reference"), 0, 4);
        form.add(referenceField, 1, 4);

        var labelCol = new ColumnConstraints();
        var fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labelCol, fieldCol);

        var toolbar = new ToolBar(new Label("Order Header"));
        toolbar.setPadding(new Insets(4));

        setTop(toolbar);
        setCenter(form);

        orderDatePicker.valueProperty().bindBidirectional(viewModel.plannedShipDateProperty());
        referenceField.textProperty().bindBidirectional(viewModel.referenceProperty());

        Controls.focusOnShow(selectBtn);
    }
}
