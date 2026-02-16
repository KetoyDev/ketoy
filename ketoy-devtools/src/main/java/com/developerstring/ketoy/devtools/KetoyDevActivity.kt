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
 * A standalone Activity for Ketoy Dev Tools.
 * Launch this to get the connection screen + live preview without
 * modifying your existing app code.
 *
 * Usage:
 * ```kotlin
 * KetoyDevActivity.launch(context)
 * // or with auto-connect:
 * KetoyDevActivity.launch(context, "192.168.1.5", 8484)
 * ```
 *
 * Or declare it in your debug AndroidManifest.xml:
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
 */
class KetoyDevActivity : ComponentActivity() {

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

    companion object {
        private const val EXTRA_HOST = "ketoy_dev_host"
        private const val EXTRA_PORT = "ketoy_dev_port"

        /**
         * Launch the Ketoy Dev Activity.
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
