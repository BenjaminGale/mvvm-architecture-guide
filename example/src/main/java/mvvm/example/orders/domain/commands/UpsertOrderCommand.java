package mvvm.example.orders.domain.commands;

import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.orders.domain.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class UpsertOrderCommand {

    private final OrderRepository orderRepository;

    public UpsertOrderCommand(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public String execute(String orderId, String customerId, String reference, LocalDate plannedShipDate, List<LineItem> lineItems) {
        return orderId == null
            ? insert(customerId, reference, plannedShipDate, lineItems)
            : update(orderId, customerId, reference, plannedShipDate, lineItems);
    }

    private String insert(String customerId, String reference, LocalDate plannedShipDate, List<LineItem> lineItems) {
        var order = new Order(UUID.randomUUID().toString(), customerId, LocalDate.now(), plannedShipDate, reference, OrderStatus.PENDING, null, lineItems);
        orderRepository.save(order);
        return order.id();
    }

    private String update(String orderId, String customerId, String reference, LocalDate plannedShipDate, List<LineItem> lineItems) {
        var existing = orderRepository
            .findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        var updated = new Order(existing.id(), customerId, existing.createdDate(), plannedShipDate, reference, existing.status(), existing.completionDate(), lineItems);
        orderRepository.save(updated);
        return updated.id();
    }
}
