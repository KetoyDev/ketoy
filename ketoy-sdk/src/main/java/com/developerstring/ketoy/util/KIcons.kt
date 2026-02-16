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
//  Maps icon names (strings) → ImageVector, where each icon is
//  referenced by a simple string key in JSON/DSL:
//
//    DSL  →  KIcon(icon = KIcons.Home)
//    DSL  →  KIcon(icon = KIcons.Outlined.Home)
//    JSON →  { "type": "Icon", "props": { "icon": "home" } }
//
//  Icons are organised by style: Filled (default), Outlined,
//  Rounded, Sharp, and TwoTone.
// ─────────────────────────────────────────────────────────────

/**
 * Bundles an icon name with its style for type-safe icon references.
 *
 * ```kotlin
 * val ref = KIcons.Outlined.Home   // KIconRef("home", "outlined")
 * KIcon(icon = ref)
 * ```
 */
data class KIconRef(
    val name: String,
    val style: String,
)

/**
 * String constants for referencing Material3 icons by name.
 * Use these in the DSL for type-safe icon references.
 *
 * ```kotlin
 * // Simple (defaults to Filled):
 * KIcon(icon = KIcons.Home)
 *
 * // Style-qualified:
 * KIcon(icon = KIcons.Outlined.Home)
 * KIcon(icon = KIcons.Rounded.Settings)
 * KIcon(icon = KIcons.Sharp.Delete)
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

    // ── Style-qualified icon references ──────────────────
    //    Usage: KIcons.Filled.Home, KIcons.Outlined.Search, etc.

    object Filled {
        val Home get() = KIconRef(KIcons.Home, STYLE_FILLED)
        val Search get() = KIconRef(KIcons.Search, STYLE_FILLED)
        val Settings get() = KIconRef(KIcons.Settings, STYLE_FILLED)
        val Menu get() = KIconRef(KIcons.Menu, STYLE_FILLED)
        val Close get() = KIconRef(KIcons.Close, STYLE_FILLED)
        val ArrowBack get() = KIconRef(KIcons.ArrowBack, STYLE_FILLED)
        val ArrowForward get() = KIconRef(KIcons.ArrowForward, STYLE_FILLED)
        val MoreVert get() = KIconRef(KIcons.MoreVert, STYLE_FILLED)
        val MoreHoriz get() = KIconRef(KIcons.MoreHoriz, STYLE_FILLED)
        val Check get() = KIconRef(KIcons.Check, STYLE_FILLED)
        val Add get() = KIconRef(KIcons.Add, STYLE_FILLED)
        val Remove get() = KIconRef(KIcons.Remove, STYLE_FILLED)
        val Delete get() = KIconRef(KIcons.Delete, STYLE_FILLED)
        val Edit get() = KIconRef(KIcons.Edit, STYLE_FILLED)
        val Share get() = KIconRef(KIcons.Share, STYLE_FILLED)
        val Refresh get() = KIconRef(KIcons.Refresh, STYLE_FILLED)
        val Favorite get() = KIconRef(KIcons.Favorite, STYLE_FILLED)
        val FavoriteBorder get() = KIconRef(KIcons.FavoriteBorder, STYLE_FILLED)
        val Star get() = KIconRef(KIcons.Star, STYLE_FILLED)
        val StarBorder get() = KIconRef(KIcons.StarBorder, STYLE_FILLED)
        val ThumbUp get() = KIconRef(KIcons.ThumbUp, STYLE_FILLED)
        val ThumbDown get() = KIconRef(KIcons.ThumbDown, STYLE_FILLED)
        val Bookmark get() = KIconRef(KIcons.Bookmark, STYLE_FILLED)
        val BookmarkBorder get() = KIconRef(KIcons.BookmarkBorder, STYLE_FILLED)
        val Email get() = KIconRef(KIcons.Email, STYLE_FILLED)
        val Call get() = KIconRef(KIcons.Call, STYLE_FILLED)
        val Chat get() = KIconRef(KIcons.Chat, STYLE_FILLED)
        val Send get() = KIconRef(KIcons.Send, STYLE_FILLED)
        val Notifications get() = KIconRef(KIcons.Notifications, STYLE_FILLED)
        val NotificationsNone get() = KIconRef(KIcons.NotificationsNone, STYLE_FILLED)
        val Person get() = KIconRef(KIcons.Person, STYLE_FILLED)
        val PersonAdd get() = KIconRef(KIcons.PersonAdd, STYLE_FILLED)
        val People get() = KIconRef(KIcons.People, STYLE_FILLED)
        val AccountCircle get() = KIconRef(KIcons.AccountCircle, STYLE_FILLED)
        val PlayArrow get() = KIconRef(KIcons.PlayArrow, STYLE_FILLED)
        val Pause get() = KIconRef(KIcons.Pause, STYLE_FILLED)
        val Stop get() = KIconRef(KIcons.Stop, STYLE_FILLED)
        val SkipNext get() = KIconRef(KIcons.SkipNext, STYLE_FILLED)
        val SkipPrevious get() = KIconRef(KIcons.SkipPrevious, STYLE_FILLED)
        val VolumeUp get() = KIconRef(KIcons.VolumeUp, STYLE_FILLED)
        val VolumeOff get() = KIconRef(KIcons.VolumeOff, STYLE_FILLED)
        val Folder get() = KIconRef(KIcons.Folder, STYLE_FILLED)
        val FolderOpen get() = KIconRef(KIcons.FolderOpen, STYLE_FILLED)
        val AttachFile get() = KIconRef(KIcons.AttachFile, STYLE_FILLED)
        val CloudUpload get() = KIconRef(KIcons.CloudUpload, STYLE_FILLED)
        val CloudDownload get() = KIconRef(KIcons.CloudDownload, STYLE_FILLED)
        val Download get() = KIconRef(KIcons.Download, STYLE_FILLED)
        val Upload get() = KIconRef(KIcons.Upload, STYLE_FILLED)
        val Place get() = KIconRef(KIcons.Place, STYLE_FILLED)
        val LocationOn get() = KIconRef(KIcons.LocationOn, STYLE_FILLED)
        val MyLocation get() = KIconRef(KIcons.MyLocation, STYLE_FILLED)
        val Directions get() = KIconRef(KIcons.Directions, STYLE_FILLED)
        val Map get() = KIconRef(KIcons.Map, STYLE_FILLED)
        val Wifi get() = KIconRef(KIcons.Wifi, STYLE_FILLED)
        val Bluetooth get() = KIconRef(KIcons.Bluetooth, STYLE_FILLED)
        val Battery get() = KIconRef(KIcons.Battery, STYLE_FILLED)
        val Camera get() = KIconRef(KIcons.Camera, STYLE_FILLED)
        val CameraAlt get() = KIconRef(KIcons.CameraAlt, STYLE_FILLED)
        val Phone get() = KIconRef(KIcons.Phone, STYLE_FILLED)
        val Keyboard get() = KIconRef(KIcons.Keyboard, STYLE_FILLED)
        val Print get() = KIconRef(KIcons.Print, STYLE_FILLED)
        val Visibility get() = KIconRef(KIcons.Visibility, STYLE_FILLED)
        val VisibilityOff get() = KIconRef(KIcons.VisibilityOff, STYLE_FILLED)
        val Lock get() = KIconRef(KIcons.Lock, STYLE_FILLED)
        val LockOpen get() = KIconRef(KIcons.LockOpen, STYLE_FILLED)
        val Info get() = KIconRef(KIcons.Info, STYLE_FILLED)
        val Warning get() = KIconRef(KIcons.Warning, STYLE_FILLED)
        val Error get() = KIconRef(KIcons.Error, STYLE_FILLED)
        val Help get() = KIconRef(KIcons.Help, STYLE_FILLED)
        val ShoppingCart get() = KIconRef(KIcons.ShoppingCart, STYLE_FILLED)
        val DateRange get() = KIconRef(KIcons.DateRange, STYLE_FILLED)
        val Schedule get() = KIconRef(KIcons.Schedule, STYLE_FILLED)
        val DarkMode get() = KIconRef(KIcons.DarkMode, STYLE_FILLED)
        val LightMode get() = KIconRef(KIcons.LightMode, STYLE_FILLED)
        val FilterList get() = KIconRef(KIcons.FilterList, STYLE_FILLED)
        val Sort get() = KIconRef(KIcons.Sort, STYLE_FILLED)
        val ContentCopy get() = KIconRef(KIcons.ContentCopy, STYLE_FILLED)
        val ContentPaste get() = KIconRef(KIcons.ContentPaste, STYLE_FILLED)
        val ContentCut get() = KIconRef(KIcons.ContentCut, STYLE_FILLED)
        val Undo get() = KIconRef(KIcons.Undo, STYLE_FILLED)
        val Redo get() = KIconRef(KIcons.Redo, STYLE_FILLED)
        val ZoomIn get() = KIconRef(KIcons.ZoomIn, STYLE_FILLED)
        val ZoomOut get() = KIconRef(KIcons.ZoomOut, STYLE_FILLED)
        val FullScreen get() = KIconRef(KIcons.FullScreen, STYLE_FILLED)
        val ExitFullScreen get() = KIconRef(KIcons.ExitFullScreen, STYLE_FILLED)
        val Dashboard get() = KIconRef(KIcons.Dashboard, STYLE_FILLED)
        val Build get() = KIconRef(KIcons.Build, STYLE_FILLED)
        val Code get() = KIconRef(KIcons.Code, STYLE_FILLED)
        val BugReport get() = KIconRef(KIcons.BugReport, STYLE_FILLED)
        val Palette get() = KIconRef(KIcons.Palette, STYLE_FILLED)
        val Extension get() = KIconRef(KIcons.Extension, STYLE_FILLED)
        val AttachMoney get() = KIconRef(KIcons.AttachMoney, STYLE_FILLED)
        val Savings get() = KIconRef(KIcons.Savings, STYLE_FILLED)
        val Wallet get() = KIconRef(KIcons.Wallet, STYLE_FILLED)
        val Receipt get() = KIconRef(KIcons.Receipt, STYLE_FILLED)
        val BarChart get() = KIconRef(KIcons.BarChart, STYLE_FILLED)
        val Insights get() = KIconRef(KIcons.Insights, STYLE_FILLED)
        val CreditCard get() = KIconRef(KIcons.CreditCard, STYLE_FILLED)
        val CallReceived get() = KIconRef(KIcons.CallReceived, STYLE_FILLED)
        val AccountBalance get() = KIconRef(KIcons.AccountBalance, STYLE_FILLED)
        val TrendingUp get() = KIconRef(KIcons.TrendingUp, STYLE_FILLED)
        val TrendingDown get() = KIconRef(KIcons.TrendingDown, STYLE_FILLED)
        val SwapHoriz get() = KIconRef(KIcons.SwapHoriz, STYLE_FILLED)
        val LocalCafe get() = KIconRef(KIcons.LocalCafe, STYLE_FILLED)
        val Movie get() = KIconRef(KIcons.Movie, STYLE_FILLED)
        val Restaurant get() = KIconRef(KIcons.Restaurant, STYLE_FILLED)
        val DirectionsCar get() = KIconRef(KIcons.DirectionsCar, STYLE_FILLED)
        val LocalHospital get() = KIconRef(KIcons.LocalHospital, STYLE_FILLED)
        val Language get() = KIconRef(KIcons.Language, STYLE_FILLED)
        val HelpOutline get() = KIconRef(KIcons.HelpOutline, STYLE_FILLED)
        val ExitToApp get() = KIconRef(KIcons.ExitToApp, STYLE_FILLED)
        val ExpandMore get() = KIconRef(KIcons.ExpandMore, STYLE_FILLED)
        val ExpandLess get() = KIconRef(KIcons.ExpandLess, STYLE_FILLED)
        val ChevronLeft get() = KIconRef(KIcons.ChevronLeft, STYLE_FILLED)
        val ChevronRight get() = KIconRef(KIcons.ChevronRight, STYLE_FILLED)
        val ArrowDropDown get() = KIconRef(KIcons.ArrowDropDown, STYLE_FILLED)
        val ArrowDropUp get() = KIconRef(KIcons.ArrowDropUp, STYLE_FILLED)
        val KeyboardArrowUp get() = KIconRef(KIcons.KeyboardArrowUp, STYLE_FILLED)
        val KeyboardArrowDown get() = KIconRef(KIcons.KeyboardArrowDown, STYLE_FILLED)
        val KeyboardArrowLeft get() = KIconRef(KIcons.KeyboardArrowLeft, STYLE_FILLED)
        val KeyboardArrowRight get() = KIconRef(KIcons.KeyboardArrowRight, STYLE_FILLED)
    }

    object Outlined {
        val Home get() = KIconRef(KIcons.Home, STYLE_OUTLINED)
        val Search get() = KIconRef(KIcons.Search, STYLE_OUTLINED)
        val Settings get() = KIconRef(KIcons.Settings, STYLE_OUTLINED)
        val Menu get() = KIconRef(KIcons.Menu, STYLE_OUTLINED)
        val Close get() = KIconRef(KIcons.Close, STYLE_OUTLINED)
        val ArrowBack get() = KIconRef(KIcons.ArrowBack, STYLE_OUTLINED)
        val ArrowForward get() = KIconRef(KIcons.ArrowForward, STYLE_OUTLINED)
        val MoreVert get() = KIconRef(KIcons.MoreVert, STYLE_OUTLINED)
        val MoreHoriz get() = KIconRef(KIcons.MoreHoriz, STYLE_OUTLINED)
        val Check get() = KIconRef(KIcons.Check, STYLE_OUTLINED)
        val Add get() = KIconRef(KIcons.Add, STYLE_OUTLINED)
        val Remove get() = KIconRef(KIcons.Remove, STYLE_OUTLINED)
        val Delete get() = KIconRef(KIcons.Delete, STYLE_OUTLINED)
        val Edit get() = KIconRef(KIcons.Edit, STYLE_OUTLINED)
        val Share get() = KIconRef(KIcons.Share, STYLE_OUTLINED)
        val Refresh get() = KIconRef(KIcons.Refresh, STYLE_OUTLINED)
        val Favorite get() = KIconRef(KIcons.Favorite, STYLE_OUTLINED)
        val FavoriteBorder get() = KIconRef(KIcons.FavoriteBorder, STYLE_OUTLINED)
        val Star get() = KIconRef(KIcons.Star, STYLE_OUTLINED)
        val StarBorder get() = KIconRef(KIcons.StarBorder, STYLE_OUTLINED)
        val ThumbUp get() = KIconRef(KIcons.ThumbUp, STYLE_OUTLINED)
        val ThumbDown get() = KIconRef(KIcons.ThumbDown, STYLE_OUTLINED)
        val Bookmark get() = KIconRef(KIcons.Bookmark, STYLE_OUTLINED)
        val BookmarkBorder get() = KIconRef(KIcons.BookmarkBorder, STYLE_OUTLINED)
        val Email get() = KIconRef(KIcons.Email, STYLE_OUTLINED)
        val Call get() = KIconRef(KIcons.Call, STYLE_OUTLINED)
        val Chat get() = KIconRef(KIcons.Chat, STYLE_OUTLINED)
        val Send get() = KIconRef(KIcons.Send, STYLE_OUTLINED)
        val Notifications get() = KIconRef(KIcons.Notifications, STYLE_OUTLINED)
        val NotificationsNone get() = KIconRef(KIcons.NotificationsNone, STYLE_OUTLINED)
        val Person get() = KIconRef(KIcons.Person, STYLE_OUTLINED)
        val PersonAdd get() = KIconRef(KIcons.PersonAdd, STYLE_OUTLINED)
        val People get() = KIconRef(KIcons.People, STYLE_OUTLINED)
        val AccountCircle get() = KIconRef(KIcons.AccountCircle, STYLE_OUTLINED)
        val PlayArrow get() = KIconRef(KIcons.PlayArrow, STYLE_OUTLINED)
        val Pause get() = KIconRef(KIcons.Pause, STYLE_OUTLINED)
        val Stop get() = KIconRef(KIcons.Stop, STYLE_OUTLINED)
        val SkipNext get() = KIconRef(KIcons.SkipNext, STYLE_OUTLINED)
        val SkipPrevious get() = KIconRef(KIcons.SkipPrevious, STYLE_OUTLINED)
        val VolumeUp get() = KIconRef(KIcons.VolumeUp, STYLE_OUTLINED)
        val VolumeOff get() = KIconRef(KIcons.VolumeOff, STYLE_OUTLINED)
        val Folder get() = KIconRef(KIcons.Folder, STYLE_OUTLINED)
        val FolderOpen get() = KIconRef(KIcons.FolderOpen, STYLE_OUTLINED)
        val AttachFile get() = KIconRef(KIcons.AttachFile, STYLE_OUTLINED)
        val CloudUpload get() = KIconRef(KIcons.CloudUpload, STYLE_OUTLINED)
        val CloudDownload get() = KIconRef(KIcons.CloudDownload, STYLE_OUTLINED)
        val Download get() = KIconRef(KIcons.Download, STYLE_OUTLINED)
        val Upload get() = KIconRef(KIcons.Upload, STYLE_OUTLINED)
        val Place get() = KIconRef(KIcons.Place, STYLE_OUTLINED)
        val LocationOn get() = KIconRef(KIcons.LocationOn, STYLE_OUTLINED)
        val MyLocation get() = KIconRef(KIcons.MyLocation, STYLE_OUTLINED)
        val Directions get() = KIconRef(KIcons.Directions, STYLE_OUTLINED)
        val Map get() = KIconRef(KIcons.Map, STYLE_OUTLINED)
        val Wifi get() = KIconRef(KIcons.Wifi, STYLE_OUTLINED)
        val Bluetooth get() = KIconRef(KIcons.Bluetooth, STYLE_OUTLINED)
        val Battery get() = KIconRef(KIcons.Battery, STYLE_OUTLINED)
        val Camera get() = KIconRef(KIcons.Camera, STYLE_OUTLINED)
        val CameraAlt get() = KIconRef(KIcons.CameraAlt, STYLE_OUTLINED)
        val Phone get() = KIconRef(KIcons.Phone, STYLE_OUTLINED)
        val Keyboard get() = KIconRef(KIcons.Keyboard, STYLE_OUTLINED)
        val Print get() = KIconRef(KIcons.Print, STYLE_OUTLINED)
        val Visibility get() = KIconRef(KIcons.Visibility, STYLE_OUTLINED)
        val VisibilityOff get() = KIconRef(KIcons.VisibilityOff, STYLE_OUTLINED)
        val Lock get() = KIconRef(KIcons.Lock, STYLE_OUTLINED)
        val LockOpen get() = KIconRef(KIcons.LockOpen, STYLE_OUTLINED)
        val Info get() = KIconRef(KIcons.Info, STYLE_OUTLINED)
        val Warning get() = KIconRef(KIcons.Warning, STYLE_OUTLINED)
        val Error get() = KIconRef(KIcons.Error, STYLE_OUTLINED)
        val Help get() = KIconRef(KIcons.Help, STYLE_OUTLINED)
        val ShoppingCart get() = KIconRef(KIcons.ShoppingCart, STYLE_OUTLINED)
        val DateRange get() = KIconRef(KIcons.DateRange, STYLE_OUTLINED)
        val Schedule get() = KIconRef(KIcons.Schedule, STYLE_OUTLINED)
        val DarkMode get() = KIconRef(KIcons.DarkMode, STYLE_OUTLINED)
        val LightMode get() = KIconRef(KIcons.LightMode, STYLE_OUTLINED)
        val FilterList get() = KIconRef(KIcons.FilterList, STYLE_OUTLINED)
        val Sort get() = KIconRef(KIcons.Sort, STYLE_OUTLINED)
        val ContentCopy get() = KIconRef(KIcons.ContentCopy, STYLE_OUTLINED)
        val ContentPaste get() = KIconRef(KIcons.ContentPaste, STYLE_OUTLINED)
        val ContentCut get() = KIconRef(KIcons.ContentCut, STYLE_OUTLINED)
        val Undo get() = KIconRef(KIcons.Undo, STYLE_OUTLINED)
        val Redo get() = KIconRef(KIcons.Redo, STYLE_OUTLINED)
        val ZoomIn get() = KIconRef(KIcons.ZoomIn, STYLE_OUTLINED)
        val ZoomOut get() = KIconRef(KIcons.ZoomOut, STYLE_OUTLINED)
        val FullScreen get() = KIconRef(KIcons.FullScreen, STYLE_OUTLINED)
        val ExitFullScreen get() = KIconRef(KIcons.ExitFullScreen, STYLE_OUTLINED)
        val Dashboard get() = KIconRef(KIcons.Dashboard, STYLE_OUTLINED)
        val Build get() = KIconRef(KIcons.Build, STYLE_OUTLINED)
        val Code get() = KIconRef(KIcons.Code, STYLE_OUTLINED)
        val BugReport get() = KIconRef(KIcons.BugReport, STYLE_OUTLINED)
        val Palette get() = KIconRef(KIcons.Palette, STYLE_OUTLINED)
        val Extension get() = KIconRef(KIcons.Extension, STYLE_OUTLINED)
        val AttachMoney get() = KIconRef(KIcons.AttachMoney, STYLE_OUTLINED)
        val Savings get() = KIconRef(KIcons.Savings, STYLE_OUTLINED)
        val Wallet get() = KIconRef(KIcons.Wallet, STYLE_OUTLINED)
        val Receipt get() = KIconRef(KIcons.Receipt, STYLE_OUTLINED)
        val BarChart get() = KIconRef(KIcons.BarChart, STYLE_OUTLINED)
        val Insights get() = KIconRef(KIcons.Insights, STYLE_OUTLINED)
        val CreditCard get() = KIconRef(KIcons.CreditCard, STYLE_OUTLINED)
        val CallReceived get() = KIconRef(KIcons.CallReceived, STYLE_OUTLINED)
        val AccountBalance get() = KIconRef(KIcons.AccountBalance, STYLE_OUTLINED)
        val TrendingUp get() = KIconRef(KIcons.TrendingUp, STYLE_OUTLINED)
        val TrendingDown get() = KIconRef(KIcons.TrendingDown, STYLE_OUTLINED)
        val SwapHoriz get() = KIconRef(KIcons.SwapHoriz, STYLE_OUTLINED)
        val LocalCafe get() = KIconRef(KIcons.LocalCafe, STYLE_OUTLINED)
        val Movie get() = KIconRef(KIcons.Movie, STYLE_OUTLINED)
        val Restaurant get() = KIconRef(KIcons.Restaurant, STYLE_OUTLINED)
        val DirectionsCar get() = KIconRef(KIcons.DirectionsCar, STYLE_OUTLINED)
        val LocalHospital get() = KIconRef(KIcons.LocalHospital, STYLE_OUTLINED)
        val Language get() = KIconRef(KIcons.Language, STYLE_OUTLINED)
        val HelpOutline get() = KIconRef(KIcons.HelpOutline, STYLE_OUTLINED)
        val ExitToApp get() = KIconRef(KIcons.ExitToApp, STYLE_OUTLINED)
        val ExpandMore get() = KIconRef(KIcons.ExpandMore, STYLE_OUTLINED)
        val ExpandLess get() = KIconRef(KIcons.ExpandLess, STYLE_OUTLINED)
        val ChevronLeft get() = KIconRef(KIcons.ChevronLeft, STYLE_OUTLINED)
        val ChevronRight get() = KIconRef(KIcons.ChevronRight, STYLE_OUTLINED)
        val ArrowDropDown get() = KIconRef(KIcons.ArrowDropDown, STYLE_OUTLINED)
        val ArrowDropUp get() = KIconRef(KIcons.ArrowDropUp, STYLE_OUTLINED)
        val KeyboardArrowUp get() = KIconRef(KIcons.KeyboardArrowUp, STYLE_OUTLINED)
        val KeyboardArrowDown get() = KIconRef(KIcons.KeyboardArrowDown, STYLE_OUTLINED)
        val KeyboardArrowLeft get() = KIconRef(KIcons.KeyboardArrowLeft, STYLE_OUTLINED)
        val KeyboardArrowRight get() = KIconRef(KIcons.KeyboardArrowRight, STYLE_OUTLINED)
    }

    object Rounded {
        val Home get() = KIconRef(KIcons.Home, STYLE_ROUNDED)
        val Search get() = KIconRef(KIcons.Search, STYLE_ROUNDED)
        val Settings get() = KIconRef(KIcons.Settings, STYLE_ROUNDED)
        val Menu get() = KIconRef(KIcons.Menu, STYLE_ROUNDED)
        val Close get() = KIconRef(KIcons.Close, STYLE_ROUNDED)
        val Check get() = KIconRef(KIcons.Check, STYLE_ROUNDED)
        val Add get() = KIconRef(KIcons.Add, STYLE_ROUNDED)
        val Delete get() = KIconRef(KIcons.Delete, STYLE_ROUNDED)
        val Edit get() = KIconRef(KIcons.Edit, STYLE_ROUNDED)
        val Share get() = KIconRef(KIcons.Share, STYLE_ROUNDED)
        val Refresh get() = KIconRef(KIcons.Refresh, STYLE_ROUNDED)
        val Favorite get() = KIconRef(KIcons.Favorite, STYLE_ROUNDED)
        val FavoriteBorder get() = KIconRef(KIcons.FavoriteBorder, STYLE_ROUNDED)
        val Star get() = KIconRef(KIcons.Star, STYLE_ROUNDED)
        val ThumbUp get() = KIconRef(KIcons.ThumbUp, STYLE_ROUNDED)
        val Email get() = KIconRef(KIcons.Email, STYLE_ROUNDED)
        val Call get() = KIconRef(KIcons.Call, STYLE_ROUNDED)
        val Notifications get() = KIconRef(KIcons.Notifications, STYLE_ROUNDED)
        val Person get() = KIconRef(KIcons.Person, STYLE_ROUNDED)
        val AccountCircle get() = KIconRef(KIcons.AccountCircle, STYLE_ROUNDED)
        val PlayArrow get() = KIconRef(KIcons.PlayArrow, STYLE_ROUNDED)
        val Folder get() = KIconRef(KIcons.Folder, STYLE_ROUNDED)
        val Place get() = KIconRef(KIcons.Place, STYLE_ROUNDED)
        val LocationOn get() = KIconRef(KIcons.LocationOn, STYLE_ROUNDED)
        val Wifi get() = KIconRef(KIcons.Wifi, STYLE_ROUNDED)
        val Bluetooth get() = KIconRef(KIcons.Bluetooth, STYLE_ROUNDED)
        val Camera get() = KIconRef(KIcons.Camera, STYLE_ROUNDED)
        val Phone get() = KIconRef(KIcons.Phone, STYLE_ROUNDED)
        val Keyboard get() = KIconRef(KIcons.Keyboard, STYLE_ROUNDED)
        val Lock get() = KIconRef(KIcons.Lock, STYLE_ROUNDED)
        val Info get() = KIconRef(KIcons.Info, STYLE_ROUNDED)
        val Warning get() = KIconRef(KIcons.Warning, STYLE_ROUNDED)
        val ShoppingCart get() = KIconRef(KIcons.ShoppingCart, STYLE_ROUNDED)
        val DateRange get() = KIconRef(KIcons.DateRange, STYLE_ROUNDED)
        val Schedule get() = KIconRef(KIcons.Schedule, STYLE_ROUNDED)
        val DarkMode get() = KIconRef(KIcons.DarkMode, STYLE_ROUNDED)
        val LightMode get() = KIconRef(KIcons.LightMode, STYLE_ROUNDED)
        val Build get() = KIconRef(KIcons.Build, STYLE_ROUNDED)
        val Code get() = KIconRef(KIcons.Code, STYLE_ROUNDED)
        val ExpandMore get() = KIconRef(KIcons.ExpandMore, STYLE_ROUNDED)
        val ExpandLess get() = KIconRef(KIcons.ExpandLess, STYLE_ROUNDED)
        val ChevronLeft get() = KIconRef(KIcons.ChevronLeft, STYLE_ROUNDED)
        val ChevronRight get() = KIconRef(KIcons.ChevronRight, STYLE_ROUNDED)
        val ArrowDropDown get() = KIconRef(KIcons.ArrowDropDown, STYLE_ROUNDED)
        val KeyboardArrowUp get() = KIconRef(KIcons.KeyboardArrowUp, STYLE_ROUNDED)
        val KeyboardArrowDown get() = KIconRef(KIcons.KeyboardArrowDown, STYLE_ROUNDED)
    }

    object Sharp {
        val Home get() = KIconRef(KIcons.Home, STYLE_SHARP)
        val Search get() = KIconRef(KIcons.Search, STYLE_SHARP)
        val Settings get() = KIconRef(KIcons.Settings, STYLE_SHARP)
        val Menu get() = KIconRef(KIcons.Menu, STYLE_SHARP)
        val Close get() = KIconRef(KIcons.Close, STYLE_SHARP)
        val Check get() = KIconRef(KIcons.Check, STYLE_SHARP)
        val Add get() = KIconRef(KIcons.Add, STYLE_SHARP)
        val Delete get() = KIconRef(KIcons.Delete, STYLE_SHARP)
        val Edit get() = KIconRef(KIcons.Edit, STYLE_SHARP)
        val Share get() = KIconRef(KIcons.Share, STYLE_SHARP)
        val Refresh get() = KIconRef(KIcons.Refresh, STYLE_SHARP)
        val Favorite get() = KIconRef(KIcons.Favorite, STYLE_SHARP)
        val FavoriteBorder get() = KIconRef(KIcons.FavoriteBorder, STYLE_SHARP)
        val Star get() = KIconRef(KIcons.Star, STYLE_SHARP)
        val Email get() = KIconRef(KIcons.Email, STYLE_SHARP)
        val Call get() = KIconRef(KIcons.Call, STYLE_SHARP)
        val Notifications get() = KIconRef(KIcons.Notifications, STYLE_SHARP)
        val Person get() = KIconRef(KIcons.Person, STYLE_SHARP)
        val AccountCircle get() = KIconRef(KIcons.AccountCircle, STYLE_SHARP)
        val PlayArrow get() = KIconRef(KIcons.PlayArrow, STYLE_SHARP)
        val Folder get() = KIconRef(KIcons.Folder, STYLE_SHARP)
        val Place get() = KIconRef(KIcons.Place, STYLE_SHARP)
        val LocationOn get() = KIconRef(KIcons.LocationOn, STYLE_SHARP)
        val Wifi get() = KIconRef(KIcons.Wifi, STYLE_SHARP)
        val Bluetooth get() = KIconRef(KIcons.Bluetooth, STYLE_SHARP)
        val Camera get() = KIconRef(KIcons.Camera, STYLE_SHARP)
        val Phone get() = KIconRef(KIcons.Phone, STYLE_SHARP)
        val Lock get() = KIconRef(KIcons.Lock, STYLE_SHARP)
        val Info get() = KIconRef(KIcons.Info, STYLE_SHARP)
        val Warning get() = KIconRef(KIcons.Warning, STYLE_SHARP)
        val ShoppingCart get() = KIconRef(KIcons.ShoppingCart, STYLE_SHARP)
        val Build get() = KIconRef(KIcons.Build, STYLE_SHARP)
        val ExpandMore get() = KIconRef(KIcons.ExpandMore, STYLE_SHARP)
        val ExpandLess get() = KIconRef(KIcons.ExpandLess, STYLE_SHARP)
        val ChevronLeft get() = KIconRef(KIcons.ChevronLeft, STYLE_SHARP)
        val ChevronRight get() = KIconRef(KIcons.ChevronRight, STYLE_SHARP)
        val ArrowDropDown get() = KIconRef(KIcons.ArrowDropDown, STYLE_SHARP)
    }

    object TwoTone {
        val Home get() = KIconRef(KIcons.Home, STYLE_TWO_TONE)
        val Search get() = KIconRef(KIcons.Search, STYLE_TWO_TONE)
        val Settings get() = KIconRef(KIcons.Settings, STYLE_TWO_TONE)
        val Menu get() = KIconRef(KIcons.Menu, STYLE_TWO_TONE)
        val Close get() = KIconRef(KIcons.Close, STYLE_TWO_TONE)
        val Check get() = KIconRef(KIcons.Check, STYLE_TWO_TONE)
        val Add get() = KIconRef(KIcons.Add, STYLE_TWO_TONE)
        val Delete get() = KIconRef(KIcons.Delete, STYLE_TWO_TONE)
        val Edit get() = KIconRef(KIcons.Edit, STYLE_TWO_TONE)
        val Share get() = KIconRef(KIcons.Share, STYLE_TWO_TONE)
        val Refresh get() = KIconRef(KIcons.Refresh, STYLE_TWO_TONE)
        val Favorite get() = KIconRef(KIcons.Favorite, STYLE_TWO_TONE)
        val FavoriteBorder get() = KIconRef(KIcons.FavoriteBorder, STYLE_TWO_TONE)
        val Star get() = KIconRef(KIcons.Star, STYLE_TWO_TONE)
        val Email get() = KIconRef(KIcons.Email, STYLE_TWO_TONE)
        val Call get() = KIconRef(KIcons.Call, STYLE_TWO_TONE)
        val Notifications get() = KIconRef(KIcons.Notifications, STYLE_TWO_TONE)
        val Person get() = KIconRef(KIcons.Person, STYLE_TWO_TONE)
        val AccountCircle get() = KIconRef(KIcons.AccountCircle, STYLE_TWO_TONE)
        val PlayArrow get() = KIconRef(KIcons.PlayArrow, STYLE_TWO_TONE)
        val Folder get() = KIconRef(KIcons.Folder, STYLE_TWO_TONE)
        val Place get() = KIconRef(KIcons.Place, STYLE_TWO_TONE)
        val LocationOn get() = KIconRef(KIcons.LocationOn, STYLE_TWO_TONE)
        val Wifi get() = KIconRef(KIcons.Wifi, STYLE_TWO_TONE)
        val Bluetooth get() = KIconRef(KIcons.Bluetooth, STYLE_TWO_TONE)
        val Camera get() = KIconRef(KIcons.Camera, STYLE_TWO_TONE)
        val Phone get() = KIconRef(KIcons.Phone, STYLE_TWO_TONE)
        val Lock get() = KIconRef(KIcons.Lock, STYLE_TWO_TONE)
        val Info get() = KIconRef(KIcons.Info, STYLE_TWO_TONE)
        val Warning get() = KIconRef(KIcons.Warning, STYLE_TWO_TONE)
        val ShoppingCart get() = KIconRef(KIcons.ShoppingCart, STYLE_TWO_TONE)
        val Build get() = KIconRef(KIcons.Build, STYLE_TWO_TONE)
        val ExpandMore get() = KIconRef(KIcons.ExpandMore, STYLE_TWO_TONE)
        val ExpandLess get() = KIconRef(KIcons.ExpandLess, STYLE_TWO_TONE)
        val ChevronLeft get() = KIconRef(KIcons.ChevronLeft, STYLE_TWO_TONE)
        val ChevronRight get() = KIconRef(KIcons.ChevronRight, STYLE_TWO_TONE)
        val ArrowDropDown get() = KIconRef(KIcons.ArrowDropDown, STYLE_TWO_TONE)
    }

    /** Create a [KIconRef] from any icon name and style. */
    fun of(name: String, style: String = STYLE_FILLED) = KIconRef(name, style)
}

// ─────────────────────────────────────────────────────────────
//  Icon maps – string → ImageVector
// ─────────────────────────────────────────────────────────────

/**
 * Resolves the [ImageVector] for the given icon [name] and [style].
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

/**
 * Resolves the [ImageVector] for the given [KIconRef].
 *
 * ```kotlin
 * val icon = resolveIcon(KIcons.Outlined.Home)
 * ```
 */
fun resolveIcon(ref: KIconRef): ImageVector? = resolveIcon(ref.name, ref.style)

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
