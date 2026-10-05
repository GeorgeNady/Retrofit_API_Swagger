# IntelliJ Platform SDK & Concurrency Rules

This rule defines the threading, PSI access, and platform integration standards for **Retrofit_API_Swagger**.

---

## Concurrency & Threading

1. **Protect the UI / Main Thread**:
   - Never block the UI thread with I/O, file scanning, AST parsing, or network calls.
   - Dispatch background operations explicitly to `Dispatchers.Default` or `Dispatchers.IO`.

2. **PSI Read Operations**:
   - Any access to PSI trees, `PsiElement`, or `PsiShortNamesCache` must be wrapped in a Read Action:
     ```kotlin
     ReadAction.run<Throwable> { ... }
     // or
     ReadAction.compute<T, Throwable> { ... }
     ```

3. **PSI Write Operations**:
   - Any source code modification, file creation, or annotation injection must be wrapped in a write command:
     ```kotlin
     WriteCommandAction.runWriteCommandAction(project, "Action Name", "GroupID", Runnable {
         // PSI mutation via KtPsiFactory
     })
     ```

4. **JCEF IPC Handlers (`JBCefJSQuery`)**:
   - Handlers execute on CEF's background IO thread.
   - Never manipulate Swing components or PSI directly inside a raw handler; dispatch using `viewModelScope` or `ApplicationManager.getApplication().invokeLater { ... }`.

---

## IntelliJ Native Dependency Injection (DI)

1. **Use Platform Services for DI**:
   - Always rely on IntelliJ's built-in service container instead of external DI frameworks:
     - Project-level services: Annotate with `@Service(Service.Level.PROJECT)` or declare via `<projectService>` in `plugin.xml`.
     - Application-level services: Annotate with `@Service(Service.Level.APP)` or declare via `<applicationService>` in `plugin.xml`.
   - Retrieve service instances via Kotlin idioms:
     ```kotlin
     val myService = project.service<MyProjectService>()
     val appService = service<MyApplicationService>()
     ```

2. **Constructor Injection**:
   - Project-level services should inject dependencies via constructor:
     ```kotlin
     @Service(Service.Level.PROJECT)
     class MyProjectService(
         private val project: Project,
         private val coroutineScope: CoroutineScope
     )
     ```

3. **Pluggable Architecture via Extension Points (EPs)**:
   - For extensible components and actions, define native IntelliJ Extension Points (`<extensionPoints>` in `plugin.xml`) and retrieve via `ExtensionPointName<T>`.
   - Avoid ad-hoc singleton registries or service locators when Extension Points are the platform-native mechanism.

4. **Prohibition of Third-Party DI Frameworks**:
   - **Never** introduce Dagger, Guice, Koin, Spring, or Hilt into plugin modules.
   - Third-party DI containers trigger fatal ClassLoader leaks during dynamic plugin unloading/reloading and conflict with IDE sandbox lifecycles.

5. **No Deprecated Components**:
   - Do not use `ProjectComponent` or `ApplicationComponent` (deprecated in modern IntelliJ SDK). Use `@Service` and `ProjectActivity` / listeners instead.

---

## ClassLoader Hygiene

1. **No Third-Party Template Engines**:
   - Never bundle external template engines (e.g., standalone `org.apache.velocity` or `freemarker`) into plugin modules.
   - The IntelliJ platform classloader already loads internal versions, triggering fatal `instanceof` / `ResourceManager` linkage crashes at runtime.
   - Practice: Use pure Kotlin token evaluation (`Regex`, string interpolation) or native IntelliJ SDK live template mechanisms.

2. **Decommissioning Legacy Services**:
   - When replacing hardcoded logic with extensible systems (e.g., Edge Actions), fully delete obsolete service classes, `plugin.xml` entries, and ViewModel references. Do not leave dead fallback pathways.
