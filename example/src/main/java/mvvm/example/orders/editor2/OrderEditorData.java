package mvvm.example.orders.editor2;

import mvvm.example.customers.domain.Customer;
import mvvm.example.orders.domain.Order;

import java.util.Map;

public record OrderEditorData(
    Order order,
    Customer customer,
    Map<String, Integer> allocations
) {}
