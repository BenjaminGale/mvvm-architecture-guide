package mvvm.example.orders.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Orders.OrderContext")
class OrderContextTest {

    @Nested
    @DisplayName("when the pending count is set")
    class WhenPendingCountIsSet {

        @Test
        @DisplayName("the pending count property reflects the new value")
        void pendingCountPropertyReflectsNewValue() {
            var context = new OrderContext();

            context.setCount(7);

            assertEquals(7, context.overdueOrderCountProperty().get());
        }

        @Test
        @DisplayName("listeners are notified when the count changes")
        void listenersNotifiedOnChange() {
            var context = new OrderContext();
            var observed = new int[]{0};
            context.overdueOrderCountProperty().addListener((obs, old, value) -> observed[0] = value.intValue());

            context.setCount(4);

            assertEquals(4, observed[0]);
        }
    }
}
