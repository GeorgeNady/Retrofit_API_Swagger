import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
}

kotlin {
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_0)
        languageVersion.set(KotlinVersion.KOTLIN_2_0)

        // Instructs Kotlin to use native JVM 8 default interface methods
        // instead of generating synthetic bridge methods that trigger the verifier.
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

dependencies {
    implementation(project(":parser"))
    implementation(project(":scanner"))
    implementation(project(":utils"))
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        pluginComposedModule(project(":parser"))
        pluginComposedModule(project(":scanner"))
        pluginComposedModule(project(":utils"))

        androidStudio("2024.2.1.12")
        jetbrainsRuntime()

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
    // --- Webview Build Automation ---
    val webviewDir = rootProject.file("webview-ui")
    val webviewDistDir = webviewDir.resolve("dist")
    val webviewTargetDir = file("src/main/resources/webview")

    val npmInstall by registering(Exec::class) {
        workingDir = webviewDir
        group = "build"
        description = "Installs npm dependencies"
        if (org.apache.tools.ant.taskdefs.condition.Os.isFamily(org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS)) {
            commandLine("cmd", "/c", "npm install")
        } else {
            commandLine("bash", "-c", "npm install")
        }
        inputs.file(webviewDir.resolve("package.json"))
        outputs.dir(webviewDir.resolve("node_modules"))
    }

    val buildWebview by registering(Exec::class) {
        dependsOn(npmInstall)
        workingDir = webviewDir
        group = "build"
        description = "Builds the React webview application"
        
        if (org.apache.tools.ant.taskdefs.condition.Os.isFamily(org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS)) {
            commandLine("cmd", "/c", "npm run build")
        } else {
            commandLine("bash", "-c", "npm run build")
        }
        
        inputs.dir(webviewDir.resolve("src"))
        inputs.file(webviewDir.resolve("package.json"))
        outputs.dir(webviewDistDir)
    }

    val copyWebview by registering(Copy::class) {
        dependsOn(buildWebview)
        from(webviewDistDir)
        into(webviewTargetDir)
    }

    processResources {
        dependsOn(copyWebview)
    }

    patchPluginXml {
        sinceBuild.set("242") // Matches 2024.2
        untilBuild.set(provider { null }) // Open-ended for all future releases
    }
    buildPlugin {
        archiveBaseName.set("Retrofit_API_Swagger")
    }
}

