# JCEF & Webview Bridge Architecture

This rule defines the communication and synchronization patterns between the IntelliJ Kotlin plugin and the React/Vite webview.

---

## 1. Single Bridge Gateway
- All frontend communication to Kotlin must route strictly through `webview-ui/src/api/KotlinBridge.js`.
- Never create ad-hoc global `window.cefQuery` calls scattered across React components.

---

## 2. Unidirectional Data Synchronization
- **Source of Truth**: The Kotlin backend holds the canonical state in `MainToolViewModel.uiState` (`StateFlow<MainToolUiState>`).
- **State Serialization**: The UI state is transformed via `toGraphPayload()` into a clean, JSON-serializable Map.
- **State Push**: Pushed to the webview via `window.updateGraphData(jsonPayload, isDark)`.
- React does not maintain duplicate out-of-sync copies of endpoint source data.

---

## 3. Bridge Event Standards
- **Navigation**: `navigateToSource(signature)`, `navigateToType(typeName, interfaceClassName)`
- **Interaction**: `nodeSelected(signature)`, `executeApiCall(signature, url, body)`
- **Edge Actions**: `executeEdgeAction(actionId, sourceSignature, targetSignature)`
- **View Mode**: `switchViewMode(mode)`
- **Data Editing**: `createOrUpdateApi(payload)`

---

## 4. UI Design & Aesthetics
- **Theme Awareness**: Dynamic dark/light mode synchronization using JetBrains IDE color schemes.
- **Aesthetic Excellence**: Clean monospaced typography for code/signatures (`JetBrains Mono`), subtle micro-animations, glassmorphism overlays, and zero jarring layout shifts.
