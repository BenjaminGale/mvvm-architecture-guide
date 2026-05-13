package mvvm.example.orders.explorer;

import mvvm.example.orders.domain.Order;

import java.util.List;

public interface OrdersExplorerService {
    List<Order> fetchAllOrders();
}
