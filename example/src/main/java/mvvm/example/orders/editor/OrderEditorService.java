package mvvm.example.orders.editor;

import mvvm.example.customers.domain.Customer;
import mvvm.example.orders.domain.Order;

import java.util.Optional;

public interface OrderEditorService {
    Order fetchOrder(String orderId);
    Optional<Customer> findCustomer(String customerId);
    void saveOrder(Order order);
    String copyOrder(String orderId);
    void deleteOrder(String orderId);
}
