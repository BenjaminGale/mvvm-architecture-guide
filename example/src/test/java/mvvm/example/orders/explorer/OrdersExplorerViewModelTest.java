package mvvm.example.orders.explorer;

import mvvm.example.orders.MockOrders;
import mvvm.example.orders.StubOrderRepository;
import mvvm.example.orders.domain.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrdersExplorerViewModel")
class OrdersExplorerViewModelTest {

    private static final LocalDate RECENT = LocalDate.now().minusDays(1);
    private static final LocalDate OLDER  = LocalDate.now().minusDays(10);
    private static final LocalDate OVERDUE = LocalDate.now().minusDays(31);

    private static StubOrderRepository repositoryWith(Order... orders) {
        return new StubOrderRepository(orders);
    }

    private static OrdersExplorerHost nullHost() {
        return new OrdersExplorerHost() {
            @Override public void showOrderDetails(Order order) {}
            @Override public void setPendingOrderCount(int count) {}
        };
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("orders are loaded immediately")
        void ordersLoadedImmediately() {
            var vm = new OrdersExplorerViewModel(
                () -> repositoryWith(MockOrders.of("1", RECENT)).findAll(),
                nullHost()
            );

            assertEquals(1, vm.getOrders().size());
        }

        @Test
        @DisplayName("the status text shows the total and overdue order counts")
        void statusTextShowsCountsOnCreation() {
            var vm = new OrdersExplorerViewModel(
                () -> repositoryWith(MockOrders.of("1", RECENT), MockOrders.of("2", OVERDUE)).findAll(),
                nullHost()
            );

            assertEquals("2 orders, 1 overdue", vm.statusTextProperty().get());
        }

        @Test
        @DisplayName("the pending order count on the context is updated")
        void pendingCountUpdatedOnCreation() {
            var host = new MockOrdersExplorerHost();
            new OrdersExplorerViewModel(
                () -> repositoryWith(MockOrders.of("1", OVERDUE), MockOrders.of("2", OVERDUE)).findAll(),
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
                () -> repositoryWith().findAll(),
                nullHost()
            );

            vm.refresh();

            assertEquals(0, vm.getOrders().size());
        }

        @Test
        @DisplayName("the status text is updated to reflect the new order list")
        void statusTextUpdated() {
            var repo = repositoryWith();
            var vm = new OrdersExplorerViewModel(
                repo::findAll,
                nullHost()
            );

            repo.save(MockOrders.of("1", RECENT));
            repo.save(MockOrders.of("2", RECENT));
            repo.save(MockOrders.of("3", RECENT));

            vm.refresh();

            assertEquals("3 orders, 0 overdue", vm.statusTextProperty().get());
        }

        @Test
        @DisplayName("orders are sorted with the most recent date first")
        void ordersSortedByDateDescending() {
            var vm = new OrdersExplorerViewModel(
                () -> repositoryWith(MockOrders.of("older", OLDER), MockOrders.of("recent", RECENT)).findAll(),
                nullHost()
            );

            assertEquals("recent", vm.getOrders().getFirst().id());
            assertEquals("older", vm.getOrders().getLast().id());
        }

        @Test
        @DisplayName("the pending count on the context is updated")
        void pendingCountUpdated() {
            var repo = repositoryWith();
            var host = new MockOrdersExplorerHost();
            var vm = new OrdersExplorerViewModel(
                repo::findAll,
                host
            );

            repo.save(MockOrders.of("1", OVERDUE));

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
            var host = new MockOrdersExplorerHost();
            var order = MockOrders.of("1", RECENT);
            var vm = new OrdersExplorerViewModel(
                () -> repositoryWith(order).findAll(),
                host
            );

            vm.openOrder(order);

            host.assertOrderWasShown(order);
        }

        @Test
        @DisplayName("the navigation callback is not invoked when called with null")
        void navigationCallbackNotInvokedForNull() {
            var selected = new AtomicReference<Order>();
            var vm = new OrdersExplorerViewModel(
                () -> repositoryWith().findAll(),
                nullHost()
            );

            vm.openOrder(null);

            assertNull(selected.get());
        }
    }
}
