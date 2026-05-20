package mvvm.example.orders.domain.commands;

import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.orders.domain.OrderStatus;

import java.time.LocalDate;
import java.util.UUID;

public class CopyOrderCommand {

    private final OrderRepository repository;

    public CopyOrderCommand(OrderRepository repository) {
        this.repository = repository;
    }

    public String copy(String orderId) {
        var original = repository
            .findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        var copy = new Order(
            UUID.randomUUID().toString(),
            original.customerId(),
            LocalDate.now(),
            null,
            "COPY-" + original.reference(),
            OrderStatus.PENDING,
            null,
            original.lineItems()
        );

        repository.save(copy);

        return copy.id();
    }
}
