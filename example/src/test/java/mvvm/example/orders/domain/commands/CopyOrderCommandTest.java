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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Orders.CopyOrderCommand")
class CopyOrderCommandTest {

    private static final UUID CUST_ID = UUID.randomUUID();
    private static final UUID PROD_ID = UUID.randomUUID();

    private final InMemoryOrderRepository repository = new InMemoryOrderRepository();
    private final CopyOrderCommand command = new CopyOrderCommand(repository);

    private Order savedOrder(String reference, List<LineItem> lineItems) {
        var id = UUID.randomUUID();
        var order = new Order(id, CUST_ID, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), reference, OrderStatus.IN_PROGRESS, null, lineItems);
        repository.save(order);
        return order;
    }

    @Test
    @DisplayName("returns the id of the new order")
    void returnsNewOrderId() {
        var original = savedOrder("REF-001", List.of());

        var newId = command.execute(original.id());

        assertNotNull(newId);
        assertNotEquals(original.id(), newId);
    }

    @Test
    @DisplayName("saves the copy to the repository")
    void savesExecuteToRepository() {
        var original = savedOrder("REF-001", List.of());

        var newId = command.execute(original.id());

        assertTrue(repository.findById(newId).isPresent());
    }

    @Test
    @DisplayName("prefixes the reference with COPY-")
    void prefixesReference() {
        var original = savedOrder("REF-001", List.of());

        var newId = command.execute(original.id());

        assertEquals("COPY-REF-001", repository.findById(newId).orElseThrow().reference());
    }

    @Test
    @DisplayName("copies the customer id from the original")
    void copiesCustomerId() {
        var original = savedOrder("REF-001", List.of());

        var newId = command.execute(original.id());

        assertEquals(CUST_ID, repository.findById(newId).orElseThrow().customerId());
    }

    @Test
    @DisplayName("copies line items from the original")
    void copiesLineItems() {
        var lineItems = List.of(new LineItem(PROD_ID, "Widget", 3, BigDecimal.TEN));
        var original = savedOrder("REF-001", lineItems);

        var newId = command.execute(original.id());

        var copy = repository.findById(newId).orElseThrow();
        assertEquals(1, copy.lineItems().size());
        var item = copy.lineItems().getFirst();
        assertEquals(PROD_ID, item.productId());
        assertEquals("Widget", item.description());
        assertEquals(3, item.quantity());
        assertEquals(BigDecimal.TEN, item.unitPrice());
    }

    @Test
    @DisplayName("does not modify the original order")
    void doesNotModifyOriginal() {
        var original = savedOrder("REF-001", List.of());

        command.execute(original.id());

        assertEquals("REF-001", repository.findById(original.id()).orElseThrow().reference());
    }

    @Test
    @DisplayName("throws when the order does not exist")
    void throwsWhenOrderNotFound() {
        var missingId = UUID.randomUUID();

        var ex = assertThrows(IllegalArgumentException.class, () -> command.execute(missingId));
        assertTrue(ex.getMessage().contains(missingId.toString()));
    }
}
