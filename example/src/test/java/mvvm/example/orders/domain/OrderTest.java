package mvvm.example.orders.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Orders.Order")
class OrderTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate YESTERDAY = TODAY.minusDays(1);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);

    private static PendingOrder pendingOrder(LocalDate plannedShipDate) {
        return new PendingOrder("id", "cust-1", "Acme", TODAY, plannedShipDate, "REF-001", List.of());
    }

    private static FulfilledOrder fulfilledOrder(LocalDate plannedShipDate) {
        return new FulfilledOrder("id", "cust-1", "Acme", TODAY, plannedShipDate, "REF-001", List.of());
    }

    @Nested
    @DisplayName("isOverdue")
    class IsOverdue {

        @Test
        @DisplayName("pending order with past ship date is overdue")
        void pendingOrderWithPastShipDateIsOverdue() {
            assertTrue(pendingOrder(YESTERDAY).isOverdue());
        }

        @Test
        @DisplayName("pending order with future ship date is not overdue")
        void pendingOrderWithFutureShipDateIsNotOverdue() {
            assertFalse(pendingOrder(TOMORROW).isOverdue());
        }

        @Test
        @DisplayName("fulfilled order with past ship date is overdue")
        void fulfilledOrderWithPastShipDateIsOverdue() {
            assertTrue(fulfilledOrder(YESTERDAY).isOverdue());
        }

        @Test
        @DisplayName("fulfilled order with future ship date is not overdue")
        void fulfilledOrderWithFutureShipDateIsNotOverdue() {
            assertFalse(fulfilledOrder(TOMORROW).isOverdue());
        }

        @Test
        @DisplayName("shipped order is never overdue")
        void shippedOrderIsNeverOverdue() {
            var order = new ShippedOrder("id", "cust-1", "Acme", TODAY, YESTERDAY, "REF-001", TODAY, List.of());
            assertFalse(order.isOverdue());
        }

        @Test
        @DisplayName("cancelled order is never overdue")
        void cancelledOrderIsNeverOverdue() {
            var order = new CancelledOrder("id", "cust-1", "Acme", TODAY, YESTERDAY, "REF-001", TODAY, List.of());
            assertFalse(order.isOverdue());
        }
    }

    @Nested
    @DisplayName("total")
    class Total {

        @Test
        @DisplayName("returns zero for an order with no line items")
        void zeroForNoLineItems() {
            assertEquals(BigDecimal.ZERO, pendingOrder(TOMORROW).total());
        }

        @Test
        @DisplayName("returns the sum of all line item totals")
        void sumOfLineItemTotals() {
            var order = new PendingOrder("id", "cust-1", "Acme", TODAY, TOMORROW, "REF-001", List.of(
                new LineItem("Widget A", 2, 0, new BigDecimal("10.00")),
                new LineItem("Widget B", 3, 0, new BigDecimal("5.00"))
            ));

            assertEquals(new BigDecimal("35.00"), order.total());
        }
    }
}
