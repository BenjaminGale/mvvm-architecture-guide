package mvvm.example.orders.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record Order(String id, String customerName, LocalDate date, String reference, List<LineItem> lineItems) {

    public record Header(String customerName, LocalDate date, String reference) {
    }

    public Order(String id, String customerName, LocalDate date, String reference, List<LineItem> lineItems) {
        this.id = id;
        this.customerName = customerName;
        this.date = date;
        this.reference = reference;
        this.lineItems = List.copyOf(lineItems);
    }

    public BigDecimal total() {
        return lineItems
            .stream()
            .map(LineItem::total)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isOverdue() {
        return date.isBefore(LocalDate.now().minusDays(30));
    }

    public boolean isValid() {
        return customerName != null
            && !customerName.isBlank()
            && date != null
            && reference != null
            && !reference.isBlank()
            && !lineItems.isEmpty();
    }

    public Order withHeader(Header header) {
        return new Order(id, header.customerName(), header.date(), header.reference(), lineItems);
    }

    public Order withLineItems(List<LineItem> newItems) {
        return new Order(id, customerName, date, reference, newItems);
    }
}
