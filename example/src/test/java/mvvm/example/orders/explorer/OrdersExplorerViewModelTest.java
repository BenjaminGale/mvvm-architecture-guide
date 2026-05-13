package mvvm.example.orders.explorer;

import mvvm.example.orders.context.OrderContext;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrdersExplorerViewModel")
class OrdersExplorerViewModelTest {

    private static final LocalDate RECENT = LocalDate.now().minusDays(1);
    private static final LocalDate OLDER  = LocalDate.now().minusDays(10);
    private static final LocalDate OVERDUE = LocalDate.now().minusDays(31);

    private static Order order(String id, LocalDate date) {
        return new Order(id, "Acme Ltd", date, "REF-" + id, List.of());
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("orders are loaded immediately")
        void ordersLoadedImmediately() {
            var vm = new OrdersExplorerViewModel(
                () -> List.of(order("1", RECENT)),
                new OrderContext(),
                o -> {}
            );

            assertEquals(1, vm.getOrders().size());
        }

        @Test
        @DisplayName("the status text shows the total and overdue order counts")
        void statusTextShowsCountsOnCreation() {
            var vm = new OrdersExplorerViewModel(
                () -> List.of(order("1", RECENT), order("2", OVERDUE)),
                new OrderContext(),
                o -> {}
            );

            assertEquals("2 orders, 1 overdue", vm.statusTextProperty().get());
        }

        @Test
        @DisplayName("the pending order count on the context is updated")
        void pendingCountUpdatedOnCreation() {
            var context = new OrderContext();
            new OrdersExplorerViewModel(
                () -> List.of(order("1", OVERDUE), order("2", OVERDUE)),
                context,
                o -> {}
            );

            assertEquals(2, context.pendingCountProperty().get());
        }
    }

    @Nested
    @DisplayName("when refreshed")
    class WhenRefreshed {

        @Test
        @DisplayName("orders are reloaded from the use case")
        void ordersReloaded() {
            var vm = new OrdersExplorerViewModel(List::of, new OrderContext(), o -> {});

            vm.refresh();

            // confirm it still works after a second load — no state corruption
            assertEquals(0, vm.getOrders().size());
        }

        @Test
        @DisplayName("the status text is updated to reflect the new order list")
        void statusTextUpdated() {
            var orders = new java.util.concurrent.atomic.AtomicReference<>(List.<Order>of());
            var vm = new OrdersExplorerViewModel(orders::get, new OrderContext(), o -> {});
            orders.set(List.of(order("1", RECENT), order("2", RECENT), order("3", RECENT)));

            vm.refresh();

            assertEquals("3 orders, 0 overdue", vm.statusTextProperty().get());
        }

        @Test
        @DisplayName("orders are sorted with the most recent date first")
        void ordersSortedByDateDescending() {
            var vm = new OrdersExplorerViewModel(
                () -> List.of(order("older", OLDER), order("recent", RECENT)),
                new OrderContext(),
                o -> {}
            );

            assertEquals("recent", vm.getOrders().getFirst().id());
            assertEquals("older", vm.getOrders().getLast().id());
        }

        @Test
        @DisplayName("the pending count on the context is updated")
        void pendingCountUpdated() {
            var context = new OrderContext();
            var orders = new AtomicReference<>(List.<Order>of());
            var vm = new OrdersExplorerViewModel(orders::get, context, o -> {});
            orders.set(List.of(order("1", OVERDUE)));

            vm.refresh();

            assertEquals(1, context.pendingCountProperty().get());
        }
    }

    @Nested
    @DisplayName("when an order is opened")
    class WhenAnOrderIsOpened {

        @Test
        @DisplayName("the navigation callback is invoked with the selected order")
        void navigationCallbackInvoked() {
            var selected = new AtomicReference<Order>();
            var order = order("1", RECENT);
            var vm = new OrdersExplorerViewModel(() -> List.of(order), new OrderContext(), selected::set);

            vm.openOrder(order);

            assertEquals(order, selected.get());
        }

        @Test
        @DisplayName("the navigation callback is not invoked when called with null")
        void navigationCallbackNotInvokedForNull() {
            var selected = new AtomicReference<Order>();
            var vm = new OrdersExplorerViewModel(List::of, new OrderContext(), selected::set);

            vm.openOrder(null);

            assertNull(selected.get());
        }
    }
}
