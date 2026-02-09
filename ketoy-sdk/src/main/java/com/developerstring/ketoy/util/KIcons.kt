package com.developerstring.ketoy.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.sharp.*
import androidx.compose.material.icons.twotone.*
import androidx.compose.ui.graphics.vector.ImageVector

// ─────────────────────────────────────────────────────────────
//  KIcons – Material3 icon registry
//
//  Maps icon names (strings) → ImageVector.
//  Mirrors the approach used by stac.dev for Flutter, where each
//  icon is referenced by a simple string key in JSON/DSL:
//
//    DSL  →  KIcon(icon = KIcons.Home)
//    JSON →  { "type": "Icon", "props": { "icon": "home" } }
//
//  Icons are organised by style: Filled (default), Outlined,
//  Rounded, Sharp, and TwoTone.
// ─────────────────────────────────────────────────────────────

/**
 * String constants for referencing Material3 icons by name.
 * Use these in the DSL for type-safe icon references.
 *
 * ```kotlin
 * KIcon(icon = KIcons.Home)
 * KIconButton(icon = KIcons.Menu, onClick = { })
 * ```
 */
object KIcons {
    // ── Common navigation & actions ─────────────────────
    const val Home = "home"
    const val Search = "search"
    const val Settings = "settings"
    const val Menu = "menu"
    const val Close = "close"
    const val ArrowBack = "arrow_back"
    const val ArrowForward = "arrow_forward"
    const val MoreVert = "more_vert"
    const val MoreHoriz = "more_horiz"
    const val Check = "check"
    const val Add = "add"
    const val Remove = "remove"
    const val Delete = "delete"
    const val Edit = "edit"
    const val Share = "share"
    const val Refresh = "refresh"

    // ── Content & media ─────────────────────────────────
    const val Favorite = "favorite"
    const val FavoriteBorder = "favorite_border"
    const val Star = "star"
    const val StarBorder = "star_border"
    const val ThumbUp = "thumb_up"
    const val ThumbDown = "thumb_down"
    const val Bookmark = "bookmark"
    const val BookmarkBorder = "bookmark_border"

    // ── Communication ───────────────────────────────────
    const val Email = "email"
    const val Call = "call"
    const val Chat = "chat"
    const val Send = "send"
    const val Notifications = "notifications"
    const val NotificationsNone = "notifications_none"

    // ── People & accounts ───────────────────────────────
    const val Person = "person"
    const val PersonAdd = "person_add"
    const val People = "people"
    const val AccountCircle = "account_circle"

    // ── Media playback ──────────────────────────────────
    const val PlayArrow = "play_arrow"
    const val Pause = "pause"
    const val Stop = "stop"
    const val SkipNext = "skip_next"
    const val SkipPrevious = "skip_previous"
    const val VolumeUp = "volume_up"
    const val VolumeOff = "volume_off"

    // ── File & data ─────────────────────────────────────
    const val Folder = "folder"
    const val FolderOpen = "folder_open"
    const val AttachFile = "attach_file"
    const val CloudUpload = "cloud_upload"
    const val CloudDownload = "cloud_download"
    const val Download = "download"
    const val Upload = "upload"

    // ── Navigation & maps ───────────────────────────────
    const val Place = "place"
    const val LocationOn = "location_on"
    const val MyLocation = "my_location"
    const val Directions = "directions"
    const val Map = "map"

    // ── Device & hardware ───────────────────────────────
    const val Wifi = "wifi"
    const val Bluetooth = "bluetooth"
    const val Battery = "battery_full"
    const val Camera = "camera"
    const val CameraAlt = "camera_alt"
    const val Phone = "phone"
    const val Keyboard = "keyboard"
    const val Print = "print"

    // ── UI & layout ─────────────────────────────────────
    const val Visibility = "visibility"
    const val VisibilityOff = "visibility_off"
    const val Lock = "lock"
    const val LockOpen = "lock_open"
    const val Info = "info"
    const val Warning = "warning"
    const val Error = "error"
    const val Help = "help"
    const val ShoppingCart = "shopping_cart"
    const val DateRange = "date_range"
    const val Schedule = "schedule"
    const val DarkMode = "dark_mode"
    const val LightMode = "light_mode"
    const val FilterList = "filter_list"
    const val Sort = "sort"
    const val ContentCopy = "content_copy"
    const val ContentPaste = "content_paste"
    const val ContentCut = "content_cut"
    const val Undo = "undo"
    const val Redo = "redo"
    const val ZoomIn = "zoom_in"
    const val ZoomOut = "zoom_out"
    const val FullScreen = "fullscreen"
    const val ExitFullScreen = "fullscreen_exit"
    const val Dashboard = "dashboard"
    const val Build = "build"
    const val Code = "code"
    const val BugReport = "bug_report"
    const val Palette = "palette"
    const val Extension = "extension"

    // ── Finance & commerce ──────────────────────────────
    const val AttachMoney = "attach_money"
    const val Savings = "savings"
    const val Wallet = "wallet"
    const val Receipt = "receipt"
    const val BarChart = "bar_chart"
    const val Insights = "insights"
    const val CreditCard = "credit_card"
    const val CallReceived = "call_received"
    const val AccountBalance = "account_balance"
    const val TrendingUp = "trending_up"
    const val TrendingDown = "trending_down"
    const val SwapHoriz = "swap_horiz"

    // ── Places & food ───────────────────────────────────
    const val LocalCafe = "local_cafe"
    const val Movie = "movie"
    const val Restaurant = "restaurant"

    // ── Travel & transport ──────────────────────────────
    const val DirectionsCar = "directions_car"

    // ── Health ──────────────────────────────────────────
    const val LocalHospital = "local_hospital"

    // ── Misc / UI ───────────────────────────────────────
    const val Language = "language"
    const val HelpOutline = "help_outline"
    const val ExitToApp = "exit_to_app"

    // ── Arrows & directions ─────────────────────────────
    const val ExpandMore = "expand_more"
    const val ExpandLess = "expand_less"
    const val ChevronLeft = "chevron_left"
    const val ChevronRight = "chevron_right"
    const val ArrowDropDown = "arrow_drop_down"
    const val ArrowDropUp = "arrow_drop_up"
    const val KeyboardArrowUp = "keyboard_arrow_up"
    const val KeyboardArrowDown = "keyboard_arrow_down"
    const val KeyboardArrowLeft = "keyboard_arrow_left"
    const val KeyboardArrowRight = "keyboard_arrow_right"

    // ── Styles ──────────────────────────────────────────
    /** Default icon style (Material Filled). */
    const val STYLE_FILLED = "filled"
    const val STYLE_OUTLINED = "outlined"
    const val STYLE_ROUNDED = "rounded"
    const val STYLE_SHARP = "sharp"
    const val STYLE_TWO_TONE = "twotone"
}

// ─────────────────────────────────────────────────────────────
//  Icon maps – string → ImageVector
// ─────────────────────────────────────────────────────────────

/**
 * Resolved the [ImageVector] for the given icon [name] and [style].
 *
 * Returns `null` if the icon name is not recognised.
 */
fun resolveIcon(name: String, style: String = KIcons.STYLE_FILLED): ImageVector? {
    val key = name.lowercase()
    val icon = when (style.lowercase()) {
        KIcons.STYLE_FILLED   -> filledIconMap[key]
        KIcons.STYLE_OUTLINED -> outlinedIconMap[key]
        KIcons.STYLE_ROUNDED  -> roundedIconMap[key]
        KIcons.STYLE_SHARP    -> sharpIconMap[key]
        KIcons.STYLE_TWO_TONE -> twoToneIconMap[key]
        else                  -> filledIconMap[key]
    }
    // Fall back to filled when the requested style doesn't contain the icon
    return icon ?: filledIconMap[key]
}

// ── Filled icons (default) ──────────────────────────────────

private val filledIconMap: Map<String, ImageVector> by lazy {
    mapOf(
        // Navigation & actions
        "home" to Icons.Filled.Home,
        "search" to Icons.Filled.Search,
        "settings" to Icons.Filled.Settings,
        "menu" to Icons.Filled.Menu,
        "close" to Icons.Filled.Close,
        "arrow_back" to Icons.AutoMirrored.Filled.ArrowBack,
        "arrow_forward" to Icons.AutoMirrored.Filled.ArrowForward,
        "more_vert" to Icons.Filled.MoreVert,
        "check" to Icons.Filled.Check,
        "add" to Icons.Filled.Add,
        "delete" to Icons.Filled.Delete,
        "edit" to Icons.Filled.Edit,
        "share" to Icons.Filled.Share,
        "refresh" to Icons.Filled.Refresh,
        "clear" to Icons.Filled.Clear,

        // Content & media
        "favorite" to Icons.Filled.Favorite,
        "favorite_border" to Icons.Filled.FavoriteBorder,
        "star" to Icons.Filled.Star,
        "thumb_up" to Icons.Filled.ThumbUp,
        "bookmark" to Icons.Filled.Bookmark,

        // Communication
        "email" to Icons.Filled.Email,
        "call" to Icons.Filled.Call,
        "send" to Icons.AutoMirrored.Filled.Send,
        "notifications" to Icons.Filled.Notifications,

        // People & accounts
        "person" to Icons.Filled.Person,
        "person_add" to Icons.Filled.PersonAdd,
        "account_circle" to Icons.Filled.AccountCircle,

        // Media playback
        "play_arrow" to Icons.Filled.PlayArrow,

        // File & data
        "folder" to Icons.Filled.Folder,
        "attach_file" to Icons.Filled.AttachFile,
        "cloud" to Icons.Filled.Cloud,

        // Navigation & maps
        "place" to Icons.Filled.Place,
        "location_on" to Icons.Filled.LocationOn,

        // Device & hardware
        "wifi" to Icons.Filled.Wifi,
        "bluetooth" to Icons.Filled.Bluetooth,
        "camera" to Icons.Filled.Camera,
        "phone" to Icons.Filled.Phone,
        "keyboard" to Icons.Filled.Keyboard,

        // UI & layout
        "visibility" to Icons.Filled.Visibility,
        "visibility_off" to Icons.Filled.VisibilityOff,
        "lock" to Icons.Filled.Lock,
        "info" to Icons.Filled.Info,
        "warning" to Icons.Filled.Warning,
        "error" to Icons.Filled.Error,
        "help" to Icons.AutoMirrored.Filled.Help,
        "shopping_cart" to Icons.Filled.ShoppingCart,
        "date_range" to Icons.Filled.DateRange,
        "schedule" to Icons.Filled.Schedule,
        "dark_mode" to Icons.Filled.DarkMode,
        "light_mode" to Icons.Filled.LightMode,
        "filter_list" to Icons.Filled.FilterList,
        "sort" to Icons.AutoMirrored.Filled.Sort,
        "content_copy" to Icons.Filled.ContentCopy,
        "content_paste" to Icons.Filled.ContentPaste,
        "content_cut" to Icons.Filled.ContentCut,
        "zoom_in" to Icons.Filled.ZoomIn,
        "zoom_out" to Icons.Filled.ZoomOut,
        "fullscreen" to Icons.Filled.Fullscreen,
        "fullscreen_exit" to Icons.Filled.FullscreenExit,
        "dashboard" to Icons.Filled.Dashboard,
        "build" to Icons.Filled.Build,
        "code" to Icons.Filled.Code,
        "bug_report" to Icons.Filled.BugReport,
        "palette" to Icons.Filled.Palette,
        "extension" to Icons.Filled.Extension,

        // Arrows & directions
        "expand_more" to Icons.Filled.ExpandMore,
        "expand_less" to Icons.Filled.ExpandLess,
        "chevron_left" to Icons.Filled.ChevronLeft,
        "chevron_right" to Icons.Filled.ChevronRight,
        "arrow_drop_down" to Icons.Filled.ArrowDropDown,
        "keyboard_arrow_up" to Icons.Filled.KeyboardArrowUp,
        "keyboard_arrow_down" to Icons.Filled.KeyboardArrowDown,
        "keyboard_arrow_left" to Icons.AutoMirrored.Filled.KeyboardArrowLeft,
        "keyboard_arrow_right" to Icons.AutoMirrored.Filled.KeyboardArrowRight,

        // Finance & commerce
        "attach_money" to Icons.Filled.AttachMoney,
        "savings" to Icons.Filled.Savings,
        "wallet" to Icons.Filled.Wallet,
        "receipt" to Icons.Filled.Receipt,
        "bar_chart" to Icons.Filled.BarChart,
        "insights" to Icons.Filled.Insights,
        "credit_card" to Icons.Filled.CreditCard,
        "call_received" to Icons.AutoMirrored.Filled.CallReceived,
        "account_balance" to Icons.Filled.AccountBalance,
        "trending_up" to Icons.AutoMirrored.Filled.TrendingUp,
        "trending_down" to Icons.AutoMirrored.Filled.TrendingDown,
        "swap_horiz" to Icons.Filled.SwapHoriz,

        // Places & food
        "local_cafe" to Icons.Filled.LocalCafe,
        "movie" to Icons.Filled.Movie,
        "restaurant" to Icons.Filled.Restaurant,

        // Travel & transport
        "directions_car" to Icons.Filled.DirectionsCar,

        // Health
        "local_hospital" to Icons.Filled.LocalHospital,

        // File & download
        "download" to Icons.Filled.Download,

        // Misc / UI
        "language" to Icons.Filled.Language,
        "help_outline" to Icons.AutoMirrored.Filled.Help,
        "exit_to_app" to Icons.AutoMirrored.Filled.ExitToApp,
    )
}

// ── Outlined icons ──────────────────────────────────────────

private val outlinedIconMap: Map<String, ImageVector> by lazy {
    mapOf(
        "home" to Icons.Outlined.Home,
        "search" to Icons.Outlined.Search,
        "settings" to Icons.Outlined.Settings,
        "menu" to Icons.Outlined.Menu,
        "close" to Icons.Outlined.Close,
        "check" to Icons.Outlined.Check,
        "add" to Icons.Outlined.Add,
        "delete" to Icons.Outlined.Delete,
        "edit" to Icons.Outlined.Edit,
        "share" to Icons.Outlined.Share,
        "refresh" to Icons.Outlined.Refresh,
        "clear" to Icons.Outlined.Clear,
        "favorite" to Icons.Outlined.Favorite,
        "favorite_border" to Icons.Outlined.FavoriteBorder,
        "star" to Icons.Outlined.Star,
        "thumb_up" to Icons.Outlined.ThumbUp,
        "bookmark" to Icons.Outlined.Bookmark,
        "email" to Icons.Outlined.Email,
        "call" to Icons.Outlined.Call,
        "notifications" to Icons.Outlined.Notifications,
        "person" to Icons.Outlined.Person,
        "account_circle" to Icons.Outlined.AccountCircle,
        "play_arrow" to Icons.Outlined.PlayArrow,
        "folder" to Icons.Outlined.Folder,
        "place" to Icons.Outlined.Place,
        "location_on" to Icons.Outlined.LocationOn,
        "wifi" to Icons.Outlined.Wifi,
        "bluetooth" to Icons.Outlined.Bluetooth,
        "camera" to Icons.Outlined.Camera,
        "phone" to Icons.Outlined.Phone,
        "keyboard" to Icons.Outlined.Keyboard,
        "visibility" to Icons.Outlined.Visibility,
        "visibility_off" to Icons.Outlined.VisibilityOff,
        "lock" to Icons.Outlined.Lock,
        "info" to Icons.Outlined.Info,
        "warning" to Icons.Outlined.Warning,
        "shopping_cart" to Icons.Outlined.ShoppingCart,
        "date_range" to Icons.Outlined.DateRange,
        "schedule" to Icons.Outlined.Schedule,
        "dark_mode" to Icons.Outlined.DarkMode,
        "light_mode" to Icons.Outlined.LightMode,
        "filter_list" to Icons.Outlined.FilterList,
        "build" to Icons.Outlined.Build,
        "code" to Icons.Outlined.Code,
        "bug_report" to Icons.Outlined.BugReport,
        "palette" to Icons.Outlined.Palette,
        "extension" to Icons.Outlined.Extension,
        "expand_more" to Icons.Outlined.ExpandMore,
        "expand_less" to Icons.Outlined.ExpandLess,
        "chevron_left" to Icons.Outlined.ChevronLeft,
        "chevron_right" to Icons.Outlined.ChevronRight,
        "arrow_drop_down" to Icons.Outlined.ArrowDropDown,
        "keyboard_arrow_up" to Icons.Outlined.KeyboardArrowUp,
        "keyboard_arrow_down" to Icons.Outlined.KeyboardArrowDown,
        "keyboard_arrow_left" to Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
        "keyboard_arrow_right" to Icons.AutoMirrored.Outlined.KeyboardArrowRight,
    )
}

// ── Rounded icons ───────────────────────────────────────────

private val roundedIconMap: Map<String, ImageVector> by lazy {
    mapOf(
        "home" to Icons.Rounded.Home,
        "search" to Icons.Rounded.Search,
        "settings" to Icons.Rounded.Settings,
        "menu" to Icons.Rounded.Menu,
        "close" to Icons.Rounded.Close,
        "check" to Icons.Rounded.Check,
        "add" to Icons.Rounded.Add,
        "delete" to Icons.Rounded.Delete,
        "edit" to Icons.Rounded.Edit,
        "share" to Icons.Rounded.Share,
        "refresh" to Icons.Rounded.Refresh,
        "clear" to Icons.Rounded.Clear,
        "favorite" to Icons.Rounded.Favorite,
        "favorite_border" to Icons.Rounded.FavoriteBorder,
        "star" to Icons.Rounded.Star,
        "thumb_up" to Icons.Rounded.ThumbUp,
        "email" to Icons.Rounded.Email,
        "call" to Icons.Rounded.Call,
        "notifications" to Icons.Rounded.Notifications,
        "person" to Icons.Rounded.Person,
        "account_circle" to Icons.Rounded.AccountCircle,
        "play_arrow" to Icons.Rounded.PlayArrow,
        "folder" to Icons.Rounded.Folder,
        "place" to Icons.Rounded.Place,
        "location_on" to Icons.Rounded.LocationOn,
        "wifi" to Icons.Rounded.Wifi,
        "bluetooth" to Icons.Rounded.Bluetooth,
        "camera" to Icons.Rounded.Camera,
        "phone" to Icons.Rounded.Phone,
        "keyboard" to Icons.Rounded.Keyboard,
        "lock" to Icons.Rounded.Lock,
        "info" to Icons.Rounded.Info,
        "warning" to Icons.Rounded.Warning,
        "shopping_cart" to Icons.Rounded.ShoppingCart,
        "date_range" to Icons.Rounded.DateRange,
        "schedule" to Icons.Rounded.Schedule,
        "dark_mode" to Icons.Rounded.DarkMode,
        "light_mode" to Icons.Rounded.LightMode,
        "build" to Icons.Rounded.Build,
        "code" to Icons.Rounded.Code,
        "expand_more" to Icons.Rounded.ExpandMore,
        "expand_less" to Icons.Rounded.ExpandLess,
        "chevron_left" to Icons.Rounded.ChevronLeft,
        "chevron_right" to Icons.Rounded.ChevronRight,
        "arrow_drop_down" to Icons.Rounded.ArrowDropDown,
        "keyboard_arrow_up" to Icons.Rounded.KeyboardArrowUp,
        "keyboard_arrow_down" to Icons.Rounded.KeyboardArrowDown,
    )
}

// ── Sharp icons ─────────────────────────────────────────────

private val sharpIconMap: Map<String, ImageVector> by lazy {
    mapOf(
        "home" to Icons.Sharp.Home,
        "search" to Icons.Sharp.Search,
        "settings" to Icons.Sharp.Settings,
        "menu" to Icons.Sharp.Menu,
        "close" to Icons.Sharp.Close,
        "check" to Icons.Sharp.Check,
        "add" to Icons.Sharp.Add,
        "delete" to Icons.Sharp.Delete,
        "edit" to Icons.Sharp.Edit,
        "share" to Icons.Sharp.Share,
        "refresh" to Icons.Sharp.Refresh,
        "clear" to Icons.Sharp.Clear,
        "favorite" to Icons.Sharp.Favorite,
        "favorite_border" to Icons.Sharp.FavoriteBorder,
        "star" to Icons.Sharp.Star,
        "email" to Icons.Sharp.Email,
        "call" to Icons.Sharp.Call,
        "notifications" to Icons.Sharp.Notifications,
        "person" to Icons.Sharp.Person,
        "account_circle" to Icons.Sharp.AccountCircle,
        "play_arrow" to Icons.Sharp.PlayArrow,
        "folder" to Icons.Sharp.Folder,
        "place" to Icons.Sharp.Place,
        "location_on" to Icons.Sharp.LocationOn,
        "wifi" to Icons.Sharp.Wifi,
        "bluetooth" to Icons.Sharp.Bluetooth,
        "camera" to Icons.Sharp.Camera,
        "phone" to Icons.Sharp.Phone,
        "lock" to Icons.Sharp.Lock,
        "info" to Icons.Sharp.Info,
        "warning" to Icons.Sharp.Warning,
        "shopping_cart" to Icons.Sharp.ShoppingCart,
        "build" to Icons.Sharp.Build,
        "expand_more" to Icons.Sharp.ExpandMore,
        "expand_less" to Icons.Sharp.ExpandLess,
        "chevron_left" to Icons.Sharp.ChevronLeft,
        "chevron_right" to Icons.Sharp.ChevronRight,
        "arrow_drop_down" to Icons.Sharp.ArrowDropDown,
    )
}

// ── Two-tone icons ──────────────────────────────────────────

private val twoToneIconMap: Map<String, ImageVector> by lazy {
    mapOf(
        "home" to Icons.TwoTone.Home,
        "search" to Icons.TwoTone.Search,
        "settings" to Icons.TwoTone.Settings,
        "menu" to Icons.TwoTone.Menu,
        "close" to Icons.TwoTone.Close,
        "check" to Icons.TwoTone.Check,
        "add" to Icons.TwoTone.Add,
        "delete" to Icons.TwoTone.Delete,
        "edit" to Icons.TwoTone.Edit,
        "share" to Icons.TwoTone.Share,
        "refresh" to Icons.TwoTone.Refresh,
        "clear" to Icons.TwoTone.Clear,
        "favorite" to Icons.TwoTone.Favorite,
        "favorite_border" to Icons.TwoTone.FavoriteBorder,
        "star" to Icons.TwoTone.Star,
        "email" to Icons.TwoTone.Email,
        "call" to Icons.TwoTone.Call,
        "notifications" to Icons.TwoTone.Notifications,
        "person" to Icons.TwoTone.Person,
        "account_circle" to Icons.TwoTone.AccountCircle,
        "play_arrow" to Icons.TwoTone.PlayArrow,
        "folder" to Icons.TwoTone.Folder,
        "place" to Icons.TwoTone.Place,
        "location_on" to Icons.TwoTone.LocationOn,
        "wifi" to Icons.TwoTone.Wifi,
        "bluetooth" to Icons.TwoTone.Bluetooth,
        "camera" to Icons.TwoTone.Camera,
        "phone" to Icons.TwoTone.Phone,
        "lock" to Icons.TwoTone.Lock,
        "info" to Icons.TwoTone.Info,
        "warning" to Icons.TwoTone.Warning,
        "shopping_cart" to Icons.TwoTone.ShoppingCart,
        "build" to Icons.TwoTone.Build,
        "expand_more" to Icons.TwoTone.ExpandMore,
        "expand_less" to Icons.TwoTone.ExpandLess,
        "chevron_left" to Icons.TwoTone.ChevronLeft,
        "chevron_right" to Icons.TwoTone.ChevronRight,
        "arrow_drop_down" to Icons.TwoTone.ArrowDropDown,
    )
}
