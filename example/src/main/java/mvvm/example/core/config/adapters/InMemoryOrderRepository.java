package mvvm.example.core.config.adapters;

import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.orders.domain.OrderStatus;

import static mvvm.example.core.config.adapters.InMemoryCustomerRepository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> store = new HashMap<>();

    public InMemoryOrderRepository() {
        seed();
    }

    @Override
    public List<Order> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void save(Order order) {
        store.put(order.id(), order);
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }

    private void seed() {
        add(
            "ORD-001",
            ACME_CORP, "Acme Corp",
            LocalDate.now().minusDays(5),
            OrderStatus.WIP,
            null,
            List.of(
                new LineItem("Widget A", 10, new BigDecimal("9.99")),
                new LineItem("Widget B", 5, new BigDecimal("24.99"))
            )
        );

        add(
            "ORD-002",
            GLOBEX_INC, "Globex Inc",
            LocalDate.now().minusDays(45),
            OrderStatus.WIP,
            null,
            List.of(
                new LineItem("Gizmo X", 2, new BigDecimal("149.00"))
            )
        );

        add(
            "ORD-003",
            INITECH, "Initech",
            LocalDate.now().minusDays(8),
            OrderStatus.FULFILLED,
            null,
            List.of(
                new LineItem("Sprocket", 20, new BigDecimal("4.50")),
                new LineItem("Cog", 8, new BigDecimal("12.75"))
            )
        );

        add(
            "ORD-004",
            UMBRELLA_LTD, "Umbrella Ltd",
            LocalDate.now().minusDays(60),
            OrderStatus.SHIPPED,
            LocalDate.now().minusDays(55),
            List.of(
                new LineItem("Reagent", 100, new BigDecimal("1.20"))
            )
        );

        add(
            "ORD-005",
            SOYLENT_CORP, "Soylent Corp",
            LocalDate.now().minusDays(40),
            OrderStatus.CANCELLED,
            LocalDate.now().minusDays(38),
            List.of(
                new LineItem("Mystery Item", 1, new BigDecimal("999.00"))
            )
        );

        add(
            "ORD-006",
            CYBERDYNE_SYSTEMS, "Cyberdyne Systems",
            LocalDate.now().minusDays(35),
            OrderStatus.WIP,
            null,
            List.of(
                new LineItem("Widget A", 10, new BigDecimal("9.99")),
                new LineItem("Widget B", 5, new BigDecimal("24.99"))
            )
        );
    }

    private void add(String reference, String customerId, String customerName, LocalDate date, OrderStatus status, LocalDate completionDate, List<LineItem> items) {
        var order = new Order(UUID.randomUUID().toString(), customerId, customerName, date, reference, status, completionDate, items);
        store.put(order.id(), order);
    }
}
