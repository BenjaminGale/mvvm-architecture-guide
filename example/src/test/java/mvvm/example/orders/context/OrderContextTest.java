package mvvm.example.orders.context;

import mvvm.example.orders.domain.Order;
import mvvm.example.orders.explorer.OrdersExplorerViewModel;
import mvvm.example.shell.sidebar.SidebarViewModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderContext")
class OrderContextTest {

    private static Order overdueOrder(String id) {
        return new Order(id, "Acme Ltd", LocalDate.now().minusDays(31), "REF-" + id, List.of());
    }

    @Nested
    @DisplayName("when the pending count is set")
    class WhenPendingCountIsSet {

        @Test
        @DisplayName("the pending count property reflects the new value")
        void pendingCountPropertyReflectsNewValue() {
            var context = new OrderContext();

            context.setPendingCount(7);

            assertEquals(7, context.pendingCountProperty().get());
        }

        @Test
        @DisplayName("listeners are notified when the count changes")
        void listenersNotifiedOnChange() {
            var context = new OrderContext();
            var observed = new int[]{0};
            context.pendingCountProperty().addListener((obs, old, value) -> observed[0] = value.intValue());

            context.setPendingCount(4);

            assertEquals(4, observed[0]);
        }
    }

    @Nested
    @DisplayName("when shared between ViewModels")
    class WhenSharedBetweenViewModels {

        @Test
        @DisplayName("the sidebar reflects the pending count set by OrdersExplorerViewModel on refresh")
        void sidebarReflectsCountFromOrdersViewModel() {
            var context = new OrderContext();
            var ordersVm = new OrdersExplorerViewModel(
                () -> List.of(overdueOrder("1"), overdueOrder("2")),
                context,
                order -> {}
            );
            var sidebarVm = new SidebarViewModel(context, () -> {}, () -> {}, () -> {});

            ordersVm.refresh();

            assertEquals(2, sidebarVm.pendingOrderCountProperty().get());
        }
    }
}
