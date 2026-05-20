package mvvm.example.orders.domain.commands;

import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.orders.domain.OrderStatus;
import mvvm.example.stock.domain.StockRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class UpsertOrderCommand {

    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;

    public UpsertOrderCommand(OrderRepository orderRepository, StockRepository stockRepository) {
        this.orderRepository = orderRepository;
        this.stockRepository = stockRepository;
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

        stockRepository.findByOrderId(orderId).forEach(a -> stockRepository.delete(a.id()));

        var updated = new Order(existing.id(), customerId, existing.createdDate(), plannedShipDate, reference, existing.status(), existing.completionDate(), lineItems);
        orderRepository.save(updated);
        return updated.id();
    }
}
