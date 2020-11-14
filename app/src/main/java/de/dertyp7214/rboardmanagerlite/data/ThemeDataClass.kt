package de.dertyp7214.rboardmanagerlite.data

import android.graphics.Bitmap

data class ThemeDataClass(
    val image: Bitmap? = null,
    val name: String,
    val path: String,
    var selected: Boolean = false
)