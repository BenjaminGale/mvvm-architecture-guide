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
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DeleteOrderUseCase")
class DeleteOrderUseCaseTest {

    private static final Order AN_ORDER = new Order(
        "id-1", "Acme Ltd", LocalDate.of(2025, 1, 15), "REF-001",
        List.of(new LineItem("Widget", 1, BigDecimal.TEN))
    );

    @Nested
    @DisplayName("when executed")
    class WhenExecuted {

        @Test
        @DisplayName("the order is removed from the service")
        void orderIsRemoved() {
            var repo = new StubOrderRepository(AN_ORDER);
            var useCase = new DeleteOrderUseCase(new OrderService(repo), () -> {});

            useCase.execute(AN_ORDER);

            assertTrue(repo.findAll().isEmpty());
        }

        @Test
        @DisplayName("the onDeleted callback is invoked after deletion")
        void onDeletedCallbackInvoked() {
            var deleted = new AtomicBoolean(false);
            var repo = new StubOrderRepository(AN_ORDER);
            var useCase = new DeleteOrderUseCase(new OrderService(repo), () -> deleted.set(true));

            useCase.execute(AN_ORDER);

            assertTrue(deleted.get());
        }
    }
}
