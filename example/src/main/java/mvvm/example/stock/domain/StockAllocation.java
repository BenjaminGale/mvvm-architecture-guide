package mvvm.example.stock.domain;

import java.util.UUID;

public record StockAllocation(UUID id, UUID productId, UUID orderId, int quantity) {

    public static StockAllocation create(UUID productId, UUID orderId, int quantity) {
        return new StockAllocation(UUID.randomUUID(), productId, orderId, quantity);
    }
}
