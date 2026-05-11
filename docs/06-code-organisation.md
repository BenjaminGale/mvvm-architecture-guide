## 6. Code organisation

This section describes how to arrange the classes introduced throughout this guide into packages. Classes are grouped by screen — everything needed to implement one screen lives in the same package.

### 6.1 Top-level packages

Each top-level package corresponds to one screen. Two packages sit outside this structure because they are shared across screens:

```
com.example/
├── App.java
├── orders/
├── ordereditor/
├── customers/
├── customerdetail/
├── settings/
├── shell/
└── navigation/
```

- **Screen packages** contain the ViewModel, View, use cases, and any sub-ViewModels and sub-views for that screen.
- **`shell`** — The application shell: the main window, sidebar, and dialog manager. Treated as a screen like any other.
- **`navigation`** — Cross-cutting infrastructure: `ViewFactory`, `ViewRouter`, `Action`, and `AsyncAction`. Shared by all screens and not specific to any one of them.

`App.java` sits at the root as the single composition root.

### 6.2 Screen packages

Each screen package is flat. All classes needed to render and operate a single screen are co-located:

```
orders/
├── Order.java
├── LineItem.java
├── OrderService.java
├── OrderContext.java
├── LoadOrdersUseCase.java
├── OrdersViewModel.java
└── OrdersView.java
```

Sub-ViewModels and sub-views belong in the same package as the screen they form part of — they are an internal implementation detail of that screen and are not navigated to independently:

```
ordereditor/
├── CopyOrderUseCase.java
├── DeleteOrderUseCase.java
├── OrderEditorUseCases.java
├── SaveOrderUseCase.java
├── LineItemRow.java
├── LineItemsViewModel.java
├── OrderEditorViewModel.java
├── OrderHeaderViewModel.java
├── LineItemsView.java
├── OrderEditorView.java
└── OrderHeaderView.java
```

Domain objects and services that are shared between screens live in the package of the screen that owns them. The `ordereditor` screen imports `Order` and `OrderService` from `orders` — the orders list is the natural owner of those types.

```
customers/
├── Customer.java
├── CustomerService.java
├── CustomersViewModel.java
└── CustomersView.java

customerdetail/
├── CustomerDetailViewModel.java
└── CustomerDetailView.java
```

The shell package follows the same flat convention:

```
shell/
├── MainViewModel.java
├── SidebarViewModel.java
├── MainView.java
├── SidebarView.java
└── DialogManagerView.java
```

### 6.3 Cross-cutting infrastructure

`ViewFactory`, `ViewRouter`, `Action`, and `AsyncAction` are not specific to any screen. They live in `navigation/` and may be imported by any screen package:

```
navigation/
├── Action.java
├── AsyncAction.java
├── ViewFactory.java
└── ViewRouter.java
```

### 6.4 Complete package layout

```
com.example/
│
├── App.java
│
├── orders/
│   ├── Order.java
│   ├── LineItem.java
│   ├── OrderContext.java
│   ├── OrderService.java
│   ├── LoadOrdersUseCase.java
│   ├── OrdersViewModel.java
│   └── OrdersView.java
│
├── ordereditor/
│   ├── CopyOrderUseCase.java
│   ├── DeleteOrderUseCase.java
│   ├── OrderEditorUseCases.java
│   ├── SaveOrderUseCase.java
│   ├── LineItemRow.java
│   ├── LineItemsViewModel.java
│   ├── OrderEditorViewModel.java
│   ├── OrderHeaderViewModel.java
│   ├── LineItemsView.java
│   ├── OrderEditorView.java
│   └── OrderHeaderView.java
│
├── customers/
│   ├── Customer.java
│   ├── CustomerService.java
│   ├── CustomersViewModel.java
│   └── CustomersView.java
│
├── customerdetail/
│   ├── CustomerDetailViewModel.java
│   └── CustomerDetailView.java
│
├── settings/
│   ├── SettingsViewModel.java
│   └── SettingsView.java
│
├── shell/
│   ├── MainViewModel.java
│   ├── SidebarViewModel.java
│   ├── MainView.java
│   ├── SidebarView.java
│   └── DialogManagerView.java
│
└── navigation/
    ├── Action.java
    ├── AsyncAction.java
    ├── ViewFactory.java
    └── ViewRouter.java
```
