package mvvm.example.stock.domain;

import java.util.List;
import java.util.UUID;

public interface StockRepository {
    List<StockAllocation> findByOrderId(String orderId);
    List<StockAllocation> findByProductId(String productId);
    void save(StockAllocation allocation);
    void delete(UUID id);
}
