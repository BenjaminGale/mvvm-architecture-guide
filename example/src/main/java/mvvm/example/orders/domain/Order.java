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
        return (status == OrderStatus.PENDING || status == OrderStatus.FULFILLED)
            && plannedShipDate != null
            && plannedShipDate.isBefore(LocalDate.now());
    }

    public static Order empty() {
        return new Order(
            UUID.randomUUID(),
            null,
            LocalDate.now(),
            null,
            "",
            OrderStatus.PENDING,
            null,
            List.of()
        );
    }
}
