# 1. Introduction

This document describes a practical approach to implementing the Model-View-ViewModel (MVVM) pattern.

The examples use Java, with JavaFX as the view technology. The architectural principles are not tied to either: they apply to any UI technology with a suitable property binding mechanism (e.g. WPF).

This document explains how to build applications that stay maintainable as they grow: dependency management, navigation, composition, testability, and keeping responsibilities separated between layers.

This document is my personal take on MVVM, developed while building a real application. Some parts are strict rules I've chosen to enforce, treated as invariants throughout, as below. Other parts are just how I'd do it, one way among many that would work.

Most discussions of MVVM stop at the basics: binding, commands, a single screen. They don't show how the pattern holds up once an application has multiple features that need to interoperate. That gap is what this guide is for. Some choices here, like wiring dependencies manually rather than through a DI framework, exist to make the underlying pattern and integration points visible, not to prescribe a specific tool. They're not a claim that this is how a production application should handle that concern.

The approach here is intentionally strict. The constraints are designed to prevent forms of coupling that tend to emerge gradually in large MVVM applications, and are treated as invariants rather than guidelines so the structure stays consistent as new screens and workflows are added.

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

This layer also includes services responsible for retrieving, persisting, and coordinating Models. Services encapsulate infrastructure concerns such as repositories, APIs, messaging, and external systems, and stay independent from the View and ViewModel layers.

## ViewModel

An abstraction of a View.

The ViewModel exposes observable state for the View to bind to and provides operations the View invokes in response to user interaction. A ViewModel contains no rendering logic and has no knowledge of how the View is constructed or displayed.

The ViewModel exists to model presentation state and interaction flow, not business logic or infrastructure concerns.

## View

Represents the user interface.

The View binds to observable state exposed by the ViewModel and delegates user interaction back to the ViewModel. The View contains presentation concerns only and has no knowledge of domain logic or data access.

---

# 1.2 Why use MVVM

The primary benefit of MVVM is a strict one-way dependency structure.

Views depend on ViewModels, ViewModels depend on application logic and services, and services depend on nothing above them. This lets each layer evolve independently while keeping responsibilities explicit.

MVVM's core value is separating presentation concerns from application behaviour. Enforced consistently, this reduces coupling, improves maintainability, and keeps UI code manageable as complexity increases.

Because ViewModels contain no UI framework types or rendering concerns, they can be tested directly in isolation without launching a UI runtime. State changes and interaction behaviour can be verified with ordinary unit tests.

The pattern also scales predictably. Applications can grow from a handful of screens to large multi-area systems without changing the architectural model. Each screen follows the same structure, so new functionality can be added without touching unrelated components.

---

# 1.3 Core architectural guidelines

The following guidelines define the architectural constraints used throughout this document. They exist to prevent forms of coupling that commonly emerge in large MVVM applications over time, and together determine whether an application stays maintainable as screens, workflows, and dependencies increase.

## 1.3.1 Keep ViewModels narrowly focused

A ViewModel should act only as an abstraction of a View.

Its job is to expose observable state and respond to user interaction. It should not become responsible for navigation, dependency construction, persistence coordination, workflow orchestration, or unrelated application logic.

As applications grow, ViewModels naturally accumulate responsibilities unless explicit boundaries are maintained. This often starts small, through extra commands, validation rules, or service interactions, until the ViewModel becomes a general-purpose coordinator.

Keeping ViewModels narrowly focused preserves clear responsibility boundaries and lets screens evolve independently. Smaller ViewModels are easier to reason about, easier to test, and less likely to become coupled to unrelated application concerns.

A ViewModel should stay primarily concerned with presentation state and interaction flow.

---

## 1.3.2 Depend only on required behaviour

ViewModels should depend only on the behaviour they directly use.

Injecting broad service interfaces into ViewModels creates unnecessary coupling between the presentation layer and application logic. Large interfaces force ViewModels to depend on operations they don't use, while splitting those interfaces into many smaller services often produces constructors with too many dependencies.

A ViewModel should not coordinate persistence directly or make assumptions about operations such as save, delete, reload, or synchronization. Those concerns belong to application-level use cases or services.

Instead, ViewModels should receive narrowly scoped collaborators representing the specific behaviour the screen requires. Dependencies should stay explicit and minimal.

This reduces construction complexity, improves reuse across different contexts, and stops ViewModels from becoming orchestration layers, leaving a ViewModel that's a thin presentation abstraction rather than a container for application infrastructure.

---

## 1.3.3 Keep navigation independent of presentation

ViewModels should express workflow intent rather than presentation mechanics.

A ViewModel may need to request confirmation, collect additional information, or start a transition to another application area. But it shouldn't assume how those interactions are presented to the user.

Interfaces with methods like `showDialog()`, `openWindow()`, or `navigateToScreen()` tightly couple application flow to a specific UI implementation. Replacing a dialog with an inline panel, embedded workflow, or different navigation structure shouldn't require changing ViewModels.

Presentation decisions belong to the composition and view layers, where screens, layouts, and transitions are assembled.

Separating workflow intent from presentation structure lets the UI evolve independently from application behaviour and stops presentation concerns leaking upward into ViewModels.

---

## 1.3.4 Prefer composition over inheritance

Shared ViewModel behaviour should generally be extracted into collaborating components rather than base classes.

Inheritance introduces implicit coupling between ViewModels that may otherwise be unrelated. Over time, base classes accumulate utility behaviour, lifecycle assumptions, and hidden dependencies that subclasses inherit whether they need them or not.

This makes behaviour harder to reason about and increases the risk that changes to shared infrastructure produce unintended side effects elsewhere in the application.

Composition keeps dependencies explicit. Behaviour can be introduced where it's needed without forcing unrelated ViewModels into the same hierarchy.

Composition also improves testability: collaborating components can be tested independently and substituted without affecting unrelated classes.

Inheritance should model genuine "is-a" relationships, not act as a mechanism for code reuse.

---

## 1.3.5 Keep command logic out of ViewModels

Commands should translate user interaction into application behaviour, not act as containers for business logic.

When command handlers are implemented directly inside a ViewModel, the dependencies those handlers need accumulate in the ViewModel constructor. Over time the ViewModel becomes responsible for coordination, validation, persistence, and workflow management on top of state exposure.

This causes command-heavy screens to grow disproportionately and makes ViewModels harder to construct, test, and maintain.

Command behaviour should instead delegate meaningful work to dedicated application services. The ViewModel stays responsible for interaction state while the application layer performs the underlying operation.

This keeps command implementations small, prevents dependency accumulation, and preserves the ViewModel's role as a presentation abstraction.

---

## 1.3.6 Design for practical testability

Testability should be an observable property of the architecture, not an abstract claim.

A ViewModel should be constructable with minimal setup and verifiable through observable state changes. Tests should focus on externally visible behaviour rather than implementation details or internal method calls.

Heavy dependency injection, broad service interfaces, and tightly coupled infrastructure increase the amount of mocking needed before meaningful assertions can be made. That produces brittle tests that are hard to read and expensive to maintain.

Well-structured ViewModels require only the dependencies directly relevant to the behaviour under test, which keeps tests small, focused, and resilient to refactoring.

Tests should stay easy to write and maintain as the application grows.

---

# 1.4 Design goals

The following constraints are treated as architectural invariants throughout this document. Violating any one introduces special cases that weaken the consistency of the application structure over time.

- Every View is bound to exactly one ViewModel.
- ViewModels have no knowledge of Views or how they are constructed.
- Each ViewModel holds only the dependencies it directly uses.
- Nothing creates its own dependencies: all construction is performed externally.
- All object construction and wiring exists in a single composition root.
- The composition root is the complete map of screens, workflows, and transitions within the application.
- Presentation concerns remain isolated from domain and application logic.
- Application workflows are expressed independently from UI implementation details.
