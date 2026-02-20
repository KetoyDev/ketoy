package com.developerstring.ketoy.widget

import androidx.compose.runtime.Composable
import kotlinx.serialization.json.JsonObject

/**
 * Base interface for all Ketoy widget parsers.
 *
 * A widget parser bridges the gap between a JSON widget definition and
 * its rendered Compose UI. Each parser:
 *
 * 1. Declares a unique [type] string matching the JSON `"type"` field.
 * 2. Deserialises the JSON [JsonObject] into a model via [getModel].
 * 3. Renders the model as a Compose widget via [parse].
 *
 * ## Creating a custom parser
 * ```kotlin
 * class KetoyBadgeParser : KetoyWidgetParser<BadgeModel> {
 *     override val type = "badge"
 *
 *     override fun getModel(json: JsonObject): BadgeModel {
 *         return BadgeModel(
 *             text = json["text"]?.jsonPrimitive?.content ?: "",
 *             color = json["color"]?.jsonPrimitive?.content
 *         )
 *     }
 *
 *     @Composable
 *     override fun parse(model: BadgeModel) {
 *         Badge(text = model.text, color = model.color?.toColor())
 *     }
 * }
 * ```
 *
 * ## Registering
 * ```kotlin
 * Ketoy.initialize(
 *     widgetParsers = listOf(KetoyBadgeParser())
 * )
 * ```
 *
 * @param T The model type this parser produces from JSON.
 * @see KetoyWidgetRegistry
 * @see com.developerstring.ketoy.renderer.WidgetRenderer
 */
interface KetoyWidgetParser<T> {

    /**
     * The unique type identifier for this widget.
     * Must match the `"type"` field in the JSON widget definition.
     */
    val type: String

    /**
     * Deserialise a [JsonObject] into the model type [T].
     *
     * Implementations should extract relevant fields from the JSON and
     * construct the model. Unknown keys should be silently ignored to
     * allow forward-compatible schema evolution.
     *
     * ### Example JSON input
     * ```json
     * { "type": "badge", "text": "New", "color": "#FF0000" }
     * ```
     *
     * @param json The JSON object containing widget properties.
     * @return The fully initialised model of type [T].
     */
    fun getModel(json: JsonObject): T

    /**
     * Render the model as a Composable widget.
     *
     * This method is called inside a Compose composition scope.
     * It should emit one or more Composable nodes that represent the
     * visual output of the widget described by [model].
     *
     * @param model The parsed model produced by [getModel].
     */
    @Composable
    fun parse(model: T)
}
