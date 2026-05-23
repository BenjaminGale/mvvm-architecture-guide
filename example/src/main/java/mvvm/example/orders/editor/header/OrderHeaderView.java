package mvvm.example.orders.editor.header;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import mvvm.example.core.view.controls.Buttons;
import mvvm.example.core.view.controls.Controls;
import mvvm.example.core.view.controls.FormGrid;
import mvvm.example.customers.domain.Customer;

import java.time.format.DateTimeFormatter;

import static javafx.beans.binding.Bindings.when;

public class OrderHeaderView extends BorderPane {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public OrderHeaderView(OrderHeaderViewModel viewModel) {
        setTop(heading());
        setCenter(form(viewModel));
    }

    private static ToolBar heading() {
        var toolbar = new ToolBar(new Label("Order Header"));
        toolbar.setPadding(new Insets(4));
        return toolbar;
    }

    private static FormGrid form(OrderHeaderViewModel viewModel) {
        var form = new FormGrid(new Insets(8));
        form.addRow("Customer", customerRow(viewModel));
        form.addRow("Created", createdDateLabel(viewModel));
        form.addRow("Status", new Label(viewModel.status().displayName()));
        form.addRow("Ship By", shipByPicker(viewModel));
        form.addRow("Reference", referenceField(viewModel));
        return form;
    }

    private static DatePicker shipByPicker(OrderHeaderViewModel viewModel) {
        var picker = new DatePicker();
        picker.valueProperty().bindBidirectional(viewModel.plannedShipDateProperty());
        return picker;
    }

    private static TextField referenceField(OrderHeaderViewModel viewModel) {
        var field = new TextField();
        field.textProperty().bindBidirectional(viewModel.referenceProperty());
        return field;
    }

    private static HBox customerRow(OrderHeaderViewModel viewModel) {
        var row = new HBox(8, customerNameLabel(viewModel), selectCustomerButton(viewModel));
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static Label customerNameLabel(OrderHeaderViewModel viewModel) {
        var label = new Label();
        label.textProperty().bind(
            viewModel
                .selectedCustomerProperty()
                .map(Customer::name)
                .orElse("No customer selected")
        );
        label.styleProperty().bind(
            when(viewModel.selectedCustomerProperty().isNull())
                .then("-fx-text-fill: -fx-mid-text-color;")
                .otherwise("")
        );
        return label;
    }

    private static Button selectCustomerButton(OrderHeaderViewModel viewModel) {
        var btn = Buttons.button("Select…", viewModel.selectCustomerAction);
        Controls.focusOnShow(btn);
        return btn;
    }

    private static Label createdDateLabel(OrderHeaderViewModel viewModel) {
        var date = viewModel.createdDate();
        return new Label(date != null ? date.format(DATE_FORMAT) : "");
    }
}
