package mvvm.example.orders.editor.lineitems;

import mvvm.example.orders.domain.LineItem;

import java.util.List;
import java.util.function.Consumer;

public record LineItemEditorRequest(
    LineItem item,
    List<LineItem> currentLineItems,
    Consumer<LineItem> onConfirmed
) {
    public void confirmChanges(LineItem updated) { onConfirmed.accept(updated); }
}
