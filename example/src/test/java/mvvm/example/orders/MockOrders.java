package mvvm.example.orders;

import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerStatus;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderStatus;
import mvvm.example.orders.domain.queries.OrderSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class MockOrders {
    private MockOrders() {}

    private static final LocalDate A_DATE = LocalDate.of(2025, 1, 15);

    public static final UUID ACME_CUSTOMER_ID = UUID.randomUUID();
    public static final UUID A_PRODUCT_ID = UUID.randomUUID();

    private static final LineItem A_LINE_ITEM = new LineItem(A_PRODUCT_ID, "Widget", 1, BigDecimal.TEN);

    public static final Customer ACME_CUSTOMER = new Customer(ACME_CUSTOMER_ID, "Acme Ltd", "acme@example.com", CustomerStatus.ACTIVE);

    public static Order of(UUID id, LocalDate date) {
        return new Order(id, ACME_CUSTOMER_ID, date, date, "REF-" + id, OrderStatus.IN_PROGRESS, null, List.of());
    }

    public static Order validOrderWithLineItems() {
        return new Order(UUID.randomUUID(), ACME_CUSTOMER_ID, A_DATE, A_DATE, "REF-001", OrderStatus.IN_PROGRESS, null, List.of(A_LINE_ITEM));
    }

    public static Order orderWithNoCustomer() {
        return new Order(UUID.randomUUID(), null, A_DATE, A_DATE, "REF-001", OrderStatus.IN_PROGRESS, null, List.of(A_LINE_ITEM));
    }

    public static Order orderWithNoLineItems() {
        return new Order(UUID.randomUUID(), ACME_CUSTOMER_ID, A_DATE, A_DATE, "REF-001", OrderStatus.IN_PROGRESS, null, List.of());
    }

    public static OrderSummary summaryOf(UUID id, LocalDate date) {
        boolean overdue = date.isBefore(LocalDate.now());
        return new OrderSummary(id, "REF-" + id, "Acme Ltd", date, date, "In Progress", BigDecimal.ZERO, overdue);
    }
}
