package mvvm.example.core.config.adapters;

import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderRepository;
import mvvm.example.orders.domain.OrderStatus;

import static mvvm.example.core.config.adapters.InMemoryCustomerRepository.*;
import static mvvm.example.core.config.adapters.InMemoryProductRepository.*;

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
        add("ORD-001", ACME_CORP,         LocalDate.now().minusDays(5),  LocalDate.now().plusDays(10),   OrderStatus.PENDING,    null,                          List.of(new LineItem(WIDGET_A, "Widget A", 10, new BigDecimal("9.99")), new LineItem(WIDGET_B, "Widget B", 5, new BigDecimal("24.99"))));
        add("ORD-002", GLOBEX_INC,         LocalDate.now().minusDays(45), LocalDate.now().minusDays(15),  OrderStatus.PENDING,    null,                          List.of(new LineItem(GIZMO_X, "Gizmo X", 2, new BigDecimal("149.00"))));
        add("ORD-003", INITECH,            LocalDate.now().minusDays(8),  LocalDate.now().plusDays(5),    OrderStatus.FULFILLED,  null,                          List.of(new LineItem(SPROCKET, "Sprocket", 20, new BigDecimal("4.50")), new LineItem(COG, "Cog", 8, new BigDecimal("12.75"))));
        add("ORD-004", UMBRELLA_LTD,       LocalDate.now().minusDays(60), LocalDate.now().minusDays(55),  OrderStatus.SHIPPED,    LocalDate.now().minusDays(55), List.of(new LineItem(REAGENT, "Reagent", 100, new BigDecimal("1.20"))));
        add("ORD-005", SOYLENT_CORP,       LocalDate.now().minusDays(40), LocalDate.now().minusDays(38),  OrderStatus.CANCELLED,  LocalDate.now().minusDays(38), List.of(new LineItem(MYSTERY_ITEM, "Mystery Item", 1, new BigDecimal("999.00"))));
        add("ORD-006", CYBERDYNE_SYSTEMS,  LocalDate.now().minusDays(35), LocalDate.now().minusDays(5),   OrderStatus.PENDING,    null,                          List.of(new LineItem(WIDGET_A, "Widget A", 10, new BigDecimal("9.99")), new LineItem(WIDGET_B, "Widget B", 5, new BigDecimal("24.99"))));
    }

    private void add(String reference, String customerId, LocalDate createdDate, LocalDate plannedShipDate, OrderStatus status, LocalDate completionDate, List<LineItem> items) {
        var id = UUID.randomUUID().toString();
        var order = new Order(id, customerId, createdDate, plannedShipDate, reference, status, completionDate, items);
        store.put(order.id(), order);
    }
}
