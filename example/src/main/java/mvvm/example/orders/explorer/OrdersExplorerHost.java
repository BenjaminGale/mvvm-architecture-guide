package mvvm.example.orders.explorer;

import mvvm.example.orders.requests.EditOrderRequest;

public interface OrdersExplorerHost {
    void showOrderDetails(EditOrderRequest request);
    void setPendingOrderCount(int count);
}
