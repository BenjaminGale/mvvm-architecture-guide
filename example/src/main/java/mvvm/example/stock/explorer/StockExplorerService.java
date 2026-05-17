package mvvm.example.stock.explorer;

import mvvm.example.stock.domain.Product;

import java.util.List;

public interface StockExplorerService {
    List<Product> fetchProducts();
}
