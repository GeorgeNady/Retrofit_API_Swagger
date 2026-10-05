# Clean Architecture & Engineering Rules

Standardized architectural principles, engineering practices, and code hygiene rules for **Retrofit_API_Swagger** across AI agents (Antigravity & Claude).

### Modular Rule Files (`.agents/rules/`)
The architecture rules are split into modular files under `.agents/rules/` so you can selectively apply or toggle rules per topic:
| Rule File | Topic | Size |
| :--- | :--- | :--- |
| [clean-architecture.md](file:///Users/georgenady/IdeaProjects/Retrofit_API_Swagger/.agents/rules/clean-architecture.md) | Multi-module boundaries (`:parser`, `:scanner`, `:plugin`, `webview-ui`), layer constraints (domain, data, presentation), zero-UI rule for `:parser`. | ~2.8 KB |
| [intellij-platform-sdk.md](file:///Users/georgenady/IdeaProjects/Retrofit_API_Swagger/.agents/rules/intellij-platform-sdk.md) | Native DI (`@Service`, `project.service<T>()`), Threading & PSI (`ReadAction`, `WriteCommandAction`), UI thread safety, ClassLoader hygiene. | ~3.0 KB |
| [solid-principles.md](file:///Users/georgenady/IdeaProjects/Retrofit_API_Swagger/.agents/rules/solid-principles.md) | Single Responsibility (SRP), Open/Closed (OCP), Liskov Substitution (LSP), Interface Segregation (ISP), Dependency Inversion (DIP). | ~2.5 KB |
| [code-quality-and-hygiene.md](file:///Users/georgenady/IdeaProjects/Retrofit_API_Swagger/.agents/rules/code-quality-and-hygiene.md) | One Entity Per File, submodule `internal` encapsulation in `impl/`, immutability & YAGNI. | ~1.4 KB |
| [react-architecture.md](file:///Users/georgenady/IdeaProjects/Retrofit_API_Swagger/.agents/rules/react-architecture.md) | Zero-business-logic pure UI components, layered building blocks & screens, custom hooks for logic, One Component Per File. | ~3.0 KB |
| [jcef-webview-bridge.md](file:///Users/georgenady/IdeaProjects/Retrofit_API_Swagger/.agents/rules/jcef-webview-bridge.md) | `KotlinBridge.js` single gateway, unidirectional `StateFlow` synchronization (`window.updateGraphData`), event standards, dark/light theme support. | ~1.5 KB |
| [testing-and-verification.md](file:///Users/georgenady/IdeaProjects/Retrofit_API_Swagger/.agents/rules/testing-and-verification.md) | Automated unit testing standards for use cases & parsers, regression prevention, standard workflow commands. | ~1.1 KB |

---

## 1. General Principles
- **SOLID Compliance**:
  - **SRP**: Single responsibility per class/function.
  - **OCP**: Open for extension, closed for modification.
  - **LSP**: Subtypes must be substitutable for their base types.
  - **ISP**: Small, focused interfaces over bloated god-interfaces.
  - **DIP**: High-level domain logic must depend on abstractions, never on low-level UI or concrete data details.
- **One Entity Per File**: Every class, interface, enum, and component resides in its own dedicated file matching its type name.
- **Simplicity & YAGNI**: Write the minimal code that solves the task. Deletion over addition. Avoid premature abstractions.

---

## 2. Multi-Module Clean Architecture

```text
┌────────────────────────────────────────────────────────┐
│                        :plugin                         │
│  ├── presentation/  (Swing, ToolWindow, JCEF, Bridges) │
│  ├── domain/        (UseCases, Repository Contracts)   │
│  ├── data/          (Repo Impls, Settings Services)    │
│  └── core/          (Configurables, System Setup)      │
└──────────────┬──────────────────────────┬──────────────┘
               │                          │
               ▼                          ▼
┌───────────────────────────┐ ┌──────────────────────────┐
│         :scanner          │ │         :parser          │
│ Candidate filtering &     │ │ AST/PSI parsers for      │
│ project file scanning     │ │ Retrofit & Ktorfit APIs  │
└───────────────────────────┘ └──────────────────────────┘
               ▲
               │
┌───────────────────────────┐
│       webview-ui/         │
│ React 18 + Vite frontend  │
│ (Cytoscape Graph + List)  │
└───────────────────────────┘
```

### Module Boundaries:
1. **`:parser`**: Pure AST/PSI parsing and domain models (`ApiNode`, `ParameterDetail`). **Zero UI, Swing, or JCEF dependencies.**
2. **`:scanner`**: File discovery and candidate filtering logic.
3. **`:plugin`**:
   - **`domain/`**: Pure business use cases (`operator fun invoke(...)`) and repository interfaces (`ApiRepository`). Zero UI/Swing imports.
   - **`data/`**: Repository implementations (`ApiRepositoryImpl`), persistent storage (`PersistentStateComponent`), and platform I/O.
   - **`presentation/`**: `MainToolViewModel` (`StateFlow`), JCEF Chromium browser integration (`JBCefBrowser`), JavaScript bridges, and split editor panels.
4. **`webview-ui`**: React 18 + Vite UI. Single gateway communication via `KotlinBridge.js`. Unidirectional state flow (Kotlin pushes state snapshots, React dispatches user actions).

---

## 3. Submodule Encapsulation (`internal` by default)
Single-purpose submodules (`:parser`, `:scanner`) must encapsulate implementation details:
```text
com.github.georgenady.<module>/
├── model/                 // Public domain models and value objects
├── utils/                 // Constants and helper utilities
├── <Module>Service.kt     // Public interface / contract
└── impl/                  // Internal implementations (marked 'internal class')
```
- Only expose domain interfaces and models to consumers.
- All classes inside `impl/` must be marked `internal`.

---

## 4. IntelliJ Platform SDK & Concurrency Rules

### Threading & PSI Operations:
- **UI / Main Thread Protection**: Never block the UI thread with I/O, file scanning, or network requests. Dispatch to `Dispatchers.Default` or `Dispatchers.IO`.
- **PSI Read Actions**: Accessing AST/PSI trees requires a read action: `ReadAction.run` or `ReadAction.compute`.
- **PSI Write Actions**: Any source code modification, file creation, or annotation injection must be wrapped in `WriteCommandAction.runWriteCommandAction(project) { ... }`.
- **JCEF IPC Queries (`JBCefJSQuery`)**: IPC handlers run on CEF background IO threads. Dispatch to `viewModelScope` or `ApplicationManager.getApplication().invokeLater { ... }` before touching UI or state.

### Native Dependency Injection (DI) & Services:
- **IntelliJ Service Container**: Use `@Service(Service.Level.PROJECT)` / `<projectService>` and `@Service(Service.Level.APP)` / `<applicationService>`. Access instances using `project.service<T>()` or `service<T>()`.
- **Constructor Injection**: Project-level services should inject `Project` and `CoroutineScope` via constructor parameters.
- **Extension Points**: For extensible architectures, declare `<extensionPoints>` and consume via `ExtensionPointName` instead of custom service locators or registries.
- **No Third-Party DI Frameworks**: Never bundle Dagger, Guice, Koin, Spring, or Hilt. They cause ClassLoader leaks during dynamic plugin unloading.
- **No Deprecated Components**: Avoid `ProjectComponent` and `ApplicationComponent`; use modern services and listeners.

### ClassLoader Hygiene (No 3rd-Party Template Engines):
- **Rule**: Never bundle external template engines (e.g., standalone `org.apache.velocity` or `freemarker`) into plugin modules. The IntelliJ platform classloader already loads internal versions, triggering fatal `instanceof` / `ResourceManager` linkage crashes.
- **Practice**: Use pure Kotlin token evaluation (`Regex`, string interpolation) or native platform live template mechanisms.
- **Decommission Legacy Services**: When replacing hardcoded logic with extensible systems (e.g., Edge Actions), fully delete obsolete service classes, `plugin.xml` entries, and ViewModel references.

---

## 5. React.js Component Architecture

- **Zero-Business-Logic Reusable UI Primitives**:
  - Reusable components (`components/ui/`: buttons, inputs, modals, badges) must have **zero** domain logic and **zero** knowledge of `KotlinBridge.js`.
  - Built on pure abstractions: props-in, events-out.
- **Layered Hierarchy**:
  - **Screens / Containers** (`App.jsx`, view modes): Orchestrate state, custom hooks, and IPC bridge calls.
  - **Building Blocks / Features** (`ApiNode`, `EdgeActionDialog`): Translate domain data into generic UI props by composing primitives.
  - **Pure UI Primitives** (`Modal`, `Button`, `Input`): 100% agnostic and reusable across screens and projects.
- **Business Logic in Custom Hooks**:
  - Extract state flows, Cytoscape graph setup, and search/filter logic into custom hooks (`src/hooks/`).
- **One Component Per File**:
  - Every component has its dedicated `.jsx` file with companion `.css` in its folder.

---

## 6. State Management & Data Flow

- **Single Source of Truth**: `MainToolViewModel` holds state in an immutable `StateFlow<MainToolUiState>`.
- **Immutable State Updates**: Mutate state exclusively via `_uiState.update { it.copy(...) }`.
- **JCEF Synchronization**: State updates serialize via `toGraphPayload()` and push to JavaScript via `window.updateGraphData(jsonPayload, isDark)`.
- **React Gateway**: All frontend requests flow strictly through `webview-ui/src/api/KotlinBridge.js`.

---

## 7. Testing & Quality Assurance

- **Unit Test Coverage**: All domain use cases, parsing logic, and template evaluators must have automated JUnit tests.
- **Interface Mocking**: Test at interface boundaries; avoid mocking platform internals.
- **Regression Protection**: Every bug fix must include an automated check preventing regressions.

---

## 8. Verification & Build Commands

```bash
# Backend unit tests
./gradlew test

# Frontend bundle build
cd webview-ui && npm run build && cd ..

# Plugin package distribution
./gradlew buildPlugin

# Deploy to local Android Studio
rm -rf "$HOME/Library/Application Support/Google/AndroidStudio2026.1.2/plugins/Retrofit_API_Swagger"
unzip -q build/distributions/Retrofit_API_Swagger-1.1.10.zip -d "$HOME/Library/Application Support/Google/AndroidStudio2026.1.2/plugins"
```
