package mvvm.example.orders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public List<Order> fetchAll() {
        return repository.findAll();
    }

    public void save(Order order) {
        repository.save(order);
    }

    public void delete(String id) {
        repository.delete(id);
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
