package mvvm.example.stock.explorer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mvvm.example.stock.domain.Product;

import java.util.Comparator;

public class StockExplorerViewModel {

    private final ObservableList<Product> products = FXCollections.observableArrayList();
    private final StockExplorerService service;

    public StockExplorerViewModel(StockExplorerService service) {
        this.service = service;
        refresh();
    }

    public ObservableList<Product> getProducts() {
        return products;
    }

    public void refresh() {
        var sorted = service.fetchProducts()
            .stream()
            .sorted(Comparator.comparing(Product::name))
            .toList();
        products.setAll(sorted);
    }
}
