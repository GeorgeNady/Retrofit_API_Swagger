# React.js Architecture & Component Design Rules

This rule establishes component layering, reusability standards, and separation of concerns for the React / Vite frontend in **Retrofit_API_Swagger**.

---

## 1. Core Philosophy: Zero-Business-Logic Reusability

1. **Pure Presentation Over Business Coupling**:
   - Reusable UI components must **never** contain domain business logic or knowledge of backend IPC bridges (`KotlinBridge.js`).
   - Reusable components must be built on top of generic abstractions (props, slots, callbacks).
   - Any component intended for reuse across screens or features must operate strictly on **props-in, events-out** (e.g., `value`, `onChange`, `onAction`, `isOpen`, `onClose`).

2. **No Backend or Platform Leaks in UI Primitives**:
   - Never call `window.cefQuery` or `KotlinBridge.*` directly from reusable buttons, inputs, dialogs, or list items.
   - Event callbacks must be passed up to container/screen components that handle IPC interactions.

---

## 2. Component Layering Hierarchy

```text
┌────────────────────────────────────────────────────────┐
│               Screens / Views / Containers             │
│  (e.g., App.jsx, GraphView, ListView, SplitView)       │
│  • Manages top-level state and lifecycle               │
│  • Coordinates with KotlinBridge.js (IPC)              │
│  • Binds custom hooks and orchestrates layouts         │
└───────────────────────────┬────────────────────────────┘
                            │ composes
                            ▼
┌────────────────────────────────────────────────────────┐
│            Building Block / Feature Components         │
│  (e.g., ApiNode, ModernToolbar, EdgeActionDialog)       │
│  • Encapsulates specific domain concepts               │
│  • Translates domain models into generic UI props      │
│  • Built entirely by composing Layer 1 UI primitives   │
└───────────────────────────┬────────────────────────────┘
                            │ composes
                            ▼
┌────────────────────────────────────────────────────────┐
│            Pure Reusable UI Primitives                 │
│  (e.g., Modal, Button, SearchInput, Badge, Dropdown)   │
│  • 100% agnostic of Retrofit, PSI, AST, or graph logic │
│  • Reusable in any React project without modification   │
│  • Strictly driven by props and callbacks              │
└────────────────────────────────────────────────────────┘
```

---

## 3. Screen & Container Architecture

- **Screens as Orchestrators**:
  - Screen containers are smart components that compose building blocks.
  - Screens must not contain dense inline DOM soup or low-level CSS styling.
  - Complex UI sections within a screen must be broken down into cohesive building blocks following the same abstraction principles.

- **Business Logic in Custom Hooks**:
  - Complex state flows, event listeners, layout algorithms (e.g., Cytoscape initialization and edge wiring), and filter logic must be extracted into dedicated custom hooks in `src/hooks/` (e.g., `useCytoscape`, `useFilter`).
  - JSX remains a clean, declarative view representation of the hook's returned state and action callbacks.

---

## 4. Component Structure & Hygiene

1. **One Component Per File**:
   - Every component resides in its own dedicated file: `ComponentName.jsx`.
   - Colocate component-scoped styles: `ComponentName.css` in the same directory.
   - Example directory structure:
     ```text
     components/
     ├── ui/                    # Pure, reusable UI primitives (generic)
     │   ├── Button/
     │   │   ├── Button.jsx
     │   │   └── Button.css
     │   └── Modal/
     │       ├── Modal.jsx
     │       └── Modal.css
     └── features/              # Domain-aware building blocks
         ├── ApiNode/
         │   ├── ApiNode.jsx
         │   └── ApiNode.css
         └── EdgeActionDialog/
             ├── EdgeActionDialog.jsx
             └── EdgeActionDialog.css
     ```

2. **Immutable Props & Pure Rendering**:
   - Never mutate props directly.
   - Use `React.memo` or fine-grained component splitting to prevent unnecessary re-renders of heavy canvas/graph elements.

3. **Theme & Design Tokens**:
   - Styling must consume CSS variables (`var(--bg-primary)`, `var(--text-color)`, `var(--border-color)`) driven by the IDE's active theme.
   - No hardcoded hex colors for structural layout elements.
