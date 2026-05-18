package mvvm.example.orders.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record Order(String id, String customerId, String customerName, LocalDate date, String reference, OrderStatus status, LocalDate completionDate, List<LineItem> lineItems) {

    public record Header(String customerId, String customerName, LocalDate date, String reference) {
    }

    public Order {
        lineItems = List.copyOf(lineItems);
    }

    @Deprecated
    public Order(String id, String customerName, LocalDate date, String reference, List<LineItem> lineItems) {
        this(id, null, customerName, date, reference, OrderStatus.WIP, null, lineItems);
    }

    public BigDecimal total() {
        return lineItems
            .stream()
            .map(LineItem::total)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isOverdue() {
        return status != OrderStatus.SHIPPED
            && status != OrderStatus.CANCELLED
            && date.isBefore(LocalDate.now().minusDays(30));
    }

    public boolean isValid() {
        return customerId != null
            && !customerId.isBlank()
            && date != null
            && reference != null
            && !reference.isBlank()
            && !lineItems.isEmpty();
    }

    public Order withHeader(Header header) {
        return new Order(id, header.customerId(), header.customerName(), header.date(), header.reference(), status, completionDate, lineItems);
    }

    public Order withLineItems(List<LineItem> newItems) {
        return new Order(id, customerId, customerName, date, reference, status, completionDate, newItems);
    }

    public Order withStatus(OrderStatus newStatus) {
        return new Order(id, customerId, customerName, date, reference, newStatus, completionDate, lineItems);
    }

    public Order withCompletionDate(LocalDate newCompletionDate) {
        return new Order(id, customerId, customerName, date, reference, status, newCompletionDate, lineItems);
    }
}
