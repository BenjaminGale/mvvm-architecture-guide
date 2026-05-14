package mvvm.example.orders.explorer;

import mvvm.example.orders.MockOrders;
import mvvm.example.orders.StubOrderRepository;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

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

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#refreshListCases")
        @DisplayName("it loads orders from storage")
        void reloadsOrdersFromStorage(String caseName, List<Order> input, List<String> expectedOrder) {
            input.forEach(repository::save);

            var vm = createViewModel();

            var actual = vm.getOrders()
                .stream()
                .map(Order::id)
                .toList();

            assertEquals(expectedOrder, actual);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#sortingCases")
        @DisplayName("it sorts orders by most recent date")
        void sortsOrdersByDateDescendingOnCreation(String caseName, List<Order> input, List<String> expectedOrder) {
            input.forEach(repository::save);

            var vm = createViewModel();

            var actual = vm.getOrders()
                .stream()
                .map(Order::id)
                .toList();

            assertEquals(expectedOrder, actual);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#statusTextCases")
        @DisplayName("it shows expected status label")
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

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#refreshListCases")
        @DisplayName("it reloads orders from storage")
        void reloadsOrdersFromStorage(String caseName, List<Order> input, List<String> expectedOrder) {
            input.forEach(repository::save);

            var vm = createViewModel();
            vm.refresh();

            var actual = vm.getOrders()
                .stream()
                .map(Order::id)
                .toList();

            assertEquals(expectedOrder, actual);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#statusTextCases")
        @DisplayName("it updates status label")
        void showsExpectedStatusText(String caseName, List<Order> orders, String expected) {
            orders.forEach(repository::save);

            var vm = createViewModel();
            vm.refresh();

            assertEquals(expected, vm.statusTextProperty().get());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#sortingCases")
        @DisplayName("it sorts orders by most recent date")
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
        @DisplayName("it reports updated pending order count")
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
        @DisplayName("it displays the selected order")
        void navigationCallbackInvoked() {
            var order = MockOrders.of("1", RECENT);
            repository.save(order);

            var vm = createViewModel();
            vm.openOrder(order);

            host.assertOrderWasShown(order);
        }

        @Test
        @DisplayName("it does nothing when no order selected")
        void navigationCallbackNotInvokedForNull() {
            var vm = createViewModel();
            vm.openOrder(null);

            host.assertNoOrderWasShown();
        }
    }

    @Nested
    @DisplayName("when refreshed multiple times")
    class WhenRefreshedMultipleTimes {

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#multipleRefreshCases")
        @DisplayName("it does not retain stale order state")
        void remainsConsistentAcrossMultipleRefreshCycles(
            String caseName,
            List<Order> initialOrders,
            Consumer<OrderRepository> repoUpdate,
            List<String> expectedIds
        ) {
            initialOrders.forEach(repository::save);
            var vm = createViewModel();

            vm.refresh();

            repoUpdate.accept(repository);
            vm.refresh();

            assertEquals(expectedIds, vm.getOrders().stream().map(Order::id).toList());
        }
    }
}
