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
        @DisplayName("it shows total and overdue order counts")
        void showsExpectedStatusText() {
            repository.save(MockOrders.of("1", RECENT));
            repository.save(MockOrders.of("2", OVERDUE));

            var vm = createViewModel();

            assertEquals("2 orders, 1 overdue", vm.statusTextProperty().get());
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
        @DisplayName("reloads orders from storage")
        void ordersReloaded() {
            var vm = createViewModel();
            vm.refresh();
            assertEquals(0, vm.getOrders().size());
        }

        @Test
        @DisplayName("updates status text after refresh")
        void statusTextUpdated() {
            repository.save(MockOrders.of("1", RECENT));
            repository.save(MockOrders.of("2", RECENT));
            repository.save(MockOrders.of("3", RECENT));

            var vm = createViewModel();
            vm.refresh();

            assertEquals("3 orders, 0 overdue", vm.statusTextProperty().get());
        }

        @Test
        @DisplayName("sorts orders by most recent date first")
        void ordersSortedByDateDescending() {
            repository.save(MockOrders.of("older", OLDER));
            repository.save(MockOrders.of("recent", RECENT));

            var vm = createViewModel();

            assertEquals("recent", vm.getOrders().getFirst().id());
            assertEquals("older", vm.getOrders().getLast().id());
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
