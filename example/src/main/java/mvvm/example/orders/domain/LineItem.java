package mvvm.example.orders.domain;

import java.math.BigDecimal;

public record LineItem(String productId, String description, int quantity, int quantityAllocated, BigDecimal unitPrice) {

    @Deprecated
    public LineItem(String description, int quantity, BigDecimal unitPrice) {
        this(null, description, quantity, 0, unitPrice);
    }

    public BigDecimal total() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean isFullyAllocated() {
        return quantityAllocated >= quantity;
    }

    public static LineItem empty() {
        return new LineItem(null, "", 1, 0, BigDecimal.ZERO);
    }
}
