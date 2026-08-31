# Retrofit API Swagger

![Build](https://github.com/GeorgeNady/Retrofit_API_Graph/workflows/Build/badge.svg)

**Retrofit API Swagger** is a high-performance visual toolkit for Android developers to visualize, manage, and test Retrofit API definitions. It provides a visual "Design Mode" similar to the Jetpack Navigation Editor, directly inside your favorite IDE.

## 🚀 Key Features

- **Reactive Architecture**: Built on SOLID principles for maximum stability and performance. No UI freezes, even with hundreds of endpoints.
- **Split Editor (Design View)**: Open any Retrofit interface and see a visual representation of your endpoints side-by-side with your code.
- **Interactive Graph**: Visualize service-to-endpoint relationships with a draggable and zoomable canvas.
- **Deep Inspection**: Automatic extraction of annotations, parameters, and return types.
- **Direct Navigation**: Use gutter icons next to your Retrofit methods to jump instantly to the visual editor.
- **Native IDE Integration**: Real-time background scanning progress in the IDE status bar.
- **Request Testing**: Built-in "Try It Out" functionality to test endpoints with auto-generated JSON mocks.

## 📖 How to Use

1. **Open Tool Window**: Find the **Retrofit API Swagger** tab on the right side of your IDE to see the project-wide dashboard.
2. **Design View**: Open a Retrofit interface file. Click the **Design** tab at the top of the editor to enter the split view.
3. **Quick Navigation**: Click the executable icon in the gutter next to any Retrofit method to jump directly to its Design view.
4. **Interactive Graph**: Switch to **Graph Mode** in the tool window to see a high-level architectural view of your network layer.
5. **Search & Filter**: Use the side panel to filter by HTTP method, module, or specific annotations.

## 🛠️ Installation

- Using the IDE built-in plugin system:
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Retrofit API Swagger"</kbd> > <kbd>Install</kbd>

- Manually:
  Download the [latest release](https://github.com/GeorgeNady/Retrofit_API_Graph/releases/latest) and install using <kbd>Install plugin from disk...</kbd>

## 🔒 Privacy Policy

Your privacy is important to us. **Retrofit API Swagger** operates entirely locally on your machine and does not collect, store, or transmit any personal data or source code. Read our full [Privacy Policy](PRIVACY.md).

## 📄 License

Copyright © 2026 George Nady. All Rights Reserved.  
This software is provided under a **Proprietary and Non-Commercial Source License**. Copying, redistribution, commercial use, trading, reselling, or making any profit from this software is strictly prohibited. See [LICENSE](LICENSE) for details.

---
Plugin based on the [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template).
