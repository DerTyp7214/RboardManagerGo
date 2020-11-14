package de.dertyp7214.rboardmanagerlite.helper

import android.graphics.Bitmap
import androidx.core.graphics.ColorUtils
import de.dertyp7214.rboardmanagerlite.core.getDominantColor

object ColorHelper {
    fun dominantColor(image: Bitmap): Int {
        return image.getDominantColor()
    }

    fun isColorLight(color: Int): Boolean {
        return ColorUtils.calculateLuminance(color) > .5
    }
}