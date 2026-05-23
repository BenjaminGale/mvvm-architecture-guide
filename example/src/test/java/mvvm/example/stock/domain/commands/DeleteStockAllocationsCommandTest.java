package mvvm.example.stock.domain.commands;

import mvvm.example.core.config.adapters.InMemoryStockRepository;
import mvvm.example.stock.domain.StockAllocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Stock.DeleteStockAllocationsCommand")
class DeleteStockAllocationsCommandTest {

    private static final UUID PROD_1 = UUID.randomUUID();
    private static final UUID PROD_2 = UUID.randomUUID();
    private static final UUID ORDER_1 = UUID.randomUUID();
    private static final UUID ORDER_2 = UUID.randomUUID();

    @Test
    @DisplayName("deletes allocations for the given product and order")
    void deletesMatchingAllocations() {
        var stock = new InMemoryStockRepository();
        stock.save(StockAllocation.create(PROD_1, ORDER_1, 5));

        new DeleteStockAllocationsCommand(stock).execute(PROD_1, ORDER_1);

        assertTrue(stock.findByOrderId(ORDER_1).isEmpty());
    }

    @Test
    @DisplayName("does not delete allocations for a different product on the same order")
    void doesNotDeleteOtherProducts() {
        var stock = new InMemoryStockRepository();
        stock.save(StockAllocation.create(PROD_1, ORDER_1, 5));
        stock.save(StockAllocation.create(PROD_2, ORDER_1, 3));

        new DeleteStockAllocationsCommand(stock).execute(PROD_1, ORDER_1);

        assertEquals(1, stock.findByOrderId(ORDER_1).size());
        assertEquals(PROD_2, stock.findByOrderId(ORDER_1).getFirst().productId());
    }

    @Test
    @DisplayName("does not delete allocations for the same product on a different order")
    void doesNotDeleteOtherOrders() {
        var stock = new InMemoryStockRepository();
        stock.save(StockAllocation.create(PROD_1, ORDER_1, 5));
        stock.save(StockAllocation.create(PROD_1, ORDER_2, 3));

        new DeleteStockAllocationsCommand(stock).execute(PROD_1, ORDER_1);

        assertEquals(1, stock.findByOrderId(ORDER_2).size());
    }

    @Test
    @DisplayName("does nothing when there are no matching allocations")
    void doesNothingWhenNoMatch() {
        var stock = new InMemoryStockRepository();

        assertDoesNotThrow(() -> new DeleteStockAllocationsCommand(stock).execute(PROD_1, ORDER_1));
    }
}
