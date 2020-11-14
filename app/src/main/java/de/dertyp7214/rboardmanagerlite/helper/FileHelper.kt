package de.dertyp7214.rboardmanagerlite.helper

import android.content.Context
import android.os.Environment
import java.io.File

object FileHelper {
    fun getThemePacksPath(context: Context): File {
        return File(
            context.getExternalFilesDirs(Environment.DIRECTORY_NOTIFICATIONS)[0].absolutePath.removeSuffix(
                "Notifications"
            ), "ThemePacks"
        ).apply { if (!exists()) mkdirs() }
    }

    fun getSoundPacksPath(context: Context): File {
        return File(
            context.getExternalFilesDirs(Environment.DIRECTORY_NOTIFICATIONS)[0].absolutePath.removeSuffix(
                "Notifications"
            ), "SoundPacks"
        ).apply { if (!exists()) mkdirs() }
    }
}