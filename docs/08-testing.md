## 8. Testing

This section demonstrates how to practically unit test the components introduced throughout this document.

## Contents

- [8.1 Testing ViewModels](#81-testing-viewmodels)
  - [8.1.1 Property updates](#811-property-updates)
  - [8.1.2 Navigation callbacks](#812-navigation-callbacks)
  - [8.1.3 Sub-ViewModel validity](#813-sub-viewmodel-validity)
  - [8.1.4 Composed validity (canSave)](#814-composed-validity-cansave)
  - [8.1.5 AsyncAction](#815-asyncaction)
- [8.2 Testing inter-ViewModel communication](#82-testing-inter-viewmodel-communication)
- [8.3 Stub implementations](#83-stub-implementations)
- [8.4 Testing Action and ViewLocator directly](#84-testing-action-and-viewlocator-directly)
  - [8.4.1 Action](#841-action)
  - [8.4.2 AsyncAction](#842-asyncaction)
  - [8.4.3 ViewLocator](#843-viewlocator)

### 8.1 Testing ViewModels

#### 8.1.1 Property updates

Construct the ViewModel, invoke a method, and assert the resulting property value. The navigation callback is a no-op lambda; navigation is not under test:

```java
@Test
void refresh_updatesStatusTextWithOrderCount() {
    LoadOrdersUseCase loadOrders = () -> List.of(order1, order2, order3);
    var vm = new OrdersViewModel(loadOrders, order -> {});

    vm.refresh();

    assertEquals("3 orders", vm.statusTextProperty().get());
}
```

#### 8.1.2 Navigation callbacks

Where the test concerns navigation, the callback captures what it receives. No `ViewLocator` is involved:

```java
@Test
void openOrder_firesNavigationCallbackWithCorrectOrder() {
    Consumer<Order> onNavigate = mock();
    OrdersViewModel vm = new OrdersViewModel(() -> List.of(order1), onNavigate);

    vm.openOrder(order1);

    verify(onNavigate).accept(order1);
}
```

#### 8.1.3 Sub-ViewModel validity

Sub-ViewModels are tested in isolation without the parent ViewModel. The reactive case covers both initial state and change propagation:

```java
@Test
void validity_updatesWhenFieldChanges() {
    var vm = new OrderHeaderViewModel(orderWithBlankCustomerName());

    assertFalse(vm.validProperty().get());

    vm.customerNameProperty().set("Acme Ltd");

    assertTrue(vm.validProperty().get());
}
```

#### 8.1.4 Composed validity (canSave)

The parent ViewModel's `canSave` property is derived from its sub-ViewModels. No-op use cases are injected — they are not under test here:

```java
SaveOrderUseCase noOpSave = () -> CompletableFuture.completedFuture(() -> {});
DeleteOrderUseCase noOpDelete = order -> {};
CopyOrderUseCase noOpCopy = order -> {};

@Test
void canSave_falseWhenHeaderInvalid() {
    var vm = new OrderEditorViewModel(
        orderWithBlankCustomerName(), noOpSave, noOpDelete, noOpCopy);

    assertFalse(vm.canSaveProperty().get());
}

@Test
void canSave_reactsToHeaderChange() {
    var vm = new OrderEditorViewModel(
        orderWithBlankCustomerName(), noOpSave, noOpDelete, noOpCopy);

    vm.getHeader().customerNameProperty().set("Acme Ltd");

    assertTrue(vm.canSaveProperty().get());
}
```

#### 8.1.5 AsyncAction

Tests pass a synchronous executor (`Runnable::run`) so the full async flow happens on the calling thread with no coordination required:

```java
@Test
void save_disabledWhileExecuting() {
    var blocker = new CompletableFuture<Runnable>();
    var vm = new OrderEditorViewModel(validOrder, () -> blocker, order -> {}, order -> {});

    vm.save.executeAsync(Runnable::run);

    assertTrue(vm.save.isExecuting());
    blocker.complete(() -> {});
    assertFalse(vm.save.isExecuting());
}
```

### 8.2 Testing inter-ViewModel communication

Tests verify that a consuming ViewModel's property reflects context changes, and that a producing ViewModel updates the context on refresh:

```java
@Test
void sidebarPendingCount_updatesWhenContextChanges() {
    var context = new OrderContext();
    var sidebarVm = new SidebarViewModel(context, () -> {}, () -> {}, () -> {});

    context.setPendingCount(5);

    assertEquals(5, sidebarVm.pendingOrderCountProperty().get());
}
```

### 8.3 Stub implementations

Services are replaced with in-memory stubs rather than framework-generated mocks. A stub is a concrete implementation that provides only what the test requires:

```java
public class StubOrderService implements OrderService {
    private final List<Order> orders;
    private final Consumer<Order> onSave;

    public StubOrderService() { this(List.of(), order -> {}); }
    public StubOrderService(Consumer<Order> onSave) { this(List.of(), onSave); }
    public StubOrderService(List<Order> orders) { this(orders, order -> {}); }

    public StubOrderService(List<Order> orders, Consumer<Order> onSave) {
        this.orders = orders;
        this.onSave = onSave;
    }

    @Override public List<Order> fetchAll() { return orders; }
    @Override public void save(Order order) { onSave.accept(order); }
    @Override public void delete(UUID id) {}
}
```

Stubs are preferable to mocks because test setup describes what the service _does_ rather than which methods must be called. They are also resilient to refactoring: a mock asserting `orderService.save()` was called breaks if the method is renamed; a stub recording the saved entity does not.

### 8.4 Testing Action and ViewLocator directly

#### 8.4.1 Action

Mock the `Listener` to verify execution, and assert that `canExecute` correctly reflects a bound property:

```java
@Test
void action_executesTheListener() {
    Action.Listener listener = mock();
    var action = new Action(listener);

    action.execute();

    verify(listener).actionExecuted();
}

@Test
void action_throwsWhenCanExecuteIsFalse() {
    var canExecute = new SimpleBooleanProperty(false);
    var action = new Action(() -> {}, canExecute);

    assertThrows(IllegalStateException.class, action::execute);
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

#### 8.4.2 AsyncAction

The key behaviour is double-submission prevention: `canExecute` becomes false while a task is in flight and recovers on completion:

```java
@Test
void asyncAction_preventsDoubleSubmission() {
    var blocker = new CompletableFuture<Runnable>();
    var action = new AsyncAction(() -> blocker);

    action.executeAsync(Runnable::run);

    assertFalse(action.canExecute());
    assertTrue(action.isExecuting());

    blocker.complete(() -> {});

    assertTrue(action.canExecute());
    assertFalse(action.isExecuting());
}
```

#### 8.4.3 ViewLocator

```java
@Test
void viewLocator_resolvesCorrectViewForRegisteredViewModel() {
    var locator = new ViewLocator();
    locator.register(OrdersViewModel.class, OrdersView::new);

    var view = locator.resolve(new OrdersViewModel(List::of, order -> {}));

    assertInstanceOf(OrdersView.class, view);
}

@Test
void viewLocator_throwsForUnregisteredViewModel() {
    var locator = new ViewLocator();

    assertThrows(IllegalStateException.class,
        () -> locator.resolve(new OrdersViewModel(List::of, order -> {})));
}
```
