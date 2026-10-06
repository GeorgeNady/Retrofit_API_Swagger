# Clean Architecture & Module Boundaries

This rule establishes the multi-module Clean Architecture boundaries for **Retrofit_API_Swagger**.

---

## Multi-Module Hierarchy

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

---

## Module Constraints

1. **`:parser` Module**:
   - Contains domain entities (`ApiNode`, `ParameterDetail`, `AnnotationDetail`) and AST/PSI parsing logic.
   - **Zero UI Rule**: Must never import or depend on Swing, JCEF, or presentation classes.

2. **`:scanner` Module**:
   - Contains candidate file discovery, filtering, and caching.
   - Depends only on `:parser` and platform core.

3. **`:plugin` Module Layers**:
   - **`domain/`**: Pure business use cases (`operator fun invoke(...)`) and repository interfaces (`ApiRepository`). Zero UI/Swing imports.
   - **`data/`**: Implements repository interfaces, manages persistence (`PersistentStateComponent`), and handles platform I/O.
   - **`presentation/`**: `MainToolViewModel` (`StateFlow`), JCEF Chromium browser integration (`JBCefBrowser`), JavaScript bridges, and split editor panels.

4. **`webview-ui` Module**:
   - React 18 + Vite web application running inside JCEF.
   - Unidirectional data flow: Kotlin pushes state snapshots, React dispatches user actions back.
