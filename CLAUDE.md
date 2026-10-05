# CLAUDE.md — Architecture & Coding Standards

This document establishes the architectural standards, development rules, and workflow commands for the **Retrofit_API_Swagger** IntelliJ / Android Studio plugin. Both Claude and Antigravity agents strictly adhere to these rules.

### Modular Rule Files (`.agents/rules/`)
The rules have been split into standalone, modular files under `.agents/rules/` so you can apply or toggle only what is needed:
| Rule File | Scope & Purpose | Size |
| :--- | :--- | :--- |
| [clean-architecture.md](file:///Users/georgenady/IdeaProjects/Retrofit_API_Swagger/.agents/rules/clean-architecture.md) | Multi-module boundaries (`:parser`, `:scanner`, `:plugin`, `webview-ui`), layer constraints (domain, data, presentation), zero-UI rule for `:parser`. | ~2.8 KB |
| [intellij-platform-sdk.md](file:///Users/georgenady/IdeaProjects/Retrofit_API_Swagger/.agents/rules/intellij-platform-sdk.md) | Native DI (`@Service`, `project.service<T>()`), Threading & PSI (`ReadAction`, `WriteCommandAction`), UI thread safety, ClassLoader hygiene. | ~3.0 KB |
| [solid-principles.md](file:///Users/georgenady/IdeaProjects/Retrofit_API_Swagger/.agents/rules/solid-principles.md) | Single Responsibility (SRP), Open/Closed (OCP), Liskov Substitution (LSP), Interface Segregation (ISP), Dependency Inversion (DIP). | ~2.5 KB |
| [code-quality-and-hygiene.md](file:///Users/georgenady/IdeaProjects/Retrofit_API_Swagger/.agents/rules/code-quality-and-hygiene.md) | One Entity Per File, submodule `internal` encapsulation in `impl/`, immutability & YAGNI. | ~1.4 KB |
| [react-architecture.md](file:///Users/georgenady/IdeaProjects/Retrofit_API_Swagger/.agents/rules/react-architecture.md) | Zero-business-logic pure UI components, layered building blocks & screens, custom hooks for logic, One Component Per File. | ~3.0 KB |
| [jcef-webview-bridge.md](file:///Users/georgenady/IdeaProjects/Retrofit_API_Swagger/.agents/rules/jcef-webview-bridge.md) | `KotlinBridge.js` single gateway, unidirectional `StateFlow` synchronization (`window.updateGraphData`), event standards, dark/light theme support. | ~1.5 KB |
| [testing-and-verification.md](file:///Users/georgenady/IdeaProjects/Retrofit_API_Swagger/.agents/rules/testing-and-verification.md) | Automated unit testing standards for use cases & parsers, regression prevention, standard workflow commands. | ~1.1 KB |

---

## 1. Project Overview & Multi-Module Architecture

The project is an IntelliJ Platform / Android Studio plugin with a hybrid Kotlin + JCEF (React/Vite) stack.

```
┌────────────────────────────────────────────────────────┐
│                      :plugin                           │
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

### Module Responsibilities:
1. **`:parser`**: Core domain entities (`ApiNode`, `ParameterDetail`, `AnnotationDetail`) and Kotlin/Java PSI endpoint parsers. **Must have zero UI/Swing/JCEF dependencies.**
2. **`:scanner`**: File system candidate discovery, cache filtering, and file type classification.
3. **`:plugin`**: Plugin integration layer containing `domain` use cases, `data` repository implementations/settings, and `presentation` (JCEF bridges, ViewModels, UI panels).
4. **`webview-ui`**: React application loaded inside the JCEF Chromium browser, communicating exclusively via `KotlinBridge.js`.

---

## 2. Clean Architecture Rules

### Unidirectional Dependency Rule
- **Presentation** depends on **Domain**.
- **Data** implements contracts defined in **Domain**.
- **Domain** contains business logic (Use Cases, Models) and **never** references Presentation, Swing, JCEF, or concrete Data implementations.

### Layer Structure in `:plugin`:
```text
com.github.georgenady.retrofitApiSwagger/
├── domain/
│   ├── model/               # Pure business models and enums
│   ├── repository/          # Repository contracts (interfaces)
│   ├── usecase/             # Single-responsibility use cases (operator fun invoke)
│   └── edgeaction/          # Edge action domain abstractions and engines
├── data/
│   ├── repository/          # Concrete ApiRepositoryImpl
│   ├── service/             # PersistentStateComponent services (SwaggerSettings, EdgeActionSettings)
│   └── mapper/              # Mappers between AST/PSI and domain models
└── presentation/
    ├── main/                # MainToolViewModel, ToolWindowFactory, StateFlow UI State
    ├── panels/
    │   ├── graphPanel/      # ReactGraphPanel, GraphJavascriptBridge
    │   └── swaggerPanel/    # SwaggerPanel, SwaggerJavascriptBridge
    ├── editor/              # DataSourceDesignEditor (Split editor mode)
    └── theme/               # Method colors and IDE theme mapping
```

### Use Case Convention
- Each use case must have a single public responsibility, exposed via `operator fun invoke(...)`.
- Example: `ScanProjectEndpointsUseCase`, `FilterEndpointsUseCase`, `CreateOrUpdateApiUseCase`, `ExecuteHttpRequestUseCase`.

---

## 3. IntelliJ Platform SDK & Concurrency Rules

### Concurrency & Thread Safety
1. **Never block the UI Thread**: Expensive operations (file scanning, AST parsing, network calls) must run on background coroutines (`Dispatchers.Default` / `Dispatchers.IO`).
2. **PSI Read Operations**: Accessing PSI trees, `PsiElement`, or `PsiShortNamesCache` requires a read action:
   ```kotlin
   ReadAction.run<Throwable> { ... }
   // or
   ReadAction.compute<T, Throwable> { ... }
   ```
3. **PSI Write Operations**: Any source code modification, file creation, or annotation injection must be wrapped in a write command:
   ```kotlin
   WriteCommandAction.runWriteCommandAction(project, "Action Name", "GroupID", Runnable {
       // PSI mutation via KtPsiFactory
   })
   ```
4. **JCEF IPC Queries (`JBCefJSQuery`)**: Handlers execute on CEF's background IO thread. Never manipulate Swing components or PSI directly inside a raw handler; dispatch using `viewModelScope` or `ApplicationManager.getApplication().invokeLater { ... }`.

### Native Dependency Injection (DI) & Services
1. **Platform Service Container**:
   - Use `@Service(Service.Level.PROJECT)` / `<projectService>` for project services and `@Service(Service.Level.APP)` for application services.
   - Access via `project.service<T>()` or `service<T>()`.
   - Project services inject `Project` and `CoroutineScope` via constructor.
2. **Extension Points (EPs)**:
   - For extensible behaviors, use `<extensionPoints>` and `ExtensionPointName` instead of custom service locators or reflection.
3. **No External DI Libraries**:
   - Never use Dagger, Guice, Koin, Spring, or Hilt. They cause ClassLoader leaks during dynamic plugin unloading.
4. **No Deprecated Components**:
   - Avoid `ProjectComponent` and `ApplicationComponent`; use services and listeners.

### ClassLoader Hygiene (No Colliding 3rd-Party Libraries)
- **Do not bundle external template engines** (e.g. standalone `velocity-engine-core`, `freemarker`). The IntelliJ platform classloader already loads internal versions, causing fatal `ResourceManager` linkage collisions.
- Use pure Kotlin string/regex token evaluation (`TemplateEvaluationEngine`) or native platform mechanisms.

---

## 4. JCEF & Webview Architecture

1. **Single Bridge Gateway**: All communication from React to Kotlin must pass through `webview-ui/src/api/KotlinBridge.js`.
2. **Immutable UI State Pushes**:
   - The Kotlin backend holds the source of truth in `MainToolViewModel.uiState` (`StateFlow<MainToolUiState>`).
   - The UI state is serialized via `toGraphPayload()` and pushed to JavaScript via `window.updateGraphData(jsonPayload, isDark)`.
3. **Event Separation**:
   - Navigation: `navigateToSource`, `navigateToType`
   - Node Interaction: `nodeSelected`, `executeApiCall`
   - Edge Actions: `executeEdgeAction` (routing through `EdgeActionExecutor`)
   - View Switching: `switchViewMode`

---

## 5. React.js Component Architecture

1. **Zero-Business-Logic Reusable UI Primitives**:
   - Components in `components/ui/` (buttons, inputs, modals, badges) must have **zero** domain business logic and **zero** knowledge of `KotlinBridge.js`.
   - Built on pure abstractions: props-in, events-out.
2. **Layered Component Composition**:
   - **Screens / Containers** (e.g., `App.jsx`): Orchestrate top-level state, custom hooks, and IPC bridge calls.
   - **Building Blocks / Features** (e.g., `ApiNode`, `EdgeActionDialog`): Translate domain data into generic UI props by composing primitives.
   - **Pure UI Primitives** (e.g., `Modal`, `Button`, `Input`): 100% agnostic and reusable across screens and projects.
3. **Business Logic in Custom Hooks**:
   - Extract state orchestration, graph configuration (Cytoscape), and filtering into custom hooks (`src/hooks/`).
4. **One Component Per File & Colocated Styles**:
   - Every component has its dedicated `.jsx` file with companion `.css` in its folder.

---

## 6. Coding & Style Conventions

1. **One Entity Per File**: Every class, interface, enum, and component must have its own dedicated file matching the entity name.
2. **Immutability by Default**:
   - Domain models must be immutable `data class` definitions with `val` properties.
   - UI State must be immutable and updated via `.update { it.copy(...) }`.
3. **YAGNI & Simplicity**:
   - Write the simplest working implementation.
   - Deletion over addition. Remove dead or obsolete code when introducing replacements.
4. **Unit Testing**:
   - Every domain use case, parser rule, and template evaluator must have runnable JUnit tests.

---

## 7. Build, Test & Deployment Commands

```bash
# Run unit tests across all modules
./gradlew test

# Build the React webview bundle
cd webview-ui && npm run build && cd ..

# Build the complete IntelliJ plugin distribution zip
./gradlew buildPlugin

# Deploy to local Android Studio installation
rm -rf "$HOME/Library/Application Support/Google/AndroidStudio2026.1.2/plugins/Retrofit_API_Swagger"
unzip -q build/distributions/Retrofit_API_Swagger-1.2.0.zip -d "$HOME/Library/Application Support/Google/AndroidStudio2026.1.2/plugins"
```
