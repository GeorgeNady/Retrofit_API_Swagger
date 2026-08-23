<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Retrofit API Swagger Changelog

## [1.1.8] - 2026-08-23
### Added
- **Reactive UI Architecture**: Full refactoring to a decoupled reactive model (SOLID). Individual API cards now manage their own state, significantly improving stability.
- **Zero-Freeze Performance**: Optimized the rendering pipeline to eliminate UI thread blocking. Expanding or selecting endpoints is now instantaneous.
- **Split Editor Integration**: Introducing the **Design View**. View your Retrofit interfaces and visual API cards side-by-side in the editor.
- **Gutter Icon Navigation**: Enhanced gutter icons to jump directly to the Split Editor's Design view.
- **Native Progress Integration**: Real-time scanning progress now appears in the IDE status bar with file counts and cancellation support.
- **Lazy Data Loading**: Heavy JSON schema generation is now performed asynchronously only when a card is expanded.
- **Stable Signatures**: Implemented unique identity signatures for API nodes to ensure consistent state across background re-scans.

### Fixed
- **Reliable Expansion**: Fixed the "arrow down" toggle responsiveness by improving hit areas and switching to robust mouse-event handling.
- **Accordion Jitter**: Smoothed out the accordion-style expansion logic across multiple UI panels.

## [1.1.6] - 2026-08-20
### Added
- **Unified Dashboard**: All-in-one interface with resizable side-by-side panels.
- **Interactive Graph**: Visual node-based architecture with service-to-endpoint mapping.
- **Draggable & Zoomable**: Free canvas navigation with a custom floating design toolbar (Hand pan, Zoom In/Out, Fit).
- **Deep Metadata**: Automatic collection of custom annotations (e.g., `@SupportCache`) and method parameters.
- **Search & Filters**: Instant live search by path or name, and quick-toggle filters for HTTP methods and modules.
- **View Switcher**: Jump between high-level architectural Graph mode and detailed searchable List mode.
- **i18n Support**: Full string extraction to `MyBundle.properties` with descriptive emojis for a modern UI feel.
- **Smart Wrapping**: Intelligent multiline text wrapping in the details panel for long API signatures.

### Fixed
- **Multi-Module Discovery**: Overhauled scanner to use `FileTypeIndex` and manual brute-force walk for complex Android projects.
- **K2 Mode Compatibility**: Explicitly declared support for Kotlin K2 mode in Android Studio.
- **Rendering Fail-Safes**: Fixed "Nothing to show" issues by ensuring robust UI initialization and bundling visual libraries.
- **Horizontal Stability**: Prevented side panel from expanding horizontally due to long text.

## [1.0.0] - 2026-08-19
### Added
- Initial project scaffold.
- Basic Retrofit annotation scanning.
- Simple Tool Window registration.
