package mvvm.example.orders.editor.lineitems;

import mvvm.example.orders.domain.LineItem;

import java.util.Set;
import java.util.function.Consumer;

public record LineItemEditorRequest(LineItem item, Set<String> excludedProductIds, Consumer<LineItem> listener) {

    public void confirmChanges(LineItem updated) { listener.accept(updated); }
}
