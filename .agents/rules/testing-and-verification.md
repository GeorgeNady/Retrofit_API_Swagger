# Testing & Verification Rules

This rule establishes testing requirements, automated verification workflows, and build commands for **Retrofit_API_Swagger**.

---

## 1. Testing Standards
- **Core Logic Coverage**: All domain use cases, endpoint parsers, and template evaluation engines must be covered by automated unit tests.
- **Interface Boundary Testing**: Mock or fake external dependencies at interface boundaries; avoid mocking platform internals.
- **Regression Protection**: Every bug fix must include an automated JUnit test reproducing the scenario to ensure regression prevention.

---

## 2. Standard Workflow Commands

```bash
# 1. Run all unit tests across all Gradle modules
./gradlew test

# 2. Build the React webview bundle
cd webview-ui && npm run build && cd ..

# 3. Package the complete IntelliJ plugin distribution
./gradlew buildPlugin

# 4. Deploy to local Android Studio installation
rm -rf "$HOME/Library/Application Support/Google/AndroidStudio2026.1.2/plugins/Retrofit_API_Swagger"
unzip -q build/distributions/Retrofit_API_Swagger-1.1.10.zip -d "$HOME/Library/Application Support/Google/AndroidStudio2026.1.2/plugins"
```
