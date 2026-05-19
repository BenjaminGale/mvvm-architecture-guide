package mvvm.example.orders.domain;

import mvvm.example.core.config.adapters.InMemoryStockRepository;
import mvvm.example.stock.domain.StockAllocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Orders.GetOrderAllocationsQuery")
class GetOrderAllocationsQueryTest {

    @Test
    @DisplayName("returns allocated quantity per product for the given order")
    void returnsAllocatedQuantityPerProduct() {
        var stock = new InMemoryStockRepository();
        stock.save(StockAllocation.create("prod-1", "order-1", 3));
        stock.save(StockAllocation.create("prod-2", "order-1", 5));

        var result = new GetOrderAllocationsQuery(stock).execute("order-1");

        assertEquals(3, result.get("prod-1"));
        assertEquals(5, result.get("prod-2"));
    }

    @Test
    @DisplayName("sums multiple allocations for the same product")
    void sumsMultipleAllocationsForSameProduct() {
        var stock = new InMemoryStockRepository();
        stock.save(StockAllocation.create("prod-1", "order-1", 3));
        stock.save(StockAllocation.create("prod-1", "order-1", 2));

        var result = new GetOrderAllocationsQuery(stock).execute("order-1");

        assertEquals(5, result.get("prod-1"));
    }

    @Test
    @DisplayName("only includes allocations for the given order")
    void isolatedToOrder() {
        var stock = new InMemoryStockRepository();
        stock.save(StockAllocation.create("prod-1", "order-1", 3));
        stock.save(StockAllocation.create("prod-1", "order-2", 10));

        var result = new GetOrderAllocationsQuery(stock).execute("order-1");

        assertEquals(1, result.size());
        assertEquals(3, result.get("prod-1"));
    }

    @Test
    @DisplayName("returns an empty map when there are no allocations for the order")
    void emptyWhenNoAllocations() {
        var stock = new InMemoryStockRepository();

        var result = new GetOrderAllocationsQuery(stock).execute("order-1");

        assertTrue(result.isEmpty());
    }
}
