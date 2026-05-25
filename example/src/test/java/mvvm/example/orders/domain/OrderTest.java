package mvvm.example.orders.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Orders.Order")
class OrderTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate YESTERDAY = TODAY.minusDays(1);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);
    private static final UUID AN_ID = UUID.randomUUID();
    private static final UUID A_CUST = UUID.randomUUID();

    private static Order order(OrderStatus status, LocalDate plannedShipDate) {
        return new Order(AN_ID, A_CUST, TODAY, plannedShipDate, "REF-001", status, null, List.of());
    }

    @Nested
    @DisplayName("isOverdue")
    class IsOverdue {

        @Test
        @DisplayName("pending order with past ship date is overdue")
        void pendingOrderWithPastShipDateIsOverdue() {
            assertTrue(order(OrderStatus.IN_PROGRESS, YESTERDAY).isOverdue());
        }

        @Test
        @DisplayName("pending order with future ship date is not overdue")
        void pendingOrderWithFutureShipDateIsNotOverdue() {
            assertFalse(order(OrderStatus.IN_PROGRESS, TOMORROW).isOverdue());
        }

        @Test
        @DisplayName("in progress order with null ship date is not overdue")
        void inProgressOrderWithNullShipDateIsNotOverdue() {
            assertFalse(order(OrderStatus.IN_PROGRESS, null).isOverdue());
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
            assertEquals(BigDecimal.ZERO, order(OrderStatus.IN_PROGRESS, TOMORROW).total());
        }

        @Test
        @DisplayName("returns the sum of all line item totals")
        void sumOfLineItemTotals() {
            var order = new Order(AN_ID, A_CUST, TODAY, TOMORROW, "REF-001", OrderStatus.IN_PROGRESS, null, List.of(
                new LineItem(null, "Widget A", 2, new BigDecimal("10.00")),
                new LineItem(null, "Widget B", 3, new BigDecimal("5.00"))
            ));

            assertEquals(new BigDecimal("35.00"), order.total());
        }
    }
}
