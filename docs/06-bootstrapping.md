## 6. Application bootstrapping

This section covers the composition root — the single place in the codebase where services, use cases, ViewModels, and navigation callbacks are constructed and wired together. It shows how to structure the application startup class and how to organise it using Module classes.

## Contents

- [6.1 The role of App](#61-the-role-of-app)
- [6.2 ShellModule](#62-shellmodule)
- [6.3 Domain modules](#63-domain-modules)
- [6.4 Wiring it together](#64-wiring-it-together)

### 6.1 The role of App

`App` is the single composition root. It is the only place in the codebase where dependencies are constructed and wired across layer boundaries. Reading it top to bottom gives a complete picture of every screen and every possible navigation transition.

> Here we show how to wire up the dependencies manually but this can easily be done with a dependency injection framework if you wish.

`App.start` delegates immediately to a `bootstrap` method that creates modules in dependency order and returns the root view:

```java
public class App extends Application {

    @Override
    public void start(Stage stage) {
        var mainView = bootstrap(stage);

        stage.setTitle("Order Management");
        stage.setScene(new Scene(mainView, 1024, 768));
        stage.show();
    }

    private Parent bootstrap(Stage stage) {
        var shell = new ShellModule(stage);
        var orders = new OrdersModule(shell.appContext(), shell.workspaceContext());
        var customers = new CustomersModule(shell.appContext(), shell.workspaceContext());

        var navigation = shell.navigation(orders, customers);

        shell.workspaceContext().show(orders.ordersExplorerViewModel());

        return shell.mainView(orders.orderContext(), navigation);
    }
}
```

### 6.2 ShellModule

`ShellModule` owns the application's navigation infrastructure. Its constructor takes the JavaFX `Window` (needed to anchor dialogs) and creates two shared objects that every domain module receives:

- **`ViewServices`** — bundles a `ViewLocator` for workspace views and a `DialogManager` for dialog views. Every module registers its own ViewModel-to-View mappings here at construction time.
- **`WorkspaceContext`** — holds the currently displayed workspace ViewModel as an observable property. Domain modules call `workspaceContext.show(viewModel)` to navigate between screens.

```java
public class ShellModule {

    private final ViewServices appContext;
    private final WorkspaceContext workspaceContext;

    public ShellModule(Window window) {
        this.appContext = new ViewServices(
            new ViewLocator<>(),
            new DialogManager(window, new ViewLocator<>())
        );

        this.workspaceContext = new WorkspaceContext();

        appContext.viewLocator().register(MainViewModel.class,
            vm -> new MainView(vm, appContext.viewLocator()));
    }

    public ViewServices appContext() { return appContext; }
    public WorkspaceContext workspaceContext() { return workspaceContext; }
    ...
}
```

`ShellModule.navigation` wires the sidebar callbacks. Each callback calls `workspaceContext.show` with a freshly constructed ViewModel, so navigating to the same screen twice yields independent instances:

```java
public Navigation navigation(OrdersModule orders, CustomersModule customers) {
    return new Navigation(
        () -> workspaceContext.show(orders.ordersExplorerViewModel()),
        () -> workspaceContext.show(customers.customersExplorerViewModel()),
    );
}

public record Navigation(
    Runnable navigateToOrders,
    Runnable navigateToCustomers
) {}
```

`ShellModule.mainView` constructs `MainViewModel` — which embeds `SidebarViewModel` — and locates the corresponding view:

```java
public Parent mainView(OrderContext orderContext, Navigation navigation) {
    return appContext.viewLocator().locate(new MainViewModel(
        new SidebarViewModel(
            orderContext,
            navigation.navigateToOrders,
            navigation.navigateToCustomers
        ),
        workspaceContext
    ));
}
```

### 6.3 Domain modules

As an application grows, `App` accumulates more factory methods. Modules are the natural way to organise them. Each module is self-contained: it creates its own services and repositories, registers its own ViewModel-to-View mappings with the shared `ViewServices`, and exposes factory methods for the screens in its domain.

```java
public class OrdersModule {

    private final WorkspaceContext workspaces;
    private final OrderRepository orderRepository;
    private final OrderContext orderContext;

    public OrdersModule(ViewServices appContext, WorkspaceContext workspaces) {
        this.workspaces = workspaces;
        this.orderRepository = new InMemoryOrderRepository();
        this.orderContext = new OrderContext();

        appContext.viewLocator().register(OrdersExplorerViewModel.class, OrdersExplorerView::new);
        appContext.viewLocator().register(OrderEditorViewModel.class, OrderEditorView::new);
        appContext.dialogManager().register(EditItemViewModel.class, EditItemView::dialog);
    }

    public OrderContext orderContext() { return orderContext; }

    public OrdersExplorerViewModel ordersExplorerViewModel() { ... }

    private OrderEditorViewModel orderEditorViewModel(Order order) { ... }
}
```

The constructor does three things: creates the module's own infrastructure, registers its views, and stores any dependencies needed by the factory methods. Public factory methods are the entry points exposed to `App`; private ones handle internal navigation within the domain.

The sample application is split into four modules:

- **`ShellModule`** — navigation infrastructure (`ViewServices`, `WorkspaceContext`), the main window layout, and the sidebar. This is created first and passes its shared objects to the domain modules.
- **`OrdersModule`** — order explorer, order editor, and the line item edit dialog. Owns `OrderRepository`, `CopyOrderService`, and `OrderContext`.
- **`CustomersModule`** — customer explorer and customer detail. Owns `CustomerService`.

### 6.4 Wiring it together

`App.bootstrap` creates modules in dependency order, wires navigation, sets the initial screen, and returns the root view:

```java
private Parent bootstrap(Stage stage) {
    var shell = new ShellModule(stage);
    var orders = new OrdersModule(shell.appContext(), shell.workspaceContext());
    var customers = new CustomersModule(shell.appContext(), shell.workspaceContext());

    var navigation = shell.navigation(orders, customers);

    shell.workspaceContext().show(orders.ordersExplorerViewModel());

    return shell.mainView(orders.orderContext(), navigation);
}
```

Each module is fully self-contained: `CustomersModule` has no knowledge of `OrderService` or `OrderContext`; `OrdersModule` has no knowledge of `CustomerService`. Adding a new domain area means writing a new Module — `App` itself requires only one new line to create it.

Each factory method produces a fresh ViewModel instance. No state persists between visits to a screen unless it is held in a context object or service.
