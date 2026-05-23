package mvvm.example.orders.editor.lineitems;

import mvvm.example.orders.domain.LineItem;
import mvvm.example.stock.domain.Product;

import java.util.List;
import java.util.function.Consumer;

public record ProductSelectorRequest(
    List<LineItem> currentLineItems,
    String currentProductId,
    Consumer<Product> onSelected
) {
    public void confirmSelection(Product product) { onSelected.accept(product); }
}
