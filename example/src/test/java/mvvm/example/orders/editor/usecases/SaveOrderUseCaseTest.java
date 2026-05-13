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

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SaveOrderUseCase")
class SaveOrderUseCaseTest {

    private static final Order AN_ORDER = new Order(
        "id-1", "Acme Ltd", LocalDate.of(2025, 1, 15), "REF-001",
        List.of(new LineItem("Widget", 1, BigDecimal.TEN))
    );

    @Nested
    @DisplayName("when executed")
    class WhenExecuted {

        @Test
        @DisplayName("the order is persisted via the service")
        void orderIsPersisted() {
            var repo = new StubOrderRepository();
            var useCase = new SaveOrderUseCase(new OrderService(repo), () -> {});

            useCase.execute(AN_ORDER).join();

            assertEquals(List.of(AN_ORDER), repo.findAll());
        }

    }
}
