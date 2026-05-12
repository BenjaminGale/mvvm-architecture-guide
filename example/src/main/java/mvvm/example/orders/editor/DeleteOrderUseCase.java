package mvvm.example.orders.editor;

import mvvm.example.orders.Order;
import mvvm.example.orders.OrderService;

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
