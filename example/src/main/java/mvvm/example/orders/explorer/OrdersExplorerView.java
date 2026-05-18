package mvvm.example.orders.explorer;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import mvvm.example.core.view.ExplorerView;
import mvvm.example.core.view.controls.CurrencyTableCell;
import mvvm.example.core.view.controls.LocalDateTableCell;
import mvvm.example.core.view.controls.TableViews;
import mvvm.example.orders.domain.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrdersExplorerView extends ExplorerView<Order> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public OrdersExplorerView(OrdersExplorerViewModel viewModel) {
        super(viewModel);
        TableViews.bind(table(), viewModel.editItemAction());
        table().setRowFactory(_ -> {
            var row = new TableRow<Order>() {
                @Override
                protected void updateItem(Order order, boolean empty) {
                    super.updateItem(order, empty);
                    setStyle(!empty && order != null && order.isOverdue() && !isSelected()
                        ? "-fx-background-color: #fff3cd;"
                        : "");
                }
            };

            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) viewModel.editItemAction().execute();
            });

            row.selectedProperty().addListener((obs, old, selected) -> {
                var order = row.getItem();
                row.setStyle(order != null && !row.isEmpty() && order.isOverdue() && !selected
                    ? "-fx-background-color: #fff3cd;"
                    : "");
            });

            return row;
        });
    }

    @Override
    protected List<TableColumn<Order, ?>> columns() {
        return List.of(referenceColumn(), customerColumn(), createdDateColumn(), plannedShipDateColumn(), statusColumn(), totalColumn());
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

    private static TableColumn<Order, LocalDate> createdDateColumn() {
        var col = new TableColumn<Order, LocalDate>("Created");
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().createdDate()));
        col.setCellFactory(LocalDateTableCell.forTableColumn(DATE_FORMAT));
        return col;
    }

    private static TableColumn<Order, LocalDate> plannedShipDateColumn() {
        var col = new TableColumn<Order, LocalDate>("Ship By");
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().plannedShipDate()));
        col.setCellFactory(LocalDateTableCell.forTableColumn(DATE_FORMAT));
        return col;
    }

    private static TableColumn<Order, String> statusColumn() {
        var col = new TableColumn<Order, String>("Status");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().status().displayName()));
        return col;
    }

    private static TableColumn<Order, BigDecimal> totalColumn() {
        var col = new TableColumn<Order, BigDecimal>("Total");
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().total()));
        col.setCellFactory(CurrencyTableCell.forTableColumn());
        return col;
    }
}
