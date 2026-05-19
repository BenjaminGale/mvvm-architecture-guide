package mvvm.example.orders.domain;

import java.time.LocalDate;
import java.util.List;

public record ShippedOrder(String id, String customerId, LocalDate createdDate, LocalDate plannedShipDate, String reference, LocalDate completionDate, List<LineItem> lineItems) implements Order {

    public ShippedOrder {
        lineItems = List.copyOf(lineItems);
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.SHIPPED;
    }

    @Override
    public Order withLineItems(List<LineItem> newItems) {
        return new ShippedOrder(id, customerId, createdDate, plannedShipDate, reference, completionDate, newItems);
    }
}
