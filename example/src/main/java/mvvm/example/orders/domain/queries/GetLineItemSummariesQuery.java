package mvvm.example.orders.domain.queries;

import mvvm.example.orders.domain.LineItem;
import mvvm.example.stock.domain.Product;
import mvvm.example.stock.domain.ProductRepository;
import mvvm.example.stock.domain.StockAllocation;
import mvvm.example.stock.domain.StockRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class GetLineItemSummariesQuery {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    public GetLineItemSummariesQuery(ProductRepository productRepository, StockRepository stockRepository) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
    }

    public CompletableFuture<List<LineItemSummary>> execute(List<LineItem> items, String orderId) {
        var productNames = productRepository
            .findAll()
            .stream()
            .collect(Collectors.toMap(Product::id, Product::name));

        List<StockAllocation> allocations = orderId != null
            ? stockRepository.findByOrderId(orderId)
            : List.of();

        var allocatedByProduct = allocations
            .stream()
            .collect(
                Collectors.groupingBy(
                    StockAllocation::productId,
                    Collectors.summingInt(StockAllocation::quantity)
                )
            );

        var results = items
            .stream()
            .map(item -> new LineItemSummary(
                item.productId(),
                productNames.getOrDefault(item.productId(), item.description()),
                item.quantity(),
                item.unitPrice(),
                allocatedByProduct.getOrDefault(item.productId(), 0)
            ))
            .toList();

        return CompletableFuture.completedFuture(results);
    }
}
