package com.developerstring.ketoy_app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.developerstring.ketoy.core.KetoyDataProvider
import com.developerstring.ketoy.core.KetoyVariableRegistry
import com.developerstring.ketoy.core.syncListToKetoy
import com.developerstring.ketoy.core.syncToKetoy
import com.developerstring.ketoy.model.KetoyVariable
import com.developerstring.ketoy.registry.KetoyFunctionRegistry
import com.developerstring.ketoy.util.KColors
import com.developerstring.ketoy.util.KIcons
import kotlin.math.roundToInt

/**
 * Main ViewModel for the Ketoy demo app.
 *
 * Demonstrates how SDUI functions bridge to app business logic —
 * function calls from JSON resolve here via [KetoyFunctionRegistry].
 */
class MainViewModel : ViewModel(), KetoyDataProvider {

    override val ketoyPrefix = "user"

    // ── State ───────────────────────────────────────────────────

    var userName by mutableStateOf("Aditya")
        private set

    var totalBalance by androidx.compose.runtime.mutableDoubleStateOf(24567.80)
        private set

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                totalBalance = Random.nextDouble(10000.0, 50000.0)
                    .let { (it * 100).roundToInt() / 100.0 }

                selectedCardIndex = Random.nextInt(0, 2)
                syncVariables()
            }
        }
    }

    var income by mutableStateOf(8240.00)
        private set

    var expenses by mutableStateOf(3820.00)
        private set

    var savings by mutableStateOf(18240.00)
        private set

    var notificationCount by mutableIntStateOf(3)
        private set

    var isDarkMode by mutableStateOf(true)
        private set

    var toastMessage by mutableStateOf<String?>(null)
        private set

    var selectedCardIndex by mutableIntStateOf(1)
        private set

    data class Transaction(
        val icon: String,
        val title: String,
        val subtitle: String,
        val amount: String,
        val isIncome: Boolean
    ) : KetoyDataProvider {
        override val ketoyPrefix = ""
        override fun provideData() = mapOf(
            "title" to title,
            "subtitle" to subtitle,
            "amount" to amount,
            "isIncome" to isIncome,
        )
    }

    var transactions by mutableStateOf(
        listOf(
            Transaction("ShoppingCart", "Grocery Store", "Today, 2:30 PM", "- \$45.20", false),
            Transaction("AttachMoney", "Salary Deposit", "Yesterday", "+ \$3,200", true),
            Transaction("LocalCafe", "Coffee Shop", "Yesterday", "- \$6.50", false),
            Transaction("Movie", "Netflix Subscription", "Feb 1", "- \$15.99", false),
            Transaction("Phone", "App Store", "Jan 30", "- \$9.99", false),
            Transaction("SwapHoriz", "Transfer from Alex", "Jan 28", "+ \$150.00", true),
        )
    )
        private set

    // ── Actions (registered as SDUI functions) ──────────────────

    /**
     * Provides ViewModel state as a field map for [KetoyDataProvider].
     * Each entry becomes `{{data:user:field}}` when registered.
     */
    override fun provideData(): Map<String, Any?> = mapOf(
        "name" to userName,
        "initials" to userName.take(2).uppercase(),
        "totalBalance" to "\$${"%.2f".format(totalBalance)}",
        "income" to "\$${"%.2f".format(income)}",
        "expenses" to "\$${"%.2f".format(expenses)}",
        "savings" to "\$${"%.2f".format(savings)}",
        "notificationCount" to notificationCount.toString(),
        "isDarkMode" to isDarkMode,
        "selectedCardIndex" to selectedCardIndex,
        // Derived dark-mode variables for ProfileScreen templates
        "darkModeIcon" to if (isDarkMode) KIcons.DarkMode else KIcons.LightMode,
        "darkModeLabel" to if (isDarkMode) "ON" else "OFF",
        "darkModeToggleBg" to if (isDarkMode) KColors.Primary else KColors.Outline,
        "darkModeToggleTextColor" to if (isDarkMode) KColors.OnPrimary else "#FFFFFF",
    )

    /**
     * Sync ViewModel state into [KetoyVariableRegistry] via [KetoyDataProvider].
     * Call whenever state changes that should be reflected in SDUI templates.
     */
    fun syncVariables() {
        syncToKetoy()
        syncListToKetoy("user.transactions", transactions)
    }

    /**
     * Register all ViewModel actions with [KetoyFunctionRegistry].
     * Called once during app initialization.
     */
    fun registerFunctions() {
        KetoyFunctionRegistry.register(
            name = "showToast",
            parameterTypes = mapOf("message" to "String"),
            description = "Show a toast message"
        ) { params ->
            val message = params["message"] as? String ?: "Action triggered"
            toastMessage = message
        }

        KetoyFunctionRegistry.register(
            name = "toggleDarkMode",
            description = "Toggle dark/light theme"
        ) {
            isDarkMode = !isDarkMode
            toastMessage = if (isDarkMode) "Dark mode enabled" else "Light mode disabled"
        }

        KetoyFunctionRegistry.register(
            name = "sendMoney",
            parameterTypes = mapOf("amount" to "Double", "recipient" to "String"),
            description = "Send money to a recipient"
        ) { params ->
            val amount = (params["amount"] as? Number)?.toDouble() ?: 0.0
            val recipient = params["recipient"] as? String ?: "Unknown"
            totalBalance -= amount
            expenses += amount
            transactions = listOf(
                Transaction("Send", "Sent to $recipient", "Just now", "- \$${"%.2f".format(amount)}", false)
            ) + transactions
            toastMessage = "Sent \$${"%.2f".format(amount)} to $recipient"
        }

        KetoyFunctionRegistry.register(
            name = "addTransaction",
            parameterTypes = mapOf("title" to "String", "amount" to "Double", "isIncome" to "Boolean"),
            description = "Add a new transaction"
        ) { params ->
            val title = params["title"] as? String ?: "Transaction"
            val amount = (params["amount"] as? Number)?.toDouble() ?: 0.0
            val isIncome = params["isIncome"] as? Boolean ?: false
            val sign = if (isIncome) "+" else "-"
            transactions = listOf(
                Transaction(
                    if (isIncome) "AttachMoney" else "ShoppingCart",
                    title, "Just now",
                    "$sign \$${"%.2f".format(amount)}",
                    isIncome
                )
            ) + transactions
            if (isIncome) {
                totalBalance += amount
                income += amount
            } else {
                totalBalance -= amount
                expenses += amount
            }
            toastMessage = "$title: $sign\$${"%.2f".format(amount)}"
        }

        KetoyFunctionRegistry.register(
            name = "clearNotifications",
            description = "Clear all notifications"
        ) {
            notificationCount = 0
            toastMessage = "Notifications cleared"
        }

        KetoyFunctionRegistry.register(
            name = "selectCard",
            parameterTypes = mapOf("index" to "Int"),
            description = "Select a payment card"
        ) { params ->
            val index = params["index"] as? Int ?: 0
            selectedCardIndex = index
            toastMessage = "Card ${index + 1} selected"
        }

        KetoyFunctionRegistry.register(
            name = "freezeCard",
            parameterTypes = mapOf("cardName" to "String"),
            description = "Freeze a payment card"
        ) { params ->
            val name = params["cardName"] as? String ?: "Card"
            toastMessage = "$name has been frozen"
        }

        KetoyFunctionRegistry.register(
            name = "editProfile",
            parameterTypes = mapOf("field" to "String"),
            description = "Navigate to edit a profile field"
        ) { params ->
            val field = params["field"] as? String ?: "profile"
            toastMessage = "Edit $field"
        }

        KetoyFunctionRegistry.register(
            name = "logout",
            description = "Sign out the current user"
        ) {
            toastMessage = "Logged out successfully"
            userName = "Guest"
        }
    }

    /** Consume the current toast message. */
    fun consumeToast() {
        toastMessage = null
    }
}
