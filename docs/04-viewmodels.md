# 4. ViewModel Layer

The ViewModel layer coordinates application behaviour into observable UI state.

ViewModels expose properties the view binds to, react to user input, coordinate application interactions through hosts, and communicate through observable state rather than direct coupling.

A ViewModel does not construct views, own navigation infrastructure, or decide how presentation is hosted. Instead, it communicates intent through capability-oriented host interfaces and exposes state through observable properties.

This chapter introduces several supporting patterns for use with ViewModels:

* **ViewModels** — screen or section coordination objects.
* **Hosts** — application-facing interfaces used to request interactions.
* **Requests** — short-lived interaction contracts.
* **Contexts** — shared observable state objects.
* **Actions** — executable interaction objects with observable execution state.
* **ViewModel service interfaces** — adapter contracts defining the operations a ViewModel needs from the domain and data layers.

Together these form the reactive coordination boundary between the view layer and the rest of the application.

---

# Contents

* [4.1 Responsibilities of a ViewModel](#41-responsibilities-of-a-viewmodel)

  * [4.1.1 Observable state](#411-observable-state)
  * [4.1.2 Coordinating application interactions](#412-coordinating-application-interactions)
  * [4.1.3 Presentation boundaries](#413-presentation-boundaries)
* [4.2 ViewModel construction boundaries](#42-viewmodel-construction-boundaries)

  * [4.2.1 Local composition ViewModels](#421-local-composition-viewmodels)
  * [4.2.2 Hosted ViewModels](#422-hosted-viewmodels)
  * [4.2.3 Hosts as the application boundary](#423-hosts-as-the-application-boundary)
* [4.3 Decomposing ViewModels](#43-decomposing-viewmodels)

  * [4.3.1 Sub-ViewModels](#431-sub-viewmodels)
  * [4.3.2 Composing derived state](#432-composing-derived-state)
  * [4.3.3 Local decomposition versus navigation](#433-local-decomposition-versus-navigation)
* [4.4 Shared observable state](#44-shared-observable-state)

  * [4.4.1 Composition-time property binding](#441-composition-time-property-binding)
  * [4.4.2 Context objects](#442-context-objects)
  * [4.4.3 Contexts versus ViewModels](#443-contexts-versus-viewmodels)
* [4.5 Request objects](#45-request-objects)

  * [4.5.1 Request objects as interaction contracts](#451-request-objects-as-interaction-contracts)
  * [4.5.2 Observable request state](#452-observable-request-state)
  * [4.5.3 Requests versus contexts](#453-requests-versus-contexts)
* [4.6 Action classes](#46-action-classes)

  * [4.6.1 The problem they solve](#461-the-problem-they-solve)
  * [4.6.2 Action interfaces](#462-action-interfaces)
  * [4.6.3 Binding Actions in views](#463-binding-actions-in-views)
* [4.7 ViewModel service interfaces](#47-viewmodel-service-interfaces)

  * [4.7.1 Rules for ViewModel service interfaces](#471-rules-for-viewmodel-service-interfaces)
* [4.8 Architectural summary](#48-architectural-summary)

---

# 4.1 Responsibilities of a ViewModel

A ViewModel is an observable coordination object that adapts application behaviour into UI-bindable state.

Its responsibilities are deliberately narrow:

* expose observable state for the view,
* coordinate interactions between the view and application services,
* derive presentation-oriented state,
* and maintain UI-specific state such as selection, validation and loading state.

A ViewModel is not responsible for:

* constructing views,
* rendering UI,
* owning application infrastructure,
* or deciding how presentation is hosted.

The view binds to the ViewModel reactively. User interactions are forwarded through method calls or Actions, and observable state changes automatically propagate back to the view.

## 4.1.1 Observable state

The primary responsibility of a ViewModel is exposing observable state.

The view binds directly to ViewModel properties and updates automatically when those properties change.

> The examples in this chapter use JavaFX properties as the concrete observable mechanism, but the same architectural role exists in frameworks such as WPF, SwiftUI and Android.

```java
public class OrdersExplorerViewModel {

    private final ObservableList<Order> orders =
        FXCollections.observableArrayList();

    private final StringProperty statusText =
        new SimpleStringProperty();

    public ObservableList<Order> getOrders() {
        return orders;
    }

    public ReadOnlyStringProperty statusTextProperty() {
        return statusText;
    }

    public void refresh() {
        // Adapt domain data into presentation state
    }
}
```

The ViewModel adapts application data into a form suitable for presentation:

* exposing observable collections,
* deriving summary state,
* and maintaining presentation-facing state.

These concerns belong in the ViewModel rather than the view itself.

## 4.1.2 Coordinating application interactions

A ViewModel frequently needs to interact with the surrounding application:

* opening another screen,
* showing a dialog,
* requesting a file,
* or activating a workspace.

These interactions are expressed through injected host interfaces.

```java
public interface OrdersExplorerHost {
    void showOrderDetails(Order order);
}
```

The ViewModel communicates intent through the host:

```java
public class OrdersExplorerViewModel {

    private final OrdersExplorerHost host;

    public void openOrder(Order order) {
        if (order != null) {
            host.showOrderDetails(order);
        }
    }
}
```

Hosts expose application capabilities rather than infrastructure APIs.

Prefer:

```java
host.showOrderDetails(order);
```

instead of:

```java
router.navigateTo(...);
dialogService.show(...);
```

The ViewModel communicates what should happen rather than how the application performs it.

## 4.1.3 Presentation boundaries

A ViewModel coordinates presentation state but does not own presentation itself.

In particular, a ViewModel should not:

* construct views,
* construct hosted ViewModels,
* depend on routers or dialog systems,
* or decide where presentation appears.

Hosted ViewModels often require application-level dependencies and presentation decisions that belong to the hosting application rather than to another ViewModel.

Instead, the ViewModel communicates intent through its host:

```java
host.showOrderDetails(order);
```

The hosting application then decides:

* how the next ViewModel is constructed,
* which dependencies it receives,
* and how the resulting view is presented.

---

# 4.2 ViewModel construction boundaries

Not all ViewModels are constructed in the same way.

Some ViewModels are local implementation details of another ViewModel and can be constructed directly. Others participate in the wider application shell and require application-level dependencies and presentation coordination.

This chapter refers to these two categories as:

* local composition ViewModels,
* and hosted ViewModels.

## 4.2.1 Local composition ViewModels

A local composition ViewModel represents a subsection of another ViewModel's presentation state.

Typical examples include:

* form sections,
* editable tables,
* inspectors,
* filter panels,
* and reusable UI fragments.

A local composition ViewModel:

* is constructed directly by its parent,
* uses state already owned by the parent,
* introduces no new application dependencies,
* and exists entirely within the parent's presentation scope.

```java
public class OrderEditorViewModel {

    private final OrderHeaderViewModel header;
    private final LineItemsViewModel lineItems;

    public OrderEditorViewModel(Order order) {
        this.header = new OrderHeaderViewModel(order);
        this.lineItems = new LineItemsViewModel(order.lineItems());
    }
}
```

This construction is safe because no application-level hosting or infrastructure is involved.

## 4.2.2 Hosted ViewModels

A hosted ViewModel participates in the wider application environment.

Examples include:

* screens,
* dialogs,
* workspaces,
* tabs,
* overlays,
* and docked panels.

Hosted ViewModels frequently require:

* services,
* hosts,
* shared contexts,
* and application-scoped state.

Unlike local composition ViewModels, hosted ViewModels are not constructed directly by another ViewModel.

```java
public class OrdersExplorerViewModel {

    private final OrdersExplorerHost host;

    public void openOrder(Order order) {
        if (order != null) {
            host.showOrderDetails(order);
        }
    }
}
```

The current ViewModel communicates intent through its host but does not construct the next ViewModel itself.

This keeps the ViewModel independent from:

* presentation infrastructure,
* hosted ViewModel dependencies,
* and hosting decisions.

## 4.2.3 Hosts as the application boundary

Host interfaces form the boundary between the ViewModel layer and the hosting application.

```java
public interface OrdersExplorerHost {
    void showOrderDetails(Order order);
    void showCreateOrder();
}
```

The ViewModel communicates intent through the host:

```java
public void openOrder(Order order) {
    if (order != null) {
        host.showOrderDetails(order);
    }
}
```

The host implementation — typically wired in the application shell or composition root — performs the actual work:

* constructing hosted ViewModels,
* resolving dependencies,
* choosing presentation behaviour,
* and coordinating the surrounding application.

This keeps ViewModels focused on:

* observable state,
* user interaction,
* and presentation-oriented coordination.

---

# 4.3 Decomposing ViewModels

As screens grow in complexity, ViewModels accumulate:

* validation rules,
* editable state,
* selection state,
* filtering,
* and interaction logic.

ViewModel decomposition separates distinct areas of presentation behaviour into smaller compositional ViewModels.

The goal is not arbitrary fragmentation. A ViewModel should generally represent a coherent area of UI behaviour.

## 4.3.1 Sub-ViewModels

A sub-ViewModel encapsulates the state and behaviour of a distinct subsection of a larger screen.

Typical candidates include:

* form sections,
* editable tables,
* inspectors,
* side panels,
* and reusable composite controls.

Each sub-ViewModel owns:

* its own observable state,
* its own validation rules,
* and its own derived properties.

```java
public class OrderHeaderViewModel {

    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    public OrderHeader buildHeader() {
        ...
    }
}
```

```java
public class LineItemsViewModel {

    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }
}
```

The parent ViewModel coordinates these sections rather than owning all behaviour directly.

## 4.3.2 Composing derived state

A parent ViewModel should compose state from its sub-ViewModels rather than duplicating their rules.

```java
public class OrderEditorViewModel {

    private final OrderHeaderViewModel header;
    private final LineItemsViewModel lineItems;

    private final BooleanProperty canSave =
        new SimpleBooleanProperty(false);

    public OrderEditorViewModel(Order order) {
        this.header = new OrderHeaderViewModel(order);
        this.lineItems = new LineItemsViewModel(order.lineItems());

        canSave.bind(
            header.validProperty()
                .and(lineItems.validProperty())
        );
    }
}
```

The parent ViewModel does not know how each subsection validates itself. It coordinates the resulting observable state.

This structure remains highly compositional:

* child ViewModels own local rules,
* the parent composes larger behaviour from them.

## 4.3.3 Local decomposition versus navigation

Sub-ViewModels are local composition objects rather than navigated application features.

A sub-ViewModel:

* exists entirely within the parent's presentation scope,
* uses state already owned by the parent,
* and introduces no new application dependencies.

For this reason it is safe for the parent ViewModel to construct sub-ViewModels directly.

Hosted ViewModels are different.

Hosted ViewModels may require:

* services,
* hosts,
* application contexts,
* requests,
* or presentation coordination.

Those ViewModels are constructed by the hosting application rather than by another ViewModel directly.

A useful rule is:

* if the ViewModel exists only within the parent's layout, local composition is appropriate,
* if the ViewModel participates in application hosting or navigation, construction belongs to the hosting application.

---

# 4.4 Shared observable state

Not all ViewModel communication requires direct interaction.

In many cases one ViewModel exposes observable state while another consumes or binds to it.

These relationships are reactive rather than imperative:

* one ViewModel publishes observable state,
* another observes or binds to it.

Several mechanisms can be used depending on the ownership and lifetime of the shared state.

## 4.4.1 Composition-time property binding

The simplest form of shared state is direct property binding performed during composition.

A ViewModel exposes observable state:

```java
public class OrdersExplorerViewModel {

    private final IntegerProperty pendingOrderCount =
        new SimpleIntegerProperty();

    public ReadOnlyIntegerProperty pendingOrderCountProperty() {
        return pendingOrderCount;
    }
}
```

Another ViewModel exposes a compatible property:

```java
public class SidebarViewModel {

    private final IntegerProperty pendingOrderCount =
        new SimpleIntegerProperty();

    public IntegerProperty pendingOrderCountProperty() {
        return pendingOrderCount;
    }
}
```

The relationship is established during composition:

```java
sidebarVm.pendingOrderCountProperty().bind(
    explorerVm.pendingOrderCountProperty()
);
```

Neither ViewModel knows the other exists.

This approach works well when:

* ownership of the state is clear,
* the relationship is localised,
* and the state naturally belongs to one ViewModel.

## 4.4.2 Context objects

Sometimes shared state does not belong naturally to a single ViewModel.

For example:

* multiple workspaces may share the same selection,
* application-wide notifications may need global observation,
* or the state may outlive any single ViewModel.

In these situations a dedicated context object can act as a shared observable state container.

```java
public class WorkspaceContext {

    private final ObjectProperty<WorkspaceViewModel> activeWorkspace =
            new SimpleObjectProperty<>();

    public ObjectProperty<WorkspaceViewModel> activeWorkspaceProperty() {
        return activeWorkspace;
    }
}
```

The context contains observable state but generally little or no behaviour.

Unlike direct ViewModel references:

* relationships remain indirect,
* and ViewModels remain decoupled.

Contexts can exist at different scopes.

A local context coordinates state shared within a feature area:

```java
OrderEditorContext
```

An application context represents longer-lived application concepts:

```java
WorkspaceContext
UserSessionContext
MessageBoxContext
```

Contexts should remain focused around a coherent area of shared state.

Large global state containers such as:

```java
ApplicationState
```

typically become god objects over time and should generally be avoided.

## 4.4.3 Contexts versus ViewModels

Contexts and ViewModels both expose observable state, but their responsibilities differ.

A ViewModel:

* coordinates UI behaviour,
* derives presentation state,
* and represents a specific screen or UI area.

A context:

* represents shared observable state,
* has no presentation responsibility,
* and exists independently from any particular view.

A useful guideline is:

* if the object primarily coordinates UI behaviour, it is likely a ViewModel,
* if the object primarily exists to share observable state, it is likely a context.

---

# 4.5 Request objects

Some hosted interactions require more than a simple notification that something should happen.

For example:

* a dialog may need initial state,
* a picker may require configuration,
* or a hosted interaction may need to return a result.

In these situations a request object acts as an interaction contract between the initiating ViewModel and the hosted interaction.

A request is:

* short-lived,
* interaction-scoped,
* and typically created immediately before the interaction is hosted.

## 4.5.1 Request objects as interaction contracts

A request packages together:

* the input required by the hosted interaction,
* and the communication mechanism used to return information.

```java
public interface EditLineItemRequest {
    LineItem item();
    void confirm(LineItem updated);
}
```

The initiating ViewModel creates the request and passes it through the host:

```java
public class OrderEditorViewModel {

    private final OrderEditorHost host;

    public void editLineItem(LineItem item) {
        host.showEditLineItemDialog(new EditLineItemRequest(...));
    }
}
```

The hosted ViewModel communicates back through the request:

```java
public class EditLineItemViewModel {

    private final EditLineItemRequest request;

    public void confirm() {
        request.confirm(...);
    }
}
```

This keeps the interaction loosely coupled while still supporting bidirectional communication.

## 4.5.2 Observable request state

Some interactions require ongoing communication rather than a single completion callback.

For example:

* live previews,
* progress reporting,
* incremental validation,
* or selection synchronisation.

In these cases a request may expose observable state directly:

```java
public class ColourPickerRequest {

    private final ObjectProperty<Color> selectedColour =
            new SimpleObjectProperty<>();

    public ObjectProperty<Color> selectedColourProperty() {
        return selectedColour;
    }
}
```

The initiating ViewModel observes changes reactively:

```java
request.selectedColourProperty()
    .addListener((obs, oldColour, newColour) -> {
        previewColour.set(newColour);
    });
```

The hosted interaction updates the request state directly:

```java
request.selectedColourProperty().set(currentSelection);
```

## 4.5.3 Requests versus contexts

Requests and contexts both facilitate communication between ViewModels, but they solve different problems.

A context:

* represents long-lived shared observable state,
* typically exists independently of any single interaction,
* and may be shared by many ViewModels simultaneously.

A request:

* represents a single interaction contract,
* is created at the point of invocation,
* and is typically discarded when the interaction completes.

A useful distinction is:

* contexts model shared application state,
* requests model temporary interaction state.

---

# 4.6 Action classes

Actions are optional utility objects that encapsulate executable UI behaviour together with observable execution state.

Without Actions, views typically coordinate multiple concerns separately:

* whether a control is enabled,
* what happens when it is activated,
* whether an operation is already executing,
* and whether loading or progress indicators should appear.

Action classes consolidate this behaviour into a single executable object.

## 4.6.1 The problem they solve

Without an Action, a view often coordinates execution and availability separately:

```java
saveButton.disableProperty().bind(viewModel.canSaveProperty().not());
saveButton.setOnAction(e -> viewModel.save());
```

As interactions become asynchronous, additional coordination is introduced:

* loading indicators,
* double-submit prevention,
* progress state,
* and execution guards.

An Action centralises these concerns into a reusable interaction object.

## 4.6.2 Action interfaces

`Action` represents a synchronous executable interaction.

```java
public interface Action {
    ReadOnlyBooleanProperty canExecuteProperty();
    void execute();
}
```

`AsyncAction` extends the same model to asynchronous interactions.

```java
public interface AsyncAction {
    ReadOnlyBooleanProperty canExecuteProperty();
    ReadOnlyBooleanProperty isExecutingProperty();
    CompletableFuture<Void> executeAsync();
}
```

A ViewModel exposes Actions directly:

```java
public class OrderEditorViewModel {
    public final AsyncAction save;
    public final Action delete;
}
```

Actions centralise:

* execution semantics,
* availability state,
* and execution coordination.

## 4.6.3 Binding Actions in views

Views bind directly to Action state:

```java
saveButton.disableProperty()
    .bind(viewModel.save
        .canExecuteProperty()
        .not());

progressIndicator.visibleProperty().bind(
    viewModel.save.isExecutingProperty());

saveButton.setOnAction(e -> viewModel.save.executeAsync());
```

This keeps views declarative while keeping execution state close to the interaction itself.

Actions are optional utilities rather than a required part of MVVM itself. Their purpose is to reduce repetitive interaction wiring and centralise execution semantics around executable UI behaviour.

---

# 4.7 ViewModel service interfaces

A ViewModel service interface defines the operations a specific ViewModel needs from the outside world. It is an adapter contract — implemented in the module layer — that decouples the ViewModel from the infrastructure beneath it.

The interface is shaped by what the ViewModel needs, not by what the domain provides. It may aggregate operations from multiple repositories or domain operations behind a single dependency, hiding that detail from the ViewModel entirely.

```java
public interface OrderEditorService {
    void saveOrder(Order order);
    Order copyOrder(String orderId);
    void deleteOrder(String orderId);
}
```

The ViewModel depends only on this interface:

```java
public class OrderEditorViewModel {

    private final OrderEditorService service;

    public OrderEditorViewModel(Order order, OrderEditorService service, OrderEditorHost host) {
        this.service = service;
        ...
    }
}
```

The implementation is wired in the module layer, delegating to whichever repositories and domain operations are required:

```java
new OrderEditorService() {
    public void saveOrder(Order order) { repository.save(order); }
    public Order copyOrder(String id) { return orderCopier.copy(id); }
    public void deleteOrder(String id) { repository.delete(id); }
}
```

This keeps the ViewModel testable in isolation and free from knowledge of how operations are fulfilled.

## 4.7.1 Rules for ViewModel service interfaces

- defined in the same package as the ViewModel they serve
- named after the ViewModel they serve (`OrderEditorService` for `OrderEditorViewModel`)
- implemented in the module layer, not the domain layer
- may aggregate operations from multiple repositories or domain operations
- distinct from domain operations, which are named after the operation they perform and live in the domain package

---

# 4.8 Architectural summary

The ViewModel layer presented in this chapter is centred around a small set of architectural principles:

* ViewModels expose observable presentation state.
* Views bind reactively.
* Application interactions are expressed through hosts.
* Hosted interactions communicate through requests.
* Shared state is coordinated through observable properties and contexts.
* Complex screens are decomposed into smaller compositional ViewModels.
* Hosted ViewModels are constructed by the hosting application rather than by other ViewModels.

These principles preserve separation between:

* presentation coordination,
* application infrastructure,
* and rendering concerns.

A ViewModel does not construct views or own navigation infrastructure. Instead, it communicates intent declaratively through observable state and capability-oriented interfaces.

The resulting architecture keeps:

* ViewModels testable,
* dependencies localised,
* presentation structure compositional,
* and application coordination reactive rather than tightly coupled.

The supporting patterns introduced throughout this chapter — hosts, requests, contexts, Actions, and ViewModel service interfaces — are compositional techniques used to maintain these boundaries consistently as applications grow in complexity.
