import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform.module")
}

kotlin {
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_0)
        languageVersion.set(KotlinVersion.KOTLIN_2_0)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        androidStudio("2024.2.1.12")

        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.android")

        testFramework(TestFrameworkType.Platform)
    }
}
