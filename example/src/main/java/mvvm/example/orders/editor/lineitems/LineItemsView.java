package mvvm.example.orders.editor.lineitems;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import mvvm.example.core.view.CurrencyTableCell;

import java.math.BigDecimal;

public class LineItemsView extends BorderPane {

    public LineItemsView(LineItemsViewModel viewModel) {
        var table = new TableView<LineItemRowViewModel>();
        table.setItems(viewModel.getRows());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(descriptionColumn());
        table.getColumns().add(quantityColumn());
        table.getColumns().add(unitPriceColumn());
        table.getColumns().add(totalColumn());

        var addBtn = new Button("Add");
        var editBtn = new Button("Edit");
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

        table.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, old, row) -> viewModel.selectRow(row));
    }

    private TableColumn<LineItemRowViewModel, String> descriptionColumn() {
        var col = new TableColumn<LineItemRowViewModel, String>("Description");
        col.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        return col;
    }

    private TableColumn<LineItemRowViewModel, Number> quantityColumn() {
        var col = new TableColumn<LineItemRowViewModel, Number>("Qty");
        col.setCellValueFactory(cell -> cell.getValue().quantityProperty());
        return col;
    }

    private TableColumn<LineItemRowViewModel, BigDecimal> unitPriceColumn() {
        var col = new TableColumn<LineItemRowViewModel, BigDecimal>("Unit Price");
        col.setCellValueFactory(cell -> cell.getValue().unitPriceProperty());
        col.setCellFactory(CurrencyTableCell.forTableColumn());
        return col;
    }

    private TableColumn<LineItemRowViewModel, BigDecimal> totalColumn() {
        var col = new TableColumn<LineItemRowViewModel, BigDecimal>("Total");
        col.setCellValueFactory(cell -> cell.getValue().totalProperty());
        col.setCellFactory(CurrencyTableCell.forTableColumn());
        return col;
    }
}
