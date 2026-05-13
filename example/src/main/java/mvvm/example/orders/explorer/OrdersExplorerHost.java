package mvvm.example.orders.explorer;

import mvvm.example.orders.domain.Order;

public interface OrdersExplorerHost {
    void showOrderDetails(Order order);
    void setPendingOrderCount(int count);
}
