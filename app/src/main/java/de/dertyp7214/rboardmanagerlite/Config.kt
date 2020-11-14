package de.dertyp7214.rboardmanagerlite

import de.dertyp7214.rootutils.Magisk.Companion.MODULES_PATH

object Config {
    var THEME_LOCATION = "/system/etc/gboard_theme"

    const val MODULE_ID = "rboard-themes"
    const val MODULE_PATH = "$MODULES_PATH/$MODULE_ID"
    const val GBOARD_PACKAGE_NAME = "com.google.android.inputmethod.latin"

    val MAGISK_THEME_LOC: String
        get() {
            return if (!THEME_LOCATION.startsWith("/system")) THEME_LOCATION else "$MODULE_PATH$THEME_LOCATION"
        }

    const val PACKS_URL =
        "https://raw.githubusercontent.com/GboardThemes/Packs/master/download_list.json"

    const val SOUNDS_PACKS_URL =
        "https://raw.githubusercontent.com/GboardThemes/Soundpack/master/download_sounds.json"

    var themeCount: Int? = null
}