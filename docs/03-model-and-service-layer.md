# 3. The Model and Service Layers

The Model and Service layers sit below the ViewModel layer. They are completely UI-agnostic and must not depend on any presentation framework. Their purpose is to encapsulate domain state, business rules, and persistence concerns in a way that remains independent of how the UI is structured.

### Contents

- [3.1 Model objects](#31-model-objects)
  - [3.1.1 Data carriers](#311-data-carriers)
  - [3.1.2 Rich domain objects](#312-rich-domain-objects)
- [3.2 Repositories](#32-repositories)
- [3.3 Services](#33-services)
  - [3.3.1 Responsibilities of services](#331-responsibilities-of-services)

---

## 3.1 Model objects

Models represent the application’s core domain concepts — such as Order, Customer, or LineItem. They encapsulate state and, where appropriate, domain behaviour. They are never aware of the UI and do not expose observable properties or framework-specific constructs.

Models are used across layers:
- Repositories persist and retrieve them
- Services operate on them
- ViewModel expose them to the presentation layer

### 3.1.1 Data carriers

Data carriers are immutable value objects used to transfer data between layers. They contain no business logic and exist purely to represent state.

```java
public record LineItem(String description, int quantity, BigDecimal unitPrice) {}

public record Order(UUID id, String customerName, LocalDate date, List<LineItem> lineItems) {

    public Order withLineItems(List<LineItem> lineItems) {
        return new Order(id, customerName, date, lineItems);
    }
}
```

### 3.1.2 Rich domain objects

Rich domain objects encapsulate business rules and enforce invariants. This logic must remain in the domain model rather than in Services or ViewModels.

```java
public class Order {

    private final UUID id;
    private final String customerName;
    private final LocalDate date;
    private final List<LineItem> lineItems;

    public Order(UUID id, String customerName, LocalDate date, List<LineItem> lineItems) {
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("Customer name is required");
        }

        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("An order must have at least one line item");
        }

        this.id = id;
        this.customerName = customerName;
        this.date = date;
        this.lineItems = List.copyOf(lineItems);
    }

    public BigDecimal total() {
        return lineItems.stream()
            .map(LineItem::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isOverdue() {
        return date.isBefore(LocalDate.now());
    }

    public Order withLineItems(List<LineItem> lineItems) {
        return new Order(id, customerName, date, lineItems);
    }
}
```

A model must never expose observable state or UI-driven behaviour. The ViewModel is responsible for adapting model data into observable properties suitable for presentation.

---

## 3.2 Repositories

Repositories define the persistence boundary of the application. They abstract all data access concerns such as database queries, remote APIs, and file storage. They operate exclusively on models and contain no business logic.

Repositories are purely infrastructural and define how data is stored and retrieved, not how it is used.

```java
public interface OrderRepository {

    List<Order> findAll();

    Optional<Order> findById(UUID id);

    Order save(Order order);

    void delete(UUID id);
}
```

Repository implementations may vary, but the interface remains stable and is not exposed to the ViewModel layer.

Repositories are consumed exclusively by Services.

---

## 3.3 Services

Services form the application layer above Repositories and below ViewModels. They implement meaningful business and application logic and are responsible for enforcing invariants that do not belong in the domain model itself.

Services are not CRUD facades over repositories. Methods that simply delegate persistence operations are avoided unless they add meaningful constraints or domain value.

### 3.3.1 Responsibilities of services

A service is responsible for:
- enforcing application-level rules and invariants
- coordinating repository operations when needed
- exposing intent-oriented operations rather than storage-oriented APIs
- producing derived or computed results based on domain rules

```java
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public List<Order> fetchPendingOrders() {
        return repository.findAll()
            .stream()
            .filter(Order::isOverdue)
            .toList();
    }

    public Order save(Order order) {
        var existing = repository.findById(order.id());

        if (existing.isPresent()
                && !existing.get().customerName().equals(order.customerName())) {
            throw new IllegalStateException("Customer name cannot be changed on an existing order");
        }

        return repository.save(order);
    }

    public Order copy(UUID id) {
        var source = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        var copy = new Order(
            UUID.randomUUID(),
            source.customerName(),
            LocalDate.now(),
            source.lineItems()
        );

        return repository.save(copy);
    }

    public void delete(UUID id) {
        var order = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        if (order.isOverdue()) {
            throw new IllegalStateException("Overdue orders cannot be deleted");
        }

        repository.delete(id);
    }
}
```
