package mvvm.example.orders.domain.queries;

import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.customers.domain.CustomerStatus;
import mvvm.example.orders.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Orders.GetOrderSummariesQuery")
class GetOrderSummariesQueryTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate YESTERDAY = TODAY.minusDays(1);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);

    private static final Customer ACME = new Customer(UUID.randomUUID(), "Acme Ltd", "acme@example.com", CustomerStatus.ACTIVE);

    private static Order pending(UUID customerId, LocalDate plannedShipDate) {
        return new Order(UUID.randomUUID(), customerId, TODAY, plannedShipDate, "REF", OrderStatus.PENDING, null, List.of());
    }

    private static Order pending(UUID customerId, LocalDate plannedShipDate, List<LineItem> items) {
        return new Order(UUID.randomUUID(), customerId, TODAY, plannedShipDate, "REF", OrderStatus.PENDING, null, items);
    }

    private static Order shipped(UUID customerId) {
        return new Order(UUID.randomUUID(), customerId, TODAY, TOMORROW, "REF", OrderStatus.SHIPPED, TODAY, List.of());
    }

    private static OrderRepository ordersRepoWith(Order... orders) {
        return new OrderRepository() {
            @Override public List<Order> findAll() { return List.of(orders); }
            @Override public Optional<Order> findById(UUID id) { return Optional.empty(); }
            @Override public void save(Order order) {}
            @Override public void delete(UUID id) {}
        };
    }

    private static CustomerRepository customersRepoWith(Customer... customers) {
        return new CustomerRepository() {
            @Override public List<Customer> findAll() { return List.of(customers); }
            @Override public Optional<Customer> findById(UUID id) { return Optional.empty(); }
            @Override public void save(Customer customer) {}
        };
    }

    private static List<OrderSummary> execute(OrderRepository orders, CustomerRepository customers) {
        return new GetOrderSummariesQuery(orders, customers).execute().join();
    }

    @Nested
    @DisplayName("summary fields")
    class SummaryFields {

        @Test
        @DisplayName("id is taken from the order")
        void id() {
            var order = pending(ACME.id(), TOMORROW);
            var summaries = execute(ordersRepoWith(order), customersRepoWith(ACME));

            assertEquals(order.id(), summaries.getFirst().id());
        }

        @Test
        @DisplayName("reference is taken from the order")
        void reference() {
            var order = pending(ACME.id(), TOMORROW);
            var summaries = execute(ordersRepoWith(order), customersRepoWith(ACME));

            assertEquals(order.reference(), summaries.getFirst().reference());
        }

        @Test
        @DisplayName("customer name is resolved from the customer repository")
        void customerName() {
            var summaries = execute(ordersRepoWith(pending(ACME.id(), TOMORROW)), customersRepoWith(ACME));

            assertEquals("Acme Ltd", summaries.getFirst().customerName());
        }

        @Test
        @DisplayName("customer name is empty when the customer is not found")
        void customerNameUnknown() {
            var summaries = execute(ordersRepoWith(pending(UUID.randomUUID(), TOMORROW)), customersRepoWith());

            assertEquals("", summaries.getFirst().customerName());
        }

        @Test
        @DisplayName("status is the display name of the order status")
        void status() {
            var pendingOrder = pending(ACME.id(), TOMORROW);
            var shippedOrder = shipped(ACME.id());
            var summaries = execute(ordersRepoWith(pendingOrder, shippedOrder), customersRepoWith(ACME));
            var statusById = summaries.stream().collect(Collectors.toMap(OrderSummary::id, OrderSummary::status));

            assertEquals("Pending", statusById.get(pendingOrder.id()));
            assertEquals("Shipped", statusById.get(shippedOrder.id()));
        }

        @Test
        @DisplayName("total is the sum of line item totals")
        void total() {
            var order = pending(ACME.id(), TOMORROW, List.of(new LineItem(null, "Widget", 2, new BigDecimal("5.00"))));
            var summaries = execute(ordersRepoWith(order), customersRepoWith(ACME));

            assertEquals(new BigDecimal("10.00"), summaries.getFirst().total());
        }

        @Test
        @DisplayName("isOverdue is true for a pending order past its ship date")
        void overdue() {
            var summaries = execute(ordersRepoWith(pending(ACME.id(), YESTERDAY)), customersRepoWith(ACME));

            assertTrue(summaries.getFirst().isOverdue());
        }

        @Test
        @DisplayName("isOverdue is false for an order with a future ship date")
        void notOverdue() {
            var summaries = execute(ordersRepoWith(pending(ACME.id(), TOMORROW)), customersRepoWith(ACME));

            assertFalse(summaries.getFirst().isOverdue());
        }
    }

    @Nested
    @DisplayName("result set")
    class ResultSet {

        @Test
        @DisplayName("returns a summary for every order")
        void allOrdersIncluded() {
            var summaries = execute(ordersRepoWith(pending(ACME.id(), TOMORROW), pending(ACME.id(), TOMORROW)), customersRepoWith(ACME));

            assertEquals(2, summaries.size());
        }

        @Test
        @DisplayName("returns an empty list when there are no orders")
        void noOrders() {
            var summaries = execute(ordersRepoWith(), customersRepoWith(ACME));

            assertTrue(summaries.isEmpty());
        }
    }
}
