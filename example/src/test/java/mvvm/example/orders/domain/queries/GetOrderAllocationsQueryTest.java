package mvvm.example.orders.domain.queries;

import mvvm.example.core.config.adapters.InMemoryStockRepository;
import mvvm.example.stock.domain.StockAllocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Orders.GetOrderAllocationsQuery")
class GetOrderAllocationsQueryTest {

    private static final UUID PROD_1  = UUID.randomUUID();
    private static final UUID PROD_2  = UUID.randomUUID();
    private static final UUID ORDER_1 = UUID.randomUUID();
    private static final UUID ORDER_2 = UUID.randomUUID();

    @Test
    @DisplayName("returns allocated quantity per product for the given order")
    void returnsAllocatedQuantityPerProduct() {
        var stock = new InMemoryStockRepository();
        stock.save(StockAllocation.create(PROD_1, ORDER_1, 3));
        stock.save(StockAllocation.create(PROD_2, ORDER_1, 5));

        var result = new GetOrderAllocationsQuery(stock).execute(ORDER_1);

        assertEquals(3, result.get(PROD_1));
        assertEquals(5, result.get(PROD_2));
    }

    @Test
    @DisplayName("sums multiple allocations for the same product")
    void sumsMultipleAllocationsForSameProduct() {
        var stock = new InMemoryStockRepository();
        stock.save(StockAllocation.create(PROD_1, ORDER_1, 3));
        stock.save(StockAllocation.create(PROD_1, ORDER_1, 2));

        var result = new GetOrderAllocationsQuery(stock).execute(ORDER_1);

        assertEquals(5, result.get(PROD_1));
    }

    @Test
    @DisplayName("only includes allocations for the given order")
    void isolatedToOrder() {
        var stock = new InMemoryStockRepository();
        stock.save(StockAllocation.create(PROD_1, ORDER_1, 3));
        stock.save(StockAllocation.create(PROD_1, ORDER_2, 10));

        var result = new GetOrderAllocationsQuery(stock).execute(ORDER_1);

        assertEquals(1, result.size());
        assertEquals(3, result.get(PROD_1));
    }

    @Test
    @DisplayName("returns an empty map when there are no allocations for the order")
    void emptyWhenNoAllocations() {
        var stock = new InMemoryStockRepository();

        var result = new GetOrderAllocationsQuery(stock).execute(ORDER_1);

        assertTrue(result.isEmpty());
    }
}
