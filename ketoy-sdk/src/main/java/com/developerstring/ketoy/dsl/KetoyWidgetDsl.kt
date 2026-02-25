package com.developerstring.ketoy.dsl

import androidx.compose.runtime.Composable
import com.developerstring.ketoy.widget.KetoyWidgetParser
import com.developerstring.ketoy.widget.KetoyWidgetRegistry
import kotlinx.serialization.json.JsonObject

/**
 * DSL entry-point for defining custom widget parsers inline.
 *
 * Provides a concise Kotlin-idiomatic way to register custom widgets
 * and parsers during initialization.
 *
 * ## Example
 * ```kotlin
 * ketoyWidgets {
 *     widget<BadgeModel>("badge") {
 *         model { json ->
 *             BadgeModel(
 *                 text = json["text"]?.jsonPrimitive?.content ?: "",
 *                 color = json["color"]?.jsonPrimitive?.content
 *             )
 *         }
 *         render { model ->
 *             Badge(text = model.text, color = model.color)
 *         }
 *     }
 * }
 * ```
 */
class KetoyWidgetsScope {

    @PublishedApi
    internal val parsers = mutableListOf<KetoyWidgetParser<*>>()

    /**
     * Defines a custom widget parser inline.
     *
     * The [type] string must match the `"type"` field in your JSON payloads so
     * that Ketoy can route unknown node types to your custom parser.
     *
     * ```kotlin
     * widget<ChipModel>("chip") {
     *     model { json -> ChipModel(json["label"]!!.jsonPrimitive.content) }
     *     render { model -> Chip(label = model.label) }
     * }
     * ```
     *
     * @param T     The model type that the widget deserialises JSON into.
     * @param type  The widget type identifier (must match the JSON `"type"` field).
     * @param block Configuration block applied to a [WidgetParserBuilder] receiver.
     */
    inline fun <reified T> widget(type: String, block: WidgetParserBuilder<T>.() -> Unit) {
        val builder = WidgetParserBuilder<T>(type)
        builder.block()
        parsers.add(builder.build())
    }
}

/**
 * Builder for creating an inline [KetoyWidgetParser].
 *
 * Collects a **model factory** (JSON → model) and a **render function** (model → Composable)
 * and combines them into a [KetoyWidgetParser] that Ketoy uses at render time.
 *
 * Both [model] and [render] **must** be called; omitting either will throw
 * an [IllegalStateException] when the parser is built.
 *
 * @param T    The model type this builder produces.
 * @param type The widget type identifier.
 *
 * @see KetoyWidgetsScope.widget
 */
class WidgetParserBuilder<T>(private val type: String) {

    private var modelFactory: ((JsonObject) -> T)? = null
    private var renderer: (@Composable (T) -> Unit)? = null

    /**
     * Defines how to deserialise a [JsonObject] into the model of type [T].
     *
     * @param factory Function that extracts fields from the JSON and returns a model instance.
     */
    fun model(factory: (JsonObject) -> T) {
        modelFactory = factory
    }

    /**
     * Defines how to render the model as a Jetpack Compose `@Composable`.
     *
     * @param composable Composable lambda that receives the parsed model and renders the widget.
     */
    fun render(composable: @Composable (T) -> Unit) {
        renderer = composable
    }

    @PublishedApi
    internal fun build(): KetoyWidgetParser<T> {
        val factory = modelFactory
            ?: throw IllegalStateException("Widget '$type': model factory not defined")
        val render = renderer
            ?: throw IllegalStateException("Widget '$type': render function not defined")

        return object : KetoyWidgetParser<T> {
            override val type = this@WidgetParserBuilder.type
            override fun getModel(json: JsonObject) = factory(json)

            @Composable
            override fun parse(model: T) = render(model)
        }
    }
}

/**
 * DSL entry-point for defining and registering custom widget parsers.
 *
 * All parsers declared inside the [block] are automatically registered in
 * [KetoyWidgetRegistry] and returned as a list for additional inspection or testing.
 *
 * ```kotlin
 * ketoyWidgets {
 *     widget<RatingModel>("rating") {
 *         model { json -> RatingModel(stars = json["stars"]?.jsonPrimitive?.int ?: 0) }
 *         render { model -> RatingBar(stars = model.stars) }
 *     }
 * }
 * ```
 *
 * @param block Lambda with [KetoyWidgetsScope] receiver to define widget parsers.
 * @return The list of [KetoyWidgetParser] instances that were registered.
 *
 * @see KetoyWidgetsScope
 * @see WidgetParserBuilder
 * @see KetoyWidgetRegistry
 */
fun ketoyWidgets(block: KetoyWidgetsScope.() -> Unit): List<KetoyWidgetParser<*>> {
    val scope = KetoyWidgetsScope()
    scope.block()
    scope.parsers.forEach { KetoyWidgetRegistry.register(it) }
    return scope.parsers
}
