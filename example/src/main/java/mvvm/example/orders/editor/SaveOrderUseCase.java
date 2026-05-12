package mvvm.example.orders.editor;

import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderService;

import java.util.concurrent.CompletableFuture;

public class SaveOrderUseCase {

    private final OrderService orderService;
    private final Runnable onSaved;

    public SaveOrderUseCase(OrderService orderService, Runnable onSaved) {
        this.orderService = orderService;
        this.onSaved = onSaved;
    }

    public CompletableFuture<Runnable> execute(Order order) {
        return CompletableFuture.supplyAsync(() -> {
            orderService.save(order);
            return onSaved;
        });
    }
}
