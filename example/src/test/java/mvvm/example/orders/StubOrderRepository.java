package mvvm.example.orders;

import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StubOrderRepository implements OrderRepository {

    private final Map<String, Order> store = new LinkedHashMap<>();

    public StubOrderRepository(Order... orders) {
        for (var order : orders) store.put(order.id(), order);
    }

    @Override public List<Order> findAll()                      { return List.copyOf(store.values()); }
    @Override public Optional<Order> findById(String id)        { return Optional.ofNullable(store.get(id)); }
    @Override public void save(Order order)                     { store.put(order.id(), order); }
    @Override public void delete(String id)                     { store.remove(id); }
}
