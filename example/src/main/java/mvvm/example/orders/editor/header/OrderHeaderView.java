package mvvm.example.orders.editor.header;

import javafx.geometry.Insets;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import mvvm.example.core.view.controls.Controls;

public class OrderHeaderView extends BorderPane {

    public OrderHeaderView(OrderHeaderViewModel viewModel) {
        var customerNameField = new TextField();
        var orderDatePicker = new DatePicker();
        var referenceField = new TextField();

        var form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(8));

        form.add(new Label("Customer"), 0, 0);
        form.add(customerNameField, 1, 0);

        form.add(new Label("Date"), 0, 1);
        form.add(orderDatePicker, 1, 1);

        form.add(new Label("Reference"), 0, 2);
        form.add(referenceField, 1, 2);

        var labelCol = new ColumnConstraints();
        var fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labelCol, fieldCol);

        var toolbar = new ToolBar(new Label("Order Header"));
        toolbar.setPadding(new Insets(4));

        setTop(toolbar);
        setCenter(form);

        customerNameField.textProperty().bindBidirectional(viewModel.customerNameProperty());
        orderDatePicker.valueProperty().bindBidirectional(viewModel.orderDateProperty());
        referenceField.textProperty().bindBidirectional(viewModel.referenceProperty());

        Controls.focusOnShow(customerNameField);
    }
}
