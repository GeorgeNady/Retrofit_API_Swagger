import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
}

version = "1.0.5"

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation("org.tinyjee.jgraphx:jgraphx:3.4.1.3")

    intellijPlatform {
        local("/Users/georgenady/Applications/Android Studio.app/Contents")
        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("org.jetbrains.android")
        pluginVerifier()
        zipSigner()
    }
}

tasks {
    buildPlugin {
        // The intellij-platform plugin automatically bundles 'implementation' dependencies into the ZIP distribution.
    }
}
