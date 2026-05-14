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
