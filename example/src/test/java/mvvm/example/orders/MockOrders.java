package mvvm.example.orders;

import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MockOrders {
    private MockOrders() {}

    private static final LocalDate A_DATE = LocalDate.of(2025, 1, 15);
    private static final LineItem A_LINE_ITEM = new LineItem("Widget", 1, BigDecimal.TEN);

    public static Order validOrderWithLineItems() {
        return new Order("id-1", "Acme Ltd", A_DATE, "REF-001", List.of(A_LINE_ITEM));
    }

    public static Order orderWithBlankCustomerName() {
        return new Order("id-1", "", A_DATE, "REF-001", List.of(A_LINE_ITEM));
    }

    public static Order orderWithNoLineItems() {
        return new Order("id-1", "Acme Ltd", A_DATE, "REF-001", List.of());
    }
}
