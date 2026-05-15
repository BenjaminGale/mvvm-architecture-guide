# 9. Glossary

Quick-reference tables covering the layers, types, and roles introduced throughout this guide.

## Layers

| Layer | Responsibility | May depend on                      | May not depend on |
|---|---|------------------------------------|---|
| **Model** | Domain concepts, business rules, validation | -                                  | Services, ViewModels, Views |
| **Service** | Data retrieval and persistence (database, API, file I/O) | Model layer                        | ViewModels, Views |
| **ViewModel** | Observable state, use case coordination, navigation intent | Use cases, context objects         | Views, UI framework types |
| **View** | UI construction, property binding, user event delegation | ViewModel, ViewLocator, ViewRouter | Services, domain logic |

## Types

| Layer | Type      | Description |
|---|-----------|---|
| **Model** | Model     | Plain object representing a domain concept. Immutable or encapsulates domain rules. No observable properties, no UI imports. |
| **Service** | Repository | Interface defining the persistence contract for a Model type. Concrete implementations live in `adapters`. |
| **Service** | Service   | Retrieves and persists Models. Exposes operations the application needs, not a general-purpose CRUD API. Not injected directly into ViewModels. |
| **ViewModel** | Action    | Pairs a synchronous operation with an optional `canExecute` guard. Self-guarding: calling `execute` when disabled has no effect. |
| **ViewModel** | AsyncAction | Pairs a long-running operation with an optional `canExecute` guard. Exposes `isExecuting`; automatically blocks re-entry while running. |
| **ViewModel** | Context object | Shared observable state for a specific domain concern, injected into multiple ViewModels. Producer writes to it; consumer binds to it. Neither knows about the other. |
| **ViewModel** | Request object | Short-lived object scoped to a single interaction. Carries input data for the target ViewModel and an output callback to return the result. |
| **ViewModel** | Sub-ViewModel | ViewModel for a distinct section of a parent screen. Constructed directly by the parent; never registered with the ViewLocator or navigated to. |
| **ViewModel** | ViewModel | Observable state and behaviour for one screen or area. Holds use cases and navigation callbacks. No UI framework types, no knowledge of Views. |
| **View** | Component | Reusable chunk of UI that contains no application logic and is never registered with the ViewLocator. Typically accepts individual observable properties or plain values, though a parent may pass a ViewModel directly if the component is tightly scoped to it. |
| **View** | View      | UI class bound to exactly one ViewModel. Constructs the component tree and binds controls to ViewModel properties in the constructor. |
| **View infrastructure** | DialogManager | Handles modal dialog presentation. Wraps its own ViewLocator and manages dialog lifecycle — owner, modality, and showing. |
| **View infrastructure** | ViewLocator | Type-keyed registry mapping ViewModel classes to view factory functions. Resolves the correct View given a ViewModel instance. |
| **infrastructure** | Composition root (`App`) | The single place where all dependencies are constructed and wired across layer boundaries. Every screen and navigation transition is defined here. |
| **infrastructure** | Module    | Groups factory methods, services, and ViewLocator registrations for one feature area. Owns its own infrastructure and exposes nothing the rest of the application does not need. |
