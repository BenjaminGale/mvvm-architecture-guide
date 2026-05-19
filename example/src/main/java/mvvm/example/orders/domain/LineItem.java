package mvvm.example.orders.domain;

import java.math.BigDecimal;

public record LineItem(String productId, String description, int quantity, BigDecimal unitPrice) {

    public BigDecimal total() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public static LineItem empty() {
        return new LineItem(null, "", 1, BigDecimal.ZERO);
    }
}
