# 1. Introduction

This document describes a practical approach to implementing the Model-View-ViewModel (MVVM) pattern. It covers the core building blocks as well as navigation, view construction, and communication between application areas.

The sample code is written in Java with JavaFX as the chosen view technology however the patterns can be applied to any technology that provides a property binding system suitable for use with the MVVM pattern.

## 1.1 What is MVVM

The MVVM pattern divides an application into four layers with distinct, non-overlapping responsibilities:

- **Model:** Represents the application's core domain concepts. Encapsulates data, business logic, and validation rules. Has no knowledge of the UI or how data is fetched or persisted.
- **ViewModel:** An abstraction of a View. Exposes state as observable properties for the View to bind to, and provides methods the View calls in response to user input. Has no knowledge of how the View is rendered or where its data originates.
- **View:** Represents the UI. Binds to the ViewModel's observable properties so the display stays in sync with state, and delegates user interactions back to the ViewModel. Has no knowledge of domain logic or data sources.

The three-layer description of MVVM above is common but undersells the role of the service layer. This document treats services as a separate layer throughout.

- **Service:** The data access layer. Retrieves and persists Models on behalf of the rest of the application. Has no knowledge of the UI or ViewModel layer.

## 1.2 Why use MVVM

The primary benefit is a strict, one-way dependency graph: Views depend on ViewModels, ViewModels depend on use cases and services, services depend on nothing above them. This is enforced structurally — a ViewModel that holds no UI framework types cannot reach into the view layer regardless of developer intent.

This separation makes ViewModels directly testable. They contain no UI types and make no assumptions about presentation context, so they can be exercised in plain unit tests without launching a UI runtime.

The pattern scales predictably. An application can grow from a handful of screens to several dozen without the architecture changing shape — each screen follows the same structure. Adding a screen does not require modifying existing classes.

## 1.3 Common MVVM problems

MVVM is widely adopted but frequently misapplied. The problems described below are characteristic of naive implementations and become more acute as applications grow. The problems identified here provide context for the design decisions made throughout this document.

### 1.3.1 ViewModels with too many responsibilities

In a typical MVVM implementation the ViewModel accumulates responsibilities incrementally. Including but not limited to:

- Property change notifications.
- Calculated property updates.
- Input validation.
- Service calls.
- Data loading.
- Navigation management.
- ViewModel construction.

The class begins as a focused abstraction and becomes a god object. Decomposing it into smaller ViewModels is a partial remedy — sub-ViewModels often require the same service dependencies so the injection problem multiplies rather than diminishes.

### 1.3.2 Services injected directly into ViewModels

The standard response to ViewModel bloat is to inject service interfaces which introduces a set of compounding problems:

- A single large service interface with many methods is an Interface Segregation Principle violation. The ViewModel depends on methods it does not use and testing requires mocking the entire interface even when only one method is exercised.
- Splitting a service into multiple smaller interfaces increases the number of constructor arguments. A ViewModel with five injected interfaces is difficult to construct in tests and difficult to read in production.
- Either approach couples the ViewModel to service logic — even via an interface. This makes it difficult to reuse the ViewModel in a different context because the services it calls are baked into its contract.
- ViewModels should not know where their data comes from. A ViewModel that calls `orderService.save()` is making an assumption about the existence and shape of a save operation. That assumption should not live in presentation-layer code.

### 1.3.3 Navigation coupled to presentation

A common pattern is to inject a navigation or dialog service into a ViewModel so it can initiate transitions or prompt the user. The naming reveals the flaw: `IDialogService.showDialog()` couples a request for information to a specific presentation mechanism. If that dialog is later replaced by an inline panel, every ViewModel that called `showDialog()` requires modification. Presentation decisions are not the ViewModel's concern.

### 1.3.4 Inheritance used to share logic

A common response to repeated ViewModel logic is to push it into a base class. Inheritance is the wrong mechanism because it should be used to model 'is-a' relationships. Using it to share utility logic produces fragile hierarchies where a change to the base class has unpredictable effects on all subclasses and where subclasses are coupled to implementation details they did not choose.

### 1.3.5 Fat ViewModels from delegate commands

The delegate command pattern (where a ViewModel exposes an `ICommand` implemented as a delegate that calls back into the ViewModel) is a common source of bloat. The command logic lives in the ViewModel, the service dependencies needed to execute the command are injected into the ViewModel, and the ViewModel ends up holding everything. Each new command makes the ViewModel larger and its constructor longer.

### 1.3.6 Testability claimed but not demonstrated

MVVM is routinely justified on the grounds of testability, yet the injection patterns described above make tests expensive to write and maintain. A ViewModel with several injected interfaces requires substantial mock infrastructure before a single assertion can be made. The resulting tests are brittle. They are coupled to implementation details rather than observable behaviour and fail under refactoring that does not alter the contract. Genuine testability requires that ViewModels be constructable with minimal setup and verifiable by asserting property state directly.

In other cases, tests are omitted entirely from any discussion of the MVVM pattern apart from a passing mention.

## 1.4 Design goals

These are invariants, not guidelines. Violating any one introduces a special case that erodes the architecture over time.

- Every View is constructed with exactly one ViewModel.
- ViewModels have no knowledge of Views or how they are constructed.
- Each ViewModel holds only the dependencies it directly uses.
- Nothing creates its own dependencies — everything is injected through the constructor.
- All construction and wiring lives in a single composition root, which is the complete map of every screen and transition.
