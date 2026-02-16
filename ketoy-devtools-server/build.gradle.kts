plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("com.developerstring.ketoy.devserver.MainKt")
    applicationDefaultJvmArgs = listOf(
        "-Duser.dir=${rootProject.projectDir.absolutePath}"
    )
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.java-websocket:Java-WebSocket:1.5.7")
}
