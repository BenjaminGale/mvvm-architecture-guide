## 8. Testing

This section demonstrates how to practically unit test the components introduced throughout this document.

## Contents

- [8.1 Testing ViewModels](#81-testing-viewmodels)
  - [8.1.1 Property updates](#811-property-updates)
  - [8.1.2 Navigation callbacks](#812-navigation-callbacks)
  - [8.1.3 Sub-ViewModel validity](#813-sub-viewmodel-validity)
  - [8.1.4 Composed validity (canSave)](#814-composed-validity-cansave)
  - [8.1.5 AsyncAction](#815-asyncaction)
- [8.2 Testing use cases](#82-testing-use-cases)
- [8.3 Testing inter-ViewModel communication](#83-testing-inter-viewmodel-communication)
- [8.4 Stub implementations](#84-stub-implementations)
- [8.5 Testing Action and ViewLocator directly](#85-testing-action-and-viewlocator-directly)
  - [8.5.1 Action](#851-action)
  - [8.5.2 AsyncAction](#852-asyncaction)
  - [8.5.3 ViewLocator](#853-viewlocator)

### 8.1 Testing ViewModels

#### 7.1.1 Property updates

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

#### 7.1.2 Navigation callbacks

Where the test concerns navigation, the callback captures what it receives. No ViewRouter or `ViewLocator` is involved:

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

#### 7.1.3 Sub-ViewModel validity

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

#### 7.1.4 Composed validity (canSave)

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

#### 7.1.5 AsyncAction

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

### 8.2 Testing use cases

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

### 8.3 Testing inter-ViewModel communication

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

### 8.4 Stub implementations

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

### 8.5 Testing Action and ViewLocator directly

#### 7.5.1 Action

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

#### 7.5.2 AsyncAction

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

#### 7.5.3 ViewLocator

```java
@Test
void viewLocator_resolvesCorrectViewForRegisteredViewModel() {
    var locator = new ViewLocator();
    locator.register(OrdersViewModel.class, OrdersView::new);

    LoadOrdersUseCase loadOrders = List::of;
    var vm   = new OrdersViewModel(loadOrders, order -> {});
    var view = locator.resolve(vm);

    assertInstanceOf(OrdersView.class, view);
}

@Test
void viewLocator_throwsForUnregisteredViewModel() {
    var locator = new ViewLocator();

    assertThrows(IllegalStateException.class,
        () -> locator.resolve(new OrdersViewModel(List::of, order -> {})));
}
```
