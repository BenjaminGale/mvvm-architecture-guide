package mvvm.example.orders.editor.lineitems;

import mvvm.example.stock.domain.Product;

import java.util.Set;
import java.util.function.Consumer;

public class SelectProductRequest {

    private final Set<String> excludedProductIds;
    private final Consumer<Product> onSelected;

    public SelectProductRequest(Set<String> excludedProductIds, Consumer<Product> onSelected) {
        this.excludedProductIds = excludedProductIds;
        this.onSelected = onSelected;
    }

    public Set<String> getExcludedProductIds() { return excludedProductIds; }

    public void confirmSelection(Product product) { onSelected.accept(product); }
}
