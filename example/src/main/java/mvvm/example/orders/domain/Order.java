package mvvm.example.orders.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public sealed interface Order permits PendingOrder, FulfilledOrder, ShippedOrder, CancelledOrder {

    String id();
    String customerId();
    String customerName();
    LocalDate createdDate();
    LocalDate plannedShipDate();
    String reference();
    List<LineItem> lineItems();
    OrderStatus status();

    default BigDecimal total() {
        return lineItems().stream().map(LineItem::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default boolean isOverdue() {
        return false;
    }

    Order withLineItems(List<LineItem> newItems);

    static Order empty() {
        return new PendingOrder(
            UUID.randomUUID().toString(),
            null,
            "",
            LocalDate.now(),
            LocalDate.now(),
            "",
            new ArrayList<>()
        );
    }
}
