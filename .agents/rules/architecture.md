# Software Architecture & Engineering Rules

A standardized set of architectural principles, engineering practices, and code hygiene rules applicable across software projects and platforms.

---

## 1. General Principles

### SOLID Principles Compliance
- **Single Responsibility (SRP)**: Each class, module, or function must have one reason to change and perform a single well-defined task.
- **Open/Closed (OCP)**: Software entities should be open for extension, but closed for modification. Favor composition, strategy patterns, and polymorphism over modifying existing tested logic.
- **Liskov Substitution (LSP)**: Subtypes must be substitutable for their base types without altering program correctness.
- **Interface Segregation (ISP)**: Prefer many client-specific, focused interfaces over a single bloated "god" interface.
- **Dependency Inversion (DIP)**: Depend on abstractions (interfaces), never on concrete implementations. High-level business logic must never depend directly on low-level infrastructure or UI details.

### One Entity Per File
- **Rule**: Every class, interface, enum, record, or sealed hierarchy must reside in its own dedicated source file matching the type name.
- **Reason**: Maximizes searchability, avoids monolithic files, minimizes git merge conflicts, and clarifies ownership.
- **Exception**: Trivial, tightly-scoped private helper classes or single-purpose data holders solely used by the parent class in the same file.

### Simplicity & YAGNI
- Write the simplest code that solves the current problem. Avoid speculative abstractions, unnecessary generic indirection, and premature optimization.
- Boring, readable code wins over clever, complex code.

---

## 2. Layered Architecture & Separation of Concerns

Organize the codebase into decoupled layers with strict, unidirectional dependency flow:

```text
[ Presentation / UI Layer ]
           ↓ depends on
     [ Domain Layer ]  ←  (Core: No external dependencies)
           ↑ implemented by
[ Data / Infrastructure Layer ]
```

### 1. Domain Layer (Core Business Logic)
- **Role**: Contains enterprise business rules, entities, value objects, and repository/service interfaces.
- **Dependency Rule**: **Zero external framework or UI dependencies**. This layer must be pure, portable, and independently testable without mock frameworks or platform emulators.
- **Key Components**:
  - `model/`: Immutable entities, value objects, and domain enums.
  - `repository/`: Abstract contracts/interfaces defining how data is accessed and persisted.
  - `usecase/` (or `interactor/`): Single-responsibility business use cases executing domain operations.

### 2. Data & Infrastructure Layer
- **Role**: Implements domain interfaces and interacts with the outside world (databases, network APIs, disk I/O, hardware, operating system, external SDKs).
- **Key Components**:
  - `repository/`: Concrete implementations of domain repository interfaces.
  - `datasource/` or `client/`: Low-level data fetchers, API clients, database DAOs, and third-party wrappers.
  - `dto/` or `mapper/`: Data Transfer Objects for external serialization and mappers converting DTOs to pure domain entities.

### 3. Presentation / UI Layer
- **Role**: Manages UI rendering, user interaction, input capture, and UI state presentation.
- **Key Components**:
  - `view/` or `ui/`: Visual components, layouts, screens, or panels.
  - `state/` or `viewmodel/`: State holders (ViewModels, Presenters, Controllers) that observe use cases and emit immutable UI state.
  - `components/`: Modular, reusable UI widgets and design system elements.

### 4. Core / Common Layer
- **Role**: Cross-cutting utilities, logging, shared constants, and configurations required across modules.
- Must remain lightweight and not become a dumping ground for unrelated helpers.

### 5. Single-Purpose Submodules Architecture (Helper / Feature Modules)
Small, single-purpose helper modules (e.g., parsers, analyzers, scanners) must follow a disciplined, self-contained architecture:
- **Structure**:
  ```text
  com.yourdomain.<module>/
  ├── model/                 // Public domain models, value objects, and data transfer types
  ├── utils/                 // Module-specific constants and helper utilities
  ├── <Module>Service.kt     // Public interface / contract defining the module's capability
  └── impl/                  // Internal implementations (hidden from consumers)
      ├── ConcreteService.kt // Marked 'internal class'
      └── SubWorker.kt       // Marked 'internal class'
  ```
- **Rules for Single-Purpose Modules**:
  - Expose **only** the contract interface and models to consuming modules.
  - All concrete implementations in `impl/` must be marked with the `internal` visibility modifier.
  - External consumers must interact strictly with the public interface obtained via Dependency Injection.

---

## 3. Dependency Injection & Inversion of Control

- **Program to Contracts**: Always accept interface dependencies rather than concrete implementations.
- **Constructor Injection**: Prefer constructor injection over field or service-locator injection. This guarantees explicit dependencies, immutability, and frictionless unit testing.
- **Platform DI Integration**: Leverage the platform's standard dependency management mechanism (e.g., IntelliJ Services/Extensions, Spring, Hilt, Koin, NestJS, Service Container) without letting framework annotations leak into pure domain entities.
- **No Global Mutable Singletons**: Avoid global mutable state. Singletons must be managed by the dependency container with thread-safe access.

### Modular DI & Descriptor Separation (`xi:include`)
When working in a multi-module architecture with platform descriptor files (e.g., IntelliJ Plugin Platform, modular XML/manifest configurations):
- **Self-Contained Module Descriptors**:
  - Each helper/utility submodule must define its own descriptor file in `src/main/resources/META-INF/<module>.xml`.
  - The submodule registers its own services, extensions, or listeners against its public contract:
    ```xml
    <idea-plugin>
        <extensions defaultExtensionNs="com.intellij">
            <projectService
                serviceInterface="com.yourdomain.module.ModuleService"
                serviceImplementation="com.yourdomain.module.impl.ConcreteServiceImpl"/>
        </extensions>
    </idea-plugin>
    ```
- **Executable Module Aggregation via `<xi:include>`**:
  - The main executable module (e.g., `:plugin`, `:app`) must **not** duplicate the submodule's service or extension declarations.
  - Instead, the main module links the submodule descriptor using standard `XInclude`:
    ```xml
    <idea-plugin xmlns:xi="http://www.w3.org/2001/XInclude">
        ...
        <xi:include href="<module>.xml"/>
        ...
    </idea-plugin>
    ```
- **Benefits**:
  - **Decoupled ownership**: Submodule configurations remain entirely within the submodule boundary.
  - **Implementation concealment**: The main module consumes `project.getService(ModuleService::class.java)` without ever referencing or depending on the `internal` implementation class.

---

## 4. Encapsulation & Visibility Modifiers

- **Principle of Least Privilege**: Default to the most restrictive access modifier available (`private` > `protected` / package-private > `internal` > `public`).
- **Encapsulate Implementation Details**:
  - Keep internal helpers, low-level parsers, network adapters, and intermediate state private or module-internal.
  - In submodules, all classes inside `impl/` must be marked `internal`.
  - Public exposure is strictly reserved for domain interfaces, use cases, domain models, and designated public API entry points.
- **Immutability by Default**:
  - Expose read-only state/collections (e.g., `StateFlow`, `Observable`, unmodifiable lists) to observers while keeping mutable state private.
  - Prefer immutable data structures (`data class`, `record`, `readonly` properties) for models and value objects.

---

## 5. Concurrency & Thread Safety

- **UI / Main Thread Protection**: Never block the main/UI thread with I/O, database access, heavy computations, or network requests.
- **Offloading**: Explicitly dispatch expensive operations to background thread pools or worker dispatchers.
- **Thread Safety**: Avoid shared mutable state. When concurrency is required, use immutable copies, atomic primitives, or proper synchronization/mutexes.
- **Cancellation & Resource Cleanup**: Every asynchronous task, subscription, or background job must support lifecycle cancellation to avoid memory leaks.

---

## 6. Documentation & Code Hygiene

### Self-Documenting Code
- Use intention-revealing names for variables, methods, and types. Avoid cryptic abbreviations.
- Functions should do one thing well and ideally fit on a single screen.

### Clean Documentation Comments
- **Rule**: Every public interface, public class, exported module, and non-obvious method must have documentation comments (KDoc / JSDoc / JavaDoc / Docstrings).
- **Focus on Intent**: Explain *what* the component does and *why* specific constraints exist, not simply restating what the code visibly does.
- **Style**: Short, simple, grammatically correct sentences.

---

## 7. Error Handling & Input Validation

- **Fail Fast & Validate at Boundaries**: Validate input parameters at external boundaries (API controllers, UI form inputs, file parsers). Do not propagate invalid state deep into the domain model.
- **Explicit Failure Modeling**: Prefer explicit result wrappers (e.g., `Result<T>`, sealed class hierarchies with `Success` / `Failure`) over silent failures or unhandled exception propagation.
- **No Catch-All Swallowing**: Never catch exceptions silently (empty `catch` blocks). Always log or handle errors with appropriate user-facing messages or recovery logic.

---

## 8. Testing & Quality Assurance

- **Unit Testing Core Logic**: All domain logic, use cases, business validation, and critical algorithms must be covered by automated unit tests.
- **Test at Interface Boundaries**: Mock or fake external dependencies (databases, network, platform SDKs) at their interface boundary.
- **Regression Checks**: Every bug fix should be accompanied by a test reproducing the original issue to prevent future regressions.
