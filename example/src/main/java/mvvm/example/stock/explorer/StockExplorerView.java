package mvvm.example.stock.explorer;

import javafx.scene.control.TableColumn;
import mvvm.example.core.view.ExplorerView;
import mvvm.example.core.view.controls.CurrencyTableCell;
import mvvm.example.core.view.controls.TableColumns;
import mvvm.example.stock.domain.Product;

import java.math.BigDecimal;
import java.util.List;

public class StockExplorerView extends ExplorerView<Product> {

    public StockExplorerView(StockExplorerViewModel viewModel) {
        super(viewModel);
    }

    @Override
    protected List<TableColumn<Product, ?>> columns() {
        return List.of(
            TableColumns.column("Name", Product::name),
            TableColumns.column("Unit Price", Product::unitPrice, CurrencyTableCell.forTableColumn()),
            TableColumns.column("In Stock", p -> String.valueOf(p.quantityInStock()))
        );
    }
}
