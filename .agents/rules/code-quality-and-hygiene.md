# Code Quality & Hygiene Rules

This rule enforces code quality standards, visibility rules, and naming conventions for **Retrofit_API_Swagger**.

---

## 1. One Entity Per File
- Every class, interface, enum, record, or sealed hierarchy must reside in its own dedicated source file matching the type name.
- Exception: Trivial, tightly-scoped private helpers solely used within that single file.

---

## 2. Submodule Encapsulation (`internal` by default)
In single-purpose submodules (`:parser`, `:scanner`):
```text
com.github.georgenady.<module>/
├── model/                 // Public domain models and value objects
├── utils/                 // Constants and helper utilities
├── <Module>Service.kt     // Public interface / contract
└── impl/                  // Internal implementations (marked 'internal class')
```
- Expose **only** the contract interface and models to external consumers.
- All implementation classes inside `impl/` must be marked with the `internal` visibility modifier.

---

## 3. Immutability & Simplicity
- **Immutable by Default**: Domain models must be immutable `data class` definitions with `val` properties. State holders must expose read-only state (e.g. `StateFlow`).
- **Simplicity & YAGNI**: Write the minimal code that solves the task. Deletion over addition. Avoid speculative abstractions.
