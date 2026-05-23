package mvvm.example.orders.domain.commands;

import mvvm.example.core.config.adapters.InMemoryOrderRepository;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Orders.CopyOrderCommand")
class CopyOrderCommandTest {

    private final InMemoryOrderRepository repository = new InMemoryOrderRepository();
    private final CopyOrderCommand command = new CopyOrderCommand(repository);

    private Order savedOrder(String id, String reference, List<LineItem> lineItems) {
        var order = new Order(id, "cust-1", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), reference, OrderStatus.PENDING, null, lineItems);
        repository.save(order);
        return order;
    }

    @Test
    @DisplayName("returns the id of the new order")
    void returnsNewOrderId() {
        savedOrder("order-1", "REF-001", List.of());

        var newId = command.execute("order-1");

        assertNotNull(newId);
        assertNotEquals("order-1", newId);
    }

    @Test
    @DisplayName("saves the copy to the repository")
    void savesExecuteToRepository() {
        savedOrder("order-1", "REF-001", List.of());

        var newId = command.execute("order-1");

        assertTrue(repository.findById(newId).isPresent());
    }

    @Test
    @DisplayName("prefixes the reference with COPY-")
    void prefixesReference() {
        savedOrder("order-1", "REF-001", List.of());

        var newId = command.execute("order-1");

        var copy = repository.findById(newId).orElseThrow();
        assertEquals("COPY-REF-001", copy.reference());
    }

    @Test
    @DisplayName("copies the customer id from the original")
    void copiesCustomerId() {
        savedOrder("order-1", "REF-001", List.of());

        var newId = command.execute("order-1");

        var copy = repository.findById(newId).orElseThrow();
        assertEquals("cust-1", copy.customerId());
    }

    @Test
    @DisplayName("copies line items from the original")
    void copiesLineItems() {
        var lineItems = List.of(new LineItem("prod-1", "Widget", 3, BigDecimal.TEN));
        savedOrder("order-1", "REF-001", lineItems);

        var newId = command.execute("order-1");

        var copy = repository.findById(newId).orElseThrow();
        assertEquals(1, copy.lineItems().size());
        var item = copy.lineItems().getFirst();
        assertEquals("prod-1", item.productId());
        assertEquals("Widget", item.description());
        assertEquals(3, item.quantity());
        assertEquals(BigDecimal.TEN, item.unitPrice());
    }

    @Test
    @DisplayName("does not modify the original order")
    void doesNotModifyOriginal() {
        savedOrder("order-1", "REF-001", List.of());

        command.execute("order-1");

        var original = repository.findById("order-1").orElseThrow();
        assertEquals("REF-001", original.reference());
    }

    @Test
    @DisplayName("throws when the order does not exist")
    void throwsWhenOrderNotFound() {
        var ex = assertThrows(IllegalArgumentException.class, () -> command.execute("no-such-id"));
        assertTrue(ex.getMessage().contains("no-such-id"));
    }
}
