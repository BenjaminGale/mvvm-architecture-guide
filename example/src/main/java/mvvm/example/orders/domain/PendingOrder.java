package mvvm.example.orders.domain;

import java.time.LocalDate;
import java.util.List;

public record PendingOrder(String id, String customerId, String customerName, LocalDate createdDate, LocalDate plannedShipDate, String reference, List<LineItem> lineItems) implements Order {

    public PendingOrder {
        lineItems = List.copyOf(lineItems);
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.PENDING;
    }

    @Override
    public boolean isOverdue() {
        return plannedShipDate != null && plannedShipDate.isBefore(LocalDate.now());
    }

    @Override
    public Order withLineItems(List<LineItem> newItems) {
        return new PendingOrder(id, customerId, customerName, createdDate, plannedShipDate, reference, newItems);
    }
}
