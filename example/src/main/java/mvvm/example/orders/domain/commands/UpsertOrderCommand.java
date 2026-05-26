package mvvm.example.orders.domain.commands;

import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class UpsertOrderCommand {

    private final OrderRepository orderRepository;

    public UpsertOrderCommand(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public UUID execute(UUID orderId, UUID customerId, String reference, LocalDate plannedShipDate, List<LineItem> lineItems) {
        return orderId == null
            ? insert(customerId, reference, plannedShipDate, lineItems)
            : update(orderId, customerId, reference, plannedShipDate, lineItems);
    }

    private UUID insert(UUID customerId, String reference, LocalDate plannedShipDate, List<LineItem> lineItems) {
        var order = Order.create(customerId, reference, plannedShipDate, lineItems);
        orderRepository.save(order);
        return order.id();
    }

    private UUID update(UUID orderId, UUID customerId, String reference, LocalDate plannedShipDate, List<LineItem> lineItems) {
        var existing = orderRepository
            .findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        var updated = existing
            .withCustomerId(customerId)
            .withReference(reference)
            .withPlannedShipDate(plannedShipDate)
            .withLineItems(lineItems);
        orderRepository.save(updated);
        return updated.id();
    }
}
