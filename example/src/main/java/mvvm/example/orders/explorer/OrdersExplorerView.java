package mvvm.example.orders.explorer;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import mvvm.example.core.view.ExplorerView;
import mvvm.example.core.view.table.CurrencyTableCell;
import mvvm.example.core.view.table.LocalDateTableCell;
import mvvm.example.core.view.table.TableColumns;
import mvvm.example.core.view.table.TableViews;
import mvvm.example.orders.domain.queries.OrderSummary;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrdersExplorerView extends ExplorerView<OrderSummary> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public OrdersExplorerView(OrdersExplorerViewModel viewModel) {
        super(viewModel);
        TableViews.bind(table(), viewModel.editItemAction());
        table().setRowFactory(_ -> overdueRow(viewModel));
    }

    private static TableRow<OrderSummary> overdueRow(OrdersExplorerViewModel viewModel) {
        var row = new TableRow<OrderSummary>() {
            @Override
            protected void updateItem(OrderSummary summary, boolean empty) {
                super.updateItem(summary, empty);
                setStyle(overdueStyle(summary, isSelected()));
            }
        };
        row.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) viewModel.editItemAction().execute();
        });
        row.selectedProperty().addListener((obs, old, selected) ->
            row.setStyle(overdueStyle(row.getItem(), selected)));
        return row;
    }

    @Override
    protected List<TableColumn<OrderSummary, ?>> columns() {
        return List.of(
            TableColumns.column("Reference", OrderSummary::reference),
            TableColumns.column("Customer", OrderSummary::customerName),
            TableColumns.column("Created", OrderSummary::createdDate, LocalDateTableCell.forTableColumn(DATE_FORMAT)),
            TableColumns.column("Ship By", OrderSummary::plannedShipDate, LocalDateTableCell.forTableColumn(DATE_FORMAT)),
            TableColumns.column("Status", OrderSummary::status),
            TableColumns.column("Total", OrderSummary::total, CurrencyTableCell.forTableColumn())
        );
    }

    private static String overdueStyle(OrderSummary summary, boolean selected) {
        return summary != null && summary.isOverdue() && !selected
            ? "-fx-background-color: #fff3cd;" : "";
    }
}
