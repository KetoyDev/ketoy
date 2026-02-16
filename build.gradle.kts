// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    id("org.jetbrains.kotlin.jvm") version "2.0.21" apply false
}

// ── Ketoy Cloud Config Helper ─────────────────────────────────────
// Reads KETOY_DEVELOPER_API_KEY and KETOY_BASE_URL from local.properties
fun loadKetoyCloudConfig(): Triple<String, String, String> {
    val localProps = java.util.Properties()
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        localProps.load(localPropsFile.inputStream())
    }

    val apiKey = localProps.getProperty("KETOY_DEVELOPER_API_KEY")
        ?: throw GradleException(
            """
            |
            | ✖ KETOY_DEVELOPER_API_KEY not found in local.properties
            |
            |   Add the following to your local.properties:
            |     KETOY_DEVELOPER_API_KEY=your_developer_api_key_here
            |     KETOY_BASE_URL=https://your-ketoy-server.com
            |     KETOY_PACKAGE_NAME=com.yourcompany.app
            |
            |   Get your developer API key by registering at your Ketoy server:
            |     curl -X POST <base_url>/api/developers/register \\
            |       -H "Content-Type: application/json" \\
            |       -d '{"email": "you@example.com", "name": "Your Name"}'
            |
            """.trimMargin()
        )

    val baseUrl = (localProps.getProperty("KETOY_BASE_URL") ?: "http://localhost:3000")
        .trimEnd('/')

    val packageName = localProps.getProperty("KETOY_PACKAGE_NAME")
        ?: throw GradleException(
            """
            |
            | ✖ KETOY_PACKAGE_NAME not found in local.properties
            |
            |   Add the following to your local.properties:
            |     KETOY_PACKAGE_NAME=com.yourcompany.app
            |
            """.trimMargin()
        )

    return Triple(apiKey, baseUrl, packageName)
}

// ── HTTP helper — plain JDK, no extra dependencies ────────────────
fun ketoyHttpRequest(
    method: String,
    url: String,
    apiKey: String,
    body: String? = null
): Pair<Int, String> {
    val conn = java.net.URI(url).toURL().openConnection() as java.net.HttpURLConnection
    conn.requestMethod = method
    conn.setRequestProperty("x-developer-api-key", apiKey)
    conn.setRequestProperty("Content-Type", "application/json")
    conn.connectTimeout = 15_000
    conn.readTimeout = 30_000

    if (body != null) {
        conn.doOutput = true
        conn.outputStream.bufferedWriter().use { it.write(body) }
    }

    val code = conn.responseCode
    val responseBody = try {
        conn.inputStream.bufferedReader().readText()
    } catch (_: Exception) {
        conn.errorStream?.bufferedReader()?.readText() ?: ""
    }
    return code to responseBody
}

// ══════════════════════════════════════════════════════════════════
//  ketoyPush — Upload a screen JSON to the Ketoy cloud server
// ══════════════════════════════════════════════════════════════════
// Usage:
//   ./gradlew ketoyPush -PscreenName=home -Pversion=1.0.0
//   ./gradlew ketoyPush -PscreenName=home -Pversion=1.0.0 -PdisplayName="Home Screen"
//   ./gradlew ketoyPush -PscreenName=home -Pversion=2.0.0 -Pdescription="Updated home" -Ptags=home,landing
tasks.register("ketoyPush") {
    group = "ketoy"
    description = "Upload a screen JSON from ketoy-screens/ to the Ketoy cloud server"

    doLast {
        val (apiKey, baseUrl, packageName) = loadKetoyCloudConfig()

        // ── Required: screenName ──────────────────────────────────
        val screenName = project.findProperty("screenName") as? String
            ?: throw GradleException(
                """
                |
                | ✖ Missing -PscreenName=<name>
                |
                | Usage:  ./gradlew ketoyPush -PscreenName=home -Pversion=1.0.0
                |
                | Available screens in ketoy-screens/:
                |   ${
                    rootProject.file("ketoy-screens").listFiles()
                        ?.filter { it.extension == "json" }
                        ?.joinToString(", ") { it.nameWithoutExtension }
                        ?: "(none found)"
                }
                |
                """.trimMargin()
            )

        // ── Locate the JSON file ──────────────────────────────────
        val jsonFile = rootProject.file("ketoy-screens/$screenName.json")
        if (!jsonFile.exists()) {
            val available = rootProject.file("ketoy-screens").listFiles()
                ?.filter { it.extension == "json" }
                ?.joinToString(", ") { it.nameWithoutExtension }
                ?: "(none)"
            throw GradleException(
                """
                |
                | ✖ Screen file not found: ketoy-screens/$screenName.json
                |
                | Available screens: $available
                |
                """.trimMargin()
            )
        }

        // ── Required: version ─────────────────────────────────────
        val version = project.findProperty("version") as? String
            ?: throw GradleException(
                """
                |
                | ✖ Missing -Pversion=<semver>
                |
                | Usage:  ./gradlew ketoyPush -PscreenName=$screenName -Pversion=1.0.0
                |
                """.trimMargin()
            )

        // ── Optional parameters ───────────────────────────────────
        val displayName = (project.findProperty("displayName") as? String)
            ?: screenName.replace("_", " ")
                .replaceFirstChar { it.uppercaseChar() }
        val description = (project.findProperty("description") as? String) ?: ""
        val category = (project.findProperty("category") as? String) ?: ""
        val tags = (project.findProperty("tags") as? String)
            ?.split(",")
            ?.map { "\"${it.trim()}\"" }
            ?.joinToString(",")

        // ── Read and escape JSON content ──────────────────────────
        // The API requires jsonContent as a JSON *string*, not an object
        val rawJson = jsonFile.readText()
        val escapedJson = rawJson
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

        // ── Build request body ────────────────────────────────────
        val metadataBlock = buildString {
            append("\"metadata\": {")
            val parts = mutableListOf<String>()
            if (category.isNotBlank()) parts.add("\"category\": \"$category\"")
            if (tags != null) parts.add("\"tags\": [$tags]")
            append(parts.joinToString(", "))
            append("}")
        }

        val requestBody = buildString {
            append("{")
            append("\"screenName\": \"$screenName\",")
            append("\"displayName\": \"$displayName\",")
            if (description.isNotBlank()) append("\"description\": \"$description\",")
            append("\"version\": \"$version\",")
            append("\"jsonContent\": \"$escapedJson\",")
            append(metadataBlock)
            append("}")
        }

        // ── Upload ────────────────────────────────────────────────
        val url = "$baseUrl/api/screens/$packageName/upload"
        println()
        println("╔════════════════════════════════════════════╗")
        println("║          Ketoy Screen Upload               ║")
        println("╚════════════════════════════════════════════╝")
        println("  Server:    $baseUrl")
        println("  Package:   $packageName")
        println("  Screen:    $screenName")
        println("  Version:   $version")
        println("  File:      ketoy-screens/$screenName.json")
        println("  File size: ${rawJson.length} bytes")
        println()
        println("  Uploading...")

        val (code, response) = ketoyHttpRequest("POST", url, apiKey, requestBody)

        if (code in 200..299) {
            println("  ✔ Upload successful! (HTTP $code)")
            println()
            println("  Response:")
            println("  $response")
        } else {
            println("  ✖ Upload failed (HTTP $code)")
            println()
            println("  Response:")
            println("  $response")
            throw GradleException("Screen upload failed with HTTP $code")
        }
        println()
    }
}

// ══════════════════════════════════════════════════════════════════
//  ketoyPushAll — Upload ALL screens from ketoy-screens/ at once
// ══════════════════════════════════════════════════════════════════
// Usage:
//   ./gradlew ketoyPushAll -Pversion=1.0.0
tasks.register("ketoyPushAll") {
    group = "ketoy"
    description = "Upload all screen JSONs from ketoy-screens/ to the Ketoy cloud server"

    doLast {
        val (apiKey, baseUrl, packageName) = loadKetoyCloudConfig()

        val version = project.findProperty("version") as? String
            ?: throw GradleException(
                """
                |
                | ✖ Missing -Pversion=<semver>
                |
                | Usage:  ./gradlew ketoyPushAll -Pversion=1.0.0
                |
                """.trimMargin()
            )

        val screensDir = rootProject.file("ketoy-screens")
        val jsonFiles = screensDir.listFiles()?.filter { it.extension == "json" } ?: emptyList()

        if (jsonFiles.isEmpty()) {
            throw GradleException("No JSON files found in ketoy-screens/")
        }

        println()
        println("╔════════════════════════════════════════════╗")
        println("║       Ketoy Batch Screen Upload            ║")
        println("╚════════════════════════════════════════════╝")
        println("  Server:   $baseUrl")
        println("  Package:  $packageName")
        println("  Version:  $version")
        println("  Screens:  ${jsonFiles.size} file(s)")
        println()

        var success = 0
        var failed = 0

        jsonFiles.forEach { file ->
            val screenName = file.nameWithoutExtension
            val rawJson = file.readText()
            val escapedJson = rawJson
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")

            val displayName = screenName.replace("_", " ")
                .replaceFirstChar { it.uppercaseChar() }

            val requestBody = buildString {
                append("{")
                append("\"screenName\": \"$screenName\",")
                append("\"displayName\": \"$displayName\",")
                append("\"version\": \"$version\",")
                append("\"jsonContent\": \"$escapedJson\"")
                append("}")
            }

            val url = "$baseUrl/api/screens/$packageName/upload"
            print("  ↑ $screenName ... ")

            try {
                val (code, response) = ketoyHttpRequest("POST", url, apiKey, requestBody)
                if (code in 200..299) {
                    println("✔ (HTTP $code)")
                    success++
                } else {
                    println("✖ (HTTP $code)")
                    println("    $response")
                    failed++
                }
            } catch (e: Exception) {
                println("✖ (${e.message})")
                failed++
            }
        }

        println()
        println("  Done: $success uploaded, $failed failed")
        println()

        if (failed > 0) {
            throw GradleException("$failed screen(s) failed to upload")
        }
    }
}

// ══════════════════════════════════════════════════════════════════
//  ketoyListScreens — List all screens on the server for this app
// ══════════════════════════════════════════════════════════════════
// Usage:
//   ./gradlew ketoyListScreens
tasks.register("ketoyListScreens") {
    group = "ketoy"
    description = "List all screens deployed on the Ketoy cloud server for this app"

    doLast {
        val (apiKey, baseUrl, packageName) = loadKetoyCloudConfig()
        val url = "$baseUrl/api/screens/$packageName"

        println()
        println("  Fetching screens for $packageName ...")
        println()

        val (code, response) = ketoyHttpRequest("GET", url, apiKey)

        if (code in 200..299) {
            println("  ✔ Screens (HTTP $code):")
            println()
            println("  $response")
        } else {
            println("  ✖ Failed (HTTP $code)")
            println("  $response")
        }
        println()
    }
}

// ══════════════════════════════════════════════════════════════════
//  ketoyScreenVersions — List all versions of a specific screen
// ══════════════════════════════════════════════════════════════════
// Usage:
//   ./gradlew ketoyScreenVersions -PscreenName=home
tasks.register("ketoyScreenVersions") {
    group = "ketoy"
    description = "List all versions of a screen on the Ketoy cloud server"

    doLast {
        val (apiKey, baseUrl, packageName) = loadKetoyCloudConfig()

        val screenName = project.findProperty("screenName") as? String
            ?: throw GradleException(
                """
                |
                | ✖ Missing -PscreenName=<name>
                |
                | Usage:  ./gradlew ketoyScreenVersions -PscreenName=home
                |
                """.trimMargin()
            )

        val url = "$baseUrl/api/screens/$packageName/$screenName/versions"

        println()
        println("  Fetching versions for '$screenName' ...")
        println()

        val (code, response) = ketoyHttpRequest("GET", url, apiKey)

        if (code in 200..299) {
            println("  ✔ Versions (HTTP $code):")
            println()
            println("  $response")
        } else {
            println("  ✖ Failed (HTTP $code)")
            println("  $response")
        }
        println()
    }
}

// ══════════════════════════════════════════════════════════════════
//  ketoyScreenDetails — Get details of a specific screen (with JSON)
// ══════════════════════════════════════════════════════════════════
// Usage:
//   ./gradlew ketoyScreenDetails -PscreenName=home
tasks.register("ketoyScreenDetails") {
    group = "ketoy"
    description = "Get details of a screen including its JSON content"

    doLast {
        val (apiKey, baseUrl, packageName) = loadKetoyCloudConfig()

        val screenName = project.findProperty("screenName") as? String
            ?: throw GradleException(
                """
                |
                | ✖ Missing -PscreenName=<name>
                |
                | Usage:  ./gradlew ketoyScreenDetails -PscreenName=home
                |
                """.trimMargin()
            )

        val url = "$baseUrl/api/screens/$packageName/$screenName/details?includeJson=true"

        println()
        println("  Fetching details for '$screenName' ...")
        println()

        val (code, response) = ketoyHttpRequest("GET", url, apiKey)

        if (code in 200..299) {
            println("  ✔ Details (HTTP $code):")
            println()
            println("  $response")
        } else {
            println("  ✖ Failed (HTTP $code)")
            println("  $response")
        }
        println()
    }
}

// ══════════════════════════════════════════════════════════════════
//  ketoyRollback — Rollback a screen to a previous version
// ══════════════════════════════════════════════════════════════════
// Usage:
//   ./gradlew ketoyRollback -PscreenName=home -Pversion=1.0.0
tasks.register("ketoyRollback") {
    group = "ketoy"
    description = "Rollback a screen to a previous version on the cloud server"

    doLast {
        val (apiKey, baseUrl, packageName) = loadKetoyCloudConfig()

        val screenName = project.findProperty("screenName") as? String
            ?: throw GradleException("Missing -PscreenName=<name>")

        val version = project.findProperty("version") as? String
            ?: throw GradleException("Missing -Pversion=<target_version>")

        val url = "$baseUrl/api/screens/$packageName/$screenName/rollback/$version"

        println()
        println("  Rolling back '$screenName' to version $version ...")
        println()

        val (code, response) = ketoyHttpRequest("POST", url, apiKey)

        if (code in 200..299) {
            println("  ✔ Rollback successful (HTTP $code)")
            println()
            println("  $response")
        } else {
            println("  ✖ Rollback failed (HTTP $code)")
            println("  $response")
        }
        println()
    }
}

// ══════════════════════════════════════════════════════════════════
//  ketoyDeleteScreen — Delete a screen from the cloud server
// ══════════════════════════════════════════════════════════════════
// Usage:
//   ./gradlew ketoyDeleteScreen -PscreenName=home
tasks.register("ketoyDeleteScreen") {
    group = "ketoy"
    description = "Delete a screen and all its versions from the cloud server"

    doLast {
        val (apiKey, baseUrl, packageName) = loadKetoyCloudConfig()

        val screenName = project.findProperty("screenName") as? String
            ?: throw GradleException("Missing -PscreenName=<name>")

        val url = "$baseUrl/api/screens/$packageName/$screenName"

        println()
        println("  ⚠ Deleting '$screenName' (all versions) ...")
        println()

        val (code, response) = ketoyHttpRequest("DELETE", url, apiKey)

        if (code in 200..299) {
            println("  ✔ Deleted (HTTP $code)")
            println()
            println("  $response")
        } else {
            println("  ✖ Delete failed (HTTP $code)")
            println("  $response")
        }
        println()
    }
}

// ── Ketoy Dev Server convenience task ─────────────────────────────
// Run with: ./gradlew ketoyServe
// Options:  ./gradlew ketoyServe --args="--port 9090 --watch ./my-screens"
tasks.register("ketoyServe") {
    group = "ketoy"
    description = "Start the Ketoy Dev Server for hot-reload preview"
    dependsOn(":ketoy-devtools-server:run")
}

// ── Ketoy Live Dev — auto-export + serve (single command) ─────────
// Run with: ./gradlew ketoyDev
// This is the recommended way to develop with Ketoy live-reload.
// It watches Kotlin source files, re-exports JSON on change,
// and pushes updates to the connected app — all automatically.
tasks.register("ketoyDev") {
    group = "ketoy"
    description = "Start Ketoy Dev Server with auto-export (edit DSL → live app update)"
    doFirst {
        // Pass --auto-export flag to the server
        project(":ketoy-devtools-server").tasks.named("run") {
            this as JavaExec
            args = listOf("--auto-export")
        }
    }
    finalizedBy(":ketoy-devtools-server:run")
}

// ── Ketoy DSL → JSON export task ─────────────────────────────────
// Run with: ./gradlew ketoyExport
// Exports DSL screens to ketoy-screens/ for the dev server to watch.
tasks.register("ketoyExport") {
    group = "ketoy"
    description = "Export Ketoy DSL screens to JSON files for the dev server"
    dependsOn(":app:testDebugUnitTest")
}

// Configure the app's unit test task to only run ExportScreensTest when
// triggered via ketoyExport (otherwise run all tests as usual).
gradle.taskGraph.whenReady {
    if (hasTask(":ketoyExport") || hasTask("ketoyExport")) {
        allTasks.filterIsInstance<Test>()
            .filter { it.path == ":app:testDebugUnitTest" }
            .forEach { task ->
                task.filter {
                    includeTestsMatching("*.ExportScreensTest")
                }
            }
    }
}