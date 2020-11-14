package de.dertyp7214.rboardmanagerlite.core

import android.content.Context
import com.topjohnwu.superuser.io.SuFile
import de.dertyp7214.rboardmanagerlite.data.ThemeDataClass

fun ThemeDataClass.delete(): Boolean {
    return SuFile(path).delete()
}

fun ThemeDataClass.moveToCache(context: Context): ThemeDataClass {
    val zip = SuFile(path)
    val newZip = SuFile(context.cacheDir, zip.name)
    val imageFile = SuFile(path.removeSuffix(".zip"))
    val newImage = SuFile(context.cacheDir, imageFile.name)
    zip.copy(newZip)
    if (imageFile.exists()) imageFile.copy(newImage)
    return ThemeDataClass(image, name, newZip.absolutePath, selected)
}