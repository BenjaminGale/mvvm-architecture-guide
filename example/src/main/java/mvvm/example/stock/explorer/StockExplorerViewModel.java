package mvvm.example.stock.explorer;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import mvvm.example.core.viewmodel.ExplorerViewModel;
import mvvm.example.stock.domain.Product;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StockExplorerViewModel extends ExplorerViewModel<Product> {

    private final StockExplorerService service;

    public StockExplorerViewModel(StockExplorerService service) {
        this.service = service;
    }

    @Override
    protected ObservableBooleanValue canAddCondition() {
        return new SimpleBooleanProperty(false);
    }

    @Override
    protected ObservableBooleanValue canEditCondition() {
        return new SimpleBooleanProperty(false);
    }

    @Override
    protected ObservableBooleanValue canDeleteCondition() {
        return new SimpleBooleanProperty(false);
    }

    @Override
    protected CompletableFuture<List<Product>> fetchItemsAsync() {
        return CompletableFuture.completedFuture(
            service.fetchProducts()
                .stream()
                .sorted(Comparator.comparing(Product::name))
                .toList()
        );
    }

}
