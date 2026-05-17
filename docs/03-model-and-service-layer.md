# 3. The Model and Service Layers

The Model and Service layers sit below the ViewModel layer. They are completely UI-agnostic and must not depend on any presentation framework. Their purpose is to encapsulate domain state, business rules, and persistence concerns in a way that remains independent of how the UI is structured.

### Contents

* [3.1 Model objects](#31-model-objects)

  * [3.1.1 Data carriers](#311-data-carriers)
  * [3.1.2 Rich domain objects](#312-rich-domain-objects)
* [3.2 Repositories](#32-repositories)
* [3.3 Domain operations](#33-domain-operations)

  * [3.3.1 Rules for domain operations](#331-rules-for-domain-operations)

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

## 3.3 Domain operations

Domain operations encapsulate business logic that spans multiple domain objects or does not naturally belong to any single entity.

Each domain operation class does one thing and is named after that operation. This naming convention makes the scope of each class self-evident and resists the accumulation of unrelated responsibilities over time.

Domain operations are split into two kinds:

**Commands** perform a state-changing operation — creating, updating, deleting, or transforming domain objects. They are named with a verb-noun pattern suffixed with `Command`.

**Queries** derive or compute a result from domain state without modifying it. They are named with a verb-noun pattern suffixed with `Query`.

### 3.3.1 Rules for domain operations

- each class does one thing — either a single command or a single query
- commands are suffixed with `Command`; queries are suffixed with `Query`
- depend only on repository interfaces and domain models
- contain no presentation logic and have no knowledge of ViewModels

```java
public class CopyOrderCommand {

    private final OrderRepository repository;

    public CopyOrderCommand(OrderRepository repository) {
        this.repository = repository;
    }

    public Order copy(String id) {
        var source = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        var copy = new Order(
            UUID.randomUUID().toString(),
            source.customerName(),
            LocalDate.now(),
            "COPY-" + source.reference(),
            source.lineItems()
        );

        repository.save(copy);
        return copy;
    }
}
```
