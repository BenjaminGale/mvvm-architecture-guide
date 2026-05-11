# MVVM Architecture Guide

## Contents

- [1. Introduction](docs/01-introduction.md)
- [2. The Model and Service layers](docs/02-model-and-service-layer.md)
- [3. ViewModels](docs/03-viewmodels.md)
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

