package mvvm.example.orders.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record Order(String id, String customerId, String customerName, LocalDate createdDate, LocalDate plannedShipDate, String reference, OrderStatus status, LocalDate completionDate, List<LineItem> lineItems) {

    public record Header(String customerId, String customerName, LocalDate plannedShipDate, String reference) {
    }

    public Order {
        lineItems = List.copyOf(lineItems);
    }

    @Deprecated
    public Order(String id, String customerName, LocalDate createdDate, String reference, List<LineItem> lineItems) {
        this(id, null, customerName, createdDate, null, reference, OrderStatus.PENDING, null, lineItems);
    }

    public static Order empty() {
        return new Order(
            UUID.randomUUID().toString(),
            null,
            "",
            LocalDate.now(),
            LocalDate.now(),
            "",
            OrderStatus.PENDING,
            null,
            new ArrayList<>()
        );
    }

    public BigDecimal total() {
        return lineItems
            .stream()
            .map(LineItem::total)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isOverdue() {
        return plannedShipDate != null
            && status != OrderStatus.SHIPPED
            && status != OrderStatus.CANCELLED
            && plannedShipDate.isBefore(LocalDate.now());
    }

    public boolean isValid() {
        return customerId != null
            && !customerId.isBlank()
            && reference != null
            && !reference.isBlank()
            && !lineItems.isEmpty();
    }

    public Order withHeader(Header header) {
        return new Order(id, header.customerId(), header.customerName(), createdDate, header.plannedShipDate(), header.reference(), status, completionDate, lineItems);
    }

    public Order withLineItems(List<LineItem> newItems) {
        return new Order(id, customerId, customerName, createdDate, plannedShipDate, reference, status, completionDate, newItems);
    }

    public Order withStatus(OrderStatus newStatus) {
        return new Order(id, customerId, customerName, createdDate, plannedShipDate, reference, newStatus, completionDate, lineItems);
    }

    public Order withCompletionDate(LocalDate newCompletionDate) {
        return new Order(id, customerId, customerName, createdDate, plannedShipDate, reference, status, newCompletionDate, lineItems);
    }
}
