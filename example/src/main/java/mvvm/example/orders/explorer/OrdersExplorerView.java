package mvvm.example.orders.explorer;

import javafx.application.Platform;
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
        table.setItems(viewModel.items());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(referenceColumn());
        table.getColumns().add(customerColumn());
        table.getColumns().add(dateColumn());
        table.getColumns().add(totalColumn());
        table.getColumns().add(overdueColumn());

        viewModel.selectedItemProperty().bind(table.getSelectionModel().selectedItemProperty());

        var addButton = new Button("Add");
        addButton.disableProperty().bind(viewModel.addItemAction().canExecuteProperty().not());
        var toolbar = new ToolBar(addButton);

        BorderPane.setMargin(table, new Insets(8));
        setTop(toolbar);
        setCenter(table);

        TableViews.bind(table, viewModel.editItemAction());

        Controls.focusOnShow(table);
        viewModel.fetchItemsAction().executeAsync(Platform::runLater);
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
