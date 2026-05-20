## 7. Code organisation

This section describes how to arrange the classes introduced throughout this guide into packages. Classes are grouped by feature, with sub-packages separating concerns within each feature.

## Contents

- [7.1 Top-level packages](#71-top-level-packages)
- [7.2 Feature sub-packages](#72-feature-sub-packages)
- [7.3 Cross-cutting infrastructure](#73-cross-cutting-infrastructure)

### 7.1 Top-level packages

Each top-level package corresponds to one feature. Two packages sit outside this structure because they are shared across all features:

```
com.example/
├── App.java
├── orders/
├── customers/
├── stock/
├── shell/
└── core/
```

- **Feature packages** (`orders`, `customers`, `stock`) contain everything needed to implement that feature, organised into sub-packages.
- **`shell`** — The application shell: the main window, sidebar, and status bar. Treated as a feature like any other.
- **`core`** — Reusable infrastructure types shared across all features, organised by layer.

`App.java` sits at the root as the single composition root.

### 7.2 Feature sub-packages

Each feature package is divided into sub-packages by concern. Using the `orders` feature as an example:

```
orders/
├── domain/
│   ├── commands/
│   └── queries/
├── explorer/
└── editor/
    ├── header/
    └── lineitems/
```

**`domain`** contains the domain types and repository interface. It has no dependency on JavaFX or any other UI framework:

```
orders/domain/
├── Order.java
├── LineItem.java
├── OrderRepository.java
├── commands/
│   └── CopyOrderCommand.java
└── queries/
    ├── GetOrderSummariesQuery.java
    ├── GetLineItemSummariesQuery.java
    └── OrderSummary.java
```

Commands and queries each get their own sub-package when the domain warrants them. Pure domain types stay at the `domain/` level.

**Screen sub-packages** each contain a ViewModel, View, and any supporting types for that screen. Each independently navigable screen gets its own sub-package:

```
orders/explorer/
├── OrdersExplorerHost.java
├── OrdersExplorerService.java
├── OrdersExplorerViewModel.java
└── OrdersExplorerView.java
```

Sub-ViewModels and sub-views that are part of a larger screen are nested under that screen's sub-package. Request types used to open dialogs or pass context between screens also live here:

```
orders/editor/
├── EditOrderRequest.java
├── OrderEditorHost.java
├── OrderEditorService.java
├── OrderEditorViewModel.java
├── OrderEditorView.java
├── header/
│   ├── SelectCustomerRequest.java
│   ├── CustomerSelectorViewModel.java
│   ├── CustomerSelectorView.java
│   ├── OrderHeaderViewModel.java
│   └── OrderHeaderView.java
└── lineitems/
    ├── EditItemRequest.java
    ├── SelectProductRequest.java
    ├── LineItemsExplorerViewModel.java
    ├── LineItemsExplorerView.java
    ├── LineItemEditorViewModel.java
    ├── LineItemEditorView.java
    ├── ProductSelectorViewModel.java
    └── ProductSelectorView.java
```

The `customers` feature follows the same structure. A `requests/` package holds request types that don't belong to a specific screen sub-package:

```
customers/
├── domain/
│   ├── Customer.java
│   └── CustomerRepository.java
├── requests/
│   └── EditCustomerRequest.java
├── explorer/
│   ├── CustomerExplorerHost.java
│   ├── CustomersExplorerService.java
│   ├── CustomersExplorerViewModel.java
│   └── CustomersExplorerView.java
└── editor/
    ├── CustomerEditorService.java
    ├── CustomerEditorViewModel.java
    └── CustomerEditorView.java
```

The shell package separates its screens into sub-packages:

```
shell/
├── ShellContext.java
└── main/
    ├── MainViewModel.java
    ├── MainView.java
    ├── sidebar/
    │   ├── SidebarViewModel.java
    │   └── SidebarView.java
    └── statusbar/
        ├── StatusBarViewModel.java
        └── StatusBarView.java
```

### 7.3 Cross-cutting infrastructure

The `core` package contains types shared across all features, organised by layer. Wiring and composition live in `core/config/`:

```
core/
├── config/
│   ├── AppModule.java
│   ├── OrdersModule.java
│   ├── CustomersModule.java
│   ├── StockModule.java
│   ├── ShellModule.java
│   └── adapters/
│       ├── InMemoryOrderRepository.java
│       ├── InMemoryCustomerRepository.java
│       └── InMemoryStockRepository.java
├── view/
│   ├── ExplorerView.java
│   ├── DialogManager.java
│   ├── ViewLocator.java
│   ├── ViewServices.java
│   └── controls/
│       ├── TableViews.java
│       └── CurrencyTableCell.java
└── viewmodel/
    ├── Action.java
    ├── AsyncAction.java
    └── ExplorerViewModel.java
```

Module classes (`OrdersModule`, `CustomersModule`, etc.) live in `core/config/` rather than in each feature package. This keeps all composition in one place and prevents feature packages from depending on concrete infrastructure. In-memory repository implementations follow into `core/config/adapters/` for the same reason — they are infrastructure concerns, not feature concerns.
