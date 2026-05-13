package mvvm.example.orders.editor.usecases;

import mvvm.example.orders.StubOrderRepository;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CopyOrderUseCase")
class CopyOrderUseCaseTest {

    private static final Order AN_ORDER = new Order(
        "id-1", "Acme Ltd", LocalDate.of(2025, 1, 15), "REF-001",
        List.of(new LineItem("Widget", 1, BigDecimal.TEN))
    );

    @Nested
    @DisplayName("when executed")
    class WhenExecuted {

        @Test
        @DisplayName("the onCopied callback is invoked with the copied order")
        void onCopiedCallbackInvoked() {
            var copied = new AtomicReference<Order>();
            var repo = new StubOrderRepository(AN_ORDER);
            var useCase = new CopyOrderUseCase(new OrderService(repo), copied::set);

            useCase.execute(AN_ORDER);

            assertNotNull(copied.get());
        }

        @Test
        @DisplayName("the copied order has a different id to the original")
        void copiedOrderHasDifferentId() {
            var copied = new AtomicReference<Order>();
            var repo = new StubOrderRepository(AN_ORDER);
            var useCase = new CopyOrderUseCase(new OrderService(repo), copied::set);

            useCase.execute(AN_ORDER);

            assertNotEquals(AN_ORDER.id(), copied.get().id());
        }

        @Test
        @DisplayName("the copied order preserves the original customer name and line items")
        void copiedOrderPreservesContent() {
            var copied = new AtomicReference<Order>();
            var repo = new StubOrderRepository(AN_ORDER);
            var useCase = new CopyOrderUseCase(new OrderService(repo), copied::set);

            useCase.execute(AN_ORDER);

            assertEquals(AN_ORDER.customerName(), copied.get().customerName());
            assertEquals(AN_ORDER.lineItems(), copied.get().lineItems());
        }
    }
}
