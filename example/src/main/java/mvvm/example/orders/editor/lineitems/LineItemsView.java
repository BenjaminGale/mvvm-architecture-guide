package mvvm.example.orders.editor.lineitems;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import mvvm.example.core.view.controls.Buttons;
import mvvm.example.core.view.controls.CurrencyTableCell;
import mvvm.example.core.view.controls.Spacer;
import mvvm.example.core.view.controls.TableViews;
import mvvm.example.orders.domain.queries.LineItemSummary;

import java.math.BigDecimal;

public class LineItemsView extends BorderPane {

    public LineItemsView(LineItemsViewModel viewModel) {
        var table = new TableView<LineItemSummary>();
        table.setItems(viewModel.items());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(productNameColumn());
        table.getColumns().add(quantityColumn());
        table.getColumns().add(unitPriceColumn());
        table.getColumns().add(totalColumn());
        table.getColumns().add(allocatedColumn());

        var addBtn = new Button("Add");
        var editBtn = new Button("Edit");
        var deleteBtn = new Button("Delete");

        Buttons.bind(addBtn, viewModel.addItemAction());
        Buttons.bind(editBtn, viewModel.editItemAction());
        Buttons.bind(deleteBtn, viewModel.deleteItemAction());

        var headerToolbar = new ToolBar(new Label("Line Items"), Spacer.create(), addBtn, editBtn, deleteBtn);

        BorderPane.setMargin(table, new Insets(8, 8, 8, 8));
        setTop(headerToolbar);
        setCenter(table);

        TableViews.bind(table, viewModel.editItemAction());

        table.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, item) -> viewModel.selectedItemProperty().set(item));

        viewModel.selectedItemProperty()
            .addListener((obs, old, item) -> {
                if (item == null) table.getSelectionModel().clearSelection();
                else table.getSelectionModel().select(item);
            });
    }

    private static TableColumn<LineItemSummary, String> productNameColumn() {
        var col = new TableColumn<LineItemSummary, String>("Product");
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().productName()));
        return col;
    }

    private static TableColumn<LineItemSummary, Integer> quantityColumn() {
        var col = new TableColumn<LineItemSummary, Integer>("Qty");
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().quantity()));
        return col;
    }

    private static TableColumn<LineItemSummary, BigDecimal> unitPriceColumn() {
        var col = new TableColumn<LineItemSummary, BigDecimal>("Unit Price");
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().unitPrice()));
        col.setCellFactory(CurrencyTableCell.forTableColumn());
        return col;
    }

    private static TableColumn<LineItemSummary, BigDecimal> totalColumn() {
        var col = new TableColumn<LineItemSummary, BigDecimal>("Total");
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().total()));
        col.setCellFactory(CurrencyTableCell.forTableColumn());
        return col;
    }

    private static TableColumn<LineItemSummary, Integer> allocatedColumn() {
        var col = new TableColumn<LineItemSummary, Integer>("Allocated");
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().allocatedQuantity()));
        return col;
    }
}
