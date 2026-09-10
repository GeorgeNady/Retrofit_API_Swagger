plugins {
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.jetbrains.intellij.platform") apply false
}

allprojects {
    group = providers.gradleProperty("group").getOrElse("com.github.georgenady.androidapigraph")
    version = providers.gradleProperty("version").getOrElse("1.1.9")
}

// Copy the assembled plugin archive to the root build/distributions directory for CI workflow compatibility
tasks.register<Copy>("buildPlugin") {
    group = "intellij"
    description = "Assembles plugin archive in :plugin and copies it to root build directory"
    dependsOn(":plugin:buildPlugin")
    from(project(":plugin").layout.buildDirectory.dir("distributions"))
    into(layout.buildDirectory.dir("distributions"))
}
