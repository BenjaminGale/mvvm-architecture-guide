package mvvm.example.orders.explorer;

import mvvm.example.orders.editor.EditOrderRequest;

import static org.junit.jupiter.api.Assertions.*;

public class MockOrdersExplorerHost implements OrdersExplorerHost {

    private EditOrderRequest request;
    private Integer pendingOrderCount;

    @Override public void showOrderDetails(EditOrderRequest request) { this.request = request; }
    @Override public void setPendingOrderCount(int count) {
        pendingOrderCount = count;
    }

    public void assertOrderWasShown(String orderId) {
        assertEquals(orderId, request.orderId());
    }

    public void assertNoOrderWasShown() {
        assertNull(request);
    }

    public void assertPendingOrderCount(int count) {
        assertEquals(count, pendingOrderCount);
    }
}
