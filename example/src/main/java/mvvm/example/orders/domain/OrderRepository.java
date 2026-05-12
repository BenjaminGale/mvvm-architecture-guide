package mvvm.example.orders.domain;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    List<Order> findAll();
    Optional<Order> findById(String id);
    void save(Order order);
    void delete(String id);
}
