package mvvm.example.orders.domain;

import mvvm.example.stock.domain.StockAllocation;
import mvvm.example.stock.domain.StockRepository;

import java.util.Map;
import java.util.stream.Collectors;

public class GetOrderAllocationsQuery {

    private final StockRepository stockRepository;

    public GetOrderAllocationsQuery(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public Map<String, Integer> execute(String orderId) {
        return stockRepository.findByOrderId(orderId).stream()
            .collect(Collectors.groupingBy(
                StockAllocation::productId,
                Collectors.summingInt(StockAllocation::quantity)
            ));
    }
}
