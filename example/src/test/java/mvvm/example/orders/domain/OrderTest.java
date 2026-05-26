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
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("sets status to IN_PROGRESS")
        void setsStatusToInProgress() {
            var order = Order.create(A_CUST, "REF-001", TOMORROW, List.of());
            assertEquals(OrderStatus.IN_PROGRESS, order.status());
        }

        @Test
        @DisplayName("sets created date to today")
        void setsCreatedDateToToday() {
            var order = Order.create(A_CUST, "REF-001", TOMORROW, List.of());
            assertEquals(TODAY, order.createdDate());
        }

        @Test
        @DisplayName("has no completion date")
        void hasNoCompletionDate() {
            var order = Order.create(A_CUST, "REF-001", TOMORROW, List.of());
            assertNull(order.completionDate());
        }

        @Test
        @DisplayName("preserves supplied fields")
        void preservesSuppliedFields() {
            var items = List.of(new LineItem(null, "Widget", 1, BigDecimal.TEN));
            var order = Order.create(A_CUST, "REF-001", TOMORROW, items);
            assertAll(
                () -> assertEquals(A_CUST, order.customerId()),
                () -> assertEquals("REF-001", order.reference()),
                () -> assertEquals(TOMORROW, order.plannedShipDate()),
                () -> assertEquals(items, order.lineItems())
            );
        }
    }

    @Nested
    @DisplayName("with methods")
    class WithMethods {

        @Test
        @DisplayName("withCustomerId returns new order with updated customer")
        void withCustomerIdUpdatesCustomer() {
            var newCustomer = UUID.randomUUID();
            var updated = order(OrderStatus.IN_PROGRESS, TOMORROW).withCustomerId(newCustomer);
            assertEquals(newCustomer, updated.customerId());
        }

        @Test
        @DisplayName("withReference returns new order with updated reference")
        void withReferenceUpdatesReference() {
            var updated = order(OrderStatus.IN_PROGRESS, TOMORROW).withReference("REF-999");
            assertEquals("REF-999", updated.reference());
        }

        @Test
        @DisplayName("withPlannedShipDate returns new order with updated ship date")
        void withPlannedShipDateUpdatesShipDate() {
            var updated = order(OrderStatus.IN_PROGRESS, TOMORROW).withPlannedShipDate(YESTERDAY);
            assertEquals(YESTERDAY, updated.plannedShipDate());
        }

        @Test
        @DisplayName("withLineItems returns new order with updated line items")
        void withLineItemsUpdatesLineItems() {
            var items = List.of(new LineItem(null, "Widget", 1, BigDecimal.TEN));
            var updated = order(OrderStatus.IN_PROGRESS, TOMORROW).withLineItems(items);
            assertEquals(items, updated.lineItems());
        }

        @Test
        @DisplayName("with methods do not mutate the original order")
        void doesNotMutateOriginal() {
            var original = order(OrderStatus.IN_PROGRESS, TOMORROW);
            original.withCustomerId(UUID.randomUUID());
            original.withReference("CHANGED");
            original.withPlannedShipDate(YESTERDAY);
            original.withLineItems(List.of(new LineItem(null, "Widget", 1, BigDecimal.TEN)));
            assertEquals(order(OrderStatus.IN_PROGRESS, TOMORROW), original);
        }

        @Test
        @DisplayName("with methods preserve unmodified fields")
        void preservesUnmodifiedFields() {
            var updated = order(OrderStatus.IN_PROGRESS, TOMORROW).withReference("NEW-REF");
            assertEquals(new Order(AN_ID, A_CUST, TODAY, TOMORROW, "NEW-REF", OrderStatus.IN_PROGRESS, null, List.of()), updated);
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
