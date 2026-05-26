package mvvm.example.orders.domain.commands;

import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderRepository;

import java.util.UUID;

public class CopyOrderCommand {

    private final OrderRepository repository;

    public CopyOrderCommand(OrderRepository repository) {
        this.repository = repository;
    }

    public UUID execute(UUID orderId) {
        var original = repository
            .findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        var copy = Order.create(
            original.customerId(),
            "COPY-" + original.reference(),
            null,
            original.lineItems()
        );

        repository.save(copy);

        return copy.id();
    }
}
