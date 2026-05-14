package mvvm.example.orders.explorer;

import mvvm.example.orders.MockOrders;
import mvvm.example.orders.StubOrderRepository;
import mvvm.example.orders.domain.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrdersExplorerViewModel")
class OrdersExplorerViewModelTest {

    private static final LocalDate RECENT = LocalDate.of(2026, 5, 10);
    private static final LocalDate OLDER = LocalDate.of(2026, 5, 1);
    private static final LocalDate OVERDUE = LocalDate.of(2026, 4, 1);

    private StubOrderRepository repository;
    private MockOrdersExplorerHost host;

    @BeforeEach
    void setUp() {
        repository = new StubOrderRepository();
        host = new MockOrdersExplorerHost();
    }

    private OrdersExplorerViewModel createViewModel() {
        return new OrdersExplorerViewModel(repository::findAll, host);
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("it loads orders from storage")
        void ordersLoadedImmediately() {
            repository.save(MockOrders.of("1", RECENT));

            var vm = createViewModel();

            assertEquals(1, vm.getOrders().size());
        }

        @Test
        @DisplayName("shows empty state when no orders exist")
        void handlesEmptyRepository() {
            var vm = createViewModel();

            assertEquals(0, vm.getOrders().size());
            assertEquals("0 orders, 0 overdue", vm.statusTextProperty().get());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#statusTextCases")
        @DisplayName("it shows total and overdue order counts")
        void showsExpectedStatusText(String caseName, List<Order> orders, String expected) {
            orders.forEach(repository::save);

            var vm = createViewModel();

            assertEquals(expected, vm.statusTextProperty().get());
        }

        @Test
        @DisplayName("it reports pending order count")
        void pendingCountUpdated() {
            repository.save(MockOrders.of("1", OVERDUE));
            repository.save(MockOrders.of("2", OVERDUE));

            createViewModel();

            host.assertPendingOrderCount(2);
        }
    }

    @Nested
    @DisplayName("when refreshed")
    class WhenRefreshed {

        @Test
        @DisplayName("it reloads orders from storage")
        void ordersReloaded() {
            var vm = createViewModel();
            vm.refresh();
            assertEquals(0, vm.getOrders().size());
        }

        @Test
        @DisplayName("it updates status text after refresh")
        void statusTextUpdated() {
            repository.save(MockOrders.of("1", RECENT));
            repository.save(MockOrders.of("2", RECENT));
            repository.save(MockOrders.of("3", RECENT));

            var vm = createViewModel();
            vm.refresh();

            assertEquals("3 orders, 0 overdue", vm.statusTextProperty().get());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#sortingCases")
        @DisplayName("it sorts orders by most recent date first")
        void sortsOrdersByDateDescending(String caseName, List<Order> input, List<String> expectedOrder) {
            input.forEach(repository::save);

            var vm = createViewModel();

            var actual = vm.getOrders()
                .stream()
                .map(Order::id)
                .toList();

            assertEquals(expectedOrder, actual);
        }

        @Test
        @DisplayName("reports updated pending order count after refresh")
        void pendingCountUpdated() {
            repository.save(MockOrders.of("1", OVERDUE));

            var vm = createViewModel();
            vm.refresh();

            host.assertPendingOrderCount(1);
        }
    }

    @Nested
    @DisplayName("when an order is opened")
    class WhenAnOrderIsOpened {

        @Test
        @DisplayName("it displays the order")
        void navigationCallbackInvoked() {
            var order = MockOrders.of("1", RECENT);
            repository.save(order);

            var vm = createViewModel();
            vm.openOrder(order);

            host.assertOrderWasShown(order);
        }

        @Test
        @DisplayName("does nothing when no order found")
        void navigationCallbackNotInvokedForNull() {
            var vm = createViewModel();
            vm.openOrder(null);

            host.assertNoOrderWasShown();
        }
    }
}
