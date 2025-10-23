// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("org.sonarqube") version "4.4.1.3373"
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
