package mvvm.example.orders.editor.edititem;

import mvvm.example.orders.domain.LineItem;

import java.util.function.Consumer;

public class EditItemSession {

    private final LineItem item;
    private final Consumer<LineItem> onConfirmed;

    public EditItemSession(LineItem item, Consumer<LineItem> onConfirmed) {
        this.item = item;
        this.onConfirmed = onConfirmed;
    }

    public LineItem getItem() {
        return item;
    }

    public void confirmChanges(LineItem updated) {
        onConfirmed.accept(updated);
    }
}
