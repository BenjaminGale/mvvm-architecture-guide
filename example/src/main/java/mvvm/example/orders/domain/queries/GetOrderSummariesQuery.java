package mvvm.example.orders.domain.queries;

import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderRepository;

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
        return CompletableFuture.supplyAsync(() -> {
            var customerNames = customers
                .findAll()
                .stream()
                .collect(Collectors.toMap(Customer::id, Customer::name));

            return orders
                .findAll()
                .stream()
                .map(order -> toSummary(order, customerNames))
                .toList();
        });
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
