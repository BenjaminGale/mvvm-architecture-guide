## 6. Application bootstrapping

This section covers the composition root — the single place in the codebase where services, use cases, ViewModels, and navigation callbacks are constructed and wired together. It shows how to structure the application startup class and how to scale it as the application grows using Flow classes.

## Contents

- [6.1 The role of App](#61-the-role-of-app)
- [6.2 Infrastructure and registration](#62-infrastructure-and-registration)
- [6.3 Composing the application](#63-composing-the-application)
  - [6.3.1 Sidebar](#631-sidebar)
  - [6.3.2 Orders flow](#632-orders-flow)
  - [6.3.3 Customers flow](#633-customers-flow)
  - [6.3.4 Settings](#634-settings)
- [6.4 Scaling App with Modules](#64-scaling-app-with-modules)

### 6.1 The role of App

`App` is the single composition root. It is the only place in the codebase where dependencies are constructed and wired across layer boundaries. Reading it top to bottom gives a complete picture of every screen and every possible navigation transition.

> Here we show how to wire up the dependencies manually but this can easily be done with a dependency injection framework if you wish.

### 6.2 Infrastructure and registration

Services, shared context objects, the ViewRouter, and the `ViewLocator` are all created at startup. Every ViewModel-to-View mapping is registered in one block:

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

        // Navigation infrastructure
        var viewLocator = new ViewLocator();
        viewLocator.register(SidebarViewModel.class, SidebarView::new);
        viewLocator.register(OrdersViewModel.class, OrdersView::new);
        viewLocator.register(OrderEditorViewModel.class, OrderEditorView::new);
        viewLocator.register(CustomersViewModel.class, CustomersView::new);
        viewLocator.register(SettingsViewModel.class, SettingsView::new);

        viewRouter = new ViewRouter(viewLocator);

        // Build the application shell
        var rootVm = new MainViewModel(sidebar());
        var rootView = new MainView(rootVm, viewRouter);

        stage.setScene(new Scene(rootView, 1024, 768));
        stage.show();

        // Show the initial screen
        viewRouter.route(orders());
    }
```

### 6.3 Composing the application

#### 5.3.1 Sidebar

The sidebar is permanent — created once and held by `MainViewModel`. It receives the `OrderContext` so its badge counts stay reactive, and one callback per navigation destination:

```java
private SidebarViewModel sidebar() {
        return new SidebarViewModel(
            orderContext,
            () -> viewRouter.route(orders()),
            () -> viewRouter.route(customers()),
            () -> viewRouter.route(settings())
        );
    }
```

#### 5.3.2 Orders flow

> **Note: this section is out of date and needs updating.**

The orders list navigates to the order editor. The editor receives three use cases, each constructed with its own service dependency and completion callback. Sub-ViewModels are constructed inside `OrderEditorViewModel` itself — the composition root does not need to know about them:

```java
private OrdersViewModel orders() {
        return new OrdersViewModel(
            new LoadOrdersUseCase(orderService),
            orderContext,
            order -> viewRouter.route(orderEditor(order))
        );
    }

    private OrderEditorViewModel orderEditor(Order order) {
        var vm = new OrderEditorViewModel(
            order,
            new SaveOrderUseCase(orderService,
                vm::buildUpdatedOrder,
                () -> viewRouter.route(orders())),
            new DeleteOrderUseCase(orderService,
                () -> viewRouter.route(orders())),
            new CopyOrderUseCase(orderService,
                copy -> viewRouter.route(orderEditor(copy)))
        );
        return vm;
    }
```

#### 5.3.3 Customers flow

```java
private CustomersViewModel customers() {
        return new CustomersViewModel(
            customerService,
            customer -> viewRouter.route(customerDetail(customer))
        );
    }

    private CustomerDetailViewModel customerDetail(Customer customer) {
        return new CustomerDetailViewModel(
            customerService,
            customer,
            () -> viewRouter.route(customers())
        );
    }
```

#### 5.3.4 Settings

```java
private SettingsViewModel settings() {
        return new SettingsViewModel(
            () -> viewRouter.route(orders())
        );
    }
}
```

> Each factory method produces a fresh ViewModel instance. Navigating to the same screen twice yields two independent instances; no state persists between visits unless held in a context object or service. As the application grows, related factory methods can be grouped into dedicated Module classes — an `OrderModule`, a `CustomerModule`. The method structure is unchanged; only the organisation differs.

### 6.4 Scaling App with Modules

As the application grows, `App` acquires more factory methods. They remain individually simple — one method per screen — but their number grows. Modules are the natural way to organise them. Each Module owns its own infrastructure — creating its own services, repositories, and shared state — and registers its own ViewModel-to-View mappings. It exposes factory methods for the screens in its domain and nothing else.

```java
public class OrderModule {

    private final OrderService orderService;
    private final OrderContext orderContext;
    private final ViewRouter   viewRouter;

    public OrderModule(ViewLocator viewLocator, ViewRouter viewRouter) {
        this.orderService = new OrderService(new InMemoryOrderRepository());
        this.orderContext  = new OrderContext();
        this.viewRouter    = viewRouter;

        viewLocator.register(OrdersViewModel.class,      OrdersExplorerView::new);
        viewLocator.register(OrderEditorViewModel.class, OrderEditorView::new);
        viewLocator.register(EditItemViewModel.class,    EditItemView::new);
    }

    public OrderContext orderContext() {
        return orderContext;
    }

    public OrdersViewModel orders() {
        return new OrdersViewModel(
            orderService::fetchAll,
            orderContext,
            order -> viewRouter.route(orderEditor(order))
        );
    }

    private OrderEditorViewModel orderEditor(Order order) { ... }
}
```

```java
public class CustomerModule {

    private final CustomerService customerService;
    private final ViewRouter       viewRouter;

    public CustomerModule(ViewLocator viewLocator, ViewRouter viewRouter) {
        this.customerService = new CustomerService(new InMemoryCustomerRepository());
        this.viewRouter       = viewRouter;

        viewLocator.register(CustomersViewModel.class,      CustomersExplorerView::new);
        viewLocator.register(CustomerDetailViewModel.class, CustomerDetailView::new);
    }

    public CustomersViewModel customers() {
        return new CustomersViewModel(
            customerService,
            customer -> viewRouter.route(customerDetail(customer))
        );
    }

    private CustomerDetailViewModel customerDetail(Customer customer) { ... }
}
```

`App.start` is reduced to navigation infrastructure, creating modules, and registering views that belong to no specific module:

```java
@Override
public void start(Stage stage) {
    var viewLocator = new ViewLocator();
    var viewRouter  = new ViewRouter(viewLocator);

    var orderModule    = new OrderModule(viewLocator, viewRouter);
    var customerModule = new CustomerModule(viewLocator, viewRouter);

    viewLocator.register(SettingsViewModel.class, SettingsView::new);

    var sidebarVm = new SidebarViewModel(
        orderModule.orderContext(),
        () -> viewRouter.route(orderModule.orders()),
        () -> viewRouter.route(customerModule.customers()),
        () -> viewRouter.route(new SettingsViewModel(() -> viewRouter.route(orderModule.orders())))
    );

    var rootView = new MainView(new MainViewModel(sidebarVm), viewRouter);

    stage.setScene(new Scene(rootView, 1024, 768));
    stage.show();

    viewRouter.route(orderModule.orders());
}
```

Each Module is fully self-contained: `CustomerModule` has no knowledge of `OrderService` or `OrderContext`; `OrderModule` has no knowledge of `CustomerService`. Adding a new domain area means writing a new Module — `App` itself requires only one new line to create it.
