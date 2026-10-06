# Localization & Bundle Resource Rules

This rule enforces internationalization (i18n) and UI string hygiene across the plugin codebase.

---

## 1. Always Use `MyBundle.message(...)`
- **Zero Hardcoded User-Facing Strings**:
  - Never use hardcoded string literals for UI labels, action titles, descriptions, tooltips, buttons, status bar messages, dialogs, or notification text.
  - Always resolve user-facing strings via `MyBundle.message("key")` (or `MyBundle.message("key", param1, param2)`).
- **Centralized String Definitions**:
  - All localized keys and values must reside in `plugin/src/main/resources/messages/MyBundle.properties`.
  - Keys must follow clear semantic namespaces, e.g.:
    - `action.<action_name>.text` and `action.<action_name>.description` for `AnAction` items
    - `dashboard.<key>` for main dashboard and status bar messages
    - `details.<key>` for side panel details section
    - `filter.<key>` for search and filtering controls
    - `empty.<key>` for empty state views
    - `message.<key>` for user feedback, toasts, or copied indicators

---

## 2. Action Constructors Standard
- Every `AnAction` subclass that defines text and description in its constructor must supply them via `MyBundle`:
  ```kotlin
  class RefreshAction : AnAction(
      MyBundle.message("action.refresh.text"),
      MyBundle.message("action.refresh.description"),
      AllIcons.Actions.Refresh
  )
  ```
- Do not use hardcoded `Supplier { "..." }` or raw `"..."` string literals in action titles/descriptions.

---

## 3. Dynamic Values and Formatting
- Use parameterized bundle keys with `{0}`, `{1}`, etc. rather than manual string concatenation:
  - In `MyBundle.properties`: `dashboard.found_endpoints=Found {0} endpoints in {1} ms.`
  - In Kotlin: `MyBundle.message("dashboard.found_endpoints", count, duration)`
