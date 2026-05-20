package mvvm.example.orders.explorer;

import mvvm.example.core.viewmodel.ExplorerViewModelTest;
import mvvm.example.orders.MockOrders;
import mvvm.example.orders.domain.OrderSummary;
import mvvm.example.orders.requests.EditOrderRequest;
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
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Orders.OrdersExplorerViewModel")
class OrdersExplorerViewModelTest extends ExplorerViewModelTest<OrderSummary, OrdersExplorerViewModel> {

    private static final LocalDate RECENT = LocalDate.of(2026, 6, 10);
    private static final LocalDate OLDER = LocalDate.of(2026, 6, 1);
    private static final LocalDate OVERDUE = LocalDate.of(2026, 4, 1);

    @Mock private OrdersExplorerHost host;

    private List<OrderSummary> summaries;

    @BeforeEach
    void setUp() {
        summaries = new ArrayList<>();
    }

    @Override
    protected OrdersExplorerViewModel createViewModel() {
        return new OrdersExplorerViewModel(() -> CompletableFuture.completedFuture(List.copyOf(summaries)), host);
    }

    @Override
    protected OrderSummary createItem() {
        return MockOrders.summaryOf("1", RECENT);
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#refreshListCases")
        @DisplayName("it loads orders from storage")
        void reloadsOrdersFromStorage(String caseName, List<OrderSummary> input, List<String> expectedOrder) {
            summaries.addAll(input);
            var vm = createViewModel();
            executeFetch(vm);

            var actual = vm.items().stream().map(OrderSummary::id).toList();

            assertEquals(expectedOrder, actual);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#sortingCases")
        @DisplayName("it sorts orders by most recent date")
        void sortsOrdersByDateDescendingOnCreation(String caseName, List<OrderSummary> input, List<String> expectedOrder) {
            summaries.addAll(input);
            var vm = createViewModel();
            executeFetch(vm);

            var actual = vm.items().stream().map(OrderSummary::id).toList();

            assertEquals(expectedOrder, actual);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#statusMessageCases")
        @DisplayName("it shows expected order count in status bar")
        void showsExpectedOrderCount(String caseName, List<OrderSummary> input, int expectedOrderCount, int expectedOverdueCount) {
            summaries.addAll(input);
            var vm = createViewModel();
            executeFetch(vm);

            assertEquals(expectedOrderCount, vm.ordersCountProperty().get());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("mvvm.example.orders.explorer.OrdersExplorerViewModelScenarios#statusMessageCases")
        @DisplayName("it shows expected overdue count in status bar")
        void showsExpectedOverdueCount(String caseName, List<OrderSummary> input, int expectedOrderCount, int expectedOverdueCount) {
            summaries.addAll(input);
            var vm = createViewModel();
            executeFetch(vm);

            assertEquals(expectedOverdueCount, vm.overdueOrdersCountProperty().get());
        }

    }

    @Nested
    @DisplayName("when an order is opened")
    class WhenAnOrderIsOpened {

        @Test
        @DisplayName("it displays the selected order")
        void navigationCallbackInvoked() {
            var summary = MockOrders.summaryOf("1", RECENT);
            summaries.add(summary);
            var vm = createViewModel();
            executeFetch(vm);

            vm.selectedItemProperty().set(summary);
            vm.editItemAction().execute();

            verify(host).showOrderDetails(EditOrderRequest.of(summary.id()));
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
