# 9. Glossary

Quick-reference tables covering the layers, types, and roles introduced throughout this guide.

## Layers

| Layer | Responsibility | May depend on | May not depend on |
|---|---|---|---|
| **Model** | Domain concepts, business rules, validation | Nothing | Services, ViewModels, Views |
| **Service** | Data retrieval and persistence (database, API, file I/O) | Model layer | ViewModels, Views |
| **ViewModel** | Observable state, use case coordination, navigation intent | Use cases, context objects | Views, UI framework types |
| **View** | UI construction, property binding, user event delegation | ViewModel, ViewLocator, ViewRouter | Services, domain logic |

## Types

| Type | Layer | Description |
|---|---|---|
| **Model** | Model | Plain object representing a domain concept. Immutable or encapsulates domain rules. No observable properties, no UI imports. |
| **Repository** | Service | Interface defining the persistence contract for a Model type. Concrete implementations live in `adapters`. |
| **Service** | Service | Retrieves and persists Models. Exposes operations the application needs, not a general-purpose CRUD API. Not injected directly into ViewModels. |
| **Use case** | Service / ViewModel boundary | ~~Single-operation class. Constructor receives services; `execute` receives data from the ViewModel at invocation time. Keeps service logic out of ViewModels.~~ _Out of date, needs updating._ |
| **Use case record** | Service / ViewModel boundary | ~~Parameter object bundling related use cases into a single named argument, reducing ViewModel constructor length.~~ _Out of date, needs updating._ |
| **ViewModel** | ViewModel | Observable state and behaviour for one screen or area. Holds use cases and navigation callbacks. No UI framework types, no knowledge of Views. |
| **Sub-ViewModel** | ViewModel | ViewModel for a distinct section of a parent screen. Constructed directly by the parent; never registered with the ViewLocator or navigated to. |
| **Context object** | ViewModel | Shared observable state for a specific domain concern, injected into multiple ViewModels. Producer writes to it; consumer binds to it. Neither knows about the other. |
| **Session object** | ViewModel | Short-lived object scoped to a single interaction. Carries input data for the target ViewModel and an output callback to return the result. |
| **Action** | ViewModel | Pairs a synchronous operation with an optional `canExecute` guard. Self-guarding: calling `execute` when disabled has no effect. |
| **AsyncAction** | ViewModel | Pairs a long-running operation with an optional `canExecute` guard. Exposes `isExecuting`; automatically blocks re-entry while running. |
| **View** | View | UI class bound to exactly one ViewModel. Constructs the component tree and binds controls to ViewModel properties in the constructor. |
| **Sub-view** | View | View for a sub-ViewModel section. Constructed inline by the parent view; never registered with the ViewLocator. |
| **Component** | View | Reusable UI element with no ViewModel. Accepts plain data or observable values. Contains no application logic. |
| **ViewLocator** | View infrastructure | Type-keyed registry mapping ViewModel classes to view factory functions. Resolves the correct View given a ViewModel instance. |
| **ViewRouter** | View infrastructure | Navigation conduit. Uses the ViewLocator to resolve a View from a ViewModel, then dispatches it to whichever listener is registered for that View type. |
| **Module** | Composition | Groups factory methods, services, and ViewLocator registrations for one feature area. Owns its own infrastructure and exposes nothing the rest of the application does not need. |
| **Composition root** (`App`) | Composition | The single place where all dependencies are constructed and wired across layer boundaries. Every screen and navigation transition is defined here. |

## Roles

| Role | Who holds it | Who provides it |
|---|---|---|
| **Navigation callback** | ViewModel | Composition root — wired as a lambda that calls `viewRouter.route(...)` |
| **Completion callback** | Use case | Composition root — wired inline when the use case is constructed |
| **ViewLocator registration** | ViewLocator | Module or composition root at startup |
| **ViewRouter listener** | View (shell or presentation container) | Registered in the view constructor |
| **Context reader interface** | ViewModel that consumes shared state | Composition root passes the concrete context object cast to the reader interface |
| **Context writer interface** | ViewModel that produces shared state | Composition root passes the concrete context object cast to the writer interface |
