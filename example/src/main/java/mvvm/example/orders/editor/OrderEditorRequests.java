package mvvm.example.orders.editor;

import mvvm.example.orders.domain.Order;
import java.util.function.Consumer;

public record OrderEditorRequests(
    Runnable onSaved,
    Consumer<Order> onCopied,
    Runnable onDeleted
) {}
