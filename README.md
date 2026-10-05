# Ktorfit & Retrofit Studio

![Build](https://github.com/GeorgeNady/Retrofit_API_Graph/workflows/Build/badge.svg)

**Ktorfit & Retrofit Studio** is a high-performance visual toolkit and code generator for Android and Kotlin Multiplatform (KMP) developers to visualize, manage, and test both **Ktorfit** and **Retrofit** API definitions. It provides an interactive visual "Design Mode" similar to the Jetpack Navigation Editor, directly inside your favorite IDE.

## 🚀 Key Features

- **Ktorfit & Retrofit Dual Support**: First-class discovery and parsing for both Retrofit and Ktorfit HTTP interfaces across Android and Kotlin Multiplatform modules.
- **Custom Code Generation & Edge Actions**: Drag connections between API nodes in the graph to trigger customizable code templates and annotations. Configure custom templates under *Settings > Tools > Edge Actions*.
- **Interactive Graph**: Visualize service-to-endpoint relationships with a draggable and zoomable canvas.
- **Dual View Modes**: Seamlessly switch between the visual graph and a structured, searchable card list.
- **Split Editor (Design View)**: Open any API interface and inspect endpoints side-by-side with your code.
- **Deep Inspection**: Automatic extraction of annotations, parameters, return types, and request bodies.
- **Zero-Freeze Reactive Architecture**: Background scanning with persistent file-hash caching ensures instant interaction without blocking the UI thread.
- **Direct Gutter Navigation**: Jump directly from interface declarations to the visual editor using editor gutter icons.
- **Request Testing**: Built-in "Try It Out" functionality to execute requests and preview responses.

## 📖 How to Use

1. **Open Tool Window**: Find the **Ktorfit & Retrofit Studio** tab on the right side of your IDE to see the project-wide dashboard.
2. **Design View**: Open any Retrofit or Ktorfit interface file. Click the **Design** tab at the top of the editor to enter the split view.
3. **Quick Navigation**: Click the gutter icon next to any API method to jump directly to its Design view.
4. **Interactive Graph**: Switch to **Graph Mode** in the tool window to see a high-level architectural view of your network layer. Drag an arrow between two nodes to execute Edge Actions.
5. **Search & Filter**: Use the header search and filters to filter by HTTP method, path, or function name.

## 🛠️ Installation

- Using the IDE built-in plugin system:
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Ktorfit & Retrofit Studio"</kbd> > <kbd>Install</kbd>

- Manually:
  Download the [latest release](https://github.com/GeorgeNady/Retrofit_API_Graph/releases/latest) and install using <kbd>Install plugin from disk...</kbd>

## 🔒 Privacy Policy

Your privacy is important to us. **Ktorfit & Retrofit Studio** operates entirely locally on your machine and does not collect, store, or transmit any personal data or source code. Read our full [Privacy Policy](PRIVACY.md).

## 📄 License

Copyright © 2026 George Nady. All Rights Reserved.  
This software is provided under a **Proprietary and Non-Commercial Source License**. Copying, redistribution, commercial use, trading, reselling, or making any profit from this software is strictly prohibited. See [LICENSE](LICENSE) for details.
