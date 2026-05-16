package mvvm.example.shell.sidebar;

import mvvm.example.orders.context.OrderContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SidebarViewModel")
class SidebarViewModelTest {

    private static class StubHost implements SidebarHost {
        private boolean ordersInvoked = false;
        private boolean customersInvoked = false;

        public void openOrdersWorkspace() { ordersInvoked = true; }
        public void openCustomersWorkspace() { customersInvoked = true; }

        void assertOrdersWorkspaceOpened() { assertTrue(ordersInvoked); }
        void assertCustomersWorkspaceOpened() { assertTrue(customersInvoked); }
    }

    private static SidebarViewModel viewModelWith(OrderContext context) {
        return new SidebarViewModel(context, new StubHost());
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
            var host = new StubHost();
            var vm = new SidebarViewModel(new OrderContext(), host);

            vm.openOrdersWorkspace();

            host.assertOrdersWorkspaceOpened();
        }

        @Test
        @DisplayName("the customers callback is invoked when navigating to customers")
        void customersCallbackInvoked() {
            var host = new StubHost();
            var vm = new SidebarViewModel(new OrderContext(), host);

            vm.openCustomersWorkspace();

            host.assertCustomersWorkspaceOpened();
        }
    }
}
