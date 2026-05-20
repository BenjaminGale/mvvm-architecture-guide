package mvvm.example.orders.domain.commands;

import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.orders.domain.PendingOrder;

import java.time.LocalDate;
import java.util.UUID;

public class CopyOrderCommand {

    private final OrderRepository repository;

    public CopyOrderCommand(OrderRepository repository) {
        this.repository = repository;
    }

    public String copy(String id) {
        var original = repository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        var copy = new PendingOrder(
            UUID.randomUUID().toString(),
            original.customerId(),
            LocalDate.now(),
            null,
            "COPY-" + original.reference(),
            original.lineItems()
        );

        repository.save(copy);

        return copy.id();
    }
}
