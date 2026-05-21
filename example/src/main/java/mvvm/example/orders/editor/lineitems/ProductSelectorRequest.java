package mvvm.example.orders.editor.lineitems;

import mvvm.example.stock.domain.Product;

import java.util.Set;
import java.util.function.Consumer;

public record ProductSelectorRequest(Set<String> excludedProductIds, Consumer<Product> onSelected) {

    public void confirmSelection(Product product) { onSelected.accept(product); }
}
