package mvvm.example.orders.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    List<Order> findAll();
    Optional<Order> findById(UUID id);
    void save(Order order);
    void delete(UUID id);
}
