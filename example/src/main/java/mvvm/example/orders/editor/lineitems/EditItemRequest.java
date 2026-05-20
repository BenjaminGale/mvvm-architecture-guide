package mvvm.example.orders.editor.lineitems;

import mvvm.example.orders.domain.LineItem;

import java.util.Set;
import java.util.function.Consumer;

public class EditItemRequest {

    private final LineItem item;
    private final Set<String> excludedProductIds;
    private final Consumer<LineItem> listener;

    public EditItemRequest(LineItem item, Set<String> excludedProductIds, Consumer<LineItem> listener) {
        this.item = item;
        this.excludedProductIds = excludedProductIds;
        this.listener = listener;
    }

    public LineItem getItem() { return item; }
    public Set<String> getExcludedProductIds() { return excludedProductIds; }
    public void confirmChanges(LineItem updated) { listener.accept(updated); }
}
