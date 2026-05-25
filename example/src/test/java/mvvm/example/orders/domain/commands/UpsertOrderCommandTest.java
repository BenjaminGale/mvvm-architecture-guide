package mvvm.example.orders.domain.commands;

import mvvm.example.core.config.adapters.InMemoryOrderRepository;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Orders.UpsertOrderCommand")
class UpsertOrderCommandTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate SHIP_DATE = TODAY.plusDays(10);
    private static final UUID CUST_ID = UUID.randomUUID();
    private static final UUID PROD_ID = UUID.randomUUID();
    private static final List<LineItem> ITEMS = List.of(new LineItem(PROD_ID, "Widget", 2, BigDecimal.TEN));

    private final InMemoryOrderRepository orderRepository = new InMemoryOrderRepository();
    private final UpsertOrderCommand command = new UpsertOrderCommand(orderRepository);

    @Nested
    @DisplayName("insert (orderId is null)")
    class Insert {

        @Test
        @DisplayName("returns the id of the new order")
        void returnsNewOrderId() {
            var id = command.execute(null, CUST_ID, "REF-001", SHIP_DATE, ITEMS);

            assertNotNull(id);
        }

        @Test
        @DisplayName("saves the order to the repository")
        void savesOrder() {
            var id = command.execute(null, CUST_ID, "REF-001", SHIP_DATE, ITEMS);

            assertTrue(orderRepository.findById(id).isPresent());
        }

        @Test
        @DisplayName("new order has IN_PROGRESS status")
        void newOrderIsInProgress() {
            var id = command.execute(null, CUST_ID, "REF-001", SHIP_DATE, ITEMS);

            assertEquals(OrderStatus.IN_PROGRESS, orderRepository.findById(id).orElseThrow().status());
        }

        @Test
        @DisplayName("new order has today as its created date")
        void createdDateIsToday() {
            var id = command.execute(null, CUST_ID, "REF-001", SHIP_DATE, ITEMS);

            assertEquals(TODAY, orderRepository.findById(id).orElseThrow().createdDate());
        }

        @Test
        @DisplayName("new order carries the provided fields")
        void fieldsAreSet() {
            var id = command.execute(null, CUST_ID, "REF-001", SHIP_DATE, ITEMS);

            var order = orderRepository.findById(id).orElseThrow();
            assertEquals(CUST_ID, order.customerId());
            assertEquals("REF-001", order.reference());
            assertEquals(SHIP_DATE, order.plannedShipDate());
            assertEquals(ITEMS, order.lineItems());
        }
    }

    @Nested
    @DisplayName("update (orderId is provided)")
    class Update {

        private static final UUID CUST_2 = UUID.randomUUID();

        private Order savedOrder() {
            var id = UUID.randomUUID();
            var order = new Order(id, CUST_ID, TODAY.minusDays(5), TODAY.plusDays(5), "REF-001", OrderStatus.SHIPPED, null, List.of());
            orderRepository.save(order);
            return order;
        }

        @Test
        @DisplayName("returns the same order id")
        void returnsSameId() {
            var existing = savedOrder();

            var id = command.execute(existing.id(), CUST_2, "REF-002", SHIP_DATE, ITEMS);

            assertEquals(existing.id(), id);
        }

        @Test
        @DisplayName("saves the updated order")
        void savesUpdatedOrder() {
            var existing = savedOrder();

            command.execute(existing.id(), CUST_2, "REF-002", SHIP_DATE, ITEMS);

            var order = orderRepository.findById(existing.id()).orElseThrow();
            assertEquals(CUST_2, order.customerId());
            assertEquals("REF-002", order.reference());
            assertEquals(SHIP_DATE, order.plannedShipDate());
            assertEquals(ITEMS, order.lineItems());
        }

        @Test
        @DisplayName("preserves status and created date from the existing order")
        void preservesImmutableFields() {
            var existing = savedOrder();

            command.execute(existing.id(), CUST_2, "REF-002", SHIP_DATE, ITEMS);

            var order = orderRepository.findById(existing.id()).orElseThrow();
            assertEquals(OrderStatus.SHIPPED, order.status());
            assertEquals(TODAY.minusDays(5), order.createdDate());
        }

        @Test
        @DisplayName("throws when the order does not exist")
        void throwsWhenNotFound() {
            var missingId = UUID.randomUUID();

            var ex = assertThrows(IllegalArgumentException.class,
                () -> command.execute(missingId, CUST_ID, "REF-001", SHIP_DATE, ITEMS));

            assertTrue(ex.getMessage().contains(missingId.toString()));
        }
    }
}
