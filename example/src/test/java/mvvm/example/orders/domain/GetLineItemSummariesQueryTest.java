package mvvm.example.orders.domain;

import mvvm.example.core.config.adapters.InMemoryStockRepository;
import mvvm.example.stock.domain.Product;
import mvvm.example.stock.domain.ProductRepository;
import mvvm.example.stock.domain.StockAllocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Orders.GetLineItemSummariesQuery")
class GetLineItemSummariesQueryTest {

    private static final Product WIDGET = new Product("prod-1", "Widget", BigDecimal.valueOf(9.99), 10);

    private static ProductRepository repoWith(Product... products) {
        return new ProductRepository() {
            @Override public List<Product> findAll() { return List.of(products); }
            @Override public Optional<Product> findById(String id) { return findAll().stream().filter(p -> p.id().equals(id)).findFirst(); }
            @Override public void save(Product p) {}
        };
    }

    private static List<LineItemSummary> execute(List<LineItem> items, String orderId, ProductRepository products, InMemoryStockRepository stock) {
        return new GetLineItemSummariesQuery(products, stock).execute(items, orderId).join();
    }

    @Nested
    @DisplayName("summary fields")
    class SummaryFields {

        @Test
        @DisplayName("product name is resolved from the product repository")
        void productNameResolved() {
            var item = new LineItem("prod-1", "old name", 2, BigDecimal.valueOf(9.99));
            var summaries = execute(List.of(item), "order-1", repoWith(WIDGET), new InMemoryStockRepository());

            assertEquals("Widget", summaries.getFirst().productName());
        }

        @Test
        @DisplayName("product name falls back to the line item description when the product is not found")
        void productNameFallsBackToDescription() {
            var item = new LineItem("unknown", "My Description", 1, BigDecimal.TEN);
            var summaries = execute(List.of(item), "order-1", repoWith(), new InMemoryStockRepository());

            assertEquals("My Description", summaries.getFirst().productName());
        }

        @Test
        @DisplayName("quantity and unit price are taken from the line item")
        void quantityAndUnitPrice() {
            var item = new LineItem("prod-1", "Widget", 3, BigDecimal.valueOf(9.99));
            var summaries = execute(List.of(item), "order-1", repoWith(WIDGET), new InMemoryStockRepository());

            assertEquals(3, summaries.getFirst().quantity());
            assertEquals(BigDecimal.valueOf(9.99), summaries.getFirst().unitPrice());
        }

        @Test
        @DisplayName("allocated quantity is the total of matching stock allocations")
        void allocatedQuantity() {
            var stock = new InMemoryStockRepository();
            stock.save(StockAllocation.create("prod-1", "order-1", 3));
            stock.save(StockAllocation.create("prod-1", "order-1", 2));
            var item = new LineItem("prod-1", "Widget", 5, BigDecimal.valueOf(9.99));

            var summaries = execute(List.of(item), "order-1", repoWith(WIDGET), stock);

            assertEquals(5, summaries.getFirst().allocatedQuantity());
        }

        @Test
        @DisplayName("allocated quantity is zero when there are no allocations")
        void allocatedQuantityZeroWhenNone() {
            var item = new LineItem("prod-1", "Widget", 2, BigDecimal.valueOf(9.99));
            var summaries = execute(List.of(item), "order-1", repoWith(WIDGET), new InMemoryStockRepository());

            assertEquals(0, summaries.getFirst().allocatedQuantity());
        }

        @Test
        @DisplayName("allocated quantity only counts allocations for the queried order")
        void allocatedQuantityIsolatedToOrder() {
            var stock = new InMemoryStockRepository();
            stock.save(StockAllocation.create("prod-1", "order-1", 4));
            stock.save(StockAllocation.create("prod-1", "order-2", 10));
            var item = new LineItem("prod-1", "Widget", 4, BigDecimal.valueOf(9.99));

            var summaries = execute(List.of(item), "order-1", repoWith(WIDGET), stock);

            assertEquals(4, summaries.getFirst().allocatedQuantity());
        }
    }

    @Nested
    @DisplayName("result set")
    class ResultSet {

        @Test
        @DisplayName("returns a summary for every line item")
        void allItemsIncluded() {
            var items = List.of(
                new LineItem("prod-1", "Widget", 1, BigDecimal.TEN),
                new LineItem("prod-1", "Widget", 2, BigDecimal.TEN)
            );
            var summaries = execute(items, "order-1", repoWith(WIDGET), new InMemoryStockRepository());

            assertEquals(2, summaries.size());
        }

        @Test
        @DisplayName("returns an empty list when there are no line items")
        void emptyWhenNoItems() {
            var summaries = execute(List.of(), "order-1", repoWith(WIDGET), new InMemoryStockRepository());

            assertTrue(summaries.isEmpty());
        }

        @Test
        @DisplayName("allocated quantity is zero for all items when orderId is null")
        void nullOrderIdReturnsZeroAllocations() {
            var stock = new InMemoryStockRepository();
            stock.save(StockAllocation.create("prod-1", "order-1", 5));
            var item = new LineItem("prod-1", "Widget", 2, BigDecimal.valueOf(9.99));

            var summaries = execute(List.of(item), null, repoWith(WIDGET), stock);

            assertEquals(0, summaries.getFirst().allocatedQuantity());
        }
    }
}
