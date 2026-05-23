package mvvm.example.stock.domain;

import java.util.List;
import java.util.UUID;

public interface StockRepository {
    List<StockAllocation> findByOrderId(UUID orderId);
    List<StockAllocation> findByProductId(UUID productId);
    void save(StockAllocation allocation);
    void delete(UUID id);
}
