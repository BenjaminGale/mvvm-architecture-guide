package mvvm.example.orders.explorer;

import mvvm.example.orders.MockOrders;
import mvvm.example.orders.StubOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

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

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("orders are loaded immediately")
        void ordersLoadedImmediately() {
            repository.save(MockOrders.of("1", RECENT));

            var vm = new OrdersExplorerViewModel(
                repository::findAll,
                host
            );

            assertEquals(1, vm.getOrders().size());
        }

        @Test
        @DisplayName("the status text shows the total and overdue order counts")
        void statusTextShowsCountsOnCreation() {
            repository.save(MockOrders.of("1", RECENT));
            repository.save(MockOrders.of("2", OVERDUE));

            var vm = new OrdersExplorerViewModel(
                repository::findAll,
                host
            );

            assertEquals("2 orders, 1 overdue", vm.statusTextProperty().get());
        }

        @Test
        @DisplayName("the pending order count on the context is updated")
        void pendingCountUpdatedOnCreation() {
            repository.save(MockOrders.of("1", OVERDUE));
            repository.save(MockOrders.of("2", OVERDUE));

            new OrdersExplorerViewModel(
                repository::findAll,
                host
            );

            host.assertPendingOrderCount(2);
        }
    }

    @Nested
    @DisplayName("when refreshed")
    class WhenRefreshed {

        @Test
        @DisplayName("orders are reloaded from the service")
        void ordersReloaded() {
            var vm = new OrdersExplorerViewModel(
                repository::findAll,
                host
            );

            vm.refresh();

            assertEquals(0, vm.getOrders().size());
        }

        @Test
        @DisplayName("the status text is updated to reflect the new order list")
        void statusTextUpdated() {
            var vm = new OrdersExplorerViewModel(
                repository::findAll,
                host
            );

            repository.save(MockOrders.of("1", RECENT));
            repository.save(MockOrders.of("2", RECENT));
            repository.save(MockOrders.of("3", RECENT));

            vm.refresh();

            assertEquals("3 orders, 0 overdue", vm.statusTextProperty().get());
        }

        @Test
        @DisplayName("orders are sorted with the most recent date first")
        void ordersSortedByDateDescending() {
            repository.save(MockOrders.of("older", OLDER));
            repository.save(MockOrders.of("recent", RECENT));

            var vm = new OrdersExplorerViewModel(
                repository::findAll,
                host
            );

            assertEquals("recent", vm.getOrders().getFirst().id());
            assertEquals("older", vm.getOrders().getLast().id());
        }

        @Test
        @DisplayName("the pending count on the context is updated")
        void pendingCountUpdated() {
            var vm = new OrdersExplorerViewModel(
                repository::findAll,
                host
            );

            repository.save(MockOrders.of("1", OVERDUE));

            vm.refresh();

            host.assertPendingOrderCount(1);
        }
    }

    @Nested
    @DisplayName("when an order is opened")
    class WhenAnOrderIsOpened {

        @Test
        @DisplayName("the navigation callback is invoked with the selected order")
        void navigationCallbackInvoked() {
            var order = MockOrders.of("1", RECENT);
            var vm = new OrdersExplorerViewModel(
                repository::findAll,
                host
            );

            repository.save(order);

            vm.openOrder(order);

            host.assertOrderWasShown(order);
        }

        @Test
        @DisplayName("the navigation callback is not invoked when called with null")
        void navigationCallbackNotInvokedForNull() {
            var vm = new OrdersExplorerViewModel(
                repository::findAll,
                host
            );

            vm.openOrder(null);

            host.assertNoOrderWasShown();
        }
    }
}
