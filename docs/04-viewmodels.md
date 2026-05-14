## 4. ViewModel Layer

The ViewModel layer coordinates application behaviour into observable UI state. ViewModels expose properties the view binds to, react to user input, coordinate application interactions through hosts, and communicate through observable state rather than direct coupling.

A ViewModel does not own presentation concerns. It does not construct views, decide where views appear, or depend on application infrastructure such as routers or dialog systems. Instead, it communicates intent through injected host interfaces and exposes state through observable properties.

> This chapter focuses on the ViewModel layer as a whole and introduces several supporting patterns around viewModels.

* **ViewModels** - screen or section coordination objects.
* **Hosts** - application-facing interfaces used to request interactions with the hosting application.
* **Requests** - short-lived interaction contracts used when a hosted interaction requires input and/or returns a result.
* **Contexts** - shared observable state objects used to coordinate multiple ViewModels.
* **Actions** - executable UI interaction objects that encapsulate execution and availability state.

Together these objects form the reactive coordination boundary between the view layer and the rest of the application.

---

## Contents

- [4.1 Responsibilities of a ViewModel](#41-responsibilities-of-a-viewmodel)
  - [4.1.1 Observable state](#411-observable-state)
  - [4.1.2 Coordinating application interactions](#412-coordinating-application-interactions)
  - [4.1.3 Presentation boundaries](#413-presentation-boundaries)
- [4.2 ViewModel construction boundaries](#42-viewmodel-construction-boundaries)
  - [4.2.1 Local composition ViewModels](#421-local-composition-viewmodels)
  - [4.2.2 Hosted ViewModels](#422-hosted-viewmodels)
  - [4.2.3 Why hosted ViewModels are not constructed directly](#423-why-hosted-viewmodels-are-not-constructed-directly)
  - [4.2.4 Hosts as the application boundary](#424-hosts-as-the-application-boundary)
- [4.3 Decomposing ViewModels](#43-decomposing-viewmodels)
  - [4.3.1 Sub-ViewModels](#431-sub-viewmodels)
  - [4.3.2 Composing validity and derived state](#432-composing-validity-and-derived-state)
  - [4.3.3 Local decomposition versus navigation](#433-local-decomposition-versus-navigation)
- [4.4 Shared observable state](#44-shared-observable-state)
  - [4.4.1 Composition-time property binding](#441-composition-time-property-binding)
  - [4.4.2 Context objects](#442-context-objects)
  - [4.4.3 Local and application contexts](#443-local-and-application-contexts)
-[4.5 Request objects](#45-request-objects)
  - [4.5.1 Request objects as interaction contracts](#451-request-objects-as-interaction-contracts)
  - [4.5.2 Returning results from hosted interactions](#452-returning-results-from-hosted-interactions)
  - [4.5.3 Observable request state](#453-observable-request-state)
- [4.6 Action classes](#46-action-classes)
  - [4.6.1 The problem they solve](#461-the-problem-they-solve)
  - [4.6.2 Action](#462-action)
  - [4.6.3 AsyncAction](#463-asyncaction)
  - [4.6.4 Binding Actions in views](#464-binding-actions-in-views)

---

### 4.1 Responsibilities of a ViewModel

A ViewModel is an observable coordination object that adapts application behaviour into UI-bindable state without owning presentation or infrastructure concerns.

Its responsibilities are deliberately narrow:

- Expose observable state for the view to bind to.
- Coordinate interactions between the view, services and the hosting application.
- Derive presentation-oriented state from application data.
- Maintain UI-specific state such as selection, validation and loading state.

A ViewModel is not responsible for:

- constructing views,
- deciding where views appear,
- performing rendering,
- owning application infrastructure,
- or coordinating unrelated areas of the application directly.

The view binds to the ViewModel reactively. User interactions are forwarded to the ViewModel through method calls or Actions, and observable property changes automatically propagate back to the view.

#### 4.1.1 Observable state

The primary responsibility of a ViewModel is exposing observable state.

The view binds directly to ViewModel properties and updates automatically when those properties change. The ViewModel does not manually refresh controls or manipulate the view directly.

> The examples in this chapter use JavaFX properties and observable collections as the concrete observable mechanism but these concepts apply to any reactive UI library.

```java
public class OrdersExplorerViewModel {

    private final ObservableList<Order> orders =
        FXCollections.observableArrayList();

    private final StringProperty statusText =
        new SimpleStringProperty("");

    private final OrdersExplorerService service;
    private final OrdersExplorerHost host;

    public OrdersExplorerViewModel(
        OrdersExplorerService service,
        OrdersExplorerHost host
    ) {
        this.service = service;
        this.host = host;

        refresh();
    }

    public ObservableList<Order> getOrders() {
        return orders;
    }

    public ReadOnlyStringProperty statusTextProperty() {
        return statusText;
    }

    public void refresh() {

        var result = service
            .fetchAllOrders()
            .stream()
            .sorted(Comparator.comparing(Order::date).reversed())
            .toList();

        orders.setAll(result);

        var pendingCount = (int)
            result.stream()
                .filter(Order::isOverdue)
                .count();

        statusText.set(
            result.size() + " orders, "
            + pendingCount + " overdue"
        );
    }
}
```

The ViewModel adapts application data into a form suitable for presentation:

- sorting orders,
- deriving summary information,
- exposing observable collections,
- and maintaining UI-facing text.

None of these concerns belong in the view itself.

> JavaFX properties are used here as the concrete observable implementation. Other UI frameworks provide equivalent mechanisms — such as `INotifyPropertyChanged` in WPF or `LiveData`/`StateFlow` on Android — but the architectural role of the ViewModel remains the same.

#### 4.1.2 Coordinating application interactions

A ViewModel frequently needs to interact with the surrounding application:

- opening another screen,
- showing a dialog,
- requesting a file,
- activating a workspace,
- or publishing a notification.

These interactions are expressed through injected host interfaces.

```java
public interface OrdersExplorerHost {
    void showOrderDetails(Order order);
}
```

The ViewModel communicates intent through the host without knowing how the interaction is implemented:

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

The ViewModel does not know:

- how the next ViewModel is constructed,
- where the resulting view appears,
- whether the interaction is modal,
- or what infrastructure performs the transition.

Those concerns belong to the hosting application.

Host interfaces should express application intent rather than UI infrastructure mechanics. For example:

```java
host.showOrderDetails(order);
```

is preferable to:

```java
router.navigateTo(...);
dialogService.show(...);
```

The ViewModel communicates what it wants to happen, not how the application performs it.

#### 4.1.3 Presentation boundaries

A ViewModel coordinates presentation state but does not own presentation itself.

In particular, a ViewModel should not:

- construct views,
- construct hosted ViewModels,
- depend on routers or dialog systems,
- or make decisions about presentation layout.

These restrictions exist for two reasons.

First, hosted ViewModels often require dependencies from the wider application:

```java
new OrderDetailsViewModel(
    service,
    host,
    ...
)
```

If one ViewModel constructs another hosted ViewModel directly, it must acquire dependencies it does not itself use purely to pass them onwards. Dependencies begin leaking through unrelated ViewModels solely to support construction.

Second, the current ViewModel does not know how the application intends to present the interaction. The same request might:

- replace the workspace,
- open in a dialog,
- appear in a docked panel,
- or activate an existing tab.

Those are application-level presentation decisions.

Instead, the ViewModel communicates intent through its host:

```java
host.showOrderDetails(order);
```

The hosting application then decides:

- how the next ViewModel is constructed,
- which dependencies it receives,
- and how the resulting view is presented.

This preserves clear boundaries between:

- observable UI coordination,
- application infrastructure,
- and presentation composition.

### 4.2 ViewModel construction boundaries

Not all ViewModels are constructed in the same way.

Some ViewModels are local implementation details of another ViewModel and can be constructed directly. Others participate in the wider application shell and require application-level dependencies and presentation decisions. Treating these two cases as equivalent leads to unnecessary coupling and dependency leakage.

The distinction is not simply parent versus child ViewModels. The important distinction is whether construction crosses an application boundary.

This chapter refers to these two categories as:

- local composition ViewModels,
- and hosted ViewModels.

#### 4.2.1 Local composition ViewModels

A local composition ViewModel represents a subsection of another ViewModel's presentation state. It is an internal implementation detail of the parent ViewModel and exists entirely within the parent's presentation scope.

Typical examples include:

- form sections,
- editable tables,
- inspectors,
- filter panels,
- and reusable UI fragments.

A local composition ViewModel:

- is constructed directly by its parent,
- uses state already owned by the parent,
- introduces no new application dependencies,
- and is rendered only within the parent view's layout.

```java
public class OrderEditorViewModel {

    private final OrderHeaderViewModel header;
    private final LineItemsViewModel lineItems;

    public OrderEditorViewModel(Order order) {

        this.header =
            new OrderHeaderViewModel(order);

        this.lineItems =
            new LineItemsViewModel(order.lineItems());
    }

    public OrderHeaderViewModel getHeader() {
        return header;
    }

    public LineItemsViewModel getLineItems() {
        return lineItems;
    }
}
```

This construction is architecturally safe because:

- the parent already owns the relevant state,
- no application infrastructure is required,
- and no presentation decision is implied.

The parent view composes the resulting sub-views directly into its own layout:

The sub-ViewModels do not participate in application navigation and are not independently hosted by the application shell.

#### 4.2.2 Hosted ViewModels

A hosted ViewModel participates in the wider application environment.

Examples include:

- screens,
- dialogs,
- workspaces,
- tabs,
- overlays,
- and docked panels.

Hosted ViewModels frequently require:

- service interfaces,
- host interfaces,
- shared contexts,
- application-scoped state,
- or interaction requests.

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

The current ViewModel communicates intent through its host, but does not construct the next ViewModel itself.

This keeps the current ViewModel independent from:

- the dependencies required by the hosted ViewModel,
- the application's presentation infrastructure,
- and decisions about where or how the interaction appears.

#### 4.2.3 Why hosted ViewModels are not constructed directly

Constructing hosted ViewModels directly causes responsibilities to leak across architectural boundaries.

Consider a ViewModel that directly constructs another hosted ViewModel:

```java
public void openOrder(Order order) {

    var vm = new OrderDetailsViewModel(
        orderService,
        workspaceHost,
        dialogHost,
        ...
    );

    ...
}
```

The current ViewModel must now acquire dependencies it does not itself use solely so they can be passed into another constructor.

As applications grow this causes:

- constructor fan-out,
- infrastructure leakage,
- tighter coupling between unrelated features,
- and ViewModels that exist primarily to forward dependencies elsewhere.

It also couples the current ViewModel to presentation decisions it should not own.

The ViewModel does not know whether the interaction should:

- open in a dialog,
- activate a workspace,
- reuse an existing tab,
- or appear inline.

Those are hosting concerns.

Hosted ViewModels should therefore be constructed by the hosting application rather than by other ViewModels directly.

#### 4.2.4 Hosts as the application boundary

Host interfaces form the boundary between the ViewModel layer and the hosting application.

A host exposes application capabilities relevant to a particular ViewModel:

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

The host implementation — typically wired in the composition root or application shell — performs the actual work:

- constructing hosted ViewModels,
- resolving dependencies,
- choosing presentation behaviour,
- and coordinating the surrounding application.

This keeps ViewModels focused on:

- observable state,
- user interaction,
- and presentation-oriented coordination.

Host interfaces should remain capability-oriented rather than infrastructure-oriented.

For example:

```java
host.showOrderDetails(order);
host.showCreateOrder();
```

is preferable to exposing generic infrastructure operations such as:

```java
router.navigate(...);
dialogService.show(...);
workspaceManager.open(...);
```

The ViewModel should describe application intent, not infrastructure mechanics.

---

### 4.3 Decomposing ViewModels

As screens grow in complexity, ViewModels accumulate:

- validation rules,
- editable state,
- selection state,
- derived properties,
- table coordination,
- filtering,
- and interaction logic.

Keeping all of this inside a single ViewModel quickly produces large coordination objects that are difficult to understand and modify.

ViewModel decomposition addresses this by separating distinct areas of presentation behaviour into smaller ViewModels that are composed together.

The goal is not arbitrary fragmentation. A ViewModel should generally represent a coherent area of UI behaviour. Decomposition becomes valuable when a screen contains distinct subsections with their own state, rules or interactions.

#### 4.3.1 Sub-ViewModels

A sub-ViewModel encapsulates the state and behaviour of a distinct subsection of a larger screen.

Typical candidates include:

- form sections,
- editable tables,
- side panels,
- inspectors,
- search/filter controls,
- and reusable composite controls.

Each sub-ViewModel owns:

- its own observable state,
- its own validation rules,
- and its own derived properties.

For example, the header section of an order editor may manage customer information independently from the line items section:

```java
public class OrderHeaderViewModel {

    private final StringProperty customerName =
        new SimpleStringProperty();

    private final ObjectProperty<LocalDate> orderDate =
        new SimpleObjectProperty<>();

    private final StringProperty reference =
        new SimpleStringProperty();

    private final BooleanProperty valid =
        new SimpleBooleanProperty(false);

    public OrderHeaderViewModel(Order order) {
        customerName.set(order.customerName());
        orderDate.set(order.date());
        reference.set(order.reference());

        customerName.addListener(obs -> validate());
        orderDate.addListener(obs -> validate());
        reference.addListener(obs -> validate());

        validate();
    }

    private void validate() {
        valid.set(
            customerName.get() != null &&
            !customerName.get().isBlank() &&

            orderDate.get() != null &&

            reference.get() != null &&
            !reference.get().isBlank()
        );
    }

    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    public OrderHeader buildHeader() {
        return new OrderHeader(
            customerName.get(),
            orderDate.get(),
            reference.get()
        );
    }
}
```

A separate ViewModel owns the editable line items section:

```java id="9tt5u4"
public class LineItemsViewModel {

    private final ObservableList<LineItemRow> rows =
        FXCollections.observableArrayList();

    private final BooleanProperty valid =
        new SimpleBooleanProperty(false);

    public LineItemsViewModel(List<LineItem> items) {

        rows.setAll(
            items.stream()
                .map(LineItemRow::new)
                .toList()
        );

        rows.addListener(
            (ListChangeListener<LineItemRow>) c -> validate()
        );

        validate();
    }

    private void validate() {

        valid.set(
            !rows.isEmpty() &&
            rows.stream().noneMatch(row ->
                row.descriptionProperty().get().isBlank()
            )
        );
    }

    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    public List<LineItem> buildLineItems() {

        return rows.stream()
            .map(LineItemRow::toLineItem)
            .toList();
    }
}
```

The parent ViewModel coordinates these sections rather than owning all behaviour directly.

#### 4.3.2 Composing validity and derived state

A parent ViewModel should compose state from its sub-ViewModels rather than duplicating their rules.

Each sub-ViewModel owns the definition of what makes its subsection valid. The parent combines these observable states into higher-level derived state:

```java
public class OrderEditorViewModel {

    private final OrderHeaderViewModel header;
    private final LineItemsViewModel lineItems;

    private final BooleanProperty canSave =
        new SimpleBooleanProperty(false);

    public OrderEditorViewModel(Order order) {

        this.header =
            new OrderHeaderViewModel(order);

        this.lineItems =
            new LineItemsViewModel(order.lineItems());

        canSave.bind(
            header.validProperty()
                .and(lineItems.validProperty())
        );
    }

    public ReadOnlyBooleanProperty canSaveProperty() {
        return canSave;
    }
}
```

The parent does not know:

- how the header validates itself,
- how line item validity is determined,
- or what rules each section applies internally.

It only coordinates the resulting observable state.

This separation is important because it prevents:

- duplicated validation logic,
- large monolithic ViewModels,
- and hidden coupling between unrelated subsections.

The resulting structure is highly compositional:

- child ViewModels own local rules,
- the parent composes larger behaviour from them.

#### 4.3.3 Local decomposition versus navigation

Sub-ViewModels are local composition objects, not navigated application features.

This distinction is important because the construction rules are different.

A sub-ViewModel:

- exists entirely within the parent's presentation scope,
- uses state already owned by the parent,
- and introduces no new application dependencies.

For this reason it is safe for the parent ViewModel to construct sub-ViewModels directly:

```java
this.header =
    new OrderHeaderViewModel(order);
```

Hosted ViewModels are different.

A hosted ViewModel may require:

- services,
- hosts,
- application contexts,
- requests,
- or application-level presentation coordination.

Those ViewModels are constructed by the hosting application rather than by another ViewModel directly.

This distinction helps avoid a common MVVM failure mode where ViewModels become tightly coupled because they construct one another recursively and accumulate dependencies solely to forward them onwards.

A useful rule is:

* if the ViewModel exists only within the parent's layout, local composition is appropriate,
* if the ViewModel participates in application hosting or navigation, construction belongs to the hosting application.

---

### 4.4 Shared observable state

Not all ViewModel communication requires direct interaction.

In many cases one ViewModel simply exposes observable state while another consumes it. A sidebar may display a pending order count derived from the currently active workspace. A toolbar may enable or disable actions based on the active editor. A status bar may reflect the current connection state.

These relationships are fundamentally reactive rather than imperative:

- one ViewModel publishes observable state,
- another observes or binds to it.

Several mechanisms can be used depending on the ownership and lifetime of the shared state.

#### 4.4.1 Composition-time property binding

The simplest form of shared state is direct property binding performed during composition.

A ViewModel exposes observable state normally:

```java
public class OrdersExplorerViewModel {

    private final IntegerProperty pendingOrderCount =
        new SimpleIntegerProperty();

    public ReadOnlyIntegerProperty pendingOrderCountProperty() {
        return pendingOrderCount;
    }

    public void refresh() {

        var result = service.fetchAllOrders();

        pendingOrderCount.set(
            (int) result.stream()
                .filter(Order::isOverdue)
                .count()
        );
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

The relationship between them is then established in the composition root:

```java
sidebarVm.pendingOrderCountProperty()
    .bind(explorerVm.pendingOrderCountProperty());
```

Neither ViewModel knows the other exists.

This is often the cleanest approach when:

- ownership of the state is clear,
- the relationship is localised,
- and the lifetime of the state belongs naturally to one ViewModel.

The resulting communication is:

- declarative,
- reactive,
- and infrastructure-free.

#### 4.4.2 Context objects

Sometimes shared state does not belong naturally to a single ViewModel.

For example:

- multiple workspaces may need access to the same selection,
- application-wide notifications may need to be observed globally,
- several unrelated ViewModels may need coordinated state,
- or the state may outlive any single ViewModel.

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

The context itself contains observable state but generally little or no behaviour. It exists to provide:

- shared ownership,
- stable lifetime,
- and a central observable coordination point.

ViewModels are bound to a context object in the composition root.

Unlike direct ViewModel references:

- relationships remain indirect,
- and ViewModels remain decoupled.

#### 4.4.3 Local and application contexts

Contexts can exist at different scopes.

A local context coordinates state shared between related ViewModels within a feature area:

```java
// This context will only be shared between viewModels that describe
// the orders section of the application.
OrderEditorContext
```

An application context represents longer-lived application concepts:

```java
// These contexts may be used by any viewModel in the application.
WorkspaceContext
MessageBoxContext
UserSessionContext
```

Application contexts often correspond to concepts managed by the application shell itself:

- active workspaces,
- notifications,
- authentication state,
- theme state,
- or global selection.

Contexts should remain focused around a coherent area of shared state.

A single global state container such as:

```java
ApplicationState
```

typically becomes a god object over time and should generally be avoided.

#### 4.4.4 Contexts versus ViewModels

Contexts and ViewModels are closely related concepts because both expose observable state.

The distinction is primarily about responsibility and ownership.

A ViewModel:

- coordinates UI behaviour,
- derives presentation state,
- and represents a specific screen or UI area.

A context:

- represents shared observable state,
- has no presentation responsibility,
- and exists independently from any particular view.

In practice the distinction is sometimes subtle. Small contexts can resemble miniature ViewModels, particularly when they expose a handful of observable properties and minimal behaviour.

For this reason contexts are best understood as shared reactive state objects rather than as a completely separate architectural layer.

A useful guideline is:

- if the object primarily coordinates UI behaviour, it is likely a ViewModel,
- if the object primarily exists to share observable state, it is likely a context.
