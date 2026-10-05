# SOLID Principles

This rule defines the application and enforcement of the SOLID design principles for **Retrofit_API_Swagger**.

---

## 1. Single Responsibility Principle (SRP)
- **One Reason to Change**: Every class, function, or component must have a single, well-defined responsibility.
- **Domain Use Cases**:
  - Each use case class encapsulates exactly one business action and exposes it via `operator fun invoke(...)`.
  - Avoid multi-action "manager" or "helper" classes with dozens of loosely-related methods.
- **Presentation Separation**:
  - UI panels and Swing components only render UI and capture user events.
  - ViewModels orchestrate UI state and delegate business operations to domain use cases or executors.
  - Parsers only transform AST/PSI into domain entities; they never perform UI rendering or network I/O.

---

## 2. Open/Closed Principle (OCP)
- **Open for Extension, Closed for Modification**: Code should allow adding new behavior without altering existing, tested code.
- **Strategy & Plugin Abstractions**:
  - Use interfaces and strategies for extensible features (e.g., `ApiEdgeAction`, code generation engines, candidate filter strategies).
  - Adding a new edge action or code generator must only require adding a new class implementing the interface, without modifying core dispatchers or existing action classes.
- **Avoid Fragile Type Switching**:
  - Favor polymorphism and registered strategies over hardcoded `when (type)` branching scattered across the codebase.

---

## 3. Liskov Substitution Principle (LSP)
- **Substitutability**: Subtypes and interface implementations must be completely substitutable for their base types without altering program correctness.
- **Contract Adherence**:
  - Implementations must honor all preconditions, postconditions, and exception contracts defined by the interface.
  - Never throw `UnsupportedOperationException` or `NotImplementedError` for methods declared in a public interface.
  - Avoid type checks (`is SubClass`) on interface references.

---

## 4. Interface Segregation Principle (ISP)
- **Focused Interfaces**: Clients must never be forced to depend on interfaces containing methods they do not use.
- **Decompose Large Interfaces**:
  - Split monolithic interfaces into smaller, cohesive, role-specific contracts.
  - Prefer fine-grained functional interfaces (`fun interface`) where a single callback or execution method suffices.

---

## 5. Dependency Inversion Principle (DIP)
- **Depend on Abstractions**: High-level modules (domain logic) must not depend on low-level modules (concrete Swing components, JCEF browser, file I/O). Both must depend on abstractions.
- **Abstraction Ownership**:
  - Repository interfaces (`ApiRepository`) and action contracts reside in the `domain` layer.
  - Concrete implementations (`ApiRepositoryImpl`, `EdgeActionSettingsService`) reside in the `data` layer.
  - Domain logic must never import classes from `presentation`, Swing, JCEF, or concrete `data` implementations.
