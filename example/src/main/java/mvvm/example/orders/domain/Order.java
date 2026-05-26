package mvvm.example.orders.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record Order(
    UUID id,
    UUID customerId,
    LocalDate createdDate,
    LocalDate plannedShipDate,
    String reference,
    OrderStatus status,
    LocalDate completionDate,
    List<LineItem> lineItems
) {
    public Order {
        lineItems = List.copyOf(lineItems);
    }

    public BigDecimal total() {
        return lineItems.stream().map(LineItem::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isOverdue() {
        return status == OrderStatus.IN_PROGRESS
            && plannedShipDate != null
            && plannedShipDate.isBefore(LocalDate.now());
    }

    public Order withCustomerId(UUID customerId) {
        return new Order(id, customerId, createdDate, plannedShipDate, reference, status, completionDate, lineItems);
    }

    public Order withPlannedShipDate(LocalDate plannedShipDate) {
        return new Order(id, customerId, createdDate, plannedShipDate, reference, status, completionDate, lineItems);
    }

    public Order withReference(String reference) {
        return new Order(id, customerId, createdDate, plannedShipDate, reference, status, completionDate, lineItems);
    }

    public Order withLineItems(List<LineItem> lineItems) {
        return new Order(id, customerId, createdDate, plannedShipDate, reference, status, completionDate, lineItems);
    }

    public static Order create(UUID customerId, String reference, LocalDate plannedShipDate, List<LineItem> lineItems) {
        return new Order(UUID.randomUUID(), customerId, LocalDate.now(), plannedShipDate, reference, OrderStatus.IN_PROGRESS, null, lineItems);
    }

    public static Order draft() {
        return new Order(UUID.randomUUID(), null, LocalDate.now(), null, "", OrderStatus.IN_PROGRESS, null, List.of());
    }
}
