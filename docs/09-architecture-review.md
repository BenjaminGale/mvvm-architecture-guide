
## 9. Architecture review

This section revisits the design goals and common problems identified in the introduction, mapping each directly to the structural decisions made throughout this document.

## Contents

- [9.1 How design goals are met](#91-how-design-goals-are-met)
- [9.2 How common problems are addressed](#92-how-common-problems-are-addressed)
  - [9.2.1 ViewModels with too many responsibilities](#921-viewmodels-with-too-many-responsibilities)
  - [9.2.2 Services injected directly into ViewModels](#922-services-injected-directly-into-viewmodels)
  - [9.2.3 Navigation coupled to presentation](#923-navigation-coupled-to-presentation)
  - [9.2.4 Inheritance used to share logic](#924-inheritance-used-to-share-logic)
  - [9.2.5 Fat ViewModels from delegate commands](#925-fat-viewmodels-from-delegate-commands)
  - [9.2.6 Testability claimed but not demonstrated](#926-testability-claimed-but-not-demonstrated)

### 9.1 How design goals are met

Section 1.4 defined six design goals for this architecture. Each is addressed directly by a structural decision described in this document:

- **Every View is constructed with exactly one ViewModel** — enforced by the View interface and the ViewLocator, both of which accept a single typed ViewModel.
- **ViewModels have no knowledge of views or how they are constructed** — the ViewLocator mapping lives entirely in the view layer; ViewModels hold only callbacks.
- **Each ViewModel holds only dependencies it directly uses** — navigation callbacks remove the need to pass dependencies through to child ViewModels. The composition root handles all construction.
- **Nothing creates its own dependencies** — services, context objects, use cases, and navigation callbacks are all injected via constructors. There is no `new` inside a ViewModel.
- **Navigation callbacks are injected at construction time** — ViewModels call callbacks and know nothing about what follows. The ViewRouter is never referenced in the ViewModel layer.
- **All construction and wiring lives in the composition root** — factory methods in `App` are the sole place where services, use cases, and callbacks are assembled. Reading it gives a complete map of every screen and transition.

### 9.2 How common problems are addressed

Section 1.3 described the problems that recur in MVVM implementations. This section maps each to the design decision that addresses it.

#### 8.2.1 ViewModels with too many responsibilities

ViewModels in this architecture are state holders and coordinators. They expose observable properties and invoke callbacks. Execution logic belongs to use case objects; section-level state and validation belongs to sub-ViewModels; navigation wiring belongs to the composition root. There is no place in the design where unrelated logic can accumulate in a ViewModel.

#### 8.2.2 Services injected directly into ViewModels

> **Note: this section is out of date and needs updating.**

No services are injected into ViewModels. A ViewModel receives use case objects and callbacks; a use case takes only the service functionality it requires. The ViewModel has no knowledge of whether data originates from a database, a remote API, or a test stub. ISP violations do not arise because the ViewModel holds no service interface. Tests construct the ViewModel with lightweight dependencies and assert state directly.

#### 8.2.3 Navigation coupled to presentation

The ViewRouter carries no presentation intent — one method, no knowledge of how a ViewModel will appear. ViewModels express navigation through callbacks that convey intent without prescribing presentation. If a modal dialog is later replaced by an inline panel, no ViewModel is modified; only the view responsible for that ViewModel type changes.

#### 8.2.4 Inheritance used to share logic

The `View` interface enforces construction ordering through convention rather than inheritance — views fully initialise themselves in the constructor. Views do not share a base class. ViewModels have no base class either. Shared state is held in context objects, not extracted into common parents. There is no inheritance hierarchy that can become fragile.

#### 8.2.5 Fat ViewModels from delegate commands

> **Note: this section is out of date and needs updating.**

Use cases replace delegate commands. Each is a discrete class with its own dependencies, independently constructable and testable. The ViewModel delegates rather than housing command logic. Adding an operation means adding a use case class; the ViewModel acquires one new constructor argument.

#### 8.2.6 Testability claimed but not demonstrated

A ViewModel in this architecture requires minimal test setup. Use case objects and callbacks can be supplied as lambdas. A test constructs the ViewModel, invokes a method, and asserts the resulting property state. No service interfaces require mocking; no UI runtime needs to be launched. The testing examples in section 8 illustrate this concretely.
