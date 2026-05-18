package mvvm.example.stock.explorer;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import mvvm.example.core.view.controls.Controls;
import mvvm.example.core.view.controls.CurrencyTableCell;
import mvvm.example.stock.domain.Product;

import java.math.BigDecimal;

public class StockExplorerView extends BorderPane {

    public StockExplorerView(StockExplorerViewModel viewModel) {
        var table = new TableView<Product>();
        table.setItems(viewModel.items());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(nameColumn());
        table.getColumns().add(unitPriceColumn());
        table.getColumns().add(quantityInStockColumn());

        viewModel.selectedItemProperty().bind(table.getSelectionModel().selectedItemProperty());

        var addButton = new Button("Add");
        addButton.disableProperty().bind(viewModel.addItemAction().canExecuteProperty().not());
        addButton.setOnAction(_ -> viewModel.addItemAction().execute());
        var toolbar = new ToolBar(addButton);

        BorderPane.setMargin(table, new Insets(8));
        setTop(toolbar);
        setCenter(table);

        Controls.focusOnShow(table);
        viewModel.fetchItemsAction().executeAsync(Platform::runLater);
    }

    private static TableColumn<Product, String> nameColumn() {
        var col = new TableColumn<Product, String>("Name");
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().name()));
        return col;
    }

    private static TableColumn<Product, BigDecimal> unitPriceColumn() {
        var col = new TableColumn<Product, BigDecimal>("Unit Price");
        col.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().unitPrice()));
        col.setCellFactory(CurrencyTableCell.forTableColumn());
        return col;
    }

    private static TableColumn<Product, String> quantityInStockColumn() {
        var col = new TableColumn<Product, String>("In Stock");
        col.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().quantityInStock())));
        return col;
    }
}
