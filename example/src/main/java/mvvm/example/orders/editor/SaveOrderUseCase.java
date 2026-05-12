package mvvm.example.orders.editor;

import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderService;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class SaveOrderUseCase {

    private final OrderService orderService;
    private final Supplier<Order> orderSupplier;
    private final Runnable onSaved;

    public SaveOrderUseCase(OrderService orderService, Supplier<Order> orderSupplier, Runnable onSaved) {
        this.orderService = orderService;
        this.orderSupplier = orderSupplier;
        this.onSaved = onSaved;
    }

    public CompletableFuture<Runnable> execute() {
        return CompletableFuture.supplyAsync(() -> {
            orderService.save(orderSupplier.get());
            return onSaved;
        });
    }
}
