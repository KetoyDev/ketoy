package com.developerstring.ketoy.wire

import kotlinx.serialization.json.*

/**
 * Bidirectional key-alias registry for Ketoy SDUI JSON payloads.
 *
 * SDUI JSON is key-heavy: full property names like `"backgroundColor"`,
 * `"contentDescription"`, `"verticalArrangement"` repeat thousands of times
 * across a typical screen tree. This registry maps every known Ketoy property
 * name to a short alias (typically 1-3 characters), producing a 1.5-2.5x
 * size reduction on top of any transport-level compression.
 *
 * The alias vocabulary is fixed at compile time — both server and client must
 * share the same version. The registry is append-only: once an alias is
 * assigned it must never change, ensuring backward compatibility.
 *
 * ### Encoding (server / export side)
 * ```kotlin
 * val minified = KetoyKeyAlias.aliasKeys(originalJsonElement)
 * ```
 *
 * ### Decoding (client / render side)
 * ```kotlin
 * val expanded = KetoyKeyAlias.expandKeys(minifiedJsonElement)
 * ```
 */
object KetoyKeyAlias {

    /**
     * Full property name → short alias.
     *
     * Alias assignment rules:
     * - Structural keys (`type`, `props`, `children`) get single-char aliases.
     * - High-frequency modifier keys get 2-char aliases.
     * - Less common scaffold / text-field keys get 2-3 char aliases.
     * - Color-state keys use a prefix pattern:
     *   `f` = focused, `u` = unfocused, `d` = disabled, `e` = error,
     *   `s` = selected, `us` = unselected.
     */
    val encode: Map<String, String> = buildMap {
        // ── Structural (1 char) ───────────────────────────────
        put("type", "t")
        put("props", "p")
        put("children", "c")

        // ── KModifier (2 char) ────────────────────────────────
        put("modifier", "md")
        put("fillMaxSize", "fs")
        put("fillMaxWidth", "fw")
        put("fillMaxHeight", "fh")
        put("weight", "wt")
        put("size", "sz")
        put("width", "w")
        put("height", "h")
        put("padding", "pd")
        put("margin", "mg")
        put("background", "bg")
        put("gradient", "gr")
        put("border", "bd")
        put("shape", "sh")
        put("cornerRadius", "cr")
        put("shadow", "sw")
        put("clickable", "ck")
        put("scale", "sc")
        put("rotation", "rt")
        put("alpha", "al")
        put("verticalScroll", "vs")
        put("horizontalScroll", "hs")

        // ── KPadding / KMargin (2-3 char) ─────────────────────
        put("all", "a")
        put("horizontal", "hz")
        put("vertical", "vt")
        put("top", "tp")
        put("bottom", "bt")
        put("start", "st")
        put("end", "en")

        // ── KBorder / KShadow ─────────────────────────────────
        put("color", "cl")
        put("elevation", "el")
        put("offsetX", "ox")
        put("offsetY", "oy")
        put("blurRadius", "br")

        // ── KGradient ─────────────────────────────────────────
        put("colors", "cs")
        put("direction", "dr")
        put("angle", "ag")
        put("centerX", "cx")
        put("centerY", "cy")
        put("radius", "rd")
        put("startAngle", "sa")
        put("endAngle", "ea")

        // ── KScrollConfig ─────────────────────────────────────
        put("enabled", "eb")
        put("reverseScrolling", "rs")
        put("flingBehavior", "fb")

        // ── Layout props ──────────────────────────────────────
        put("verticalArrangement", "va")
        put("horizontalAlignment", "ha")
        put("horizontalArrangement", "hr")
        put("verticalAlignment", "vl")
        put("contentAlignment", "ca")
        put("userScrollEnabled", "us")
        put("reverseLayout", "rl")
        put("contentPadding", "cp")
        put("beyondBoundsItemCount", "bb")

        // ── Text props ────────────────────────────────────────
        put("text", "tx")
        put("fontSize", "fz")
        put("fontWeight", "fW")
        put("textAlign", "ta")
        put("maxLines", "ml")
        put("overflow", "of")
        put("letterSpacing", "ls")
        put("lineHeight", "lh")

        // ── Button / Card / Image / Icon ──────────────────────
        put("onClick", "oc")
        put("containerColor", "cc")
        put("contentColor", "cC")
        put("source", "sr")
        put("contentDescription", "cd")
        put("scaleType", "sT")
        put("icon", "ic")
        put("style", "sy")
        put("iconSize", "iS")
        put("iconColor", "iC")
        put("iconStyle", "iY")
        put("disabledContainerColor", "dC")
        put("disabledContentColor", "dN")

        // ── Component props ───────────────────────────────────
        put("name", "nm")
        put("componentName", "cn")
        put("properties", "pr")
        put("version", "vr")
        put("requiredImports", "ri")
        put("fallbackComponent", "fc")

        // ── Data props ────────────────────────────────────────
        put("id", "I")
        put("className", "cN")
        put("fields", "fl")
        put("enumName", "eN")
        put("values", "vs2")
        put("selectedValue", "sV")
        put("onSelectionChange", "oS")
        put("dataSource", "dS")
        put("itemAlias", "iA")

        // ── Scaffold props ────────────────────────────────────
        put("topBar", "tB")
        put("bottomBar", "bB")
        put("snackbarHost", "sH")
        put("floatingActionButton", "fA")
        put("floatingActionButtonPosition", "fP")
        put("contentWindowInsets", "cW")

        // ── TopAppBar props ───────────────────────────────────
        put("title", "tt")
        put("navigationIcon", "nI")
        put("actions", "ac")
        put("windowInsets", "wI")
        put("scrollBehavior", "sB")
        put("expandedHeight", "eH")
        put("canScroll", "cS")

        // ── BottomAppBar / NavigationBar ──────────────────────
        put("tonalElevation", "tE")

        // ── FAB ───────────────────────────────────────────────
        put("interactionSource", "iS2")
        put("defaultElevation", "dE")
        put("pressedElevation", "pE")
        put("focusedElevation", "fE")
        put("hoveredElevation", "hE")
        put("disabledElevation", "dEl")

        // ── SnackBar ──────────────────────────────────────────
        put("action", "at")
        put("dismissAction", "dA")
        put("actionOnNewLine", "aN")
        put("actionContentColor", "aC")
        put("dismissActionContentColor", "dAC")
        put("message", "ms")
        put("duration", "du")
        put("hostState", "hS")
        put("snackbar", "sb")

        // ── Navigation items ──────────────────────────────────
        put("selected", "sl")
        put("label", "lb")
        put("badge", "bG")
        put("selectedIcon", "sI")
        put("alwaysShowLabel", "aL")
        put("selectedContainerColor", "sCC")
        put("selectedContentColor", "sCN")
        put("indicatorColor", "iN")
        put("rippleColor", "rC")
        put("header", "hd")

        // ── Navigation drawer item colors ─────────────────────
        put("unselectedContainerColor", "uCC")
        put("selectedIconColor", "sIC")
        put("unselectedIconColor", "uIC")
        put("selectedTextColor", "sTC")
        put("unselectedTextColor", "uTC")
        put("selectedBadgeColor", "sBC")
        put("unselectedBadgeColor", "uBC")

        // ── NavigationBarItem colors ──────────────────────────
        put("disabledIconColor", "dIC")
        put("disabledTextColor", "dTC")

        // ── TopAppBar colors ──────────────────────────────────
        put("scrolledContainerColor", "scC")
        put("navigationIconContentColor", "nCC")
        put("titleContentColor", "tCC")
        put("actionIconContentColor", "aIC")

        // ── ModalBottomSheet ──────────────────────────────────
        put("onDismissRequest", "oD")
        put("sheetState", "sS")
        put("scrimColor", "sCl")
        put("dragHandle", "dH")

        // ── TextField props ───────────────────────────────────
        put("value", "v")
        put("onValueChange", "oV")
        put("readOnly", "rO")
        put("textStyle", "tS")
        put("labelContent", "lC")
        put("placeholderContent", "pC")
        put("leadingIconContent", "lIC")
        put("trailingIconContent", "tIC")
        put("prefixContent", "pxC")
        put("suffixContent", "sxC")
        put("supportingTextContent", "stC")
        put("isError", "iE")
        put("visualTransformation", "vT")
        put("keyboardOptions", "kO")
        put("keyboardActions", "kA")
        put("singleLine", "sL")
        put("minLines", "mL2")

        // ── KTextStyle ────────────────────────────────────────
        put("fontFamily", "fF")
        put("fontStyle", "fS")
        put("textDecoration", "tD")
        put("textGeometricTransform", "tG")
        put("localeList", "lL")
        put("textDirection", "tDr")

        // ── KTextGeometricTransform ───────────────────────────
        put("scaleX", "sX")
        put("skewX", "kX")

        // ── KVisualTransformation ─────────────────────────────
        put("mask", "mk")
        put("customPattern", "cP2")

        // ── KKeyboardOptions ──────────────────────────────────
        put("capitalization", "cp2")
        put("autoCorrect", "au")
        put("keyboardType", "kT")
        put("imeAction", "iM")
        put("platformImeOptions", "pI")
        put("privateImeOptions", "pvI")
        put("showPersonalizedSuggestions", "sP")

        // ── KKeyboardActions ──────────────────────────────────
        put("onDone", "odn")
        put("onGo", "ogo")
        put("onNext", "onx")
        put("onPrevious", "opr")
        put("onSearch", "osr")
        put("onSend", "osn")

        // ── TextField colors (state × element matrix) ─────────
        put("focusedTextColor", "fTC")
        put("unfocusedTextColor", "uFTC")
        put("disabledTextColor", "dTCl")
        put("errorTextColor", "eTC")
        put("focusedContainerColor", "fCC")
        put("unfocusedContainerColor", "uFCC")
        put("disabledContainerColor", "dCCl")
        put("errorContainerColor", "eCCl")
        put("cursorColor", "cuC")
        put("errorCursorColor", "eCuC")
        put("selectionColors", "seC")
        put("focusedIndicatorColor", "fInC")
        put("unfocusedIndicatorColor", "uInC")
        put("disabledIndicatorColor", "dInC")
        put("errorIndicatorColor", "eInC")
        put("focusedLeadingIconColor", "fLIC")
        put("unfocusedLeadingIconColor", "uLIC")
        put("disabledLeadingIconColor", "dLIC")
        put("errorLeadingIconColor", "eLIC")
        put("focusedTrailingIconColor", "fTIC")
        put("unfocusedTrailingIconColor", "uTIC")
        put("disabledTrailingIconColor", "dTIC")
        put("errorTrailingIconColor", "eTIC")
        put("focusedLabelColor", "fLaC")
        put("unfocusedLabelColor", "uLaC")
        put("disabledLabelColor", "dLaC")
        put("errorLabelColor", "eLaC")
        put("focusedPlaceholderColor", "fPhC")
        put("unfocusedPlaceholderColor", "uPhC")
        put("disabledPlaceholderColor", "dPhC")
        put("errorPlaceholderColor", "ePhC")
        put("focusedSupportingTextColor", "fStC")
        put("unfocusedSupportingTextColor", "uStC")
        put("disabledSupportingTextColor", "dStC")
        put("errorSupportingTextColor", "eStC")
        put("focusedPrefixColor", "fPxC")
        put("unfocusedPrefixColor", "uPxC")
        put("disabledPrefixColor", "dPxC")
        put("errorPrefixColor", "ePxC")
        put("focusedSuffixColor", "fSxC")
        put("unfocusedSuffixColor", "uSxC")
        put("disabledSuffixColor", "dSxC")
        put("errorSuffixColor", "eSxC")
        put("handleColor", "hCl")
        // backgroundColor intentionally reuses "bg" via the same mapping

        // ── KComponentInfo / KComponentMetadata ───────────────
        put("packageName", "pN")
        put("description", "ds")
        put("parameterTypes", "pT")
        put("requiredProps", "rP")
        put("optionalProps", "oP")
        put("imports", "im")

        // ── KetoyJsonSchema ───────────────────────────────────
        put("ui", "U")
        put("components", "cM")
        put("fallbackMode", "fM")
    }

    /** Short alias → full property name (inverse of [encode]). */
    val decode: Map<String, String> =
        encode.entries.associate { (full, alias) -> alias to full }

    /**
     * Recursively rename all JSON object keys from full names to short aliases.
     *
     * Array elements and primitive values are passed through unchanged.
     * Unknown keys (not in the alias map) are kept as-is.
     */
    fun aliasKeys(element: JsonElement): JsonElement = when (element) {
        is JsonObject -> JsonObject(
            element.entries.associate { (key, value) ->
                (encode[key] ?: key) to aliasKeys(value)
            }
        )
        is JsonArray -> JsonArray(element.map { aliasKeys(it) })
        is JsonPrimitive -> element
    }

    /**
     * Recursively rename all JSON object keys from short aliases back to full names.
     *
     * This is the inverse of [aliasKeys]. Unknown keys are kept as-is.
     */
    fun expandKeys(element: JsonElement): JsonElement = when (element) {
        is JsonObject -> JsonObject(
            element.entries.associate { (key, value) ->
                (decode[key] ?: key) to expandKeys(value)
            }
        )
        is JsonArray -> JsonArray(element.map { expandKeys(it) })
        is JsonPrimitive -> element
    }
}
