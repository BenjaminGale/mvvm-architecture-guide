package mvvm.example.orders.explorer;

import mvvm.example.orders.StubOrderRepository;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrdersExplorerViewModel")
class OrdersExplorerViewModelTest {

    public static class MockOrdersExplorerHost implements OrdersExplorerHost {

        private Order shownOrder;
        private Integer pendingOrderCount;

        @Override
        public void showOrderDetails(Order order) {
            shownOrder = order;
        }

        @Override
        public void setPendingOrderCount(int count) {
            pendingOrderCount = count;
        }

        public void assertOrderWasShown(Order order) {
            assertEquals(order, shownOrder);
        }

        public void assertPendingOrderCount(int count) {
            assertEquals(count, pendingOrderCount);
        }
    }

    private static final LocalDate RECENT = LocalDate.now().minusDays(1);
    private static final LocalDate OLDER  = LocalDate.now().minusDays(10);
    private static final LocalDate OVERDUE = LocalDate.now().minusDays(31);

    private static Order order(String id, LocalDate date) {
        return new Order(id, "Acme Ltd", date, "REF-" + id, List.of());
    }

    private static OrderService serviceWith(Order... orders) {
        return new OrderService(new StubOrderRepository(orders));
    }

    private static OrdersExplorerHost host() {
        return new OrdersExplorerHost() {
            @Override
            public void showOrderDetails(Order order) {

            }

            @Override
            public void setPendingOrderCount(int count) {

            }
        };
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("orders are loaded immediately")
        void ordersLoadedImmediately() {
            var vm = new OrdersExplorerViewModel(
                () -> serviceWith(order("1", RECENT)).fetchAll(),
                host()
            );

            assertEquals(1, vm.getOrders().size());
        }

        @Test
        @DisplayName("the status text shows the total and overdue order counts")
        void statusTextShowsCountsOnCreation() {
            var vm = new OrdersExplorerViewModel(
                () -> serviceWith(order("1", RECENT), order("2", OVERDUE)).fetchAll(),
                host()
            );

            assertEquals("2 orders, 1 overdue", vm.statusTextProperty().get());
        }

        @Test
        @DisplayName("the pending order count on the context is updated")
        void pendingCountUpdatedOnCreation() {
            var host = new MockOrdersExplorerHost();
            new OrdersExplorerViewModel(
                () -> serviceWith(order("1", OVERDUE), order("2", OVERDUE)).fetchAll(),
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
                () -> serviceWith().fetchAll(),
                host()
            );

            vm.refresh();

            assertEquals(0, vm.getOrders().size());
        }

        @Test
        @DisplayName("the status text is updated to reflect the new order list")
        void statusTextUpdated() {
            var repo = new StubOrderRepository();
            var vm = new OrdersExplorerViewModel(
                () -> new OrderService(repo).fetchAll(),
                host()
            );

            repo.save(order("1", RECENT));
            repo.save(order("2", RECENT));
            repo.save(order("3", RECENT));

            vm.refresh();

            assertEquals("3 orders, 0 overdue", vm.statusTextProperty().get());
        }

        @Test
        @DisplayName("orders are sorted with the most recent date first")
        void ordersSortedByDateDescending() {
            var vm = new OrdersExplorerViewModel(
                () -> serviceWith(order("older", OLDER), order("recent", RECENT)).fetchAll(),
                host()
            );

            assertEquals("recent", vm.getOrders().getFirst().id());
            assertEquals("older", vm.getOrders().getLast().id());
        }

        @Test
        @DisplayName("the pending count on the context is updated")
        void pendingCountUpdated() {
            var repo = new StubOrderRepository();
            var host = new MockOrdersExplorerHost();
            var vm = new OrdersExplorerViewModel(
                () -> new OrderService(repo).fetchAll(),
                host
            );

            repo.save(order("1", OVERDUE));

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
            var order = order("1", RECENT);
            var vm = new OrdersExplorerViewModel(
                () -> serviceWith(order).fetchAll(),
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
                () -> serviceWith().fetchAll(),
                host()
            );

            vm.openOrder(null);

            assertNull(selected.get());
        }
    }
}
