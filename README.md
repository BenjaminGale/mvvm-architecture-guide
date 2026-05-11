# MVVM Architecture Guide

## Contents

- [1. Introduction](docs/01-introduction.md)
- [2. The Model and Service layers](docs/02-model-and-service-layer.md)
- [3. ViewModels](docs/03-viewmodels.md)
- [4. Views](docs/04-views.md)
- [5. Bootstrapping](docs/05-bootstrapping.md)
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

