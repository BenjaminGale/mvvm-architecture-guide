package mvvm.example.orders.explorer;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import mvvm.example.orders.domain.Order;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class OrdersExplorerView extends BorderPane {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.UK);

    public OrdersExplorerView(OrdersViewModel viewModel) {
        var table = new TableView<Order>();
        table.setItems(viewModel.getOrders());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(referenceColumn());
        table.getColumns().add(customerColumn());
        table.getColumns().add(dateColumn());
        table.getColumns().add(totalColumn());
        table.getColumns().add(overdueColumn());

        var refreshButton = new Button("Refresh");
        var statusLabel = new Label();
        statusLabel.textProperty().bind(viewModel.statusTextProperty());

        var toolbar = new HBox(8, refreshButton, statusLabel);
        toolbar.setPadding(new Insets(8));

        setTop(toolbar);
        setCenter(table);

        refreshButton.setOnAction(e -> viewModel.refresh());

        table.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, selected) -> viewModel.openOrder(selected));
    }

    private TableColumn<Order, String> referenceColumn() {
        var col = new TableColumn<Order, String>("Reference");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().reference()));
        return col;
    }

    private TableColumn<Order, String> customerColumn() {
        var col = new TableColumn<Order, String>("Customer");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().customerName()));
        return col;
    }

    private TableColumn<Order, LocalDate> dateColumn() {
        var col = new TableColumn<Order, LocalDate>("Date");
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().date()));
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : DATE_FORMAT.format(date));
            }
        });
        return col;
    }

    private TableColumn<Order, BigDecimal> totalColumn() {
        var col = new TableColumn<Order, BigDecimal>("Total");
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().total()));
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal total, boolean empty) {
                super.updateItem(total, empty);
                setText(empty || total == null ? null : CURRENCY.format(total));
            }
        });
        return col;
    }

    private TableColumn<Order, String> overdueColumn() {
        var col = new TableColumn<Order, String>("Overdue");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().isOverdue() ? "Yes" : ""));
        return col;
    }
}
