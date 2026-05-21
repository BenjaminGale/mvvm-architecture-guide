package mvvm.example.orders.editor;

public record OrderEditorRequest(String orderId) {

    public static OrderEditorRequest of(String orderId) {
        return new OrderEditorRequest(orderId);
    }

    public static OrderEditorRequest forNewOrder() {
        return new OrderEditorRequest(null);
    }

    public boolean isNew() {
        return orderId == null;
    }
}
