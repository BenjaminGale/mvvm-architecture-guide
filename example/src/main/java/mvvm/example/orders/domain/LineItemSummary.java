package mvvm.example.orders.domain;

import java.math.BigDecimal;

public record LineItemSummary(String productId, String productName, int quantity, BigDecimal unitPrice, int allocatedQuantity) {

    public BigDecimal total() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
