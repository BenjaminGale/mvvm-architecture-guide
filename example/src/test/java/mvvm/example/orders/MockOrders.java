package mvvm.example.orders;

import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerStatus;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MockOrders {
    private MockOrders() {}

    private static final LocalDate A_DATE = LocalDate.of(2025, 1, 15);
    private static final LineItem A_LINE_ITEM = new LineItem("Widget", 1, BigDecimal.TEN);

    public static final String ACME_CUSTOMER_ID = "cust-1";
    public static final Customer ACME_CUSTOMER = new Customer(ACME_CUSTOMER_ID, "Acme Ltd", "acme@example.com", CustomerStatus.ACTIVE);

    public static Order of(String id, LocalDate date) {
        return new Order(id, ACME_CUSTOMER_ID, "Acme Ltd", date, "REF-" + id, OrderStatus.WIP, null, List.of());
    }

    public static Order validOrderWithLineItems() {
        return new Order("id-1", ACME_CUSTOMER_ID, "Acme Ltd", A_DATE, "REF-001", OrderStatus.WIP, null, List.of(A_LINE_ITEM));
    }

    public static Order orderWithNoCustomer() {
        return new Order("id-1", null, null, A_DATE, "REF-001", OrderStatus.WIP, null, List.of(A_LINE_ITEM));
    }

    public static Order orderWithNoLineItems() {
        return new Order("id-1", ACME_CUSTOMER_ID, "Acme Ltd", A_DATE, "REF-001", OrderStatus.WIP, null, List.of());
    }
}
