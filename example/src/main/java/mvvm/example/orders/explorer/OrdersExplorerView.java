package mvvm.example.orders.explorer;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import mvvm.example.core.view.controls.Controls;
import mvvm.example.core.view.controls.CurrencyTableCell;
import mvvm.example.core.view.controls.LocalDateTableCell;
import mvvm.example.core.view.controls.TableViews;
import mvvm.example.orders.domain.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class OrdersExplorerView extends BorderPane {

    public OrdersExplorerView(OrdersExplorerViewModel viewModel) {
        var table = new TableView<Order>();
        table.setItems(viewModel.getOrders());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(referenceColumn());
        table.getColumns().add(customerColumn());
        table.getColumns().add(dateColumn());
        table.getColumns().add(totalColumn());
        table.getColumns().add(overdueColumn());

        viewModel
            .selectedOrderProperty()
            .bind(table.getSelectionModel().selectedItemProperty());

        var refreshButton = new Button("Add");
        var toolbar = new ToolBar(refreshButton);

        BorderPane.setMargin(table, new Insets(8));
        setTop(toolbar);
        setCenter(table);

        TableViews.bind(table, viewModel.openOrderAction());

        Controls.focusOnShow(table);
    }

    private static TableColumn<Order, String> referenceColumn() {
        var col = new TableColumn<Order, String>("Reference");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().reference()));
        return col;
    }

    private static TableColumn<Order, String> customerColumn() {
        var col = new TableColumn<Order, String>("Customer");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().customerName()));
        return col;
    }

    private static TableColumn<Order, LocalDate> dateColumn() {
        var col = new TableColumn<Order, LocalDate>("Date");
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().date()));
        col.setCellFactory(LocalDateTableCell.forTableColumn(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        return col;
    }

    private static TableColumn<Order, BigDecimal> totalColumn() {
        var col = new TableColumn<Order, BigDecimal>("Total");
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().total()));
        col.setCellFactory(CurrencyTableCell.forTableColumn());
        return col;
    }

    private static TableColumn<Order, String> overdueColumn() {
        var col = new TableColumn<Order, String>("Overdue");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().isOverdue() ? "Yes" : ""));
        return col;
    }
}
