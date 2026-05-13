package mvvm.example.orders.editor;

import mvvm.example.orders.domain.Order;

public interface OrderEditorService {
    void saveOrder(Order order);
    Order copyOrder(String orderId);
    void deleteOrder(String orderId);
}
