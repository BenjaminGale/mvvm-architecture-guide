# 2. The Model and Service layers

The Model and Service layers sit below the ViewModel layer. They have no knowledge of the UI and no UI framework imports. Understanding them clearly is a prerequisite for understanding how ViewModels are kept focused and testable.

## 2.1 Model objects

Models are plain objects representing the application's core domain concepts — things like `Order`, `Customer`, and `LineItem`. They carry state and may encapsulate domain behaviour, but have no knowledge of the UI, no observable properties, and no dependency on external frameworks.

Models take two forms depending on their role:

**Data carriers** are immutable value objects that move data between layers. A service fetches an `Order` and returns it; a ViewModel receives it at construction; a use case takes it as an argument. Java records are a natural fit:

```java
public record LineItem(String description, int quantity, BigDecimal unitPrice) {}

public record Order(UUID id, String customerName, LocalDate date, List<LineItem> lineItems) {

    public Order withLineItems(List<LineItem> lineItems) {
        return new Order(id, customerName, date, lineItems);
    }
}
```

**Richer domain objects** encapsulate domain rules alongside their data. The business logic lives here — not in the ViewModel, and not in a service:

```java
public class Order {

    private final UUID id;
    private final String customerName;
    private final LocalDate date;
    private final List<LineItem> lineItems;

    public Order(UUID id, String customerName, LocalDate date, List<LineItem> lineItems) {
        if (customerName == null || customerName.isBlank())
            throw new IllegalArgumentException("Customer name is required");

        if (lineItems == null || lineItems.isEmpty())
            throw new IllegalArgumentException("An order must have at least one line item");

        this.id = id;
        this.customerName = customerName;
        this.date = date;
        this.lineItems = List.copyOf(lineItems);
    }

    public BigDecimal total() {
        return lineItems
            .stream()
            .map(item -> item.getTotal())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isOverdue() {
        return date.isBefore(LocalDate.now());
    }

    public Order withLineItems(List<LineItem> lineItems) {
        return new Order(id, customerName, date, lineItems);
    }

    // getters...
}
```

> A Model that starts acquiring observable properties is a sign that ViewModel responsibilities are leaking downward. Models are never observed directly by Views — the ViewModel's job is to receive plain Model data and expose it as observable properties that the View can bind to.

## 2.2 Services

Services form the application layer between Models and ViewModels. They are responsible for retrieving and persisting Models — database access, remote API calls, file I/O, and similar infrastructure concerns. Like Models, they have no UI imports and no knowledge of how the data they return will be presented.

A service exposes a focused interface reflecting the operations the application needs, not a general-purpose CRUD API. An `OrderService` might expose `fetchAll`, `fetchPending`, `save`, and `delete` — the precise operations the application requires, no more.

```java
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public List<Order> fetchAll() {
        return repository.findAll();
    }

    public List<Order> fetchPending() {
        return repository
            .findAll()
            .stream()
            .filter(Order::isOverdue)
            .toList();
    }

    public Order save(Order order) {
        var existing = repository.findById(order.id());
        
        if (existing.isPresent() && !existing.get().customerName().equals(order.customerName()))
            throw new IllegalStateException("Customer name cannot be changed on an existing order");

        return repository.save(order);
    }

    public void delete(UUID id) {
        var order = repository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        if (order.isOverdue())
            throw new IllegalStateException("Overdue orders cannot be deleted");

        repository.delete(id);
    }

    public Order copy(UUID id) {
        var source = repository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        
        var copy = new Order(
            UUID.randomUUID(),
            source.customerName(),
            LocalDate.now(),
            source.lineItems()
        );
        
        return repository.save(copy);
    }
}
```

Services are not injected directly into ViewModels. This is a deliberate invariant. A ViewModel that calls `orderService.save()` is making an assumption about the shape of a save operation that does not belong in the presentation layer. Instead, services are consumed by use cases, which are then injected into ViewModels.

## 2.3 Use cases

Use cases are the mechanism by which ViewModels interact with services without depending on them directly. Each use case is a plain Java class encapsulating a single operation: saving an order, deleting a customer, copying a line item. A use case receives the service it requires and a callback to invoke on completion. They also serve as the natural place to house ViewModel-adjacent logic — such as updating observable state during a long-running operation — keeping that coordination out of the ViewModel itself.

See section [3.2.1](03-viewmodels.md#321-use-cases-as-injectable-objects) for use case examples and how they are wired in the composition root.
