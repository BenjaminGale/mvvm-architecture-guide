## 3. ViewModels

This section describes the ViewModel layer in detail. ViewModels hold observable state, coordinate use cases, and express navigation through injected callbacks. It covers the core ViewModel structure, strategies for managing complexity as ViewModels grow, patterns for inter-ViewModel communication, and the Action utilities that reduce view-layer boilerplate.

## Contents

- [3.1 The ViewModel class](#31-the-viewmodel-class)
  - [3.1.1 Observable properties](#311-observable-properties)
  - [3.1.2 Navigation via injected callbacks](#312-navigation-via-injected-callbacks)
  - [3.1.3 Why ViewModels should not create or host views](#313-why-viewmodels-should-not-create-or-host-views)
- [3.2 Organising complex ViewModels](#32-organising-complex-viewmodels)
  - [3.2.1 Use cases as injectable objects](#321-use-cases-as-injectable-objects)
  - [Managing constructor length with a use case record](#managing-constructor-length-with-a-use-case-record)
  - [3.2.2 Sub-ViewModels for distinct UI sections](#322-sub-viewmodels-for-distinct-ui-sections)
  - [3.2.3 Composing sub-ViewModels in the parent](#323-composing-sub-viewmodels-in-the-parent)
  - [3.2.4 The result of combining all three strategies](#324-the-result-of-combining-all-three-strategies)
- [3.3 ViewModel communication patterns](#33-viewmodel-communication-patterns)
- [3.4 Action classes](#34-action-classes)
  - [3.4.1 The problem they solve](#341-the-problem-they-solve)
  - [3.4.2 Action — synchronous operations](#342-action--synchronous-operations)
- [3.5 AsyncAction — long-running operations](#35-asyncaction--long-running-operations)
  - [3.5.1 How they fit into the architecture](#351-how-they-fit-into-the-architecture)

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
public class OrderContext implements OrderContext.PendingOrderCount, OrderContext.PendingOrderCounter {

    private final IntegerProperty pendingCount = new SimpleIntegerProperty(0);

    @Override
    public void setPendingCount(int count) { pendingCount.set(count); }

    @Override
    public ReadOnlyIntegerProperty pendingCountProperty() { return pendingCount; }

    public interface PendingOrderCount {
        ReadOnlyIntegerProperty pendingCountProperty();
    }

    public interface PendingOrderCounter {
        void setPendingCount(int count);
    }
}
```

```java
// SidebarViewModel receives only the count — it cannot mutate state
public SidebarViewModel(OrderContext.PendingOrderCount orderContext, ...) {
    pendingOrderCount.bind(orderContext.pendingCountProperty());
}

// OrdersViewModel receives only the counter — it cannot observe state
public OrdersViewModel(LoadOrdersUseCase loadOrders, OrderContext.PendingOrderCounter orderContext, ...) {
    this.loadOrders   = loadOrders;
    this.orderContext = orderContext;
}
```

In the composition root the single `OrderContext` instance satisfies both interfaces:

```java
var orderContext = new OrderContext();

// The same instance satisfies both interfaces
var sidebarVm = new SidebarViewModel(orderContext, ...);
var ordersVm  = new OrdersViewModel(new LoadOrdersUseCase(orderService), orderContext, ...);
```

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
