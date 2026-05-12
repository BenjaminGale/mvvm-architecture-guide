package mvvm.example.orders.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Order {

    public record OrderHeader(String customerName, LocalDate date, String reference) {}

    private final String id;
    private final String customerName;
    private final LocalDate date;
    private final String reference;
    private final List<LineItem> lineItems;

    public Order(String id, String customerName, LocalDate date, String reference, List<LineItem> lineItems) {
        this.id = id;
        this.customerName = customerName;
        this.date = date;
        this.reference = reference;
        this.lineItems = List.copyOf(lineItems);
    }

    public String id()               { return id; }
    public String customerName()     { return customerName; }
    public LocalDate date()          { return date; }
    public String reference()        { return reference; }
    public List<LineItem> lineItems() { return lineItems; }

    public BigDecimal total() {
        return lineItems.stream()
            .map(LineItem::total)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isOverdue() {
        return date.isBefore(LocalDate.now().minusDays(30));
    }

    public boolean isValid() {
        return customerName != null && !customerName.isBlank()
            && date != null
            && reference != null && !reference.isBlank()
            && !lineItems.isEmpty();
    }

    public Order withHeader(OrderHeader header) {
        return new Order(id, header.customerName(), header.date(), header.reference(), lineItems);
    }

    public Order withLineItems(List<LineItem> newItems) {
        return new Order(id, customerName, date, reference, newItems);
    }
}
