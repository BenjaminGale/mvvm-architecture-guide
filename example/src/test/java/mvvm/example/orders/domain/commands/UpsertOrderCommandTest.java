package mvvm.example.orders.domain.commands;

import mvvm.example.core.config.adapters.InMemoryOrderRepository;
import mvvm.example.core.config.adapters.InMemoryStockRepository;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderStatus;
import mvvm.example.stock.domain.StockAllocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Orders.UpsertOrderCommand")
class UpsertOrderCommandTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate SHIP_DATE = TODAY.plusDays(10);
    private static final List<LineItem> ITEMS = List.of(new LineItem("prod-1", "Widget", 2, BigDecimal.TEN));

    private final InMemoryOrderRepository orderRepository = new InMemoryOrderRepository();
    private final InMemoryStockRepository stockRepository = new InMemoryStockRepository();
    private final UpsertOrderCommand command = new UpsertOrderCommand(orderRepository, stockRepository);

    @Nested
    @DisplayName("insert (orderId is null)")
    class Insert {

        @Test
        @DisplayName("returns the id of the new order")
        void returnsNewOrderId() {
            var id = command.execute(null, "cust-1", "REF-001", SHIP_DATE, ITEMS);

            assertNotNull(id);
        }

        @Test
        @DisplayName("saves the order to the repository")
        void savesOrder() {
            var id = command.execute(null, "cust-1", "REF-001", SHIP_DATE, ITEMS);

            assertTrue(orderRepository.findById(id).isPresent());
        }

        @Test
        @DisplayName("new order has PENDING status")
        void newOrderIsPending() {
            var id = command.execute(null, "cust-1", "REF-001", SHIP_DATE, ITEMS);

            assertEquals(OrderStatus.PENDING, orderRepository.findById(id).orElseThrow().status());
        }

        @Test
        @DisplayName("new order has today as its created date")
        void createdDateIsToday() {
            var id = command.execute(null, "cust-1", "REF-001", SHIP_DATE, ITEMS);

            assertEquals(TODAY, orderRepository.findById(id).orElseThrow().createdDate());
        }

        @Test
        @DisplayName("new order carries the provided fields")
        void fieldsAreSet() {
            var id = command.execute(null, "cust-1", "REF-001", SHIP_DATE, ITEMS);

            var order = orderRepository.findById(id).orElseThrow();
            assertEquals("cust-1", order.customerId());
            assertEquals("REF-001", order.reference());
            assertEquals(SHIP_DATE, order.plannedShipDate());
            assertEquals(ITEMS, order.lineItems());
        }
    }

    @Nested
    @DisplayName("update (orderId is provided)")
    class Update {

        private Order savedOrder() {
            var order = new Order("ord-1", "cust-1", TODAY.minusDays(5), TODAY.plusDays(5), "REF-001", OrderStatus.FULFILLED, null, List.of());
            orderRepository.save(order);
            return order;
        }

        @Test
        @DisplayName("returns the same order id")
        void returnsSameId() {
            savedOrder();

            var id = command.execute("ord-1", "cust-2", "REF-002", SHIP_DATE, ITEMS);

            assertEquals("ord-1", id);
        }

        @Test
        @DisplayName("saves the updated order")
        void savesUpdatedOrder() {
            savedOrder();

            command.execute("ord-1", "cust-2", "REF-002", SHIP_DATE, ITEMS);

            var order = orderRepository.findById("ord-1").orElseThrow();
            assertEquals("cust-2", order.customerId());
            assertEquals("REF-002", order.reference());
            assertEquals(SHIP_DATE, order.plannedShipDate());
            assertEquals(ITEMS, order.lineItems());
        }

        @Test
        @DisplayName("preserves status and created date from the existing order")
        void preservesImmutableFields() {
            savedOrder();

            command.execute("ord-1", "cust-2", "REF-002", SHIP_DATE, ITEMS);

            var order = orderRepository.findById("ord-1").orElseThrow();
            assertEquals(OrderStatus.FULFILLED, order.status());
            assertEquals(TODAY.minusDays(5), order.createdDate());
        }

        @Test
        @DisplayName("clears all stock allocations before saving")
        void clearsStockAllocations() {
            savedOrder();
            stockRepository.save(StockAllocation.create("prod-1", "ord-1", 5));
            stockRepository.save(StockAllocation.create("prod-2", "ord-1", 3));

            command.execute("ord-1", "cust-1", "REF-001", SHIP_DATE, ITEMS);

            assertTrue(stockRepository.findByOrderId("ord-1").isEmpty());
        }

        @Test
        @DisplayName("throws when the order does not exist")
        void throwsWhenNotFound() {
            var ex = assertThrows(IllegalArgumentException.class,
                () -> command.execute("no-such-id", "cust-1", "REF-001", SHIP_DATE, ITEMS));

            assertTrue(ex.getMessage().contains("no-such-id"));
        }
    }
}
