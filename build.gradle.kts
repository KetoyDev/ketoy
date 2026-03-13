// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    id("org.jetbrains.kotlin.jvm") version "2.0.21" apply false
}

// All Ketoy tasks (cloud, export, dev server) are provided by the
// ketoy-gradle-plugin (id: "dev.ketoy.devtools"), applied in app/build.gradle.kts.
// Run:  ./gradlew ketoyPush  ./gradlew ketoyServe  ./gradlew ketoyDev  etc.
//            ./gradlew ketoyExport  ./gradlew ketoyExportProd