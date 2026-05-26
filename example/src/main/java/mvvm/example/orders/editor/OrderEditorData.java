package mvvm.example.orders.editor;

import mvvm.example.customers.domain.Customer;
import mvvm.example.orders.domain.Order;

public record OrderEditorData(
    Order order,
    Customer customer
) {}
