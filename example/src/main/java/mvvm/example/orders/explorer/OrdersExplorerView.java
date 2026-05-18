package mvvm.example.orders.explorer;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
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

    public OrdersExplorerView(OrdersExplorerViewModel viewModel) {
        super(viewModel);
        TableViews.bind(table(), viewModel.editItemAction());
    }

    @Override
    protected List<TableColumn<Order, ?>> columns() {
        return List.of(referenceColumn(), customerColumn(), dateColumn(), totalColumn(), overdueColumn());
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
