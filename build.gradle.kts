import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
}

version = "1.1.6"

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation("org.tinyjee.jgraphx:jgraphx:3.4.1.3")

    intellijPlatform {
        local("/Users/georgenady/Applications/Android Studio.app/Contents")
        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.android")
        testFramework(TestFrameworkType.Platform)
        pluginVerifier()
        zipSigner()
    }
}

tasks {
    patchPluginXml {
        // Set the minimum supported IDE version (e.g., 2024.1+)
        sinceBuild.set("241")

        // Bumping to 261.* or leaving it unset allows installation on 2026 builds
        untilBuild.set("261.*")

        // OPTIONAL: To completely disable the upper version limit so it works on any future Studio/IntelliJ update:
        // untilBuild.set(provider { null })
    }
}
