package mvvm.example.stock.domain;

import java.util.UUID;

public record StockAllocation(UUID id, String productId, String orderId, int quantity) {

    public static StockAllocation create(String productId, String orderId, int quantity) {
        return new StockAllocation(UUID.randomUUID(), productId, orderId, quantity);
    }
}
