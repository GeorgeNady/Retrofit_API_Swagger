import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") // version "2.0.0" // Use your current Kotlin version
    id("org.jetbrains.intellij.platform") // version "2.18.1" // Use the latest 2.x release
    id("org.jetbrains.changelog")
}

version = "1.0.0-2"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation("com.github.vlsi.mxgraph:jgraphx:4.2.2")

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
//        androidStudio("2026.1.2.10")

        // Point directly to the Contents folder inside the Mac application bundle
        local("/Users/georgenady/Applications/Android Studio.app/Contents")

        // Add bundled plugins you need
        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("org.jetbrains.android")

//        instrumentationTools()
        pluginVerifier()
        zipSigner()
    }
}
