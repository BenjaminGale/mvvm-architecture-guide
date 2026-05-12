package mvvm.example.orders.editor.lineitems;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class LineItemsView extends BorderPane {

    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.UK);

    public LineItemsView(LineItemsViewModel viewModel) {
        var table = new TableView<LineItemRow>();
        table.setItems(viewModel.getRows());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(descriptionColumn());
        table.getColumns().add(quantityColumn());
        table.getColumns().add(unitPriceColumn());
        table.getColumns().add(totalColumn());

        var addBtn    = new Button("Add");
        var editBtn   = new Button("Edit");
        var removeBtn = new Button("Remove");

        var toolbar = new HBox(8, addBtn, editBtn, removeBtn);
        toolbar.setPadding(new Insets(8, 0, 0, 0));

        setCenter(table);
        setBottom(toolbar);

        editBtn.disableProperty().bind(viewModel.selectedRowProperty().isNull());
        removeBtn.disableProperty().bind(viewModel.canRemoveProperty().not());

        addBtn.setOnAction(e -> viewModel.addRow());
        editBtn.setOnAction(e -> viewModel.editSelected());
        removeBtn.setOnAction(e -> viewModel.removeSelected());

        table.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, row) -> viewModel.selectRow(row));
    }

    private TableColumn<LineItemRow, String> descriptionColumn() {
        var col = new TableColumn<LineItemRow, String>("Description");
        col.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        return col;
    }

    private TableColumn<LineItemRow, Number> quantityColumn() {
        var col = new TableColumn<LineItemRow, Number>("Qty");
        col.setCellValueFactory(cell -> cell.getValue().quantityProperty());
        return col;
    }

    private TableColumn<LineItemRow, BigDecimal> unitPriceColumn() {
        var col = new TableColumn<LineItemRow, BigDecimal>("Unit Price");
        col.setCellValueFactory(cell -> cell.getValue().unitPriceProperty());
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : CURRENCY.format(value));
            }
        });
        return col;
    }

    private TableColumn<LineItemRow, BigDecimal> totalColumn() {
        var col = new TableColumn<LineItemRow, BigDecimal>("Total");
        col.setCellValueFactory(cell -> {
            var row = cell.getValue();
            return Bindings.createObjectBinding(
                () -> {
                    var price = row.unitPriceProperty().get();
                    var qty   = row.quantityProperty().get();
                    return price == null ? BigDecimal.ZERO : price.multiply(BigDecimal.valueOf(qty));
                },
                row.unitPriceProperty(), row.quantityProperty()
            );
        });
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : CURRENCY.format(value));
            }
        });
        return col;
    }
}
