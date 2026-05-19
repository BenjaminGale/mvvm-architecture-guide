package mvvm.example.orders.domain;

import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.customers.domain.CustomerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Orders.GetOrderSummariesQuery")
class GetOrderSummariesQueryTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate YESTERDAY = TODAY.minusDays(1);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);

    private static final Customer ACME = new Customer("cust-1", "Acme Ltd", "acme@example.com", CustomerStatus.ACTIVE);

    private static OrderRepository ordersRepoWith(Order... orders) {
        return new OrderRepository() {
            @Override public List<Order> findAll() { return List.of(orders); }
            @Override public Optional<Order> findById(String id) { return Optional.empty(); }
            @Override public void save(Order order) {}
            @Override public void delete(String id) {}
        };
    }

    private static CustomerRepository customersRepoWith(Customer... customers) {
        return new CustomerRepository() {
            @Override public List<Customer> findAll() { return List.of(customers); }
            @Override public Optional<Customer> findById(String id) { return Optional.empty(); }
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
            var order = new PendingOrder("ord-1", ACME.id(), TODAY, TOMORROW, "REF-001", List.of());
            var summaries = execute(ordersRepoWith(order), customersRepoWith(ACME));

            assertEquals("ord-1", summaries.getFirst().id());
        }

        @Test
        @DisplayName("reference is taken from the order")
        void reference() {
            var order = new PendingOrder("ord-1", ACME.id(), TODAY, TOMORROW, "REF-001", List.of());
            var summaries = execute(ordersRepoWith(order), customersRepoWith(ACME));

            assertEquals("REF-001", summaries.getFirst().reference());
        }

        @Test
        @DisplayName("customer name is resolved from the customer repository")
        void customerName() {
            var order = new PendingOrder("ord-1", ACME.id(), TODAY, TOMORROW, "REF-001", List.of());
            var summaries = execute(ordersRepoWith(order), customersRepoWith(ACME));

            assertEquals("Acme Ltd", summaries.getFirst().customerName());
        }

        @Test
        @DisplayName("customer name is empty when the customer is not found")
        void customerNameUnknown() {
            var order = new PendingOrder("ord-1", "unknown-id", TODAY, TOMORROW, "REF-001", List.of());
            var summaries = execute(ordersRepoWith(order), customersRepoWith());

            assertEquals("", summaries.getFirst().customerName());
        }

        @Test
        @DisplayName("status is the display name of the order type")
        void status() {
            var pending = new PendingOrder("ord-1", ACME.id(), TODAY, TOMORROW, "REF-001", List.of());
            var shipped = new ShippedOrder("ord-2", ACME.id(), TODAY, TOMORROW, "REF-002", TODAY, List.of());

            var summaries = execute(ordersRepoWith(pending, shipped), customersRepoWith(ACME));
            var statusById = summaries.stream().collect(Collectors.toMap(OrderSummary::id, OrderSummary::status));

            assertEquals("Pending", statusById.get("ord-1"));
            assertEquals("Shipped", statusById.get("ord-2"));
        }

        @Test
        @DisplayName("total is the sum of line item totals")
        void total() {
            var order = new PendingOrder("ord-1", ACME.id(), TODAY, TOMORROW, "REF-001",
                List.of(new LineItem(null, "Widget", 2, new BigDecimal("5.00"))));
            var summaries = execute(ordersRepoWith(order), customersRepoWith(ACME));

            assertEquals(new BigDecimal("10.00"), summaries.getFirst().total());
        }

        @Test
        @DisplayName("isOverdue is true for a pending order past its ship date")
        void overdue() {
            var order = new PendingOrder("ord-1", ACME.id(), TODAY, YESTERDAY, "REF-001", List.of());
            var summaries = execute(ordersRepoWith(order), customersRepoWith(ACME));

            assertTrue(summaries.getFirst().isOverdue());
        }

        @Test
        @DisplayName("isOverdue is false for an order with a future ship date")
        void notOverdue() {
            var order = new PendingOrder("ord-1", ACME.id(), TODAY, TOMORROW, "REF-001", List.of());
            var summaries = execute(ordersRepoWith(order), customersRepoWith(ACME));

            assertFalse(summaries.getFirst().isOverdue());
        }
    }

    @Nested
    @DisplayName("result set")
    class ResultSet {

        @Test
        @DisplayName("returns a summary for every order")
        void allOrdersIncluded() {
            var order1 = new PendingOrder("ord-1", ACME.id(), TODAY, TOMORROW, "REF-001", List.of());
            var order2 = new PendingOrder("ord-2", ACME.id(), TODAY, TOMORROW, "REF-002", List.of());
            var summaries = execute(ordersRepoWith(order1, order2), customersRepoWith(ACME));

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
