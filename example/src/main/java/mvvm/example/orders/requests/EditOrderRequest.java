package mvvm.example.orders.requests;

public final class EditOrderRequest {
    private final String orderId;

    private EditOrderRequest(String orderId) {
        this.orderId = orderId;
    }

    public static EditOrderRequest of(String orderId) {
        return new EditOrderRequest(orderId);
    }

    public static EditOrderRequest forNewOrder() {
        return new EditOrderRequest(null);
    }

    public String orderId() {
        return orderId;
    }

    public boolean isNew() {
        return orderId == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EditOrderRequest that)) return false;
        return java.util.Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(orderId);
    }
}
