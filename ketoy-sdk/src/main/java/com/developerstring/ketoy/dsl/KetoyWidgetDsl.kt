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
     * Define a custom widget parser inline.
     *
     * @param type  The widget type identifier (matches JSON `"type"` field).
     * @param block Configuration block for the widget parser.
     */
    inline fun <reified T> widget(type: String, block: WidgetParserBuilder<T>.() -> Unit) {
        val builder = WidgetParserBuilder<T>(type)
        builder.block()
        parsers.add(builder.build())
    }
}

/**
 * Builder for creating an inline [KetoyWidgetParser].
 */
class WidgetParserBuilder<T>(private val type: String) {

    private var modelFactory: ((JsonObject) -> T)? = null
    private var renderer: (@Composable (T) -> Unit)? = null

    /** Define how to deserialise JSON into the model. */
    fun model(factory: (JsonObject) -> T) {
        modelFactory = factory
    }

    /** Define how to render the model as a Composable. */
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
 * Parsers defined here are automatically registered in [KetoyWidgetRegistry].
 *
 * ```kotlin
 * ketoyWidgets {
 *     widget<RatingModel>("rating") {
 *         model { json -> RatingModel(stars = json["stars"]?.jsonPrimitive?.int ?: 0) }
 *         render { model -> RatingBar(stars = model.stars) }
 *     }
 * }
 * ```
 */
fun ketoyWidgets(block: KetoyWidgetsScope.() -> Unit): List<KetoyWidgetParser<*>> {
    val scope = KetoyWidgetsScope()
    scope.block()
    scope.parsers.forEach { KetoyWidgetRegistry.register(it) }
    return scope.parsers
}
