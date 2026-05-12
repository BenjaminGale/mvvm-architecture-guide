package mvvm.example.orders.editor.usecases;

import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderService;

import java.util.function.Consumer;

public class CopyOrderUseCase {

    private final OrderService orderService;
    private final Consumer<Order> onCopied;

    public CopyOrderUseCase(OrderService orderService, Consumer<Order> onCopied) {
        this.orderService = orderService;
        this.onCopied = onCopied;
    }

    public void execute(Order order) {
        var copy = orderService.copy(order.id());
        onCopied.accept(copy);
    }
}
