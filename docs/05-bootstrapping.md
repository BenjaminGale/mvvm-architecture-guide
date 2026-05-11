## 5. Application bootstrapping

This section covers the composition root — the single place in the codebase where services, use cases, ViewModels, and navigation callbacks are constructed and wired together. It shows how to structure the application startup class and how to scale it as the application grows using Flow classes.

## Contents

- [5.1 The role of App](#51-the-role-of-app)
- [5.2 Infrastructure and registration](#52-infrastructure-and-registration)
- [5.3 Composing the application](#53-composing-the-application)
  - [5.3.1 Sidebar](#531-sidebar)
  - [5.3.2 Orders flow](#532-orders-flow)
  - [5.3.3 Customers flow](#533-customers-flow)
  - [5.3.4 Settings](#534-settings)
- [5.4 Scaling App with Flow classes](#54-scaling-app-with-flow-classes)

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
