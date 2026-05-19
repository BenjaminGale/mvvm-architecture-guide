package mvvm.example.orders.domain;

import java.time.LocalDate;
import java.util.List;

public record FulfilledOrder(String id, String customerId, LocalDate createdDate, LocalDate plannedShipDate, String reference, List<LineItem> lineItems) implements Order {

    public FulfilledOrder {
        lineItems = List.copyOf(lineItems);
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.FULFILLED;
    }

    @Override
    public boolean isOverdue() {
        return plannedShipDate != null && plannedShipDate.isBefore(LocalDate.now());
    }

    @Override
    public Order withLineItems(List<LineItem> newItems) {
        return new FulfilledOrder(id, customerId, createdDate, plannedShipDate, reference, newItems);
    }
}
