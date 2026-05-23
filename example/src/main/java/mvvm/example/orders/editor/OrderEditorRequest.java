package mvvm.example.orders.editor;

import java.util.UUID;

public record OrderEditorRequest(UUID orderId) {

    public static OrderEditorRequest of(UUID orderId) {
        return new OrderEditorRequest(orderId);
    }

    public static OrderEditorRequest forNewOrder() {
        return new OrderEditorRequest(null);
    }

    public boolean isNew() {
        return orderId == null;
    }
}
