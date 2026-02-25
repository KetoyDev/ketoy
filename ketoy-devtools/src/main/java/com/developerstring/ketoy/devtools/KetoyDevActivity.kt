package com.developerstring.ketoy.devtools

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

/**
 * A standalone [ComponentActivity] for the Ketoy Dev Tools suite.
 *
 * Launch this activity to get a full-screen connection screen followed by a
 * live-preview surface — **without modifying your existing app code**. This
 * is ideal during development when you want an isolated environment for
 * rapid iteration on SDUI screens served by the Ketoy Dev Server.
 *
 * The activity:
 * 1. Shows the [KetoyDevConnectScreen] for entering the dev-server address.
 * 2. Once connected, renders every screen pushed from the server in real time.
 * 3. Supports deep-link launching via the `ketoy://dev` URI scheme.
 *
 * ## Programmatic launch
 * ```kotlin
 * // From any Context (Activity, Service, Application …)
 * KetoyDevActivity.launch(context)
 *
 * // Auto-connect to a known server:
 * KetoyDevActivity.launch(context, host = "192.168.1.5", port = 8484)
 * ```
 *
 * ## Manifest declaration (debug only)
 * ```xml
 * <activity
 *     android:name="com.developerstring.ketoy.devtools.KetoyDevActivity"
 *     android:exported="true"
 *     android:label="Ketoy Dev Preview">
 *     <intent-filter>
 *         <action android:name="android.intent.action.VIEW" />
 *         <category android:name="android.intent.category.DEFAULT" />
 *         <data android:scheme="ketoy" android:host="dev" />
 *     </intent-filter>
 * </activity>
 * ```
 *
 * ## Architecture
 * Internally, `KetoyDevActivity` delegates to [KetoyDevWrapper] with an
 * empty content lambda — it exists purely as a dedicated preview host.
 * When a `host` extra is supplied via the launching [Intent], the wrapper
 * auto-connects and skips the manual connection screen.
 *
 * @see KetoyDevWrapper
 * @see KetoyDevConfig
 * @see KetoyDevConnectScreen
 */
class KetoyDevActivity : ComponentActivity() {

    /**
     * Initialises the activity, enables edge-to-edge rendering, and sets
     * the Compose content tree.
     *
     * If the launching [Intent] contains [EXTRA_HOST], the connection
     * screen is skipped and the client auto-connects to the specified
     * server. Otherwise the user is presented with [KetoyDevConnectScreen].
     *
     * @param savedInstanceState The previously saved instance state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val host = intent.getStringExtra(EXTRA_HOST) ?: ""
        val port = intent.getIntExtra(EXTRA_PORT, 8484)
        val autoConnect = host.isNotBlank()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KetoyDevWrapper(
                        config = KetoyDevConfig(
                            host = host,
                            port = port,
                            autoConnect = autoConnect,
                            showOverlay = true
                        )
                    ) {
                        // Empty content — this activity is purely for dev preview
                    }
                }
            }
        }
    }

    /**
     * Companion providing the [launch] factory and intent-extra constants.
     */
    companion object {
        /** Intent extra key for the dev-server hostname or IP address. */
        private const val EXTRA_HOST = "ketoy_dev_host"

        /** Intent extra key for the dev-server HTTP port. */
        private const val EXTRA_PORT = "ketoy_dev_port"

        /**
         * Convenience launcher for [KetoyDevActivity].
         *
         * If [host] is provided the activity auto-connects immediately;
         * otherwise the connection screen is shown.
         *
         * When called from a non-Activity [Context] (e.g. a `Service` or
         * `Application`), [Intent.FLAG_ACTIVITY_NEW_TASK] is added
         * automatically.
         *
         * ```kotlin
         * // Default — show connection screen:
         * KetoyDevActivity.launch(context)
         *
         * // Skip to auto-connect:
         * KetoyDevActivity.launch(context, host = "192.168.1.5", port = 8484)
         * ```
         *
         * @param context The [Context] used to start the activity.
         * @param host    Optional hostname / IP of the Ketoy Dev Server.
         * @param port    HTTP port of the server. Defaults to `8484`.
         *
         * @see KetoyDevConfig
         */
        fun launch(context: Context, host: String? = null, port: Int = 8484) {
            val intent = Intent(context, KetoyDevActivity::class.java).apply {
                if (host != null) {
                    putExtra(EXTRA_HOST, host)
                    putExtra(EXTRA_PORT, port)
                }
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
        }
    }
}
