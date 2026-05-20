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

    private static Order order(OrderStatus status, LocalDate plannedShipDate) {
        return new Order("id", "cust-1", TODAY, plannedShipDate, "REF-001", status, null, List.of());
    }

    @Nested
    @DisplayName("isOverdue")
    class IsOverdue {

        @Test
        @DisplayName("pending order with past ship date is overdue")
        void pendingOrderWithPastShipDateIsOverdue() {
            assertTrue(order(OrderStatus.PENDING, YESTERDAY).isOverdue());
        }

        @Test
        @DisplayName("pending order with future ship date is not overdue")
        void pendingOrderWithFutureShipDateIsNotOverdue() {
            assertFalse(order(OrderStatus.PENDING, TOMORROW).isOverdue());
        }

        @Test
        @DisplayName("fulfilled order with past ship date is overdue")
        void fulfilledOrderWithPastShipDateIsOverdue() {
            assertTrue(order(OrderStatus.FULFILLED, YESTERDAY).isOverdue());
        }

        @Test
        @DisplayName("fulfilled order with future ship date is not overdue")
        void fulfilledOrderWithFutureShipDateIsNotOverdue() {
            assertFalse(order(OrderStatus.FULFILLED, TOMORROW).isOverdue());
        }

        @Test
        @DisplayName("shipped order is never overdue")
        void shippedOrderIsNeverOverdue() {
            assertFalse(order(OrderStatus.SHIPPED, YESTERDAY).isOverdue());
        }

        @Test
        @DisplayName("cancelled order is never overdue")
        void cancelledOrderIsNeverOverdue() {
            assertFalse(order(OrderStatus.CANCELLED, YESTERDAY).isOverdue());
        }
    }

    @Nested
    @DisplayName("total")
    class Total {

        @Test
        @DisplayName("returns zero for an order with no line items")
        void zeroForNoLineItems() {
            assertEquals(BigDecimal.ZERO, order(OrderStatus.PENDING, TOMORROW).total());
        }

        @Test
        @DisplayName("returns the sum of all line item totals")
        void sumOfLineItemTotals() {
            var order = new Order("id", "cust-1", TODAY, TOMORROW, "REF-001", OrderStatus.PENDING, null, List.of(
                new LineItem(null, "Widget A", 2, new BigDecimal("10.00")),
                new LineItem(null, "Widget B", 3, new BigDecimal("5.00"))
            ));

            assertEquals(new BigDecimal("35.00"), order.total());
        }
    }
}
