package mvvm.example.orders.explorer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mvvm.example.orders.MockOrders;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.requests.EditOrderRequest;
import mvvm.example.shell.main.statusbar.LabelType;
import mvvm.example.shell.main.statusbar.StatusItemViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Orders.OrdersExplorerViewModel")
class OrdersExplorerViewModelTest {

    private static final LocalDate RECENT = LocalDate.of(2026, 5, 10);
    private static final LocalDate OLDER = LocalDate.of(2026, 5, 1);
    private static final LocalDate OVERDUE = LocalDate.of(2026, 4, 1);

    @Mock private OrdersExplorerHost host;

    private List<Order> orders;
    private ObservableList<StatusItemViewModel> statusItems;

    @BeforeEach
    void setUp() {
        orders = new ArrayList<>();
        statusItems = FXCollections.observableArrayList();
    }

    private OrdersExplorerViewModel createViewModel() {
        return new OrdersExplorerViewModel(() -> List.copyOf(orders), host, statusItems);
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#refreshListCases")
        @DisplayName("it loads orders from storage")
        void reloadsOrdersFromStorage(String caseName, List<Order> input, List<String> expectedOrder) {
            orders.addAll(input);

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
            orders.addAll(input);

            var vm = createViewModel();

            var actual = vm.getOrders()
                .stream()
                .map(Order::id)
                .toList();

            assertEquals(expectedOrder, actual);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#statusMessageCases")
        @DisplayName("it shows expected order count in status bar")
        void showsExpectedOrderCount(String caseName, List<Order> input, int expectedOrderCount, int expectedOverdueCount) {
            orders.addAll(input);

            var vm = createViewModel();

            assertEquals(expectedOrderCount, vm.ordersCountProperty().get());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#statusMessageCases")
        @DisplayName("it shows expected overdue count in status bar")
        void showsExpectedOverdueCount(String caseName, List<Order> input, int expectedOrderCount, int expectedOverdueCount) {
            orders.addAll(input);

            var vm = createViewModel();

            assertEquals(expectedOverdueCount, vm.overdueOrdersCountProperty().get());
        }

        @Test
        @DisplayName("it reports pending order count")
        void pendingCountUpdated() {
            orders.add(MockOrders.of("1", OVERDUE));
            orders.add(MockOrders.of("2", OVERDUE));

            createViewModel();

            verify(host).setPendingOrderCount(2);
        }

        @Test
        @DisplayName("it adds a status item for order count")
        void addsOrderCountStatusItem() {
            orders.add(MockOrders.of("1", RECENT));
            orders.add(MockOrders.of("2", RECENT));

            createViewModel();

            assertEquals(2, statusItems.getFirst().countProperty().get());
            assertEquals(LabelType.All_ORDERS, statusItems.getFirst().label());
        }

        @Test
        @DisplayName("it adds a status item for overdue count")
        void addsOverdueCountStatusItem() {
            orders.add(MockOrders.of("1", RECENT));
            orders.add(MockOrders.of("2", OVERDUE));

            createViewModel();

            assertEquals(1, statusItems.getLast().countProperty().get());
            assertEquals(LabelType.OVERDUE_ORDERS, statusItems.getLast().label());
        }

        @Test
        @DisplayName("it keeps status items in sync when refreshed")
        void statusItemsUpdateOnRefresh() {
            var vm = createViewModel();

            orders.add(MockOrders.of("1", RECENT));
            orders.add(MockOrders.of("2", OVERDUE));
            vm.refresh();

            assertEquals(2, statusItems.getFirst().countProperty().get());
            assertEquals(1, statusItems.getLast().countProperty().get());
        }
    }

    @Nested
    @DisplayName("when refreshed")
    class WhenRefreshed {

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#refreshListCases")
        @DisplayName("it reloads orders from storage")
        void reloadsOrdersFromStorage(String caseName, List<Order> input, List<String> expectedOrder) {
            orders.addAll(input);

            var vm = createViewModel();
            vm.refresh();

            var actual = vm.getOrders()
                .stream()
                .map(Order::id)
                .toList();

            assertEquals(expectedOrder, actual);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#statusMessageCases")
        @DisplayName("it updates order count in status bar")
        void updatesOrderCount(String caseName, List<Order> input, int expectedOrderCount, int expectedOverdueCount) {
            orders.addAll(input);

            var vm = createViewModel();
            vm.refresh();

            assertEquals(expectedOrderCount, vm.ordersCountProperty().get());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#statusMessageCases")
        @DisplayName("it updates overdue count in status bar")
        void updatesOverdueCount(String caseName, List<Order> input, int expectedOrderCount, int expectedOverdueCount) {
            orders.addAll(input);

            var vm = createViewModel();
            vm.refresh();

            assertEquals(expectedOverdueCount, vm.overdueOrdersCountProperty().get());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#sortingCases")
        @DisplayName("it sorts orders by most recent date")
        void sortsOrdersByDateDescending(String caseName, List<Order> input, List<String> expectedOrder) {
            orders.addAll(input);

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
            orders.add(MockOrders.of("1", OVERDUE));

            var vm = createViewModel();
            vm.refresh();

            verify(host, atLeastOnce()).setPendingOrderCount(1);
        }
    }

    @Nested
    @DisplayName("when an order is opened")
    class WhenAnOrderIsOpened {

        @Test
        @DisplayName("it displays the selected order")
        void navigationCallbackInvoked() {
            var order = MockOrders.of("1", RECENT);
            orders.add(order);

            var vm = createViewModel();
            vm.selectedOrderProperty().set(order);
            vm.openOrderAction().execute();

            verify(host).showOrderDetails(new EditOrderRequest(order.id()));
        }
    }

    @Nested
    @DisplayName("when no order is selected")
    class WhenNoOrderIsSelected {

        @Test
        @DisplayName("it cannot open an order")
        void cannotExecuteWhenNoOrderSelected() {
            var vm = createViewModel();

            assertFalse(vm.openOrderAction().canExecute());
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
            Consumer<List<Order>> update,
            List<String> expectedIds
        ) {
            orders.addAll(initialOrders);
            var vm = createViewModel();

            vm.refresh();

            update.accept(orders);
            vm.refresh();

            assertEquals(expectedIds, vm.getOrders().stream().map(Order::id).toList());
        }
    }
}
