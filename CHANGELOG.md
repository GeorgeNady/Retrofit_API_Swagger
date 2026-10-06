<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Retrofit API Swagger Changelog

## [1.2.0] - 2026-10-06
### Added
- **Latest Android Studio Support**: Full compatibility with the latest Android Studio versions (including Android Studio Rabbit 1 | 2026.2.1+ and IntelliJ Platform 2026.2+ modular architecture).
- **Custom Code Generation & Edge Actions**: Connect any two API nodes in the visual graph to trigger customizable code generation and annotations.
- **Edge Actions Settings UI**: Dedicated configuration page under *Settings > Tools > Edge Actions* to manage, edit, and create custom edge action templates and placement rules.
- **Built-in Response Viewer**: Inspect API responses directly inside the IDE with automatic syntax highlighting and format detection (JSON, XML, HTML, Plain Text).
- **Figma-Style Canvas Gestures**: Smooth touchpad gestures, trackpad pinch-to-zoom, and fluid canvas panning.
- **Enhanced Card List View**: Redesigned list items with clear function headers, HTTP method badges, API paths, and quick actions matching visual graph cards.
- **Fast Background Scanning**: Background endpoint scanning and smart caching ensure instant navigation without UI freezing.

### Fixed
- **Tool Window Loading ("Nothing to show")**: Resolved classloader isolation issues in Android Studio Rabbit 2026.2+ by linking the JCEF runtime module (`com.intellij.modules.jcef`).
- **Defensive UI & Error Recovery**: Added resilient fallback handling and retry mechanisms to prevent blank tool window states on unexpected runtime errors.
- **Node Connections**: Fixed node connection linking and edge generation behavior.

## [1.1.9] - 2026-08-29
### Added
- **Android Studio Compatibility**: Dedicated support and optimization for Android Studio.
- **Kotlin 2.0 & K2 Mode**: Full compatibility with Kotlin 2.0 and K2 mode.

### Fixed
- **Link Nodes**: Resolved inconsistencies in node connection behaviors.
- **Vertical List Layout**: Fixed a bug where vertical lists were displaying nodes in a grid layout instead of a proper list.

## [1.1.8] - 2026-08-23
### Added
- **Split Editor (Design View)**: View your Retrofit interfaces and visual API cards side-by-side in the editor.
- **Gutter Icon Navigation**: Enhanced gutter icons to jump directly to the Split Editor's Design view.
- **Native Status Bar Progress**: Real-time scanning progress now appears in the IDE status bar with file counts and cancellation support.
- **Instant UI Performance**: Optimized endpoint expansion and card navigation to eliminate UI freezes.

### Fixed
- **Reliable Expansion**: Fixed arrow-down toggle responsiveness and mouse interactions.
- **Accordion Jitter**: Smoothed out accordion-style expansion across UI panels.

## [1.1.6] - 2026-08-20
### Added
- **Unified Dashboard**: All-in-one interface with resizable side-by-side panels.
- **Interactive Graph**: Visual node-based architecture with service-to-endpoint mapping.
- **Draggable & Zoomable Canvas**: Free canvas navigation with a custom floating design toolbar (Hand pan, Zoom In/Out, Fit).
- **Deep Metadata**: Automatic collection of custom annotations (e.g., `@SupportCache`) and method parameters.
- **Search & Filters**: Instant live search by path or name, and quick-toggle filters for HTTP methods and modules.
- **View Switcher**: Jump between high-level architectural Graph mode and detailed searchable List mode.
- **i18n Support**: Full localized strings with descriptive emojis for a modern UI feel.
- **Smart Wrapping**: Intelligent multiline text wrapping in the details panel for long API signatures.

### Fixed
- **Multi-Module Discovery**: Enhanced scanner to reliably discover endpoints across multi-module projects.
- **Kotlin K2 Mode**: Support for Kotlin K2 mode in Android Studio.
- **Rendering Stability**: Fixed UI initialization and display stability across panels.

## [1.0.0] - 2026-08-19
### Added
- Initial plugin release.
- Retrofit annotation scanning.
- Dedicated IDE Tool Window.
