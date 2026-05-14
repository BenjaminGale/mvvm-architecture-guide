# 1. Introduction

This document describes a practical approach to implementing the Model-View-ViewModel (MVVM) pattern.

The examples use Java (with JavaFX as the view technology), however the architectural principles are independent of any particular framework and can be applied to any UI technology that provides a suitable property binding mechanism (e.g. WPF).

The focus of this document is not simply how to structure Views and ViewModels, but how to build applications that remain maintainable as they grow. Particular attention is given to dependency management, navigation, composition, testability, and keeping responsibilities clearly separated between layers.

The approach presented here is intentionally strict. The architectural constraints are designed to prevent common forms of coupling that tend to emerge gradually in large MVVM applications. These constraints are treated as invariants rather than guidelines so the overall structure remains consistent as new screens and workflows are added.

## Contents

* [1.1 What is MVVM](#11-what-is-mvvm)
* [1.2 Why use MVVM](#12-why-use-mvvm)
* [1.3 Core architectural guidelines](#13-core-architectural-guidelines)

  * [1.3.1 Keep ViewModels narrowly focused](#131-keep-viewmodels-narrowly-focused)
  * [1.3.2 Depend only on required behaviour](#132-depend-only-on-required-behaviour)
  * [1.3.3 Keep navigation independent of presentation](#133-keep-navigation-independent-from-presentation)
  * [1.3.4 Prefer composition over inheritance](#134-prefer-composition-over-inheritance)
  * [1.3.5 Keep command logic out of ViewModels](#135-keep-command-logic-out-of-viewmodels)
  * [1.3.6 Design for practical testability](#136-design-for-practical-testability)
* [1.4 Design goals](#14-design-goals)

---

# 1.1 What is MVVM

The MVVM pattern divides an application into three layers with distinct, non-overlapping responsibilities.

## Model

Represents the application's core domain concepts.

Models encapsulate data, business rules, validation, and domain behaviour. They have no knowledge of the UI, persistence mechanisms, or presentation concerns.

This layer also includes services responsible for retrieving, persisting, and coordinating Models. Services encapsulate infrastructure concerns such as repositories, APIs, messaging, and external systems while remaining independent from the View and ViewModel layers.

## ViewModel

An abstraction of a View.

The ViewModel exposes observable state for the View to bind to and provides operations the View invokes in response to user interaction. A ViewModel contains no rendering logic and has no knowledge of how the View is constructed or displayed.

The ViewModel exists to model presentation state and interaction flow, not business logic or infrastructure concerns.

## View

Represents the user interface.

The View binds to observable state exposed by the ViewModel and delegates user interaction back to the ViewModel. The View contains presentation concerns only and has no knowledge of domain logic or data access.

---

# 1.2 Why use MVVM

The primary benefit of MVVM is the establishment of a strict one-way dependency structure.

Views depend on ViewModels, ViewModels depend on application logic and services, and services depend on nothing above them. This separation allows each layer to evolve independently while keeping responsibilities explicit.

Most importantly, MVVM provides a mechanism for separating presentation concerns from application behaviour. When enforced consistently, this separation reduces coupling, improves maintainability, and keeps UI code manageable as complexity increases.


Because ViewModels contain no UI framework types or rendering concerns, they can be tested directly in isolation without launching a UI runtime. State changes and interaction behaviour can be verified using ordinary unit tests.

The pattern also scales predictably. Applications can grow from a small number of screens to large multi-area systems without changing the architectural model. Each screen follows the same structure and new functionality can be introduced without modifying unrelated components.

---

# 1.3 Core architectural guidelines

The following guidelines define the architectural constraints used throughout this document.

These constraints exist to prevent forms of coupling that commonly emerge in large MVVM applications over time.

Collectively, they determine whether an application remains maintainable as screens, workflows, and dependencies increase.

## 1.3.1 Keep ViewModels narrowly focused

A ViewModel should act only as an abstraction of a View.

Its responsibility is to expose observable state and respond to user interaction. It should not become responsible for navigation, dependency construction, persistence coordination, workflow orchestration, or unrelated application logic.

As applications grow, ViewModels naturally accumulate responsibilities unless explicit boundaries are maintained. This often begins incrementally through additional commands, validation rules, or service interactions until the ViewModel effectively becomes a general-purpose coordinator.

Keeping ViewModels narrowly focused preserves clear responsibility boundaries and allows screens to evolve independently. Smaller ViewModels are easier to reason about, easier to test, and less likely to become coupled to unrelated application concerns.

A ViewModel should remain primarily concerned with presentation state and interaction flow.

---

## 1.3.2 Depend only on required behaviour

ViewModels should depend only on the behaviour they directly use.

Injecting broad service interfaces into ViewModels creates unnecessary coupling between the presentation layer and application logic. Large interfaces force ViewModels to depend on operations they do not use, while splitting those interfaces into many smaller services often produces constructors with excessive dependencies.

A ViewModel should not coordinate persistence directly or make assumptions about operations such as save, delete, reload, or synchronization. Those concerns belong to application-level use cases or services.

Instead, ViewModels should receive narrowly scoped collaborators representing the specific behaviour required by the screen. Dependencies should remain explicit and minimal.

This approach reduces construction complexity, improves reuse across different contexts, and prevents ViewModels from becoming orchestration layers.

The result is a ViewModel that remains a thin presentation abstraction rather than a container for application infrastructure.

---

## 1.3.3 Keep navigation independent of presentation

ViewModels should express workflow intent rather than presentation mechanics.

A ViewModel may need to request confirmation, collect additional information, or initiate a transition to another application area. However, it should not assume how those interactions are presented to the user.

Interfaces that provide methods such as `showDialog()`, `openWindow()`, or `navigateToScreen()` tightly couple application flow to a specific UI implementation. Replacing a dialog with an inline panel, embedded workflow, or different navigation structure should not require changes to ViewModels.

Presentation decisions belong to the composition and view layers where screens, layouts, and transitions are assembled.

Separating workflow intent from presentation structure allows the UI to evolve independently from application behaviour and prevents presentation concerns from leaking upward into ViewModels.

---

## 1.3.4 Prefer composition over inheritance

Shared ViewModel behaviour should generally be extracted into collaborating components rather than base classes.

Inheritance introduces implicit coupling between ViewModels that may otherwise be unrelated. Over time, base classes accumulate utility behaviour, lifecycle assumptions, and hidden dependencies that subclasses inherit regardless of whether they require them.

This makes behaviour harder to reason about and increases the risk that changes to shared infrastructure produce unintended side effects throughout the application.

Composition keeps dependencies explicit. Behaviour can be introduced where required without forcing unrelated ViewModels into the same hierarchy.

Using composition also improves testability because collaborating components can be tested independently and substituted without affecting unrelated classes.

Inheritance should model genuine "is-a" relationships rather than act as a mechanism for code reuse.

---

## 1.3.5 Keep command logic out of ViewModels

Commands should translate user interaction into application behaviour, not act as containers for business logic.

When command handlers are implemented directly inside a ViewModel, the dependencies required by those handlers accumulate in the ViewModel constructor. Over time the ViewModel becomes responsible for coordination, validation, persistence, and workflow management in addition to state exposure.

This causes command-heavy screens to grow disproportionately and makes ViewModels increasingly difficult to construct, test, and maintain.

Command behaviour should instead delegate meaningful work to dedicated application services. The ViewModel remains responsible for interaction state while the application layer performs the underlying operation.

This keeps command implementations small, prevents dependency accumulation, and preserves the ViewModel's role as a presentation abstraction.

---

## 1.3.6 Design for practical testability

Testability should be an observable property of the architecture rather than an abstract claim.

A ViewModel should be constructable with minimal setup and verifiable through observable state changes. Tests should focus on externally visible behaviour rather than implementation details or internal method calls.

Heavy dependency injection, broad service interfaces, and tightly coupled infrastructure increase the amount of mocking required before meaningful assertions can be made. This produces brittle tests that are difficult to read and expensive to maintain.

Well-structured ViewModels require only the dependencies directly relevant to the behaviour under test. This keeps tests small, focused, and resilient to refactoring.

The goal is not merely that tests are possible, but that writing and maintaining them remains practical as the application grows.

---

# 1.4 Design goals

The following constraints are treated as architectural invariants throughout this document.

Violating any one introduces special cases that weaken the consistency of the application structure over time.

- Every View is bound to exactly one ViewModel.
- ViewModels have no knowledge of Views or how they are constructed.
- Each ViewModel holds only the dependencies it directly uses.
- Nothing creates its own dependencies — all construction is performed externally.
- All object construction and wiring exists in a single composition root.
- The composition root is the complete map of screens, workflows, and transitions within the application.
- Presentation concerns remain isolated from domain and application logic.
- Application workflows are expressed independently from UI implementation details.
