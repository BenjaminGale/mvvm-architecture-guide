package mvvm.example.orders.domain;

import java.time.LocalDate;
import java.util.List;

public record CancelledOrder(String id, String customerId, LocalDate createdDate, LocalDate plannedShipDate, String reference, LocalDate completionDate, List<LineItem> lineItems) implements Order {

    public CancelledOrder {
        lineItems = List.copyOf(lineItems);
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.CANCELLED;
    }

    @Override
    public Order withLineItems(List<LineItem> newItems) {
        return new CancelledOrder(id, customerId, createdDate, plannedShipDate, reference, completionDate, newItems);
    }
}
