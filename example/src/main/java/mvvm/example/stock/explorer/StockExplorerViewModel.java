package mvvm.example.stock.explorer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mvvm.example.stock.domain.Product;

import java.util.Comparator;

public class StockExplorerViewModel {

    private final ObservableList<Product> products = FXCollections.observableArrayList();

    public StockExplorerViewModel(StockExplorerService service) {
        products.setAll(
            service.fetchProducts()
                .stream()
                .sorted(Comparator.comparing(Product::name))
                .toList()
        );
    }

    public ObservableList<Product> getProducts() {
        return products;
    }
}
