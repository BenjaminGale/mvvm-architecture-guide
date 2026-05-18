package mvvm.example.stock.explorer;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import mvvm.example.core.view.ExplorerView;
import mvvm.example.core.view.controls.CurrencyTableCell;
import mvvm.example.stock.domain.Product;

import java.math.BigDecimal;
import java.util.List;

public class StockExplorerView extends ExplorerView<Product> {

    public StockExplorerView(StockExplorerViewModel viewModel) {
        super(viewModel);
    }

    @Override
    protected List<TableColumn<Product, ?>> columns() {
        return List.of(nameColumn(), unitPriceColumn(), quantityInStockColumn());
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
