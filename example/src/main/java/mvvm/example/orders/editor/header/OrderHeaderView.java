package mvvm.example.orders.editor.header;

import javafx.geometry.Insets;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class OrderHeaderView extends GridPane {

    public OrderHeaderView(OrderHeaderViewModel viewModel) {
        setHgap(8);
        setVgap(8);
        setPadding(new Insets(8));

        var customerNameField = new TextField();
        var orderDatePicker = new DatePicker();
        var referenceField = new TextField();

        add(new Label("Customer"), 0, 0);
        add(customerNameField, 1, 0);

        add(new Label("Date"), 0, 1);
        add(orderDatePicker, 1, 1);

        add(new Label("Reference"), 0, 2);
        add(referenceField, 1, 2);

        var labelCol = new ColumnConstraints();
        var fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        getColumnConstraints().addAll(labelCol, fieldCol);

        customerNameField.textProperty().bindBidirectional(viewModel.customerNameProperty());
        orderDatePicker.valueProperty().bindBidirectional(viewModel.orderDateProperty());
        referenceField.textProperty().bindBidirectional(viewModel.referenceProperty());
    }
}
