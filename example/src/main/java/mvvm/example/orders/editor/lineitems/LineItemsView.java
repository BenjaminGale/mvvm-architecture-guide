package mvvm.example.orders.editor.lineitems;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import mvvm.example.core.view.controls.CurrencyTableCell;
import mvvm.example.core.view.controls.Spacer;
import mvvm.example.core.view.controls.TableViews;
import mvvm.example.orders.domain.LineItem;

import java.math.BigDecimal;

public class LineItemsView extends BorderPane {

    public LineItemsView(LineItemsViewModel viewModel) {
        var table = new TableView<LineItem>();
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

        TableViews.onActivate(table, _ -> viewModel.editSelected());

        table.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, old, item) -> viewModel.selectRow(item));

        viewModel.selectedRowProperty()
            .addListener((obs, old, item) -> {
                if (item == null) table.getSelectionModel().clearSelection();
                else table.getSelectionModel().select(item);
            });
    }

    private static TableColumn<LineItem, String> descriptionColumn() {
        var col = new TableColumn<LineItem, String>("Description");
        col.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().description()));
        return col;
    }

    private static TableColumn<LineItem, Integer> quantityColumn() {
        var col = new TableColumn<LineItem, Integer>("Qty");
        col.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().quantity()));
        return col;
    }

    private static TableColumn<LineItem, BigDecimal> unitPriceColumn() {
        var col = new TableColumn<LineItem, BigDecimal>("Unit Price");
        col.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().unitPrice()));
        col.setCellFactory(CurrencyTableCell.forTableColumn());
        return col;
    }

    private static TableColumn<LineItem, BigDecimal> totalColumn() {
        var col = new TableColumn<LineItem, BigDecimal>("Total");
        col.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().total()));
        col.setCellFactory(CurrencyTableCell.forTableColumn());
        return col;
    }
}
