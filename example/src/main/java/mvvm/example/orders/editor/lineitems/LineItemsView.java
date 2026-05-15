package mvvm.example.orders.editor.lineitems;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import mvvm.example.core.view.controls.CurrencyTableCell;
import mvvm.example.core.view.controls.Spacer;

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
        var deleteBtn = new Button("Delete");

        var headerToolbar = new ToolBar(new Label("Line Items"), Spacer.create(), addBtn, editBtn, deleteBtn);

        BorderPane.setMargin(table, new Insets(8, 8, 8, 8));
        setTop(headerToolbar);
        setCenter(table);

        editBtn.disableProperty().bind(viewModel.selectedRowProperty().isNull());
        deleteBtn.disableProperty().bind(viewModel.canRemoveProperty().not());

        addBtn.setOnAction(e -> viewModel.addRow());
        editBtn.setOnAction(e -> viewModel.editSelected());
        deleteBtn.setOnAction(e -> viewModel.removeSelected());

        table.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, old, row) -> viewModel.selectRow(row));
    }

    private static TableColumn<LineItemRowViewModel, String> descriptionColumn() {
        var col = new TableColumn<LineItemRowViewModel, String>("Description");
        col.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        return col;
    }

    private static TableColumn<LineItemRowViewModel, Number> quantityColumn() {
        var col = new TableColumn<LineItemRowViewModel, Number>("Qty");
        col.setCellValueFactory(cell -> cell.getValue().quantityProperty());
        return col;
    }

    private static TableColumn<LineItemRowViewModel, BigDecimal> unitPriceColumn() {
        var col = new TableColumn<LineItemRowViewModel, BigDecimal>("Unit Price");
        col.setCellValueFactory(cell -> cell.getValue().unitPriceProperty());
        col.setCellFactory(CurrencyTableCell.forTableColumn());
        return col;
    }

    private static TableColumn<LineItemRowViewModel, BigDecimal> totalColumn() {
        var col = new TableColumn<LineItemRowViewModel, BigDecimal>("Total");
        col.setCellValueFactory(cell -> cell.getValue().totalProperty());
        col.setCellFactory(CurrencyTableCell.forTableColumn());
        return col;
    }
}
