package mvvm.example.shell.sidebar;

import mvvm.example.orders.context.OrderContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SidebarViewModel")
class SidebarViewModelTest {

    private static SidebarViewModel viewModelWith(OrderContext context) {
        return new SidebarViewModel(context, () -> {}, () -> {}, () -> {});
    }

    @Nested
    @DisplayName("pending order count")
    class PendingOrderCount {

        @Test
        @DisplayName("reflects the initial value from the context")
        void reflectsInitialContextValue() {
            var context = new OrderContext();
            context.setCount(3);

            var vm = viewModelWith(context);

            assertEquals(3, vm.pendingOrderCountProperty().get());
        }

        @Test
        @DisplayName("is synchronised with the context when the count changes")
        void synchronisedWithContext() {
            var context = new OrderContext();
            var vm = viewModelWith(context);

            context.setCount(5);

            assertEquals(5, vm.pendingOrderCountProperty().get());
        }
    }

    @Nested
    @DisplayName("when navigating")
    class WhenNavigating {

        @Test
        @DisplayName("the orders callback is invoked when navigating to orders")
        void ordersCallbackInvoked() {
            var invoked = new AtomicBoolean(false);
            var vm = new SidebarViewModel(new OrderContext(), () -> invoked.set(true), () -> {}, () -> {});

            vm.navigateToOrders();

            assertTrue(invoked.get());
        }

        @Test
        @DisplayName("the customers callback is invoked when navigating to customers")
        void customersCallbackInvoked() {
            var invoked = new AtomicBoolean(false);
            var vm = new SidebarViewModel(new OrderContext(), () -> {}, () -> invoked.set(true), () -> {});

            vm.navigateToCustomers();

            assertTrue(invoked.get());
        }

        @Test
        @DisplayName("the settings callback is invoked when navigating to settings")
        void settingsCallbackInvoked() {
            var invoked = new AtomicBoolean(false);
            var vm = new SidebarViewModel(new OrderContext(), () -> {}, () -> {}, () -> invoked.set(true));

            vm.navigateToSettings();

            assertTrue(invoked.get());
        }
    }
}
