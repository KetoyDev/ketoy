package com.developerstring.ketoy_app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.developerstring.ketoy.registry.KetoyFunctionRegistry

/**
 * Main ViewModel for the Ketoy demo app.
 *
 * Demonstrates how SDUI functions bridge to app business logic —
 * function calls from JSON resolve here via [KetoyFunctionRegistry].
 */
class MainViewModel : ViewModel() {

    // ── State ───────────────────────────────────────────────────

    var userName by mutableStateOf("Aditya")
        private set

    var totalBalance by mutableStateOf(24_562.80)
        private set

    var income by mutableStateOf(8_240.00)
        private set

    var expenses by mutableStateOf(3_820.00)
        private set

    var savings by mutableStateOf(18_240.00)
        private set

    var notificationCount by mutableIntStateOf(3)
        private set

    var isDarkMode by mutableStateOf(true)
        private set

    var toastMessage by mutableStateOf<String?>(null)
        private set

    var selectedCardIndex by mutableIntStateOf(0)
        private set

    data class Transaction(
        val icon: String,
        val title: String,
        val subtitle: String,
        val amount: String,
        val isIncome: Boolean
    )

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
