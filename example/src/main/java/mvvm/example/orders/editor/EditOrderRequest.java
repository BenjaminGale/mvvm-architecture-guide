package mvvm.example.orders.editor;

public record EditOrderRequest(String orderId) {

    public static EditOrderRequest of(String orderId) {
        return new EditOrderRequest(orderId);
    }

    public static EditOrderRequest forNewOrder() {
        return new EditOrderRequest(null);
    }

    public boolean isNew() {
        return orderId == null;
    }
}
