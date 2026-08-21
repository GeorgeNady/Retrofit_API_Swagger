import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
}

version = "1.1.7"

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation("org.tinyjee.jgraphx:jgraphx:3.4.1.3")

    intellijPlatform {
        // This base version covers both IntelliJ IDEA 2024.2.x and Android Studio Ladybug
        intellijIdeaCommunity("2024.2.1")

        // We only need Java and Kotlin PSI to parse Retrofit interfaces
        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("com.intellij.java")

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
