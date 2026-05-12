package mvvm.example.orders.editor.usecases;

import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderService;

public class DeleteOrderUseCase {

    private final OrderService orderService;
    private final Runnable onDeleted;

    public DeleteOrderUseCase(OrderService orderService, Runnable onDeleted) {
        this.orderService = orderService;
        this.onDeleted = onDeleted;
    }

    public void execute(Order order) {
        orderService.delete(order.id());
        onDeleted.run();
    }
}
