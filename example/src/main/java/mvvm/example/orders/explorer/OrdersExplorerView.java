package mvvm.example.orders.explorer;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import mvvm.example.core.view.ExplorerView;
import mvvm.example.core.view.controls.CurrencyTableCell;
import mvvm.example.core.view.controls.LocalDateTableCell;
import mvvm.example.core.view.controls.TableViews;
import mvvm.example.orders.domain.queries.OrderSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrdersExplorerView extends ExplorerView<OrderSummary> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public OrdersExplorerView(OrdersExplorerViewModel viewModel) {
        super(viewModel);
        TableViews.bind(table(), viewModel.editItemAction());
        table().setRowFactory(_ -> {
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
        });
    }

    @Override
    protected List<TableColumn<OrderSummary, ?>> columns() {
        return List.of(referenceColumn(), customerColumn(), createdDateColumn(), plannedShipDateColumn(), statusColumn(), totalColumn());
    }

    private static String overdueStyle(OrderSummary summary, boolean selected) {
        return summary != null && summary.isOverdue() && !selected
            ? "-fx-background-color: #fff3cd;" : "";
    }

    private static TableColumn<OrderSummary, String> referenceColumn() {
        var col = new TableColumn<OrderSummary, String>("Reference");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().reference()));
        return col;
    }

    private static TableColumn<OrderSummary, String> customerColumn() {
        var col = new TableColumn<OrderSummary, String>("Customer");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().customerName()));
        return col;
    }

    private static TableColumn<OrderSummary, LocalDate> createdDateColumn() {
        var col = new TableColumn<OrderSummary, LocalDate>("Created");
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().createdDate()));
        col.setCellFactory(LocalDateTableCell.forTableColumn(DATE_FORMAT));
        return col;
    }

    private static TableColumn<OrderSummary, LocalDate> plannedShipDateColumn() {
        var col = new TableColumn<OrderSummary, LocalDate>("Ship By");
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().plannedShipDate()));
        col.setCellFactory(LocalDateTableCell.forTableColumn(DATE_FORMAT));
        return col;
    }

    private static TableColumn<OrderSummary, String> statusColumn() {
        var col = new TableColumn<OrderSummary, String>("Status");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().status()));
        return col;
    }

    private static TableColumn<OrderSummary, BigDecimal> totalColumn() {
        var col = new TableColumn<OrderSummary, BigDecimal>("Total");
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().total()));
        col.setCellFactory(CurrencyTableCell.forTableColumn());
        return col;
    }
}
