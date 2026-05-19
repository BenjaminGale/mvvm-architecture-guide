package mvvm.example.core.config.adapters;

import mvvm.example.stock.domain.StockAllocation;
import mvvm.example.stock.domain.StockAllocationRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InMemoryStockAllocationRepository implements StockAllocationRepository {

    private final Map<UUID, StockAllocation> store = new HashMap<>();

    @Override
    public List<StockAllocation> findByOrderId(String orderId) {
        return store.values().stream()
            .filter(a -> a.orderId().equals(orderId))
            .toList();
    }

    @Override
    public List<StockAllocation> findByProductId(String productId) {
        return store.values().stream()
            .filter(a -> a.productId().equals(productId))
            .toList();
    }

    @Override
    public void save(StockAllocation allocation) {
        store.put(allocation.id(), allocation);
    }

    @Override
    public void delete(UUID id) {
        store.remove(id);
    }
}
