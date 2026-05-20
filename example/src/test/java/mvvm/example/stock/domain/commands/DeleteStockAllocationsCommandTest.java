package mvvm.example.stock.domain.commands;

import mvvm.example.core.config.adapters.InMemoryStockRepository;
import mvvm.example.stock.domain.StockAllocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Stock.DeleteStockAllocationsCommand")
class DeleteStockAllocationsCommandTest {

    @Test
    @DisplayName("deletes allocations for the given product and order")
    void deletesMatchingAllocations() {
        var stock = new InMemoryStockRepository();
        stock.save(StockAllocation.create("prod-1", "order-1", 5));

        new DeleteStockAllocationsCommand(stock).execute("prod-1", "order-1");

        assertTrue(stock.findByOrderId("order-1").isEmpty());
    }

    @Test
    @DisplayName("does not delete allocations for a different product on the same order")
    void doesNotDeleteOtherProducts() {
        var stock = new InMemoryStockRepository();
        stock.save(StockAllocation.create("prod-1", "order-1", 5));
        stock.save(StockAllocation.create("prod-2", "order-1", 3));

        new DeleteStockAllocationsCommand(stock).execute("prod-1", "order-1");

        assertEquals(1, stock.findByOrderId("order-1").size());
        assertEquals("prod-2", stock.findByOrderId("order-1").getFirst().productId());
    }

    @Test
    @DisplayName("does not delete allocations for the same product on a different order")
    void doesNotDeleteOtherOrders() {
        var stock = new InMemoryStockRepository();
        stock.save(StockAllocation.create("prod-1", "order-1", 5));
        stock.save(StockAllocation.create("prod-1", "order-2", 3));

        new DeleteStockAllocationsCommand(stock).execute("prod-1", "order-1");

        assertEquals(1, stock.findByOrderId("order-2").size());
    }

    @Test
    @DisplayName("does nothing when there are no matching allocations")
    void doesNothingWhenNoMatch() {
        var stock = new InMemoryStockRepository();

        assertDoesNotThrow(() -> new DeleteStockAllocationsCommand(stock).execute("prod-1", "order-1"));
    }
}
