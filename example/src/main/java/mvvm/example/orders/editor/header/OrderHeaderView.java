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
import javafx.scene.layout.HBox;
import mvvm.example.core.view.controls.Controls;
import mvvm.example.core.view.controls.FormGrid;
import mvvm.example.customers.domain.Customer;

import java.time.format.DateTimeFormatter;

public class OrderHeaderView extends BorderPane {

    public OrderHeaderView(OrderHeaderViewModel viewModel) {
        var customerNameLabel = new Label();
        customerNameLabel.textProperty().bind(
            viewModel.selectedCustomerProperty()
                .map(Customer::name)
                .orElse("No customer selected")
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

        var form = new FormGrid(new Insets(8));
        form.addRow("Customer", customerRow);
        form.addRow("Created", createdDateLabel);
        form.addRow("Status", statusLabel);
        form.addRow("Ship By", orderDatePicker);
        form.addRow("Reference", referenceField);

        var toolbar = new ToolBar(new Label("Order Header"));
        toolbar.setPadding(new Insets(4));

        setTop(toolbar);
        setCenter(form);

        orderDatePicker.valueProperty().bindBidirectional(viewModel.plannedShipDateProperty());
        referenceField.textProperty().bindBidirectional(viewModel.referenceProperty());

        Controls.focusOnShow(selectBtn);
    }
}
