package mvvm.example.orders.editor;

import mvvm.example.customers.domain.Customer;
import mvvm.example.orders.domain.Order;

import java.util.Map;
import java.util.UUID;

public record OrderEditorData(
    Order order,
    Customer customer,
    Map<UUID, Integer> allocations
) {}
