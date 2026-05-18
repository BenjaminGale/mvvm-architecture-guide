package mvvm.example.orders.domain;

public enum OrderStatus {
    PENDING("Pending"),
    FULFILLED("Fulfilled"),
    SHIPPED("Shipped"),
    CANCELLED("Cancelled");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
