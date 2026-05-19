package mvvm.example.orders.domain;

import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class GetOrderSummariesQuery {

    private final OrderRepository orders;
    private final CustomerRepository customers;

    public GetOrderSummariesQuery(OrderRepository orders, CustomerRepository customers) {
        this.orders = orders;
        this.customers = customers;
    }

    public CompletableFuture<List<OrderSummary>> execute() {
        var customerNames = customers
            .findAll()
            .stream()
            .collect(Collectors.toMap(Customer::id, Customer::name));

        var results = orders
            .findAll()
            .stream()
            .map(order -> toSummary(order, customerNames))
            .toList();

        return CompletableFuture.supplyAsync(() -> results);
    }

    private static OrderSummary toSummary(Order order, Map<String, String> customerNames) {
        return new OrderSummary(
            order.id(),
            order.reference(),
            customerNames.getOrDefault(order.customerId(), ""),
            order.createdDate(),
            order.plannedShipDate(),
            order.status().displayName(),
            order.total(),
            order.isOverdue()
        );
    }
}
