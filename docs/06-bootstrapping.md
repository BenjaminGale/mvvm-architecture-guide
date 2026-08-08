## 6. Application bootstrapping

This section covers the composition root: the single place in the codebase where services, use cases, ViewModels, and navigation callbacks are constructed and wired together. It shows how to structure the application startup class and how to organise it using Module classes.

## Contents

- [6.1 The role of App](#61-the-role-of-app)
- [6.2 ShellModule](#62-shellmodule)
- [6.3 Domain modules](#63-domain-modules)
- [6.4 Wiring it together](#64-wiring-it-together)

### 6.1 The role of App

`App` is the single composition root. It is the only place in the codebase where dependencies are constructed and wired across layer boundaries. Reading it top to bottom gives a complete picture of every screen and every possible navigation transition.

> Dependencies are wired by hand here rather than through a DI framework. That's deliberate: every object's dependencies are visible at its construction site, and the complete dependency graph can be read top-to-bottom starting from `App`, with no annotations or runtime scanning doing it invisibly. This can be replaced with a DI framework without changing anything below the composition root.

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
        var app = new AppModule();
        var shell = app.createShellModule(stage);

        var orders = shell.createOrdersModule();
        var customers = shell.createCustomersModule();
        var stock = shell.createStockModule();

        return shell.mainView(orders.workspace(), customers.workspace(), stock.workspace());
    }
}
```

### 6.2 ShellModule

`ShellModule` owns the application's navigation infrastructure. Its constructor takes the shared `ViewServices` (bundling a `ViewLocator` for workspace views and a `DialogManager` for dialog views) and registers the shell's own ViewModel-to-View mappings:

```java
public class ShellModule {

    private final ViewServices view;
    ...

    public ShellModule(..., ViewServices view) {
        this.view = view;

        view.viewLocator().register(ShellViewModel.class, vm -> new ShellView(vm, view.viewLocator()));
        view.viewLocator().register(StatusItemViewModel.class, StatusItemView::new);
    }

    public OrdersModule createOrdersModule() {
        return new OrdersModule(..., view, ...);
    }

    public CustomersModule createCustomersModule() {
        return new CustomersModule(..., view);
    }

    public Parent mainView(WorkspaceViewModel... workspaces) {
        return view.viewLocator().locate(new ShellViewModel(List.of(workspaces)));
    }
}
```

Unlike a single shared navigation object, domain modules don't receive anything from `ShellModule` to navigate with. Each builds its own `WorkspaceViewModel` internally (see 6.3) and hands it back through a `workspace()` accessor. `ShellModule.mainView` simply collects whatever workspaces it's given and constructs the `ShellViewModel` from them.

### 6.3 Domain modules

As an application grows, `App` accumulates more factory methods. Modules are the natural way to organise them. Each module is self-contained: it creates its own services and repositories, registers its own ViewModel-to-View mappings with the shared `ViewServices`, builds its own `WorkspaceViewModel`, and exposes it for `App` to collect.

```java
public class OrdersModule {

    private final WorkspaceViewModel workspace;
    ...

    public OrdersModule(..., ViewServices view, ...) {
        view.viewLocator().register(OrdersExplorerViewModel.class, OrdersExplorerView::new);
        view.viewLocator().register(OrderEditorViewModel.class, vm -> new OrderEditorView(vm, view.viewLocator()));
        ...

        this.workspace = new WorkspaceViewModel("Orders");
        workspace.openTab(EXPLORER_KEY, this::ordersExplorerTab);
    }

    public WorkspaceViewModel workspace() { return workspace; }

    private TabViewModel ordersExplorerTab() { ... }
}
```

The constructor does the same three things it always did: creates the module's own infrastructure, registers its views, and stores any dependencies needed by its factory methods, with one addition since the shell redesign: it builds its own `WorkspaceViewModel` rather than receiving one from outside. Opening an item for editing works the same way regardless of domain, via `workspace.openTab(key, factory)`.

> `InMemoryOrderRepository` stands in for a real persistence mechanism, a database, a remote API, so the examples stay self-contained and runnable. Swapping it for a real implementation doesn't change anything above the repository interface.

Each domain area gets its own module, built the same way: its own repositories and services, its own view registrations, its own `WorkspaceViewModel`. `App` constructs one module per domain area and collects what each returns; adding a new domain area means writing a new module, not modifying existing ones.

### 6.4 Wiring it together

`App.bootstrap` creates modules in dependency order and returns the root view:

```java
private Parent bootstrap(Stage stage) {
    var app = new AppModule();
    var shell = app.createShellModule(stage);

    var orders = shell.createOrdersModule();
    var customers = shell.createCustomersModule();
    var stock = shell.createStockModule();

    return shell.mainView(orders.workspace(), customers.workspace(), stock.workspace());
}
```

Each module is fully self-contained: `CustomersModule` has no knowledge of `OrderRepository`; `OrdersModule` has no knowledge of `ProductRepository`. Adding a new domain area means writing a new module; `App` itself requires only a few new lines to create it and collect its workspace.

Unlike a single-screen navigation model, a workspace's state now persists for the lifetime of the app once its module is constructed. Open tabs survive switching away and back, since the `WorkspaceViewModel` itself isn't rebuilt on each visit.
