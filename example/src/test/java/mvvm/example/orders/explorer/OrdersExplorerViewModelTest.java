package mvvm.example.orders.explorer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mvvm.example.core.viewmodel.ExplorerViewModelTest;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Orders.OrdersExplorerViewModel")
class OrdersExplorerViewModelTest extends ExplorerViewModelTest<Order, OrdersExplorerViewModel> {

    private static final LocalDate RECENT = LocalDate.of(2026, 6, 10);
    private static final LocalDate OLDER = LocalDate.of(2026, 6, 1);
    private static final LocalDate OVERDUE = LocalDate.of(2026, 4, 1);

    @Mock private OrdersExplorerHost host;

    private List<Order> orders;
    private ObservableList<StatusItemViewModel> statusItems;

    @BeforeEach
    void setUp() {
        orders = new ArrayList<>();
        statusItems = FXCollections.observableArrayList();
    }

    @Override
    protected OrdersExplorerViewModel createViewModel() {
        return new OrdersExplorerViewModel(() -> List.copyOf(orders), host, statusItems);
    }

    @Override
    protected Order createItem() {
        return MockOrders.of("1", RECENT);
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
            executeFetch(vm);

            var actual = vm.items().stream().map(Order::id).toList();

            assertEquals(expectedOrder, actual);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#sortingCases")
        @DisplayName("it sorts orders by most recent date")
        void sortsOrdersByDateDescendingOnCreation(String caseName, List<Order> input, List<String> expectedOrder) {
            orders.addAll(input);
            var vm = createViewModel();
            executeFetch(vm);

            var actual = vm.items().stream().map(Order::id).toList();

            assertEquals(expectedOrder, actual);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#statusMessageCases")
        @DisplayName("it shows expected order count in status bar")
        void showsExpectedOrderCount(String caseName, List<Order> input, int expectedOrderCount, int expectedOverdueCount) {
            orders.addAll(input);
            var vm = createViewModel();
            executeFetch(vm);

            assertEquals(expectedOrderCount, vm.ordersCountProperty().get());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#statusMessageCases")
        @DisplayName("it shows expected overdue count in status bar")
        void showsExpectedOverdueCount(String caseName, List<Order> input, int expectedOrderCount, int expectedOverdueCount) {
            orders.addAll(input);
            var vm = createViewModel();
            executeFetch(vm);

            assertEquals(expectedOverdueCount, vm.overdueOrdersCountProperty().get());
        }

        @Test
        @DisplayName("it adds a status item for order count")
        void addsOrderCountStatusItem() {
            orders.add(MockOrders.of("1", RECENT));
            orders.add(MockOrders.of("2", RECENT));
            var vm = createViewModel();
            executeFetch(vm);

            assertEquals(2, statusItems.getFirst().countProperty().get());
            assertEquals(LabelType.All_ORDERS, statusItems.getFirst().label());
        }

        @Test
        @DisplayName("it adds a status item for overdue count")
        void addsOverdueCountStatusItem() {
            orders.add(MockOrders.of("1", RECENT));
            orders.add(MockOrders.of("2", OVERDUE));
            var vm = createViewModel();
            executeFetch(vm);

            assertEquals(1, statusItems.getLast().countProperty().get());
            assertEquals(LabelType.OVERDUE_ORDERS, statusItems.getLast().label());
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
            executeFetch(vm);

            vm.selectedItemProperty().set(order);
            vm.editItemAction().execute();

            verify(host).showOrderDetails(EditOrderRequest.of(order.id()));
        }
    }

    @Nested
    @DisplayName("when a new order is added")
    class WhenANewOrderIsAdded {

        @Test
        @DisplayName("it shows the order details for a new order")
        void navigationCallbackInvoked() {
            var vm = createViewModel();

            vm.addItemAction().execute();

            verify(host).showOrderDetails(EditOrderRequest.forNewOrder());
        }
    }
}
