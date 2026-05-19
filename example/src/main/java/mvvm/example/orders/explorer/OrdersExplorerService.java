package mvvm.example.orders.explorer;

import mvvm.example.orders.domain.OrderSummary;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface OrdersExplorerService {
    CompletableFuture<List<OrderSummary>> fetchOrderSummaries();
}
