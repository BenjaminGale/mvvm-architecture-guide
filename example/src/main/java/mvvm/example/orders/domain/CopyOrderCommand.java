package mvvm.example.orders.domain;

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
            original.lineItems().stream().map(item -> new LineItem(item.productId(), item.description(), item.quantity(), item.unitPrice())).toList()
        );

        repository.save(copy);

        return copy.id();
    }
}
