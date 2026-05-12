## 5. Views

This section covers the view layer describing how views should be constructed and connected to their ViewModels. It describes the `ViewLocator` and `ViewRouter` infrastructure that provide support for opening dialogs and navigation between views. This section also explains how presentation decisions are made without coupling ViewModels to specific UI contexts.

## Contents

- [5.1 View types](#51-view-types)
  - [Construction patterns for Views](#construction-patterns-for-views)
- [5.2 View classes](#52-view-classes)
- [5.3 The ViewLocator](#53-the-viewlocator)
- [5.4 Navigation](#54-navigation)
  - [The ViewRouter class](#the-viewrouter-class)
  - [Navigation from the ViewModel perspective](#navigation-from-the-viewmodel-perspective)
  - [Navigation from the View perspective](#navigation-from-the-view-perspective)
- [5.5 Presentation decisions belong to the View](#55-presentation-decisions-belong-to-the-view)
- [5.6 Adding a new screen](#56-adding-a-new-screen)

### 5.1 View types

A view is what the user sees and interacts with.

Views are responsible for displaying data provided by viewModels via data binding and translating user input actions into viewModel method calls to affect changes in the application.

This architecture makes a distinction between two kinds of view classes.

**View** — A class bound to a single ViewModel. The view binds its controls to the ViewModel's observable properties and delegates user interactions back to it. All views follow the construction conventions described in section 5.2.

**Component** — A reusable chunk of UI with no ViewModel. Components accept plain data or observable values and contain no application logic. They are instantiated directly at the point of use. A status badge, a loading indicator, or a formatted label are typical examples.

#### Construction patterns for Views

Though all Views share the same structure, they are wired in two different ways depending on how their ViewModel is provided.

**Directly instantiated** views have their ViewModel provided by a parent view, which receives it from a parent ViewModel (see section 4.2.2). The parent view constructs them inline, passing the sub-ViewModel directly. These views are never registered with the `ViewLocator`.

```java
// Inside OrderEditorView — sub-views are constructed directly
var headerView    = new OrderHeaderView(viewModel.getHeader());
var lineItemsView = new LineItemsView(viewModel.getLineItems());
```

**ViewLocator-registered** views have their ViewModel constructed in the composition root, which wires all services, use cases, and navigation callbacks. These views are registered with the `ViewLocator` and presented via the `ViewRouter`.

```java
viewLocator.register(OrderEditorViewModel.class, OrderEditorView::new);
viewLocator.register(OrdersViewModel.class,      OrdersView::new);
```

The invariants from section 4.1.3 determine which pattern applies. If a view's ViewModel requires external dependencies or the view could appear in a context outside the parent's layout, it must be registered with the `ViewLocator`. If the ViewModel is provided by a parent ViewModel with no external dependencies and the view always renders within the parent's layout, it is instantiated directly.

| | Directly instantiated | ViewLocator-registered |
|---|---|---|
| **ViewModel source** | Provided by a parent ViewModel as a sub-ViewModel | Constructed in the composition root |
| **Use when** | ViewModel has no external dependencies and the view always renders inside the parent's layout | ViewModel requires services or use cases, or the view may appear outside the parent's layout |
| **ViewLocator registration** | Never registered | Always registered — `viewLocator.register(MyViewModel.class, MyView::new)` |
| **Presented via** | Constructed inline in the parent view | `ViewRouter.route` |
| **Typical examples** | Header, line items, summary panel within an editor screen | Full screens, dialogs, any view the router presents |

### 5.2 View classes

View classes follow these conventions:

- The constructor accepts a single typed ViewModel and fully initialises the view — building the component tree and binding controls to ViewModel properties.
- The constructor may accept other view layer dependencies such as dialog managers or the `ViewRouter`.
- Controls are bound to ViewModel properties in the constructor, delegating user interactions back to the ViewModel.

There is no shared base class or interface. The `ViewLocator` constructs views via registered factory functions.

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

### 5.3 The ViewLocator

The `ViewLocator` is a registry that maps ViewModel types to their corresponding view factory functions. These mappings must exist somewhere outside the ViewModel layer — `ViewLocator` is that place.

The view factory functions are registered at startup and are responsible for constructing a view from a supplied viewModel. The factory also needs to resolve other view dependencies which is why they need to be registered at startup.

```java
public class ViewLocator {
    private final Map<Class<?>, Function<Object, Region>> registry = new HashMap<>();

    public <VM> void register(Class<VM> vmClass, Function<VM, Region> factory) {
        registry.put(vmClass, vm -> factory.apply(vmClass.cast(vm)));
    }

    public Region resolve(Object viewModel) {
        var factory = registry.get(viewModel.getClass());

        if (factory == null) {
            throw new IllegalStateException("No view registered for " + viewModel.getClass().getSimpleName());
        }

        return factory.apply(viewModel);
    }
}
```

Registrations can use constructor references when a view only depends on a viewModel as shown below.

```java
viewLocator.register(OrdersViewModel.class, OrdersView::new);
```

If a view depends on other services you can supply a custom factory function i.e.

```
viewLocator.register(SettingsViewModel.class, vm -> new SettingsView(vm, viewRouter));
```

The `ViewLocator` is the sole location that defines the ViewModel-to-View mapping. Adding a screen requires one new registration line; nothing else changes.

`ViewLocator` and `ViewRouter` have distinct, complementary roles:

| | **ViewLocator** | **ViewRouter** |
|---|---|---|
| **Stores** | Map of VM type → view factory function | Map of view type → display listener |
| **Accepts** | `register(vmClass, factory)` | `addListener(viewClass, listener)` |
| **Produces** | A `Region` given a view model instance | Nothing — triggers a side effect (display) |
| **Knows about** | How to build views | Where to send them |
| **Navigation aware** | No | Yes |

### 5.4 Navigation

#### The ViewRouter class

The ViewRouter is the conduit for navigation events. It uses the `ViewLocator` to resolve the correct view and then dispatches it to whichever listener is registered for that view type — removing the need for type switching in the receiving view.

```java
public class ViewRouter {
    private final ViewLocator viewLocator;
    private final Map<Class<?>, Consumer<Region>> listeners = new HashMap<>();

    public ViewRouter(ViewLocator viewLocator) {
        this.viewLocator = viewLocator;
    }

    public <V extends Region> void addListener(Class<V> viewClass, Consumer<V> listener) {
        listeners.put(viewClass, view -> listener.accept(viewClass.cast(view)));
    }

    public void route(Object viewModel) {
        var view = viewLocator.resolve(viewModel);
        var listener = listeners.get(view.getClass());
        if (listener != null) listener.accept(view);
    }
}
```

#### Navigation from the ViewModel perspective

ViewModels never hold a ViewRouter reference. They receive callbacks injected at construction time; those callbacks invoke `viewRouter.route` internally, keeping the ViewRouter invisible to the ViewModel layer.

```java
// In the composition root — the callback wires the ViewRouter without exposing it to the ViewModel
private OrdersViewModel orders() {
    return new OrdersViewModel(
        orderService,
        order -> viewRouter.route(orderDetail(order))
    );
}
```

#### Navigation from the View perspective

The ViewRouter is created once in the composition root and injected into views that respond to navigation events — typically the application shell and any view managing a specific presentation context. Views register a listener in their constructor.

> It might seem natural to add methods like `showDialog` or `openInNewTab` to the ViewRouter to express how different ViewModels should be presented. Adding such methods is a mistake. The moment the ViewRouter carries presentation intent, it couples the ViewModel layer to specific UI concepts. A ViewModel that calls `viewRouter.showDialog` is making a presentation decision — which is not its responsibility. The next section explains how presentation decisions are made correctly.

Views that require the ViewRouter receive it through their constructor. Where a view needs additional view-layer dependencies alongside its ViewModel, a lambda is used in the `ViewLocator` registration rather than a plain constructor reference:

```java
viewLocator.register(MainViewModel.class,
    vm -> new MainView(vm, viewRouter));
```

The `ViewLocator` is passed to the `ViewRouter` at construction time in the composition root:

```java
var viewLocator = new ViewLocator();
var viewRouter  = new ViewRouter(viewLocator);
```

### 5.5 Presentation decisions belong to the View

When the ViewRouter creates a view, it makes no claim about presentation. Each listening view registers only for the view types it is responsible for and is not notified of others.

This is how different presentation styles coexist without central coordination. `MainView` registers for the view types it displays in the workspace:

```java
// Inside MainView constructor
viewRouter.addListener(OrdersView.class, view -> workspace.getChildren().setAll(view));
viewRouter.addListener(OrderDetailView.class, view -> workspace.getChildren().setAll(view));
viewRouter.addListener(CustomersView.class, view -> workspace.getChildren().setAll(view));
```

A separate `DialogManager` — also part of the shell, registering against the same `ViewRouter` — handles views that should appear as modal dialogs. It is responsible for all dialog lifecycle management: opening, closing, and owner configuration:

```java
// Inside DialogManager constructor
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

### 5.6 Adding a new screen

The architecture is designed so that adding a new screen is a mechanical, low-risk operation that touches only new files and the composition root. If any step requires modifying existing classes other than the composition root, something has drifted from the invariants.

- **Write the ViewModel** — it takes only the services and context objects it directly uses, plus one callback per navigation action it can trigger.
- **Write the View** — extend the appropriate UI component. Accept the ViewModel as the sole constructor argument, build the component tree, and bind controls to ViewModel properties, all in the constructor.
- **Register the ViewModel-to-View mapping** — `viewLocator.register(MyViewModel.class, MyView::new)`.
- **Add a factory method in the composition root** that constructs the ViewModel with its dependencies and navigation callbacks wired as lambdas.
- **Wire the navigation callback** — in the factory method of whichever ViewModel navigates to the new screen, add a callback lambda that calls `viewRouter.route(myNewScreen())`.
- **Register a listener** — in whichever view is responsible for presenting the new screen, call `viewRouter.addListener(MyView.class, view -> ...)` with the appropriate presentation logic.

Nothing else changes. The ViewRouter stays minimal. The ViewLocator stays mechanical. ViewModels stay ignorant of views. Existing ViewModels are not modified unless they need to navigate to the new screen. Each piece retains its single responsibility, and the architecture remains flat and uniform regardless of how many screens are added.
