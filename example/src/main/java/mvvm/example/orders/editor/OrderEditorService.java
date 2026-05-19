package mvvm.example.orders.editor;

import mvvm.example.customers.domain.Customer;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.LineItemSummary;
import mvvm.example.orders.domain.Order;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface OrderEditorService {
    Order fetchOrder(String orderId);
    Optional<Customer> findCustomer(String customerId);
    void saveOrder(Order order);
    String copyOrder(String orderId);
    void deleteOrder(String orderId);
    CompletableFuture<List<LineItemSummary>> fetchLineItemSummaries(List<LineItem> items, String orderId);
}
