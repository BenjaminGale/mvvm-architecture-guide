package mvvm.example.orders.editor;

import mvvm.example.orders.domain.Order;

public interface OrderEditorService {
    Order fetchOrder(String orderId);
    void saveOrder(Order order);
    String copyOrder(String orderId);
    void deleteOrder(String orderId);
}
