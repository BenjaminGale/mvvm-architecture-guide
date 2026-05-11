# MVVM Architecture Guide

## Contents

- [1. Introduction](#1-introduction)
  - [1.1 What is MVVM](#11-what-is-mvvm)
  - [1.2 Why use MVVM](#12-why-use-mvvm)
  - [1.3 Common MVVM problems](#13-common-mvvm-problems)
    - [1.3.1 ViewModels with too many responsibilities](#131-viewmodels-with-too-many-responsibilities)
    - [1.3.2 Services injected directly into ViewModels](#132-services-injected-directly-into-viewmodels)
    - [1.3.3 Navigation coupled to presentation](#133-navigation-coupled-to-presentation)
    - [1.3.4 Inheritance used to share logic](#134-inheritance-used-to-share-logic)
    - [1.3.5 Fat ViewModels from delegate commands](#135-fat-viewmodels-from-delegate-commands)
    - [1.3.6 Testability claimed but not demonstrated](#136-testability-claimed-but-not-demonstrated)
  - [1.4 Design goals](#14-design-goals)
- [2. The Model and Service layers](#2-the-model-and-service-layers)
  - [2.1 Model objects](#21-model-objects)
  - [2.2 Services](#22-services)
  - [2.3 Use cases](#23-use-cases)
- [3. ViewModels](#3-viewmodels)
  - [3.1 The ViewModel class](#31-the-viewmodel-class)
    - [3.1.1 Observable properties](#311-observable-properties)
    - [3.1.2 Navigation via injected callbacks](#312-navigation-via-injected-callbacks)
    - [3.1.3 Why ViewModels should not create or host views](#313-why-viewmodels-should-not-create-or-host-views)
  - [3.2 Organising complex ViewModels](#32-organising-complex-viewmodels)
    - [3.2.1 Use cases as injectable objects](#321-use-cases-as-injectable-objects)
    - [3.2.2 Sub-ViewModels for distinct UI sections](#322-sub-viewmodels-for-distinct-ui-sections)
    - [3.2.3 Composing sub-ViewModels in the parent](#323-composing-sub-viewmodels-in-the-parent)
    - [3.2.4 The result of combining all three strategies](#324-the-result-of-combining-all-three-strategies)
  - [3.3 ViewModel communication patterns](#33-viewmodel-communication-patterns)
  - [3.4 Action classes](#34-action-classes)
    - [3.4.1 The problem they solve](#341-the-problem-they-solve)
    - [3.4.2 Action — synchronous operations](#342-action--synchronous-operations)
  - [3.5 AsyncAction — long-running operations](#35-asyncaction--long-running-operations)
    - [3.5.1 How they fit into the architecture](#351-how-they-fit-into-the-architecture)
- [4. Views](#4-views)
  - [4.1 View types](#41-view-types)
  - [4.2 View classes](#42-view-classes)
  - [4.3 The ViewFactory](#43-the-viewfactory)
  - [4.4 Navigation](#44-navigation)
  - [4.5 Presentation decisions belong to the View](#45-presentation-decisions-belong-to-the-view)
  - [4.6 Adding a new screen](#46-adding-a-new-screen)
- [5. Application bootstrapping](#5-application-bootstrapping)
  - [5.1 The role of App](#51-the-role-of-app)
  - [5.2 Infrastructure and registration](#52-infrastructure-and-registration)
  - [5.3 Composing the application](#53-composing-the-application)
    - [5.3.1 Sidebar](#531-sidebar)
    - [5.3.2 Orders flow](#532-orders-flow)
    - [5.3.3 Customers flow](#533-customers-flow)
    - [5.3.4 Settings](#534-settings)
  - [5.4 Scaling App with Flow classes](#54-scaling-app-with-flow-classes)
- [6. Testing](#6-testing)
  - [6.1 Testing ViewModels](#61-testing-viewmodels)
    - [6.1.1 Property updates](#611-property-updates)
    - [6.1.2 Navigation callbacks](#612-navigation-callbacks)
    - [6.1.3 Sub-ViewModel validity](#613-sub-viewmodel-validity)
    - [6.1.4 Composed validity (canSave)](#614-composed-validity-cansave)
    - [6.1.5 AsyncAction](#615-asyncaction)
  - [6.2 Testing use cases](#62-testing-use-cases)
  - [6.3 Testing inter-ViewModel communication](#63-testing-inter-viewmodel-communication)
  - [6.4 Stub implementations](#64-stub-implementations)
  - [6.5 Testing Action and ViewFactory directly](#65-testing-action-and-viewfactory-directly)
    - [6.5.1 Action](#651-action)
    - [6.5.2 AsyncAction](#652-asyncaction)
    - [6.5.3 ViewFactory](#653-viewfactory)
- [7. Architecture review](#7-architecture-review)
  - [7.1 How design goals are met](#71-how-design-goals-are-met)
  - [7.2 How common problems are addressed](#72-how-common-problems-are-addressed)
    - [7.2.1 ViewModels with too many responsibilities](#721-viewmodels-with-too-many-responsibilities)
    - [7.2.2 Services injected directly into ViewModels](#722-services-injected-directly-into-viewmodels)
    - [7.2.3 Navigation coupled to presentation](#723-navigation-coupled-to-presentation)
    - [7.2.4 Inheritance used to share logic](#724-inheritance-used-to-share-logic)
    - [7.2.5 Fat ViewModels from delegate commands](#725-fat-viewmodels-from-delegate-commands)
    - [7.2.6 Testability claimed but not demonstrated](#726-testability-claimed-but-not-demonstrated)

    ---

## 1. Introduction

This document describes a practical approach to implementing the Model-View-ViewModel (MVVM) pattern. It covers the core building blocks as well as navigation, view construction, and communication between application areas.

The sample code is written in Java with JavaFX as the chosen view technology however the patterns can be applied to any technology that provides a property binding system suitable for use with the MVVM pattern.

### 1.1 What is MVVM

The MVVM pattern divides an application into four layers with distinct, non-overlapping responsibilities:

- **Model:** Represents the application's core domain concepts. Encapsulates data, business logic, and validation rules. Has no knowledge of the UI or how data is fetched or persisted.
- **ViewModel:** An abstraction of a View. Exposes state as observable properties for the View to bind to, and provides methods the View calls in response to user input. Has no knowledge of how the View is rendered or where its data originates.
- **View:** Represents the UI. Binds to the ViewModel's observable properties so the display stays in sync with state, and delegates user interactions back to the ViewModel. Has no knowledge of domain logic or data sources.

The three-layer description of MVVM above is common but undersells the role of the service layer. This document treats services as a separate layer throughout.

- **Service:** The data access layer. Retrieves and persists Models on behalf of the rest of the application. Has no knowledge of the UI or ViewModel layer.

### 1.2 Why use MVVM

The primary benefit is a strict, one-way dependency graph: Views depend on ViewModels, ViewModels depend on use cases and services, services depend on nothing above them. This is enforced structurally — a ViewModel that holds no UI framework types cannot reach into the view layer regardless of developer intent.

This separation makes ViewModels directly testable. They contain no UI types and make no assumptions about presentation context, so they can be exercised in plain unit tests without launching a UI runtime.

The pattern scales predictably. An application can grow from a handful of screens to several dozen without the architecture changing shape — each screen follows the same structure. Adding a screen does not require modifying existing classes.

### 1.3 Common MVVM problems

MVVM is widely adopted but frequently misapplied. The problems described below are characteristic of naive implementations and become more acute as applications grow. The problems identified here provide context for the design decisions made throughout this document.

#### 1.3.1 ViewModels with too many responsibilities

In a typical MVVM implementation the ViewModel accumulates responsibilities incrementally. Including but not limited to:

- Property change notifications.
- Calculated property updates.
- Input validation.
- Service calls.
- Data loading.
- Navigation management.
- ViewModel construction.

The class begins as a focused abstraction and becomes a god object. Decomposing it into smaller ViewModels is a partial remedy — sub-ViewModels often require the same service dependencies so the injection problem multiplies rather than diminishes.

#### 1.3.2 Services injected directly into ViewModels

The standard response to ViewModel bloat is to inject service interfaces which introduces a set of compounding problems:

- A single large service interface with many methods is an Interface Segregation Principle violation. The ViewModel depends on methods it does not use and testing requires mocking the entire interface even when only one method is exercised.
- Splitting a service into multiple smaller interfaces increases the number of constructor arguments. A ViewModel with five injected interfaces is difficult to construct in tests and difficult to read in production.
- Either approach couples the ViewModel to service logic — even via an interface. This makes it difficult to reuse the ViewModel in a different context because the services it calls are baked into its contract.
- ViewModels should not know where their data comes from. A ViewModel that calls `orderService.save()` is making an assumption about the existence and shape of a save operation. That assumption should not live in presentation-layer code.

#### 1.3.3 Navigation coupled to presentation

A common pattern is to inject a navigation or dialog service into a ViewModel so it can initiate transitions or prompt the user. The naming reveals the flaw: `IDialogService.showDialog()` couples a request for information to a specific presentation mechanism. If that dialog is later replaced by an inline panel, every ViewModel that called `showDialog()` requires modification. Presentation decisions are not the ViewModel's concern.

#### 1.3.4 Inheritance used to share logic

A common response to repeated ViewModel logic is to push it into a base class. Inheritance is the wrong mechanism because it should be used to model 'is-a' relationships. Using it to share utility logic produces fragile hierarchies where a change to the base class has unpredictable effects on all subclasses and where subclasses are coupled to implementation details they did not choose.

#### 1.3.5 Fat ViewModels from delegate commands

The delegate command pattern (where a ViewModel exposes an `ICommand` implemented as a delegate that calls back into the ViewModel) is a common source of bloat. The command logic lives in the ViewModel, the service dependencies needed to execute the command are injected into the ViewModel, and the ViewModel ends up holding everything. Each new command makes the ViewModel larger and its constructor longer.

#### 1.3.6 Testability claimed but not demonstrated

MVVM is routinely justified on the grounds of testability, yet the injection patterns described above make tests expensive to write and maintain. A ViewModel with several injected interfaces requires substantial mock infrastructure before a single assertion can be made. The resulting tests are brittle. They are coupled to implementation details rather than observable behaviour and fail under refactoring that does not alter the contract. Genuine testability requires that ViewModels be constructable with minimal setup and verifiable by asserting property state directly.

In other cases, tests are omitted entirely from any discussion of the MVVM pattern apart from a passing mention.

### 1.4 Design goals

These are invariants, not guidelines. Violating any one introduces a special case that erodes the architecture over time.

- Every View is constructed with exactly one ViewModel.
- ViewModels have no knowledge of Views or how they are constructed.
- Each ViewModel holds only the dependencies it directly uses.
- Nothing creates its own dependencies — everything is injected through the constructor.
- All construction and wiring lives in a single composition root, which is the complete map of every screen and transition.

---

## 2. The Model and Service layers

The Model and Service layers sit below the ViewModel layer. They have no knowledge of the UI and no UI framework imports. Understanding them clearly is a prerequisite for understanding how ViewModels are kept focused and testable.

### 2.1 Model objects

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

### 2.2 Services

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

### 2.3 Use cases

Use cases are the mechanism by which ViewModels interact with services without depending on them directly. Each use case is a plain Java class encapsulating a single operation: saving an order, deleting a customer, copying a line item. A use case receives the service it requires and a callback to invoke on completion. They also serve as the natural place to house ViewModel-adjacent logic — such as updating observable state during a long-running operation — keeping that coordination out of the ViewModel itself.

See section 3.2.1 for use case examples and how they are wired in the composition root.

---

## 3. ViewModels

> This section introduces complexity progressively. Section 3.1 covers the core ViewModel structure. Section 3.2 adds strategies for managing complexity as ViewModels grow. Section 3.4 introduces Action classes, and section 3.5 extends them to asynchronous operations. Later examples for the same classes — particularly `OrderEditorViewModel` and `SaveOrderUseCase` — supersede earlier ones; each revision reflects the addition of a new strategy.

### 3.1 The ViewModel class

A ViewModel represents the state and behaviour of a single screen or area. It exposes observable properties for the view to bind to and methods the view calls in response to user input.

#### 3.1.1 Observable properties

The ViewModel exposes state as observable properties; the view binds its controls to those properties. When a property value changes, all bound controls update automatically with no explicit refresh required.

> The sample code uses JavaFX's property system as the concrete observable mechanism. Other UI frameworks provide equivalent systems — WPF has `INotifyPropertyChanged`, Android has `LiveData` — and the same ViewModel patterns apply.

```java
public class LoadOrdersUseCase {
    private final OrderService orderService;

    public LoadOrdersUseCase(OrderService orderService) {
        this.orderService = orderService;
    }

    public List<Order> execute() {
        return orderService.fetchAll();
    }
}
```

```java
public class OrdersViewModel {

    private final ObservableList<Order> orders
        = FXCollections.observableArrayList();
    private final StringProperty statusText
        = new SimpleStringProperty("Ready");

    private final LoadOrdersUseCase loadOrders;

    public OrdersViewModel(LoadOrdersUseCase loadOrders, Consumer<Order> onOrderSelected) {
        this.loadOrders = loadOrders;
        this.onOrderSelected = onOrderSelected;
    }

    public ObservableList<Order> getOrders()   { return orders; }
    public StringProperty statusTextProperty() { return statusText; }

    public void refresh() {
        orders.setAll(loadOrders.execute());
        statusText.set(orders.size() + " orders");
    }
}
```

#### 3.1.2 Navigation via injected callbacks

A recurring question in MVVM is how a ViewModel triggers navigation to another screen. It cannot construct a view directly — that breaches the layer separation. It cannot hold a reference to a system with knowledge of view construction — that leaks view-layer concerns into the ViewModel. And it should not accumulate service dependencies solely to pass them on when constructing a child ViewModel.

Navigation is expressed through injected callbacks. When the ViewModel needs to initiate a transition, it invokes a functional interface — a Consumer, a Runnable, or similar — supplied at construction time. The ViewModel has no knowledge of what the callback does; it provides the relevant data and delegates the rest.

As a result, each ViewModel holds only what it genuinely requires. An `OrdersViewModel` presenting a list of orders needs a use case for data retrieval and a callback for the selection event:

```java
public class OrdersViewModel {

    private final LoadOrdersUseCase loadOrders;
    private final Consumer<Order> onOrderSelected;

    public OrdersViewModel(LoadOrdersUseCase loadOrders, Consumer<Order> onOrderSelected) {
        this.loadOrders = loadOrders;
        this.onOrderSelected = onOrderSelected;
    }

    public void openOrder(Order order) {
        // Signal intent. What happens next is entirely
        // determined by whoever provided this callback.
        onOrderSelected.accept(order);
    }
}
```

The `OrderDetailViewModel` that is shown when an order is clicked has its own set of possible actions — editing a line item, or going back. Each distinct navigation action is a separate injected callback. The ViewModel holds only what it uses:

```java
public class OrderDetailViewModel {

    private final Order order;
    private final Consumer<LineItem> onEditItem;
    private final Runnable onBack;

    public OrderDetailViewModel(
        Order order,
        Consumer<LineItem> onEditItem,
        Runnable onBack) {

        this.order = order;
        this.onEditItem = onEditItem;
        this.onBack = onBack;
    }

    public void editItem(LineItem item) { onEditItem.accept(item); }
    public void back() { onBack.run(); }
}
```

> Notice that `OrderDetailViewModel` does not receive `CustomerService`, even though navigating back eventually leads to a screen that uses it. Those dependencies are handled when the back callback is constructed in the composition root, where all services are already available.

The view layer is responsible for performing the actual navigation — resolving the appropriate view and deciding how it is presented. The composition root is responsible for wiring the callbacks that connect ViewModel intent to that behaviour. Both are covered in sections 4.4 and 5.

#### 3.1.3 Why ViewModels should not create or host views

Two invariants govern how a ViewModel should relate to views it may cause to appear:

- **It should not acquire dependencies solely to construct something else.** If a ViewModel holds a service or use case it does not itself use, that dependency exists only to be passed on — a sign that construction responsibility has leaked into the wrong place.
- **It should not produce views that appear outside its own presentation scope.** A ViewModel has no knowledge of the shell, the layout, or how the application is structured. Deciding where a new view appears — workspace, sidebar, modal dialog — is a view-layer concern.

These invariants are easy to satisfy when decomposing a single screen. The sub-ViewModels described in section 3.2 (`OrderHeaderViewModel`, `LineItemsViewModel`) are constructed from data the parent already holds, and their corresponding sub-views are placed directly within the parent view's own layout. No new dependencies are introduced; no question of presentation context arises.

The invariants are violated when a ViewModel tries to produce a view that crosses these boundaries. Three common patterns each break one or both.

**A ChildViewModel property**

A ViewModel might expose a property holding a child ViewModel that changes in response to user interaction, with the view observing it to decide what to show:

```java
public class OrdersViewModel {
    private final ObjectProperty<Object> activeChild = new SimpleObjectProperty<>();

    public void openOrder(Order order) {
        activeChild.set(new OrderDetailViewModel(order, ...));
    }

    public ReadOnlyObjectProperty<Object> activeChildProperty() { return activeChild; }
}
```

This violates both invariants. Constructing `OrderDetailViewModel` requires whatever dependencies it needs — `OrdersViewModel` must hold them even if it never uses them directly. And the bound view can only present the result in the context it occupies — it cannot place it in a different area of the shell or open it as a modal dialog.

> This is distinct from the sub-ViewModel pattern described in section 3.2.2, where child ViewModels represent fixed sections of the same screen. Those are constructed from data the parent already holds, with no external dependencies, and their views are always rendered within the parent's own layout — both invariants are satisfied by construction.

**A shared ViewRouter injected into the ViewModel**

Injecting a `ViewRouter` and calling `viewRouter.navigateTo(...)` moves construction out of the ViewModel but does not fix the dependency problem. To call `navigateTo`, the ViewModel must still construct the next ViewModel — which means it must know its constructor and hold all of its dependencies. ViewModels accumulate services they do not use, solely to pass them to the next screen.

**A navigation or dialog service**

Injecting an `INavigationService` or `IDialogService` couples the ViewModel to a specific presentation mechanism. `dialogService.showDialog(new EditItemViewModel(...))` is a presentation decision — prescribing modal display — that does not belong in the ViewModel layer. If that dialog is later replaced by an inline panel, every ViewModel that called `showDialog` requires modification.

**Why callbacks satisfy both invariants**

Callbacks decouple intent from construction and presentation. When a ViewModel needs another view to appear, it invokes a callback with only the data relevant to that transition:

```java
public class OrdersViewModel {
    private final Consumer<Order> onOrderSelected;

    public void openOrder(Order order) {
        onOrderSelected.accept(order);
    }
}
```

The ViewModel acquires no dependencies it does not otherwise need and makes no claim about presentation. The callback is wired in the composition root, where all dependencies are already available and where the next ViewModel — with all its services and callbacks — is constructed. Whether the resulting view replaces the workspace, opens as a dialog, or appears in a sidebar is decided by whichever view registers for that view type. Sections 4.4 and 4.5 demonstrate this concretely.

### 3.2 Organising complex ViewModels

A ViewModel supporting multiple operations and multi-step flows accumulates complexity rapidly if all logic resides within it directly. An order editor handling edit, copy, and delete — where the edit operation spans confirmation and commit steps — becomes difficult to read or modify when these concerns are not separated.

Three complementary strategies address this. They may be applied independently or together.

#### 3.2.1 Use cases as injectable objects

Each operation is extracted into its own use case class rather than implemented as a method on the ViewModel. The ViewModel holds use case instances and delegates to them, becoming a coordinator. Each use case is independently testable without the ViewModel being involved.

Each use case has three distinct parts that keep construction concerns separate from execution concerns:

- **Constructor** — services, wired once in the composition root at startup.
- **`execute` arguments** — data the ViewModel provides at the moment the user acts.
- **Return value** — a result the ViewModel reads to update its own state or decide what to do next.

This separation means the use case never holds a reference to the ViewModel, and the ViewModel never exposes its internal state to the use case. Consider an order placement operation that spans multiple services:

```java
public class PlaceOrderUseCase {
    private final CustomerService customerService;
    private final InventoryService inventoryService;
    private final OrderService orderService;

    public PlaceOrderUseCase(
        CustomerService customerService,
        InventoryService inventoryService,
        OrderService orderService) {
        this.customerService = customerService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
    }

    public PlaceResult execute(Order order, DeliveryAddress address, PaymentMethod payment) {
        var customer = customerService.findById(order.customerId());

        if (!customer.hasGoodStanding())
            return PlaceResult.failure("Customer account is not in good standing");

        var reserved = inventoryService.reserve(order.lineItems());
        orderService.place(order.withReservation(reserved), address, payment);
        return PlaceResult.success();
    }
}
```

```java
public sealed interface PlaceResult {
    record Success() implements PlaceResult {}
    record Failure(String message) implements PlaceResult {}

    static PlaceResult success()               { return new Success(); }
    static PlaceResult failure(String message) { return new Failure(message); }
}
```

The ViewModel calls `execute` with data it holds at invocation time, then reads the result and decides what to do — updating its own state on failure, or firing its navigation callback on success. The use case has no knowledge of either outcome:

```java
private final Runnable onPlaced;
private final StringProperty errorMessage = new SimpleStringProperty();

public void place() {
    var result = placeOrder.execute(order, deliveryAddress.get(), paymentMethod.get());
    switch (result) {
        case PlaceResult.Success s -> onPlaced.run();
        case PlaceResult.Failure f -> errorMessage.set(f.message());
    }
}

public StringProperty errorMessageProperty() { return errorMessage; }
```

Not all use cases return a result. When the operation has no meaningful outcome to communicate back, a completion callback is sufficient:

```java
public class DeleteOrderUseCase {
    private final OrderService orderService;
    private final Runnable onDeleted;

    public DeleteOrderUseCase(OrderService orderService, Runnable onDeleted) {
        this.orderService = orderService;
        this.onDeleted = onDeleted;
    }

    public void execute(Order order) {
        orderService.delete(order.id());
        onDeleted.run();
    }
}
```

```java
public class CopyOrderUseCase {
    private final OrderService orderService;
    private final Consumer<Order> onCopied;

    public CopyOrderUseCase(OrderService orderService, Consumer<Order> onCopied) {
        this.orderService = orderService;
        this.onCopied = onCopied;
    }

    public void execute(Order order) {
        var copy = orderService.copy(order.id());
        onCopied.accept(copy);
    }
}
```

The ViewModel receives use case instances via its constructor and delegates execution to them:

```java
public class OrderEditorViewModel {

    private final Order order;
    private final DeleteOrderUseCase deleteOrder;
    private final CopyOrderUseCase copyOrder;
    private final SaveOrderUseCase saveOrder;

    public OrderEditorViewModel(
        Order order,
        SaveOrderUseCase saveOrder,
        CopyOrderUseCase copyOrder,
        DeleteOrderUseCase deleteOrder
    ) {
        this.order = order;
        this.saveOrder = saveOrder;
        this.copyOrder = copyOrder;
        this.deleteOrder = deleteOrder;
    }

    public void delete() { deleteOrder.execute(order); }
    public void copy()   { copyOrder.execute(order); }
}
```

Use cases are constructed in the composition root alongside the ViewModel, each receiving its own service dependencies and completion callbacks. The ViewModel constructor reflects only what it directly coordinates:

```java
// In the composition root
private OrderEditorViewModel orderEditor(Order order) {
    return new OrderEditorViewModel(
        order,
        new SaveOrderUseCase(
            orderService,
            () -> viewRouter.navigateTo(orders())),
        new CopyOrderUseCase(
            orderService,
            copy -> viewRouter.navigateTo(orderEditor(copy))),
        new DeleteOrderUseCase(
            orderService,
            () -> viewRouter.navigateTo(orders()))
    );
}
```

#### Managing constructor length with a use case record

As a ViewModel grows to coordinate more operations, its constructor accumulates one argument per use case. This is an accurate reflection of its dependencies, but the signature becomes long. A use case record bundles the related use cases into a single named parameter object:

```java
public record OrderEditorUseCases(
    SaveOrderUseCase save,
    CopyOrderUseCase copy,
    DeleteOrderUseCase delete
) {}
```

The ViewModel takes one `OrderEditorUseCases` instead of three separate arguments:

```java
public class OrderEditorViewModel {

    private final Order order;
    private final SaveOrderUseCase saveOrder;
    private final CopyOrderUseCase copyOrder;
    private final DeleteOrderUseCase deleteOrder;

    public OrderEditorViewModel(Order order, OrderEditorUseCases useCases) {
        this.order      = order;
        this.saveOrder  = useCases.save();
        this.copyOrder  = useCases.copy();
        this.deleteOrder = useCases.delete();
    }

    public void delete() { deleteOrder.execute(order); }
    public void copy()   { copyOrder.execute(order); }
}
```

The composition root constructs the record immediately before the ViewModel, keeping all wiring in one place:

```java
private OrderEditorViewModel orderEditor(Order order) {
    var useCases = new OrderEditorUseCases(
        new SaveOrderUseCase(orderService,
            () -> viewRouter.navigateTo(orders())),
        new CopyOrderUseCase(orderService,
            copy -> viewRouter.navigateTo(orderEditor(copy))),
        new DeleteOrderUseCase(orderService,
            () -> viewRouter.navigateTo(orders()))
    );
    return new OrderEditorViewModel(order, useCases);
}
```

The total number of dependencies is unchanged — all three use cases still exist and are still constructed in the composition root. What changes is that the ViewModel constructor expresses a single coherent concept rather than a list of individual arguments. Adding a new use case means adding one field to the record; the ViewModel constructor signature does not change.

#### 3.2.2 Sub-ViewModels for distinct UI sections

Sub-ViewModels satisfy both invariants from section 3.1.3 by construction: they are created from data the parent ViewModel already holds, with no external service dependencies, and their corresponding sub-views are always rendered within the parent view's own layout. This is what makes the pattern architecturally sound — a child ViewModel that required services from the composition root, or whose view might appear outside the parent's layout, would need to be registered with the `ViewFactory` and presented via the `ViewRouter`, not constructed directly by the parent.

If a screen has meaningfully distinct sections — an order header, a line items table, a notes panel — each section can have its own ViewModel. The parent ViewModel composes them and reads from them when it needs to build the final result. Each sub-ViewModel owns its own state, its own validation logic and its own observable properties.

A sub-ViewModel looks exactly like any other ViewModel. The only difference is that it is not navigated to — it is constructed directly by the parent and exposed via a getter:

```java
public class OrderHeaderViewModel {

    private final StringProperty customerName = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> orderDate = new SimpleObjectProperty<>();
    private final StringProperty reference = new SimpleStringProperty();
    private final BooleanProperty valid = new SimpleBooleanProperty(false);

    public OrderHeaderViewModel(Order order) {
        customerName.set(order.customerName());
        orderDate.set(order.date());
        reference.set(order.reference());

        customerName.addListener(obs -> validate());
        orderDate.addListener(obs -> validate());
        reference.addListener(obs -> validate());

        validate();
    }

    private void validate() {
        valid.set(
            customerName.get() != null && !customerName.get().isBlank() &&
            orderDate.get() != null &&
            reference.get() != null && !reference.get().isBlank()
        );
    }

    public StringProperty customerNameProperty() { return customerName; }
    public ObjectProperty<LocalDate> orderDateProperty() { return orderDate; }
    public StringProperty referenceProperty() { return reference; }
    public ReadOnlyBooleanProperty validProperty() { return valid; }

    public OrderHeader buildHeader() {
        return new OrderHeader(
            customerName.get(), orderDate.get(), reference.get()
        );
    }
}
```

The `LineItemsViewModel` manages the table of line items. It owns the definition of what makes the line items section valid. That rule is entirely encapsulated here; the parent does not need to know it:

```java
public class LineItemsViewModel {

    private final ObservableList<LineItemRow> rows = FXCollections.observableArrayList();
    private final ObjectProperty<LineItemRow> selectedRow = new SimpleObjectProperty<>();
    private final BooleanProperty canRemove = new SimpleBooleanProperty(false);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);

    public LineItemsViewModel(List<LineItem> items) {
        rows.setAll(items.stream().map(LineItemRow::new).toList());
        selectedRow.addListener(obs ->
            canRemove.set(selectedRow.get() != null));
        rows.addListener((ListChangeListener<LineItemRow>) c -> validate());
        validate();
    }

    private void validate() {
        valid.set(
            !rows.isEmpty() &&
            rows.stream().noneMatch(r -> r.descriptionProperty().get().isBlank())
        );
    }

    public void addRow() { rows.add(new LineItemRow(LineItem.empty())); }

    public void removeSelected() {
        var row = selectedRow.get();
        if (row != null) rows.remove(row);
    }

    public void selectRow(LineItemRow row) { selectedRow.set(row); }

    public ObservableList<LineItemRow> getRows() { return rows; }

    public ReadOnlyBooleanProperty canRemoveProperty() { return canRemove; }

    public ReadOnlyBooleanProperty validProperty() { return valid; }

    public List<LineItem> buildLineItems() {
        return rows.stream().map(LineItemRow::toLineItem).toList();
    }
}
```

`LineItemRow` is a small observable wrapper around a single row, giving the table view something to bind its columns to:

```java
public class LineItemRow {

    private final StringProperty  description = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> unitPrice = new SimpleObjectProperty<>();

    public LineItemRow(LineItem item) {
        description.set(item.description());
        quantity.set(item.quantity());
        unitPrice.set(item.unitPrice());
    }

    public StringProperty  descriptionProperty() { return description; }

    public IntegerProperty quantityProperty() { return quantity; }

    public ObjectProperty<BigDecimal> unitPriceProperty() { return unitPrice; }

    public LineItem toLineItem() {
        return new LineItem(description.get(), quantity.get(), unitPrice.get());
    }
}
```

#### 3.2.3 Composing sub-ViewModels in the parent

The parent ViewModel constructs its sub-ViewModels, exposes them via getters, and derives its own validity by composing the sub-ViewModels' valid properties. Crucially, the parent does not duplicate or re-implement the validity rules of its children — it observes their `validProperty` and combines the results. Each child owns its own invariant:

```java
public class OrderEditorViewModel {

    private final Order order;

    private final OrderHeaderViewModel header;
    private final LineItemsViewModel lineItems;
    private final BooleanProperty canSave = new SimpleBooleanProperty(false);

    private final SaveOrderUseCase saveOrder;
    private final CopyOrderUseCase copyOrder;
    private final DeleteOrderUseCase deleteOrder;

    public OrderEditorViewModel(
        Order order,
        SaveOrderUseCase saveOrder,
        CopyOrderUseCase copyOrder,
        DeleteOrderUseCase deleteOrder
    ) {
        this.order = order;
        this.saveOrder = saveOrder;
        this.copyOrder = copyOrder;
        this.deleteOrder = deleteOrder;

        this.header = new OrderHeaderViewModel(order);
        this.lineItems = new LineItemsViewModel(order.lineItems());

        // Each child owns its validity rules.
        // The parent composes — it has no knowledge of what valid means
        // for each section.
        canSave.bind(
            header.validProperty()
                .and(lineItems.validProperty())
        );
    }

    public void save() { saveOrder.execute(buildUpdatedOrder()); }

    public void copy() { copyOrder.execute(order); }

    public void delete() { deleteOrder.execute(order); }

    public ReadOnlyBooleanProperty canSaveProperty() { return canSave; }

    public OrderHeaderViewModel getHeader() { return header; }

    public LineItemsViewModel getLineItems() { return lineItems; }

    private Order buildUpdatedOrder() {
        return order
            .withHeader(header.buildHeader())
            .withLineItems(lineItems.buildLineItems());
    }
}
```

The view passes each sub-ViewModel directly to its corresponding sub-view using exactly the same pattern as any other view construction. The parent view only arranges them in the layout — it has no knowledge of what they contain:

```java
// Inside OrderEditorView.build()
var headerView = new OrderHeaderView(viewModel.getHeader());
var lineItemsView = new LineItemsView(viewModel.getLineItems());

var layout = new BorderPane();
layout.setTop(headerView);
layout.setCenter(lineItemsView);

getChildren().add(layout);
```

> Sub-ViewModels are an internal implementation detail of the parent ViewModel. The composition root does not construct them and does not know they exist. It only wires the boundaries — services and navigation callbacks that cross layers. Everything internal to the editor is the editor's own concern.

#### 3.2.4 The result of combining all three strategies

A ViewModel that uses all three strategies is a pure coordinator. It holds no service logic — that lives in use cases. It contains no per-field validation for its subsections — that is owned by sub-ViewModels. Each piece is independently readable, independently changeable, and independently testable.

Adding a new operation means writing a new use case class and adding one argument to the ViewModel constructor. Adding a new section to the screen means writing a new sub-ViewModel and sub-view, then composing them in. In each case, the change is localised and the rest of the codebase is unaffected.

### 3.3 ViewModel communication patterns

Navigation callbacks address the case where one ViewModel triggers the appearance of another. A separate problem arises when two concurrently active ViewModels must share state. A sidebar displaying a pending order count must reflect updates made by the orders screen, yet direct coupling between the two ViewModels would undermine the architecture.

A shared observable context object addresses this. It is a focused Java class holding observable properties that represent one aspect of shared application state. Both ViewModels receive the same instance via their constructors. The producer writes to it; the consumer binds to it. Neither has any knowledge of the other.

```java
public class OrderContext {
    private final IntegerProperty pendingCount = new SimpleIntegerProperty(0);

    public void setPendingCount(int count) {
        pendingCount.set(count);
    }

    public ReadOnlyIntegerProperty pendingCountProperty() {
        return pendingCount;
    }
}
```

The `OrdersViewModel` updates the context after loading data, with no knowledge of what else may be observing it:

```java
public class OrdersViewModel {
    private final LoadOrdersUseCase loadOrders;
    private final OrderContext orderContext;
    private final ObservableList<Order> orders = FXCollections.observableArrayList();

    public void refresh() {
        var result = loadOrders.execute();
        orders.setAll(result);
        // Update shared state — any observer will react automatically
        orderContext.setPendingCount(result.size());
    }
}
```

The `SidebarViewModel` binds to the context property. Its count updates reactively whenever the value changes — no polling, no explicit refresh:

```java
public class SidebarViewModel {
    private final IntegerProperty pendingOrderCount = new SimpleIntegerProperty();

    public SidebarViewModel(OrderContext orderContext, ...) {
        // Automatically updated whenever OrdersViewModel calls
        // orderContext.setPendingCount(...)
        pendingOrderCount.bind(orderContext.pendingCountProperty());
    }

    public ReadOnlyIntegerProperty pendingOrderCountProperty() {
        return pendingOrderCount;
    }
}
```

In the composition root, both ViewModels are constructed with the same `OrderContext` instance:

```java
var orderContext = new OrderContext();

// Both get the same instance — producer and consumer
// are connected without knowing about each other
var sidebarVm = new SidebarViewModel(orderContext, ...);
var ordersVm  = new OrdersViewModel(new LoadOrdersUseCase(orderService), orderContext, ...);
```

Context objects should be scoped to a specific domain concern. An `OrderContext` holds order-related shared state. A `UserSessionContext` holds the authenticated user. A single global `AppState` that accumulates all shared state is a god object by another name. Each context is injected only into the ViewModels that require it.

> Context classes should implement separate `Reader` and `Writer` interfaces to enforce encapsulation boundaries. This ensures each ViewModel can only interact with the context in the way that was intended — the `SidebarViewModel` receives a `Reader` and cannot mutate state; the `OrdersViewModel` receives a `Writer` and cannot observe it:

```java
public class OrderContext implements OrderContext.Reader, OrderContext.Writer {

    private final IntegerProperty pendingCount = new SimpleIntegerProperty(0);

    @Override
    public void setPendingCount(int count) { pendingCount.set(count); }

    @Override
    public ReadOnlyIntegerProperty pendingCountProperty() { return pendingCount; }

    public interface Reader {
        ReadOnlyIntegerProperty pendingCountProperty();
    }

    public interface Writer {
        void setPendingCount(int count);
    }
}
```

```java
// SidebarViewModel receives only the read side
public SidebarViewModel(OrderContext.Reader orderContext, ...) {
    pendingOrderCount.bind(orderContext.pendingCountProperty());
}

// OrdersViewModel receives only the write side
public OrdersViewModel(LoadOrdersUseCase loadOrders, OrderContext.Writer orderContext, ...) {
    this.loadOrders   = loadOrders;
    this.orderContext = orderContext;
}
```

In the composition root the single `OrderContext` instance satisfies both interfaces:

### 3.4 Action classes

The Action classes are optional utilities between ViewModels and the view layer. They eliminate a category of boilerplate: wiring a control's disabled state to a ViewModel property and its activation handler to a ViewModel method are related operations that are better expressed as a single object.

#### 3.4.1 The problem they solve

Without `Action` a view that binds a save button must wire two separate things:

```java
// Two separate concerns the view must coordinate
saveButton.disableProperty().bind(viewModel.canSaveProperty().not());
saveButton.setOnAction(e -> viewModel.save());
```

For a single control the overhead is modest, but the pattern repeats across every actionable control in every view. If the ViewModel changes a guard condition, the view must be updated accordingly. Centralising this in the ViewModel via Action is the more appropriate location.

#### 3.4.2 Action — synchronous operations

Action holds a Listener and an optional `ObservableValue` that governs whether the action can be executed. The execute method is self-guarding: invocation when `canExecute` is false has no effect. An operation cannot be triggered through the view regardless of how bindings are arranged.

```java
public class Action {

    private final ReadOnlyBooleanWrapper canExecute = new ReadOnlyBooleanWrapper(this, "canExecute", true);
    private final Action.Listener listener;
    
    // Properties hold weak references to bindings — store a strong reference
    // to prevent garbage collection.
    private final ObservableValue<? extends Boolean> binding;

    public Action(Action.Listener listener) {
        this.listener = requireNonNull(listener);
        this.binding = null;
    }

    public Action(Action.Listener listener, ObservableValue<? extends Boolean> binding) {
        this.listener = requireNonNull(listener);
        this.binding = requireNonNull(binding);
        this.canExecute.bind(binding);
    }

    public final ReadOnlyBooleanProperty canExecuteProperty() {
        return canExecute.getReadOnlyProperty();
    }

    public final boolean canExecute() {
        return canExecuteProperty().get();
    }

    public final void execute() {
        if (canExecute()) {
            listener.actionExecuted();
        }
    }

    @FunctionalInterface
    public interface Listener {
        void actionExecuted();
    }
}
```

### 3.5 AsyncAction — long-running operations

`AsyncAction` extends the concept to asynchronous operations, providing two behaviours that would otherwise require explicit implementation in every async ViewModel method:

- `isExecuting` becomes true automatically when execution starts and false when the `CompletableFuture` completes, regardless of whether it succeeded or failed.
- `canExecute` automatically becomes false while the action is executing, preventing double-submission without any explicit guard in the ViewModel.

Accepting a `viewExecutor` rather than hardcoding an executor is a deliberate decision. Production code supplies `Platform::runLater`; tests supply a synchronous executor causing the future to complete on the calling thread and eliminating thread coordination from tests.

```java
public class AsyncAction {

    private final ReadOnlyBooleanWrapper canExecuteProperty
        = new ReadOnlyBooleanWrapper(this, "canExecute", true);

    private final ReadOnlyBooleanWrapper isExecutingProperty
        = new ReadOnlyBooleanWrapper(this, "isExecuting", false);

    private final BooleanBinding canActionExecuteBinding
        = Bindings.createBooleanBinding(() -> !isExecuting(), isExecutingProperty);

    public AsyncAction(Listener listener) {
        requireNonNull(listener);
        canExecuteProperty.bind(canActionExecuteBinding);
        this.listener = listener;
    }

    public AsyncAction(Listener listener, ObservableBooleanValue canExecuteBinding) {
        requireNonNull(listener);
        requireNonNull(canExecuteBinding);
        canExecuteProperty.bind(canActionExecuteBinding.and(canExecuteBinding));
        this.listener = listener;
    }

    public CompletableFuture<Void> executeAsync(Executor viewExecutor) {
        requireNonNull(viewExecutor);
        
        if (!canExecute()) {
            return CompletableFuture.completedFuture(null);
        }

        isExecutingProperty.set(true);

        return listener
            .actionExecutedAsync()
            .whenCompleteAsync((result, exception) -> {
                if (result != null) result.run();
                isExecutingProperty.set(false);
            }, viewExecutor)
            .thenApply(ignored -> null);;
    }

    public final ReadOnlyBooleanProperty canExecuteProperty() {
        return canExecuteProperty.getReadOnlyProperty();
    }

    public final boolean canExecute() {
        return canExecuteProperty().get();
    }

    public final ReadOnlyBooleanProperty isExecutingProperty() {
        return isExecutingProperty.getReadOnlyProperty();
    }

    public final boolean isExecuting() {
        return isExecutingProperty().get();
    }

    @FunctionalInterface
    public interface Listener {
        CompletableFuture<Runnable> actionExecutedAsync();
    }
}
```

> **Error handling in production.** The `whenCompleteAsync` block above resets `isExecuting` but silently discards any exception. In a real application the `Listener` contract should return a result type that distinguishes success from failure (e.g. `CompletableFuture<Result<Runnable, Throwable>>`), and `AsyncAction` should expose a `ReadOnlyObjectProperty<Throwable> lastErrorProperty()` that the ViewModel can observe and surface to the user. The simplified form shown here is sufficient for illustrating the threading and guard mechanics.

#### 3.5.1 How they fit into the architecture

Actions are exposed as public fields on the ViewModel. The use case implements the appropriate Listener interface, keeping execution logic out of the ViewModel. The ViewModel and its `SaveOrderUseCase` are wired at construction time in the composition root as shown above.

The use case implements `AsyncAction.Listener`. Rather than accepting the `Order` at construction time — which would capture a stale snapshot — it receives a `Supplier<Order>` that the ViewModel satisfies with `buildUpdatedOrder()`. This ensures the use case always operates on the current state of the header and line items sub-ViewModels at the moment the user clicks save:

```java
public class SaveOrderUseCase implements AsyncAction.Listener {

    private final OrderService orderService;
    private final Supplier<Order> orderSupplier;
    private final Runnable onSaved;

    public SaveOrderUseCase(
        OrderService orderService,
        Supplier<Order> orderSupplier,
        Runnable onSaved) {
        this.orderService   = orderService;
        this.orderSupplier  = orderSupplier;
        this.onSaved        = onSaved;
    }

    @Override
    public CompletableFuture<Runnable> actionExecutedAsync() {
        return CompletableFuture
            .supplyAsync(() -> {
                orderService.save(orderSupplier.get());
                return (Runnable) onSaved;
            });
    }
}
```

The ViewModel wires the supplier at construction time, passing `buildUpdatedOrder` as a method reference:

```java
public class OrderEditorViewModel {

    public final AsyncAction save;
    public final Action delete;
    public final Action copy;

    private final OrderHeaderViewModel header;
    private final LineItemsViewModel lineItems;
    private final BooleanProperty canSave = new SimpleBooleanProperty(false);

    public OrderEditorViewModel(
        Order order,
        SaveOrderUseCase saveUseCase,
        DeleteOrderUseCase deleteUseCase,
        CopyOrderUseCase copyUseCase
    ) {
        this.header    = new OrderHeaderViewModel(order);
        this.lineItems = new LineItemsViewModel(order.lineItems());

        canSave.bind(
            header
                .validProperty()
                .and(lineItems.validProperty())
        );

        this.save   = new AsyncAction(saveUseCase, canSave);
        this.delete = new Action(deleteUseCase::execute);
        this.copy   = new Action(copyUseCase::execute);
    }

    public OrderHeaderViewModel getHeader()   { return header; }
    public LineItemsViewModel   getLineItems() { return lineItems; }

    public Order buildUpdatedOrder() {
        return new Order(header.buildHeader(), lineItems.buildLineItems());
    }
}
```

In the composition root, the supplier is passed as a method reference so it is evaluated lazily at save time:

```java
private OrderEditorViewModel orderEditor(Order order) {
    var vm = new OrderEditorViewModel(
        order,
        new SaveOrderUseCase(orderService,
            vm::buildUpdatedOrder,                         // evaluated at save time
            () -> viewRouter.navigateTo(orders())),
        new DeleteOrderUseCase(orderService,
            () -> viewRouter.navigateTo(orders())),
        new CopyOrderUseCase(orderService,
            copy -> viewRouter.navigateTo(orderEditor(copy)))
    );
    return vm;
}
```

> Because `vm` is referenced inside the lambda before the constructor returns, the variable must be declared separately (`var vm = ...`) rather than inlined. The reference is not used until the user clicks save, by which point construction has long completed.

The view binds to the Action properties directly. The disabled state, the loading indicator, and the click handler are all sourced from the same object, so they are guaranteed to be consistent:

```java
// Inside OrderEditorView.bindViewModel()
saveButton.disableProperty().bind(viewModel.save.canExecuteProperty().not());
progressIndicator.visibleProperty().bind(viewModel.save.isExecutingProperty());
saveButton.setOnAction(e -> viewModel.save.executeAsync(Platform::runLater));

deleteButton.disableProperty().bind(viewModel.delete.canExecuteProperty().not());
deleteButton.setOnAction(e -> viewModel.delete.execute());

copyButton.disableProperty().bind(viewModel.copy.canExecuteProperty().not());
copyButton.setOnAction(e -> viewModel.copy.execute());
```

> This common binding configuration can easily be encapsulated in a helper method e.g. `bindButton(copyButton, viewModel.copy)`.

---

## 4. Views

### 4.1 View types

The architecture uses two kinds of view.

**View** — A class bound to a single ViewModel. The view binds its controls to the ViewModel's observable properties and delegates user interactions back to it. All views follow the construction conventions described in section 4.2.

**Component** — A reusable chunk of UI with no ViewModel. Components accept plain data or observable values and contain no application logic. They are instantiated directly at the point of use. A status badge, a loading indicator, or a formatted label are typical examples.

#### Construction patterns for Views

Though all Views share the same structure, they are wired in two different ways depending on how their ViewModel is provided.

**Directly instantiated** views have their ViewModel provided by a parent view, which receives it from a parent ViewModel (see section 3.2.2). The parent view constructs them inline, passing the sub-ViewModel directly. These views are never registered with the `ViewFactory`.

```java
// Inside OrderEditorView — sub-views are constructed directly
var headerView    = new OrderHeaderView(viewModel.getHeader());
var lineItemsView = new LineItemsView(viewModel.getLineItems());
```

**ViewFactory-registered** views have their ViewModel constructed in the composition root, which wires all services, use cases, and navigation callbacks. These views are registered with the `ViewFactory` and presented via the `ViewRouter`.

```java
viewFactory.register(OrderEditorViewModel.class, OrderEditorView::new);
viewFactory.register(OrdersViewModel.class,      OrdersView::new);
```

The invariants from section 3.1.3 determine which pattern applies. If a view's ViewModel requires external dependencies or the view could appear in a context outside the parent's layout, it must be registered with the `ViewFactory`. If the ViewModel is provided by a parent ViewModel with no external dependencies and the view always renders within the parent's layout, it is instantiated directly.

### 4.2 View classes

View classes follow two conventions:

- The constructor accepts a single typed ViewModel and fully initialises the view — building the component tree and binding controls to ViewModel properties.
- Controls are bound to ViewModel properties in the constructor, delegating user interactions back to the ViewModel.

There is no shared base class or interface. The `ViewFactory` constructs views via registered constructor references, which is sufficient.

```java
public class OrdersView extends StackPane {

    public OrdersView(OrdersViewModel viewModel) {
        var listView = new ListView<Order>();
        var refreshButton = new Button("Refresh");
        var statusLabel = new Label();

        var toolbar = new HBox(8, refreshButton, statusLabel);
        var layout = new BorderPane();
        layout.setTop(toolbar);
        layout.setCenter(listView);
        getChildren().add(layout);

        // ViewModel state -> UI (automatic, reactive)
        listView.setItems(viewModel.getOrders());
        statusLabel.textProperty().bind(viewModel.statusTextProperty());

        // UI events -> ViewModel (user intent delegated to ViewModel)
        refreshButton.setOnAction(e -> viewModel.refresh());
        listView.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, order) -> viewModel.openOrder(order));
    }
}
```

> The view is responsible for presentation only. It never calls a service directly, never constructs another ViewModel, and never decides what screen to show next. All of that belongs in the ViewModel or in the composition root.

### 4.3 The ViewFactory

The `ViewFactory` is a type-keyed registry that maps ViewModel types to their corresponding View constructors. It is owned by the ViewRouter, which calls `create` when `navigateTo` is invoked. The mapping must exist somewhere outside the ViewModel layer — `ViewFactory` is that place.

The ViewFactory is a type-keyed registry. At startup, each ViewModel type is associated with a constructor reference for its View. The ViewRouter calls `viewFactory.create(viewModel)` to resolve and construct the correct view.

```java
public class ViewFactory {
    private final Map<Class<?>, Function<Object, Region>> registry = new HashMap<>();

    public <VM> void register(Class<VM> vmClass, Function<VM, Region> factory) {
        registry.put(vmClass, vm -> factory.apply(vmClass.cast(vm)));
    }

    public Region create(Object viewModel) {
        var factory = registry.get(viewModel.getClass());

        if (factory == null) {
            throw new IllegalStateException("No view registered for " + viewModel.getClass().getSimpleName());
        }

        return factory.apply(viewModel);
    }
}
```

Registrations use constructor references. `OrdersView::new` is shorthand for `viewModel -> new OrdersView(viewModel)`. Each registration states the mapping directly:

```java
viewFactory.register(OrdersViewModel.class, OrdersView::new);
viewFactory.register(OrderDetailViewModel.class, OrderDetailView::new);
viewFactory.register(EditItemViewModel.class, EditItemView::new);
viewFactory.register(CustomersViewModel.class, CustomersView::new);
viewFactory.register(SettingsViewModel.class, SettingsView::new);
```

The `ViewFactory` is the sole location that defines the ViewModel-to-View mapping. Adding a screen requires one new registration line; nothing else in the factory changes.

### 4.4 Navigation

#### The ViewRouter class

The ViewRouter is the conduit for navigation events. It owns the `ViewFactory` and resolves the corresponding view when `navigateTo` is called. Listeners register for a specific view type and are only notified when a view of that type is created — removing the need for type switching in the receiving view.

```java
public class ViewRouter {
    private final ViewFactory viewFactory;
    private final Map<Class<?>, Consumer<Region>> listeners = new HashMap<>();

    public ViewRouter(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    public <V extends Region> void addListener(Class<V> viewClass, Consumer<V> listener) {
        listeners.put(viewClass, view -> listener.accept(viewClass.cast(view)));
    }

    public void navigateTo(Object viewModel) {
        var view = viewFactory.create(viewModel);
        var listener = listeners.get(view.getClass());
        if (listener != null) listener.accept(view);
    }
}
```

#### Navigation from the ViewModel perspective

ViewModels never hold a ViewRouter reference. They receive callbacks injected at construction time; those callbacks invoke `viewRouter.navigateTo` internally, keeping the ViewRouter invisible to the ViewModel layer.

```java
// In the composition root — the callback wires the ViewRouter without exposing it to the ViewModel
private OrdersViewModel orders() {
    return new OrdersViewModel(
        orderService,
        order -> viewRouter.navigateTo(orderDetail(order))
    );
}
```

#### Navigation from the View perspective

The ViewRouter is created once in the composition root and injected into views that respond to navigation events — typically the application shell and any view managing a specific presentation context. Views register a listener in their constructor.

> It might seem natural to add methods like `showDialog` or `openInNewTab` to the ViewRouter to express how different ViewModels should be presented. Adding such methods is a mistake. The moment the ViewRouter carries presentation intent, it couples the ViewModel layer to specific UI concepts. A ViewModel that calls `viewRouter.showDialog` is making a presentation decision — which is not its responsibility. The next section explains how presentation decisions are made correctly.

Views that require the ViewRouter receive it through their constructor. Where a view needs additional view-layer dependencies alongside its ViewModel, a lambda is used in the `ViewFactory` registration rather than a plain constructor reference:

```java
viewFactory.register(MainViewModel.class,
    vm -> new MainView(vm, viewRouter));
```

The `ViewFactory` is passed to the `ViewRouter` at construction time in the composition root:

```java
var viewFactory = new ViewFactory();
var viewRouter   = new ViewRouter(viewFactory);
```

### 4.5 Presentation decisions belong to the View

When the ViewRouter creates a view, it makes no claim about presentation. Each listening view registers only for the view types it is responsible for and is not notified of others.

This is how different presentation styles coexist without central coordination. `MainView` registers for the view types it displays in the workspace:

```java
// Inside MainView constructor
viewRouter.addListener(OrdersView.class,     view -> workspace.getChildren().setAll(view));
viewRouter.addListener(OrderDetailView.class, view -> workspace.getChildren().setAll(view));
viewRouter.addListener(CustomersView.class,  view -> workspace.getChildren().setAll(view));
```

A separate `DialogManagerView` — also part of the shell, registering against the same `ViewRouter` — handles views that should appear as modal dialogs. It is responsible for all dialog lifecycle management: opening, closing, and owner configuration:

```java
// Inside DialogManagerView constructor
viewRouter.addListener(EditItemView.class, this::openAsDialog);

private Stage currentDialog;

private void openAsDialog(Region view) {
    closeCurrentDialog();
    currentDialog = new Stage();
    currentDialog.initModality(Modality.APPLICATION_MODAL);
    currentDialog.initOwner(getScene().getWindow());
    currentDialog.setScene(new Scene(view));
    currentDialog.show();
}

private void closeCurrentDialog() {
    if (currentDialog != null) {
        currentDialog.close();
        currentDialog = null;
    }
}
```

Neither the originating ViewModel nor the ViewRouter was involved in the presentation decision. It was made entirely by the view that received the event.

Introducing a new presentation style — a slide-in panel, a notification, an additional tab — requires writing a new view component that subscribes to the ViewRouter and handles the relevant ViewModel types. No existing code is modified; the ViewRouter acquires no new methods; ViewModels are unchanged.

### 4.6 Adding a new screen

The architecture is designed so that adding a new screen is a mechanical, low-risk operation that touches only new files and the composition root. If any step requires modifying existing classes other than the composition root, something has drifted from the invariants.

- **Write the ViewModel** — it takes only the services and context objects it directly uses, plus one callback per navigation action it can trigger.
- **Write the View** — extend the appropriate UI component. Accept the ViewModel as the sole constructor argument, build the component tree, and bind controls to ViewModel properties, all in the constructor.
- **Register the ViewModel-to-View mapping** — `viewFactory.register(MyViewModel.class, MyView::new)`.
- **Add a factory method in the composition root** that constructs the ViewModel with its dependencies and navigation callbacks wired as lambdas.
- **Wire the navigation callback** — in the factory method of whichever ViewModel navigates to the new screen, add a callback lambda that calls `viewRouter.navigateTo(myNewScreen())`.
- **Register a listener** — in whichever view is responsible for presenting the new screen, call `viewRouter.addListener(MyView.class, view -> ...)` with the appropriate presentation logic.

Nothing else changes. The ViewRouter stays minimal. The ViewFactory stays mechanical. ViewModels stay ignorant of views. Existing ViewModels are not modified unless they need to navigate to the new screen. Each piece retains its single responsibility, and the architecture remains flat and uniform regardless of how many screens are added.

---

## 5. Application bootstrapping

### 5.1 The role of App

`App` is the single composition root. It is the only place in the codebase where dependencies are constructed and wired across layer boundaries. Reading it top to bottom gives a complete picture of every screen and every possible navigation transition.

> Here we show how to wire up the dependencies manually but this can easily be done with a dependency injection framework if you wish.

### 5.2 Infrastructure and registration

Services, shared context objects, the ViewRouter, and the `ViewFactory` are all created at startup. Every ViewModel-to-View mapping is registered in one block:

```java
public class App extends Application {

    private ViewRouter viewRouter;
    private OrderService orderService;
    private CustomerService customerService;
    private OrderContext orderContext;

    @Override
    public void start(Stage stage) {
        // Services
        orderService    = new OrderService();
        customerService = new CustomerService();

        // Shared observable state
        orderContext = new OrderContext();

        // Navigation bus
        var viewFactory = new ViewFactory();
        viewFactory.register(SidebarViewModel.class, SidebarView::new);
        viewFactory.register(OrdersViewModel.class, OrdersView::new);
        viewFactory.register(OrderEditorViewModel.class, OrderEditorView::new);
        viewFactory.register(CustomersViewModel.class, CustomersView::new);
        viewFactory.register(SettingsViewModel.class, SettingsView::new);

        viewRouter = new ViewRouter(viewFactory);

        // Build the application shell
        var rootVm = new MainViewModel(sidebar());
        var rootView = new MainView(rootVm, viewRouter);

        stage.setScene(new Scene(rootView, 1024, 768));
        stage.show();

        // Show the initial screen
        viewRouter.navigateTo(orders());
    }
```

### 5.3 Composing the application

#### 5.3.1 Sidebar

The sidebar is permanent — created once and held by `MainViewModel`. It receives the `OrderContext` so its badge counts stay reactive, and one callback per navigation destination:

```java
private SidebarViewModel sidebar() {
        return new SidebarViewModel(
            orderContext,
            () -> viewRouter.navigateTo(orders()),
            () -> viewRouter.navigateTo(customers()),
            () -> viewRouter.navigateTo(settings())
        );
    }
```

#### 5.3.2 Orders flow

The orders list navigates to the order editor. The editor receives three use cases, each constructed with its own service dependency and completion callback. Sub-ViewModels are constructed inside `OrderEditorViewModel` itself — the composition root does not need to know about them:

```java
private OrdersViewModel orders() {
        return new OrdersViewModel(
            new LoadOrdersUseCase(orderService),
            orderContext,
            order -> viewRouter.navigateTo(orderEditor(order))
        );
    }

    private OrderEditorViewModel orderEditor(Order order) {
        var vm = new OrderEditorViewModel(
            order,
            new SaveOrderUseCase(orderService,
                vm::buildUpdatedOrder,
                () -> viewRouter.navigateTo(orders())),
            new DeleteOrderUseCase(orderService,
                () -> viewRouter.navigateTo(orders())),
            new CopyOrderUseCase(orderService,
                copy -> viewRouter.navigateTo(orderEditor(copy)))
        );
        return vm;
    }
```

#### 5.3.3 Customers flow

```java
private CustomersViewModel customers() {
        return new CustomersViewModel(
            customerService,
            customer -> viewRouter.navigateTo(customerDetail(customer))
        );
    }

    private CustomerDetailViewModel customerDetail(Customer customer) {
        return new CustomerDetailViewModel(
            customerService,
            customer,
            () -> viewRouter.navigateTo(customers())
        );
    }
```

#### 5.3.4 Settings

```java
private SettingsViewModel settings() {
        return new SettingsViewModel(
            () -> viewRouter.navigateTo(orders())
        );
    }
}
```

> Each factory method produces a fresh ViewModel instance. Navigating to the same screen twice yields two independent instances; no state persists between visits unless held in a context object or service. As the application grows, related factory methods can be grouped into dedicated classes — an `OrderFlow`, a `CustomerFlow` — each accepting only the services it requires. The method structure is unchanged; only the organisation differs.

### 5.4 Scaling App with Flow classes

As the application grows, `App` acquires more factory methods. They remain individually simple — one method per screen — but their number grows. Flow classes are the natural way to organise them. Each Flow is a plain class that receives only the services and shared state it requires, and exposes factory methods for the screens in its domain.

```java
public class OrderFlow {

    private final OrderService  orderService;
    private final OrderContext  orderContext;
    private final ViewRouter     viewRouter;

    public OrderFlow(
        OrderService orderService,
        OrderContext orderContext,
        ViewRouter    viewRouter) {
        this.orderService = orderService;
        this.orderContext = orderContext;
        this.viewRouter    = viewRouter;
    }

    public OrdersViewModel orders() {
        return new OrdersViewModel(
            new LoadOrdersUseCase(orderService),
            orderContext,
            order -> viewRouter.navigateTo(orderEditor(order))
        );
    }

    private OrderEditorViewModel orderEditor(Order order) {
        var vm = new OrderEditorViewModel(
            order,
            new SaveOrderUseCase(orderService,
                vm::buildUpdatedOrder,
                () -> viewRouter.navigateTo(orders())),
            new DeleteOrderUseCase(orderService,
                () -> viewRouter.navigateTo(orders())),
            new CopyOrderUseCase(orderService,
                copy -> viewRouter.navigateTo(orderEditor(copy)))
        );
        return vm;
    }
}
```

```java
public class CustomerFlow {

    private final CustomerService customerService;
    private final ViewRouter       viewRouter;

    public CustomerFlow(CustomerService customerService, ViewRouter viewRouter) {
        this.customerService = customerService;
        this.viewRouter       = viewRouter;
    }

    public CustomersViewModel customers() {
        return new CustomersViewModel(
            customerService,
            customer -> viewRouter.navigateTo(customerDetail(customer))
        );
    }

    private CustomerDetailViewModel customerDetail(Customer customer) {
        return new CustomerDetailViewModel(
            customerService,
            customer,
            () -> viewRouter.navigateTo(customers())
        );
    }
}
```

`App.start` becomes a wiring site only. It constructs services, creates flows, registers view mappings, and calls `viewRouter.navigateTo` to set the initial screen:

```java
@Override
public void start(Stage stage) {
    var orderService    = new OrderService();
    var customerService = new CustomerService();
    var orderContext    = new OrderContext();
    var viewRouter       = new ViewRouter();

    var orderFlow    = new OrderFlow(orderService, orderContext, viewRouter);
    var customerFlow = new CustomerFlow(customerService, viewRouter);

    var viewFactory = new ViewFactory();
    viewFactory.register(SidebarViewModel.class,      SidebarView::new);
    viewFactory.register(OrdersViewModel.class,        OrdersView::new);
    viewFactory.register(OrderEditorViewModel.class,   OrderEditorView::new);
    viewFactory.register(CustomersViewModel.class,     CustomersView::new);
    viewFactory.register(CustomerDetailViewModel.class, CustomerDetailView::new);
    viewFactory.register(SettingsViewModel.class,      SettingsView::new);

    var sidebarVm = new SidebarViewModel(
        orderContext,
        () -> viewRouter.navigateTo(orderFlow.orders()),
        () -> viewRouter.navigateTo(customerFlow.customers()),
        () -> viewRouter.navigateTo(new SettingsViewModel(() -> viewRouter.navigateTo(orderFlow.orders())))
    );

    var rootVm   = new MainViewModel(sidebarVm);
    var rootView = new MainView(rootVm, viewRouter, viewFactory);

    stage.setScene(new Scene(rootView, 1024, 768));
    stage.show();

    viewRouter.navigateTo(orderFlow.orders());
}
```

Each Flow accepts only what it needs. `CustomerFlow` has no knowledge of `OrderService`; `OrderFlow` has no knowledge of `CustomerService`. Adding a new domain area means writing a new Flow class and registering its views — the composition root itself requires only a new field and a registration block.

---

## 6. Testing

This section demonstrates how to practically unit test the components introduced throughout this document.

### 6.1 Testing ViewModels

#### 6.1.1 Property updates

Construct the ViewModel, invoke a method, and assert the resulting property value. The navigation callback is a no-op lambda; navigation is not under test:

```java
@Test
void refresh_updatesStatusTextWithOrderCount() {
    LoadOrdersUseCase loadOrders = () -> List.of(order1, order2, order3);
    var vm = new OrdersViewModel(loadOrders, order -> {});

    vm.refresh();

    assertEquals("3 orders", vm.statusTextProperty().get());
    assertFalse(vm.loadingProperty().get());
}

@Test
void refresh_setsLoadingDuringExecution() {
    var blocker = new CountDownLatch(1);
    LoadOrdersUseCase loadOrders = () -> {
        blocker.await(); // hold until we inspect loading state
        return List.of();
    };
    var vm = new OrdersViewModel(loadOrders, order -> {});

    vm.refresh();

    assertTrue(vm.loadingProperty().get());
    blocker.countDown();
}
```

#### 6.1.2 Navigation callbacks

Where the test concerns navigation, the callback captures what it receives. No ViewRouter or `ViewFactory` is involved:

```java
@Test
void openOrder_firesNavigationCallbackWithCorrectOrder() {
    var navigatedTo = new AtomicReference<Order>();
    LoadOrdersUseCase loadOrders = () -> List.of(order1);
    var vm = new OrdersViewModel(loadOrders, navigatedTo::set);

    vm.openOrder(order1);

    assertEquals(order1, navigatedTo.get());
}
```

#### 6.1.3 Sub-ViewModel validity

Sub-ViewModels are tested in isolation without the parent ViewModel. These tests verify the validity rules defined within the sub-ViewModel:

```java
@Test
void validity_falseWhenCustomerNameBlank() {
    var vm = new OrderHeaderViewModel(orderWithBlankCustomerName());

    assertFalse(vm.validProperty().get());
}

@Test
void validity_trueWhenAllRequiredFieldsPresent() {
    var vm = new OrderHeaderViewModel(validOrder());

    assertTrue(vm.validProperty().get());
}

@Test
void validity_updatesWhenFieldChanges() {
    var vm = new OrderHeaderViewModel(orderWithBlankCustomerName());

    assertFalse(vm.validProperty().get());

    vm.customerNameProperty().set("Acme Ltd");

    assertTrue(vm.validProperty().get());
}
```

#### 6.1.4 Composed validity (canSave)

The parent ViewModel's `canSave` property is derived from its sub-ViewModels. These tests construct `OrderEditorViewModel` with no-op use cases — they are not under test here — and verify the composition:

```java
// Shared no-op use cases for tests that only care about canSave
SaveOrderUseCase   noOpSave   = () -> CompletableFuture.completedFuture(() -> {});
DeleteOrderUseCase noOpDelete = order -> {};
CopyOrderUseCase   noOpCopy   = order -> {};

@Test
void canSave_falseWhenHeaderInvalid() {
    var vm = new OrderEditorViewModel(
        orderWithBlankCustomerName(), noOpSave, noOpDelete, noOpCopy);

    assertFalse(vm.canSaveProperty().get());
}

@Test
void canSave_falseWhenNoLineItems() {
    var vm = new OrderEditorViewModel(
        orderWithNoLineItems(), noOpSave, noOpDelete, noOpCopy);

    assertFalse(vm.canSaveProperty().get());
}

@Test
void canSave_trueWhenAllSectionsValid() {
    var vm = new OrderEditorViewModel(
        validOrderWithLineItems(), noOpSave, noOpDelete, noOpCopy);

    assertTrue(vm.canSaveProperty().get());
}

@Test
void canSave_reactsToHeaderChange() {
    var vm = new OrderEditorViewModel(
        orderWithBlankCustomerName(), noOpSave, noOpDelete, noOpCopy);

    assertFalse(vm.canSaveProperty().get());

    vm.getHeader().customerNameProperty().set("Acme Ltd");

    assertTrue(vm.canSaveProperty().get());
}
```

#### 6.1.5 AsyncAction

Because `AsyncAction` accepts a `viewExecutor`, tests pass a synchronous executor that runs tasks immediately on the calling thread. This means the full async flow — start, execute, complete — happens synchronously in the test with no thread coordination required:

```java
@Test
void save_executesSuccessfullyWithValidOrder() {
    var savedOrders = new ArrayList<Order>();
    var vm = new OrderEditorViewModel(
        validOrder,
        new SaveOrderUseCase(new StubOrderService(savedOrders::add),
            vm::buildUpdatedOrder,
            () -> {}),
        order -> {},
        order -> {}
    );

    vm.save.executeAsync(Runnable::run);

    assertEquals(1, savedOrders.size());
    assertFalse(vm.save.isExecuting());
}

@Test
void save_disabledWhenCanSaveIsFalse() {
    var vm = new OrderEditorViewModel(
        orderWithBlankCustomerName(),
        new SaveOrderUseCase(new StubOrderService(), vm::buildUpdatedOrder, () -> {}),
        order -> {},
        order -> {}
    );

    assertFalse(vm.save.canExecute());
}

@Test
void save_disabledWhileExecuting() {
    var blocker  = new CompletableFuture<Runnable>();
    SaveOrderUseCase blockingUseCase = () -> blocker; // never completes until unblocked
    var vm = new OrderEditorViewModel(
        validOrder, blockingUseCase, order -> {}, order -> {});

    vm.save.executeAsync(Runnable::run);

    assertTrue(vm.save.isExecuting());
    blocker.complete(() -> {});
    assertFalse(vm.save.isExecuting());
}
```

### 6.2 Testing use cases

A use case test verifies that the service received the correct data and that the completion callback was invoked. The service is an in-memory stub:

```java
@Test
void saveOrderUseCase_callsServiceWithOrder() {
    var savedOrders = new ArrayList<Order>();
    var service     = new StubOrderService(savedOrders::add);
    var onSuccess   = new AtomicBoolean(false);
    var useCase     = new SaveOrderUseCase(service, () -> order, () -> onSuccess.set(true));

    useCase.actionExecutedAsync().join();

    assertEquals(List.of(order), savedOrders);
    assertTrue(onSuccess.get());
}

@Test
void deleteOrderUseCase_callsServiceAndFiresCallback() {
    var deletedIds = new ArrayList<UUID>();
    var service    = new StubOrderService(id -> deletedIds.add(id));
    var navigated  = new AtomicBoolean(false);
    var useCase    = new DeleteOrderUseCase(service, () -> navigated.set(true));

    useCase.execute(order);

    assertEquals(List.of(order.id()), deletedIds);
    assertTrue(navigated.get());
}
```

### 6.3 Testing inter-ViewModel communication

These tests verify that a consuming ViewModel's property reflects context changes, and that a producing ViewModel updates the context on refresh:

```java
@Test
void sidebarPendingCount_updatesWhenContextChanges() {
    var context    = new OrderContext();
    var sidebarVm  = new SidebarViewModel(context, () -> {}, () -> {}, () -> {});

    assertEquals(0, sidebarVm.pendingOrderCountProperty().get());

    context.setPendingCount(5);

    assertEquals(5, sidebarVm.pendingOrderCountProperty().get());
}

@Test
void ordersViewModel_updatesContextOnRefresh() {
    var context      = new OrderContext();
    LoadOrdersUseCase loadOrders = () -> List.of(order1, order2);
    var vm           = new OrdersViewModel(loadOrders, context, order -> {});

    vm.refresh();

    assertEquals(2, context.pendingCountProperty().get());
}
```

### 6.4 Stub implementations

Services are replaced with in-memory stubs rather than framework-generated mocks. A stub is a concrete implementation providing only what the test requires:

```java
public class StubOrderService implements OrderService {
    private final List<Order> orders;
    private final Consumer<Order> onSave;

    public StubOrderService() {
        this(List.of(), order -> {});
    }

    public StubOrderService(List<Order> orders) {
        this(orders, order -> {});
    }

    public StubOrderService(Consumer<Order> onSave) {
        this(List.of(), onSave);
    }

    public StubOrderService(List<Order> orders, Consumer<Order> onSave) {
        this.orders = orders;
        this.onSave = onSave;
    }

    @Override
    public List<Order> fetchAll() { return orders; }

    @Override
    public void save(Order order) { onSave.accept(order); }

    @Override
    public void delete(UUID id) {}

    @Override
    public Order copy(UUID id) {
        return orders.stream()
            .filter(o -> o.id().equals(id))
            .findFirst()
            .map(o -> o.withId(UUID.randomUUID()))
            .orElseThrow(() -> new IllegalArgumentException("No order with id " + id));
    }
}
```

Stubs are preferable to mocks for two reasons. First, the test setup describes what the service does rather than which methods must be called, making intent explicit. Second, stubs are resilient to refactoring that does not change behaviour: a mock asserting `orderService.save()` was called fails if the method is renamed; a stub recording the saved entity does not.

### 6.5 Testing Action and ViewFactory directly

#### 6.5.1 Action

`Action` is a small but load-bearing class. Tests verify the guard, the binding, and that the `canExecute` wrapper correctly reflects the bound value:

```java
@Test
void action_executesWhenCanExecuteIsTrue() {
    var executed = new AtomicBoolean(false);
    var action   = new Action(() -> executed.set(true));

    action.execute();

    assertTrue(executed.get());
}

@Test
void action_doesNotExecuteWhenCanExecuteIsFalse() {
    var executed   = new AtomicBoolean(false);
    var canExecute = new SimpleBooleanProperty(false);
    var action     = new Action(() -> executed.set(true), canExecute);

    action.execute();

    assertFalse(executed.get());
}

@Test
void action_canExecuteTracksBinding() {
    var canExecute = new SimpleBooleanProperty(false);
    var action     = new Action(() -> {}, canExecute);

    assertFalse(action.canExecute());

    canExecute.set(true);

    assertTrue(action.canExecute());
}
```

#### 6.5.2 AsyncAction

```java
@Test
void asyncAction_canExecuteIsTrueInitially() {
    var action = new AsyncAction(() -> CompletableFuture.completedFuture(() -> {}));

    assertTrue(action.canExecute());
}

@Test
void asyncAction_preventsDoubleSubmission() {
    var blocker = new CompletableFuture<Runnable>();
    var action  = new AsyncAction(() -> blocker);

    action.executeAsync(Runnable::run);

    assertFalse(action.canExecute());  // blocked mid-flight
    assertTrue(action.isExecuting());

    blocker.complete(() -> {});

    assertTrue(action.canExecute());   // available again
    assertFalse(action.isExecuting());
}

@Test
void asyncAction_doesNotExecuteWhenBindingIsFalse() {
    var executed   = new AtomicBoolean(false);
    var canExecute = new SimpleBooleanProperty(false);
    var action     = new AsyncAction(() -> {
        executed.set(true);
        return CompletableFuture.completedFuture(() -> {});
    }, canExecute);

    action.executeAsync(Runnable::run);

    assertFalse(executed.get());
}
```

#### 6.5.3 ViewFactory

```java
@Test
void viewFactory_createsCorrectViewForRegisteredViewModel() {
    var factory = new ViewFactory();
    factory.register(OrdersViewModel.class, OrdersView::new);

    LoadOrdersUseCase loadOrders = List::of;
    var vm   = new OrdersViewModel(loadOrders, order -> {});
    var view = factory.create(vm);

    assertInstanceOf(OrdersView.class, view);
}

@Test
void viewFactory_throwsForUnregisteredViewModel() {
    var factory = new ViewFactory();

    assertThrows(IllegalStateException.class,
        () -> factory.create(new OrdersViewModel(List::of, order -> {})));
}
```

---

## 7. Architecture review

### 7.1 How design goals are met

Section 1.4 defined six design goals for this architecture. Each is addressed directly by a structural decision described in this document:

- **Every View is constructed with exactly one ViewModel** — enforced by the View interface and the ViewFactory, both of which accept a single typed ViewModel.
- **ViewModels have no knowledge of views or how they are constructed** — the ViewFactory mapping lives entirely in the view layer; ViewModels hold only callbacks.
- **Each ViewModel holds only dependencies it directly uses** — navigation callbacks remove the need to pass dependencies through to child ViewModels. The composition root handles all construction.
- **Nothing creates its own dependencies** — services, context objects, use cases, and navigation callbacks are all injected via constructors. There is no `new` inside a ViewModel.
- **Navigation callbacks are injected at construction time** — ViewModels call callbacks and know nothing about what follows. The ViewRouter is never referenced in the ViewModel layer.
- **All construction and wiring lives in the composition root** — factory methods in `App` are the sole place where services, use cases, and callbacks are assembled. Reading it gives a complete map of every screen and transition.

### 7.2 How common problems are addressed

Section 1.3 described the problems that recur in MVVM implementations. This section maps each to the design decision that addresses it.

#### 7.2.1 ViewModels with too many responsibilities

ViewModels in this architecture are state holders and coordinators. They expose observable properties and invoke callbacks. Execution logic belongs to use case objects; section-level state and validation belongs to sub-ViewModels; navigation wiring belongs to the composition root. There is no place in the design where unrelated logic can accumulate in a ViewModel.

#### 7.2.2 Services injected directly into ViewModels

No services are injected into ViewModels. A ViewModel receives use case objects and callbacks; a use case takes only the service functionality it requires. The ViewModel has no knowledge of whether data originates from a database, a remote API, or a test stub. ISP violations do not arise because the ViewModel holds no service interface. Tests construct the ViewModel with lightweight dependencies and assert state directly.

#### 7.2.3 Navigation coupled to presentation

The ViewRouter carries no presentation intent — one method, no knowledge of how a ViewModel will appear. ViewModels express navigation through callbacks that convey intent without prescribing presentation. If a modal dialog is later replaced by an inline panel, no ViewModel is modified; only the view responsible for that ViewModel type changes.

#### 7.2.4 Inheritance used to share logic

The `View` interface enforces construction ordering through convention rather than inheritance — views fully initialise themselves in the constructor. Views do not share a base class. ViewModels have no base class either. Shared state is held in context objects, not extracted into common parents. There is no inheritance hierarchy that can become fragile.

#### 7.2.5 Fat ViewModels from delegate commands

Use cases replace delegate commands. Each is a discrete class with its own dependencies, independently constructable and testable. The ViewModel delegates rather than housing command logic. Adding an operation means adding a use case class; the ViewModel acquires one new constructor argument.

#### 7.2.6 Testability claimed but not demonstrated

A ViewModel in this architecture requires minimal test setup. Use case objects and callbacks can be supplied as lambdas. A test constructs the ViewModel, invokes a method, and asserts the resulting property state. No service interfaces require mocking; no UI runtime needs to be launched. The testing examples in section 6 illustrate this concretely.

