## 7. Code organisation

This section describes how to arrange the classes introduced throughout this guide into packages. Classes are grouped by feature, with sub-packages separating concerns within each feature.

## Contents

- [7.1 Top-level packages](#71-top-level-packages)
- [7.2 Feature sub-packages](#72-feature-sub-packages)
- [7.3 Cross-cutting infrastructure](#73-cross-cutting-infrastructure)
- [7.4 Complete package layout](#74-complete-package-layout)

### 7.1 Top-level packages

Each top-level package corresponds to one feature. Two packages sit outside this structure because they are shared across all features:

```
com.example/
├── App.java
├── orders/
├── customers/
├── shell/
└── core/
```

- **Feature packages** (`orders`, `customers`) contain everything needed to implement that feature, organised into sub-packages.
- **`shell`** — The application shell: the main window and sidebar. Treated as a feature like any other.
- **`core`** — Reusable infrastructure types shared across all features, organised by layer.

`App.java` sits at the root as the single composition root.

### 7.2 Feature sub-packages

Each feature package is divided into sub-packages by concern. Using the `orders` feature as an example:

```
orders/
├── domain/
├── context/
├── explorer/
├── editor/
│   ├── header/
│   ├── lineitems/
│   └── edititem/
└── adapters/
```

**`domain`** contains the domain types, repository interface, and services. It has no dependency on JavaFX or any other UI framework:

```
orders/domain/
├── Order.java
├── LineItem.java
├── OrderRepository.java
└── CopyOrderService.java
```

**`context`** contains shared observable state that is written by one screen and read by another. These types depend on JavaFX properties and belong in a separate package from the pure domain:

```
orders/context/
├── OrderContext.java
├── PendingOrderCount.java
└── PendingOrderCounter.java
```

**Screen sub-packages** each contain a ViewModel, View, and any supporting types for that screen. Each independently navigable screen gets its own sub-package:

```
orders/explorer/
├── OrdersExplorerHost.java
├── OrdersExplorerService.java
├── OrdersExplorerViewModel.java
└── OrdersExplorerView.java
```

Sub-ViewModels and sub-views that are part of a larger screen are nested under that screen's sub-package:

```
orders/editor/
├── OrderEditorHost.java
├── OrderEditorService.java
├── OrderEditorViewModel.java
├── OrderEditorView.java
├── header/
│   ├── OrderHeaderViewModel.java
│   └── OrderHeaderView.java
├── lineitems/
│   ├── LineItemRowViewModel.java
│   ├── LineItemsViewModel.java
│   └── LineItemsView.java
└── edititem/
    ├── EditItemRequest.java
    ├── EditItemViewModel.java
    └── EditItemView.java
```

**`adapters`** contains concrete implementations of repository interfaces and the module that wires the feature together. This is the only place that knows about specific infrastructure choices (e.g. in-memory vs database):

```
orders/adapters/
├── InMemoryOrderRepository.java
└── OrdersModule.java
```

The `customers` feature follows the same structure, omitting `context` since it has no shared observable state:

```
customers/
├── domain/
│   ├── Customer.java
│   ├── CustomerRepository.java
│   └── CustomerService.java
├── explorer/
│   ├── CustomersExplorerViewModel.java
│   └── CustomersExplorerView.java
├── detail/
│   ├── CustomerDetailViewModel.java
│   └── CustomerDetailView.java
└── adapters/
    ├── InMemoryCustomerRepository.java
    └── CustomersModule.java
```

The shell package separates its screens into sub-packages and includes its own adapters:

```
shell/
├── WorkspaceContext.java
├── main/
│   ├── MainViewModel.java
│   └── MainView.java
├── sidebar/
│   ├── SidebarViewModel.java
│   └── SidebarView.java
└── adapters/
    └── ShellModule.java
```

### 7.3 Cross-cutting infrastructure

```
core/
├── viewmodel/
│   ├── Action.java
│   └── AsyncAction.java
└── view/
    ├── CurrencyTableCell.java
    ├── DialogManager.java
    ├── ViewLocator.java
    └── ViewServices.java
```

### 7.4 Complete package layout

```
com.example/
│
├── App.java
│
├── orders/
│   ├── domain/
│   │   ├── Order.java
│   │   ├── LineItem.java
│   │   ├── OrderRepository.java
│   │   └── CopyOrderService.java
│   ├── context/
│   │   ├── OrderContext.java
│   │   ├── PendingOrderCount.java
│   │   └── PendingOrderCounter.java
│   ├── explorer/
│   │   ├── OrdersExplorerHost.java
│   │   ├── OrdersExplorerService.java
│   │   ├── OrdersExplorerViewModel.java
│   │   └── OrdersExplorerView.java
│   ├── editor/
│   │   ├── OrderEditorHost.java
│   │   ├── OrderEditorService.java
│   │   ├── OrderEditorViewModel.java
│   │   ├── OrderEditorView.java
│   │   ├── header/
│   │   │   ├── OrderHeaderViewModel.java
│   │   │   └── OrderHeaderView.java
│   │   ├── lineitems/
│   │   │   ├── LineItemRowViewModel.java
│   │   │   ├── LineItemsViewModel.java
│   │   │   └── LineItemsView.java
│   │   └── edititem/
│   │       ├── EditItemRequest.java
│   │       ├── EditItemViewModel.java
│   │       └── EditItemView.java
│   └── adapters/
│       ├── InMemoryOrderRepository.java
│       └── OrdersModule.java
│
├── customers/
│   ├── domain/
│   │   ├── Customer.java
│   │   ├── CustomerRepository.java
│   │   └── CustomerService.java
│   ├── explorer/
│   │   ├── CustomersExplorerViewModel.java
│   │   └── CustomersExplorerView.java
│   ├── detail/
│   │   ├── CustomerDetailViewModel.java
│   │   └── CustomerDetailView.java
│   └── adapters/
│       ├── InMemoryCustomerRepository.java
│       └── CustomersModule.java
│
├── shell/
│   ├── WorkspaceContext.java
│   ├── main/
│   │   ├── MainViewModel.java
│   │   └── MainView.java
│   ├── sidebar/
│   │   ├── SidebarViewModel.java
│   │   └── SidebarView.java
│   └── adapters/
│       └── ShellModule.java
│
└── core/
    ├── view/
    │   ├── CurrencyTableCell.java
    │   ├── DialogManager.java
    │   ├── ViewLocator.java
    │   └── ViewServices.java
    └── viewmodel/
        ├── Action.java
        └── AsyncAction.java
```
