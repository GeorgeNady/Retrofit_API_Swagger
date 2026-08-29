import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
}

version = "1.1.9"

kotlin {
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_0)
        languageVersion.set(KotlinVersion.KOTLIN_2_0)

        // ADD THIS LINE:
        // Instructs Kotlin to use native JVM 8 default interface methods
        // instead of generating synthetic bridge methods that trigger the verifier.
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation("org.tinyjee.jgraphx:jgraphx:3.4.1.3")

    intellijPlatform {
        androidStudio("2024.2.1.12")

        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.android")

        testFramework(TestFrameworkType.Platform)
        pluginVerifier()
        zipSigner()
    }
}

intellijPlatform {
    pluginVerification {
        ides {
            // Automatically tells the verifier to ONLY test against
            // the Android Studio Ladybug version you defined above.
            current()
        }
    }
}

tasks {
    patchPluginXml {
        sinceBuild.set("242") // Matches 2024.2
        untilBuild.set(provider { null }) // Open-ended for all future releases
    }
}
