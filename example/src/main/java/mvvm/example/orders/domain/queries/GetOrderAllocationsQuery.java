package mvvm.example.orders.domain.queries;

import mvvm.example.stock.domain.StockAllocation;
import mvvm.example.stock.domain.StockRepository;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetOrderAllocationsQuery {

    private final StockRepository stockRepository;

    public GetOrderAllocationsQuery(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public Map<UUID, Integer> execute(UUID orderId) {
        return stockRepository.findByOrderId(orderId).stream()
            .collect(Collectors.groupingBy(
                StockAllocation::productId,
                Collectors.summingInt(StockAllocation::quantity)
            ));
    }
}
