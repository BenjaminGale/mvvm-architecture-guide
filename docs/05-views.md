## 5. Views

This section covers the view layer, describing how views are constructed and connected to their ViewModels. It introduces the `ViewLocator`, `WorkspaceContext`, and `DialogManager` infrastructure that support navigation and dialog presentation, and explains how presentation decisions are made without coupling ViewModels to specific UI concepts.

## Contents

- [5.1 View types](#51-view-types)
  - [5.1.1 Construction patterns for Views](#511-construction-patterns-for-views)
- [5.2 View classes](#52-view-classes)
- [5.3 The ViewLocator](#53-the-viewlocator)
- [5.4 Navigation](#54-navigation)
  - [5.4.1 Host interfaces](#541-host-interfaces)
  - [5.4.2 WorkspaceContext](#542-workspacecontext)
  - [5.4.3 DialogManager](#543-dialogmanager)
  - [5.4.4 ViewServices](#544-viewservices)
- [5.5 Presentation decisions belong to the View](#55-presentation-decisions-belong-to-the-view)
- [5.6 Adding a new screen](#56-adding-a-new-screen)

### 5.1 View types

A view is what the user sees and interacts with. Views display data provided by ViewModels via data binding and translate user input into ViewModel method calls.

This architecture distinguishes between two kinds of view class.

**View** — A class bound to a single ViewModel. It binds its controls to the ViewModel's observable properties and delegates user interactions back to it. All views follow the construction conventions in section 5.2.

**Component** — A reusable chunk of UI that contains no application logic and is never registered with the `ViewLocator`. Components typically accept individual observable properties or plain values rather than a ViewModel, though a parent may pass a ViewModel directly if the component is tightly scoped to it. A status badge, a loading indicator, or a formatted label are typical examples.

```java
// Accepts individual observable properties
public class StatusBadge extends HBox {
    public StatusBadge(ObservableValue<Status> status) {
        label.textProperty().bind(status.map(Status::displayName));
        ...
    }
}

// Accepts plain values
public class SectionHeader extends Label {
    public SectionHeader(String title) {
        setText(title);
        getStyleClass().add("section-header");
    }
}
```

Components should expose the least derived return type — typically `Region` or `Node` — so callers receive only enough handle to place the component in the layout. A static factory method is a natural fit for this:

```java
public class StatusBadge extends HBox {
    public static Region create(ObservableValue<Status> status) {
        return new StatusBadge(status);
    }

    private StatusBadge(ObservableValue<Status> status) { ... }
}
```

Once placed, the component reacts to property changes internally. Its creator has no further interaction with it.

#### 5.1.1 Construction patterns for Views

Views are wired in one of two ways depending on how their ViewModel is provided.

**Directly instantiated** views have their ViewModel provided by a parent view, which receives it from a parent ViewModel (see section 4.2.2). The parent view constructs them inline. These views are never registered with the `ViewLocator`.

```java
// Sub-views constructed directly inside OrderEditorView
var headerView    = new OrderHeaderView(viewModel.getHeader());
var lineItemsView = new LineItemsView(viewModel.getLineItems());
```

**ViewLocator-registered** views have their ViewModel constructed in the module. These views are registered with the `ViewLocator` and resolved via `ViewLocator.locate`.

```java
viewLocator.register(OrderEditorViewModel.class, OrderEditorView::new);
```

If the ViewModel has no external dependencies and the view always renders inside the parent's layout, instantiate it directly; otherwise register it with the `ViewLocator`. Full screens, dialogs, and any view the workspace presents will always fall into the latter category. (See section 4.1.3 for the underlying invariants, and 5.3 for the registration API.)

### 5.2 View classes

View classes follow these conventions:

- The constructor accepts a single typed ViewModel and fully initialises the view — building the component tree and binding controls to ViewModel properties.
- The constructor may accept other view-layer dependencies such as the `ViewLocator`.
- Controls are bound to ViewModel properties in the constructor, delegating user interactions back to the ViewModel.

No shared base class or interface is prescribed. The `ViewLocator` constructs views via registered factory functions. A base class is acceptable where several views share substantial layout scaffolding, but it must not carry application logic or ViewModel responsibilities.

```java
public class OrdersExplorerView extends StackPane {

    public OrdersExplorerView(OrdersExplorerViewModel viewModel) {
        var listView      = new ListView<Order>();
        var refreshButton = new Button("Refresh");
        var statusLabel   = new Label();

        // ViewModel state → UI
        listView.setItems(viewModel.getOrders());
        statusLabel.textProperty().bind(viewModel.statusTextProperty());

        // UI events → ViewModel
        refreshButton.setOnAction(e -> viewModel.refresh());
        listView.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, order) -> viewModel.openOrder(order));
    }
}
```

### 5.3 The ViewLocator

`ViewLocator<TView>` is a generic registry that maps ViewModel types to view factory functions. Being generic allows the same mechanism to serve both workspace views and dialogs without requiring a shared view base type.

```java
public class ViewLocator<TView> {
    public <TViewModel> void register(Class<TViewModel> vmClass, Function<TViewModel, TView> factory) { ... }
    public TView locate(Object viewModel) { ... }
}
```

Registrations use constructor references when a view only needs its ViewModel:

```java
viewLocator.register(OrdersExplorerViewModel.class, OrdersExplorerView::new);
```

When a view needs additional view-layer dependencies, a lambda supplies them:

```java
viewLocator.register(MainViewModel.class, vm -> new MainView(vm, viewLocator));
```

`ViewLocator` is the sole place that defines the ViewModel-to-View mapping. Adding a screen requires one new registration line; nothing else changes.

### 5.4 Navigation

Navigation is the act of changing what the user sees. ViewModels never hold references to view-layer infrastructure. Instead they declare navigation intent through **host interfaces**, and the module wires up the implementation. How the host implementation surfaces that intent in the view layer — whether through a shared context object, a direct callback, or some other mechanism — is application-specific and not prescribed by this architecture.

Host interfaces declare navigation intent; `WorkspaceContext` and `DialogManager` fulfil it in the view layer; `ViewServices` groups them for convenient module access.

#### 5.4.1 Host interfaces

A host interface declares the navigation actions a ViewModel can trigger, expressed as domain operations rather than presentation concepts. The ViewModel calls the host; the module supplies an implementation that calls the appropriate view-layer infrastructure.

```java
public interface OrdersExplorerHost {
    void showOrderDetails(Order order);
    void setPendingOrderCount(int count);
}
```

The ViewModel receives the host at construction time and calls it without any knowledge of how the action is fulfilled:

```java
public class OrdersExplorerViewModel {
    public OrdersExplorerViewModel(OrdersExplorerService service, OrdersExplorerHost host) { ... }

    public void openOrder(Order order) {
        host.showOrderDetails(order);
    }
}
```

The module provides the implementation, translating navigation intent into view-layer calls (see 5.4.2 for `WorkspaceContext`):

```java
new OrdersExplorerViewModel(service, new OrdersExplorerHost() {
    @Override public void showOrderDetails(Order order) {
        workspaceContext.show(orderEditorViewModel(order));
    }
    @Override public void setPendingOrderCount(int count) { ... }
});
```

#### 5.4.2 WorkspaceContext

The example application uses a `WorkspaceContext` to coordinate workspace navigation. This is an instance of the Context pattern described in section 4.4.2 — a shared observable state object with no presentation responsibility of its own. It holds the currently active ViewModel as an observable property, and the shell view listens to that property and resolves the view via the `ViewLocator`.

```java
public class WorkspaceContext {
    public void show(Object viewModel) { ... }
    public ReadOnlyObjectProperty<Object> currentWorkspaceProperty() { ... }
}
```

```java
// Inside the shell view
workspaceContext.currentWorkspaceProperty().addListener((obs, old, vm) -> {
    workspace.getChildren().setAll(viewLocator.locate(vm));
});
```

This is one way to solve the problem. Other approaches — direct callbacks, event buses, or other observable mechanisms — can serve the same role. The architecture does not prescribe a particular mechanism; what it does prescribe is that ViewModels remain unaware of it.

#### 5.4.3 DialogManager

`DialogManager` handles modal dialog presentation. It wraps its own `ViewLocator<Dialog<Runnable>>` and manages dialog lifecycle — owner, modality, and showing.

```java
public class DialogManager {
    public <TViewModel> void register(Class<TViewModel> vmClass, Function<TViewModel, Dialog<Runnable>> factory) { ... }
    public void show(Object viewModel) { ... }
}
```

Dialogs are registered in the module alongside workspace views (see 5.4.4 for `ViewServices`):

```java
appContext.dialogManager().register(EditItemViewModel.class, EditItemView::dialog);
```

Navigating to a dialog follows the same host interface pattern. The host method name expresses domain intent; the module decides that it maps to a dialog:

```java
public interface OrderEditorHost {
    void showItemEditor(EditItemRequest request);
    ...
}

// Module implementation
@Override public void showItemEditor(EditItemRequest request) {
    appContext.dialogManager().show(editItemViewModel(request));
}
```

#### 5.4.4 ViewServices

`ViewServices` is a record that groups the shared view-layer infrastructure available to all modules — the workspace `ViewLocator` and the `DialogManager` — so modules do not receive them as separate constructor arguments.

```java
public record ViewServices(
    ViewLocator<Region> viewLocator,
    DialogManager dialogManager) {
}
```

### 5.5 Presentation decisions belong to the View

Neither the originating ViewModel nor the host interface specifies how a ViewModel should be presented. The host method names express domain intent — `showOrderDetails`, `showItemEditor` — not presentation mechanism. The module decides what that intent maps to: workspace navigation or a modal dialog.

> It might seem natural to name host methods with explicit presentation intent — `openInDialog`, `showInPanel`. This is a mistake. The moment a host interface carries presentation concepts, the ViewModel layer is coupled to specific UI contexts. A ViewModel that calls `host.openAsDialog` is making a presentation decision, which is not its responsibility. Host methods should name what happens in the domain; the module chooses how.

`WorkspaceContext` and `DialogManager` each own their presentation logic. The `DialogManager` decides how dialogs are constructed and shown. The shell view decides how workspace transitions are rendered. ViewModels and host interfaces remain ignorant of both.

Introducing a new presentation style — a slide-in panel, a notification tray, a second workspace region — requires writing a new view component that observes the appropriate context and handles the relevant ViewModel types. No existing code is modified; ViewModels are unchanged.

### 5.6 Adding a new screen

The architecture is designed so that adding a new screen is a mechanical, low-risk operation that touches only new files and the module. If any step requires modifying existing classes other than the module, something has drifted from the invariants.

- **Write the ViewModel** — it takes the services and host interface it directly uses.
- **Write the host interface** — declare one method per navigation action the ViewModel can trigger, named for domain intent.
- **Write the View** — extend the appropriate UI component, accept the ViewModel as the sole constructor argument, and bind controls to ViewModel properties in the constructor.
- **Register the view** — call `appContext.viewLocator().register(MyViewModel.class, MyView::new)` in the module constructor.
- **Implement the host** — in the module factory method, supply a host implementation that calls `workspaceContext.show` or `appContext.dialogManager().show` as appropriate.
- **Wire navigation in** — in whichever host triggers navigation to the new screen, call `workspaceContext.show(myNewScreenViewModel(...))`.

Nothing else changes. The `ViewLocator` stays mechanical. ViewModels remain ignorant of how they are presented. Each piece retains its single responsibility, and the architecture remains flat and uniform regardless of how many screens are added.
