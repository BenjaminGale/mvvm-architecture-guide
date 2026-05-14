package mvvm.example.orders.explorer;

import mvvm.example.orders.domain.Order;

import static org.junit.jupiter.api.Assertions.*;

public class MockOrdersExplorerHost implements OrdersExplorerHost {

    private Order shownOrder;
    private Integer pendingOrderCount;

    @Override public void showOrderDetails(Order order) {
        shownOrder = order;
    }
    @Override public void setPendingOrderCount(int count) {
        pendingOrderCount = count;
    }

    public void assertOrderWasShown(Order order) {
        assertEquals(order, shownOrder);
    }

    public void assertNoOrderWasShown() {
        assertNull(shownOrder);
    }

    public void assertPendingOrderCount(int count) {
        assertEquals(count, pendingOrderCount);
    }
}
