package mvvm.example.orders.editor.edititem;

import mvvm.example.orders.domain.LineItem;

import java.util.function.Consumer;

public class EditItemRequest {

    private final LineItem item;
    private final Consumer<LineItem> listener;

    public EditItemRequest(LineItem original, Consumer<LineItem> listener) {
        this.item = original;
        this.listener = listener;
    }

    public LineItem getItem() {
        return item;
    }

    public void confirmChanges(LineItem updated) {
        listener.accept(updated);
    }
}
