package com.developerstring.ketoy.model

import kotlinx.serialization.*
import kotlinx.serialization.json.JsonClassDiscriminator

// ─────────────────────────────────────────────────────────────
//  Sealed KNode hierarchy – every SDUI component is a KNode
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type")
@Serializable
sealed class KNode

// ── Layout containers ───────────────────────────────────────

@Serializable @SerialName("Column")
data class KColumnNode(
    val props: KColumnProps = KColumnProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("Row")
data class KRowNode(
    val props: KRowProps = KRowProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("Box")
data class KBoxNode(
    val props: KBoxProps = KBoxProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("LazyColumn")
data class KLazyColumnNode(
    val props: KLazyColumnProps = KLazyColumnProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("LazyRow")
data class KLazyRowNode(
    val props: KLazyRowProps = KLazyRowProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("Button")
data class KButtonNode(
    val props: KButtonProps = KButtonProps(),
    val children: List<KNode> = emptyList()
) : KNode()

// ── Leaf widgets ────────────────────────────────────────────

@Serializable @SerialName("Text")
data class KTextNode(val props: KTextProps = KTextProps()) : KNode()

@Serializable @SerialName("Spacer")
data class KSpacerNode(val props: KSpacerProps = KSpacerProps()) : KNode()

@Serializable @SerialName("Card")
data class KCardNode(
    val props: KCardProps = KCardProps(),
    val children: MutableList<KNode> = mutableListOf()
) : KNode()

@Serializable @SerialName("TextField")
data class KTextFieldNode(val props: KTextFieldProps = KTextFieldProps()) : KNode()

@Serializable @SerialName("Image")
data class KImageNode(val props: KImageProps = KImageProps()) : KNode()

@Serializable @SerialName("Icon")
data class KIconNode(val props: KIconProps = KIconProps()) : KNode()

@Serializable @SerialName("IconButton")
data class KIconButtonNode(
    val props: KIconButtonProps = KIconButtonProps(),
    val children: List<KNode> = emptyList()
) : KNode()

// ── Custom component ────────────────────────────────────────

@Serializable @SerialName("Component")
data class KComponentNode(
    val props: KComponentProps = KComponentProps(),
    val children: List<KNode> = emptyList(),
    @Transient val metadata: KComponentMetadata? = null
) : KNode()

// ── Scaffold components ─────────────────────────────────────

@Serializable @SerialName("Scaffold")
data class KScaffoldNode(
    val props: KScaffoldProps = KScaffoldProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("TopAppBar")
data class KTopAppBarNode(
    val props: KTopAppBarProps = KTopAppBarProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("BottomAppBar")
data class KBottomAppBarNode(
    val props: KBottomAppBarProps = KBottomAppBarProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("NavigationBar")
data class KNavigationBarNode(
    val props: KNavigationBarProps = KNavigationBarProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("FloatingActionButton")
data class KFloatingActionButtonNode(
    val props: KFloatingActionButtonProps = KFloatingActionButtonProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("SnackBar")
data class KSnackBarNode(
    val props: KSnackBarProps = KSnackBarProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("SnackBarHost")
data class KSnackBarHostNode(val props: KSnackBarHostProps = KSnackBarHostProps()) : KNode()

@Serializable @SerialName("NavigationDrawerItem")
data class KNavigationDrawerItemNode(
    val props: KNavigationDrawerItemProps = KNavigationDrawerItemProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("CustomNavigationItem")
data class KCustomNavigationItemNode(
    val props: KCustomNavigationItemProps = KCustomNavigationItemProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("NavigationRail")
data class KNavigationRailNode(
    val props: KNavigationRailProps = KNavigationRailProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("NavigationRailItem")
data class KNavigationRailItemNode(
    val props: KNavigationRailItemProps = KNavigationRailItemProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("AppBarAction")
data class KAppBarActionNode(
    val props: KAppBarActionProps = KAppBarActionProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("NavigationBarItem")
data class KNavigationBarItemNode(
    val props: KNavigationBarItemProps = KNavigationBarItemProps(),
    val children: List<KNode> = emptyList()
) : KNode()

@Serializable @SerialName("ModalBottomSheet")
data class KModalBottomSheetNode(
    val props: KModalBottomSheetProps = KModalBottomSheetProps(),
    val children: List<KNode> = emptyList()
) : KNode()

// ── Data structures ─────────────────────────────────────────

@Serializable @SerialName("DataClass")
data class KDataClassNode(val props: KDataClassProps = KDataClassProps()) : KNode()

@Serializable @SerialName("Enum")
data class KEnumNode(val props: KEnumProps = KEnumProps()) : KNode()
