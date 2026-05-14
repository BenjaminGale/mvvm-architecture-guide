package mvvm.example.orders.domain;

import java.time.LocalDate;
import java.util.UUID;

public class CopyOrderService {

    private final OrderRepository repository;

    public CopyOrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public Order copy(String id) {
        var original = repository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        var copy = new Order(
            UUID.randomUUID().toString(),
            original.customerName(),
            LocalDate.now(),
            "COPY-" + original.reference(),
            original.lineItems()
        );

        repository.save(copy);

        return copy;
    }
}
