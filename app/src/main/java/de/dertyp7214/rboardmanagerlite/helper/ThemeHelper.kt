package de.dertyp7214.rboardmanagerlite.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.button.MaterialButton
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileInputStream
import com.topjohnwu.superuser.io.SuFileOutputStream
import de.dertyp7214.rboardmanagerlite.Application
import de.dertyp7214.rboardmanagerlite.Config.GBOARD_PACKAGE_NAME
import de.dertyp7214.rboardmanagerlite.Config.MAGISK_THEME_LOC
import de.dertyp7214.rboardmanagerlite.Config.MODULE_ID
import de.dertyp7214.rboardmanagerlite.Config.THEME_LOCATION
import de.dertyp7214.rboardmanagerlite.Config.themeCount
import de.dertyp7214.rboardmanagerlite.R
import de.dertyp7214.rboardmanagerlite.core.decodeBitmap
import de.dertyp7214.rboardmanagerlite.core.moveToCache
import de.dertyp7214.rboardmanagerlite.data.ThemeDataClass
import de.dertyp7214.rboardmanagerlite.helper.FileHelper.getThemePacksPath
import de.dertyp7214.rootutils.Magisk
import de.dertyp7214.rootutils.asCommand
import java.io.File
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

object ThemeHelper {
    fun loadThemes(): List<ThemeDataClass> {
        val themeDir =
            SuFile(THEME_LOCATION)
        return themeDir.listFiles()?.filter {
            it.name.toLowerCase(Locale.ROOT).endsWith(".zip")
        }?.map {
            val imageFile = SuFile(THEME_LOCATION, it.name.removeSuffix(".zip"))
            if (imageFile.exists()) ThemeDataClass(
                imageFile.decodeBitmap(),
                it.name.removeSuffix(".zip"),
                it.absolutePath
            )
            else ThemeDataClass(null, it.name.removeSuffix(".zip"), it.absolutePath)
        }.apply { if (this != null) themeCount = size } ?: ArrayList()
    }

    fun getThemesPathFromProps(): String? {
        var path: String? = null
        "getprop ro.com.google.ime.themes_dir".asCommand {
            if (it.first().isNotEmpty()) path = it.first()
        }
        return path
    }

    fun checkForExistingThemes(): Boolean {
        return getThemesPathFromProps() != null
    }

    fun changeThemesPath(context: Context, path: String) {
        val oldLoc = SuFile(MAGISK_THEME_LOC)
        THEME_LOCATION = path
        val newLoc = SuFile(MAGISK_THEME_LOC)
        newLoc.mkdirs()
        oldLoc.copyRecursively(newLoc)

        val meta = Magisk.Module.Meta(
            MODULE_ID,
            "Rboard Themes",
            "v20",
            "200",
            "RKBDI & DerTyp7214",
            "Module for Rboard Themes app"
        )
        val file = mapOf(
            Pair(
                "system.prop",
                "ro.com.google.ime.themes_dir=$THEME_LOCATION"
            )
        )
        Magisk.getMagisk()?.installOrUpdateModule(meta, file)

        MaterialDialog(context).show {
            setContentView(R.layout.reboot_dialog)
            findViewById<MaterialButton>(R.id.button_later).setOnClickListener { exitProcess(0) }
            findViewById<MaterialButton>(R.id.button_restart).setOnClickListener {
                "reboot".asCommand()
            }
        }
    }

    fun getActiveThemeDataClass(): ThemeDataClass {
        val themeName = getActiveTheme()
        return if (themeName.isNotEmpty()) {
            val image = SuFile(THEME_LOCATION, themeName)
            ThemeDataClass(
                image.decodeBitmap(),
                themeName,
                SuFile(THEME_LOCATION, "$themeName.zip").absolutePath
            )
        } else ThemeDataClass(null, "", "")
    }

    fun installTheme(zip: File, move: Boolean = true): Boolean {
        return if (zip.extension == "pack") {
            Application.context.let {
                if (it != null) {
                    val installDir = File(it.cacheDir, "tmpInstall")
                    val newZip = File(
                        getThemePacksPath(it).apply { if (!exists()) mkdirs() }, zip.name
                    )
                    if (!move || "cp ${zip.absolutePath} ${newZip.absoluteFile}".asCommand()) {
                        ZipHelper().unpackZip(installDir.absolutePath, newZip.absolutePath)
                        newZip.deleteOnExit()
                        if (installDir.isDirectory) {
                            var noError = false
                            installDir.listFiles()?.forEach { theme ->
                                if (installTheme(theme) && !noError) noError = true
                            }
                            noError
                        } else false
                    } else false
                } else false
            }
        } else {
            val installPath = SuFile(MAGISK_THEME_LOC, zip.name)
            "cp ${zip.absolutePath} ${installPath.absolutePath} && chmod 644 ${installPath.absolutePath}".asCommand()
        }
    }

    @SuppressLint("SdCardPath")
    fun applyTheme(name: String, withBorders: Boolean = false): Boolean {
        val inputPackageName = GBOARD_PACKAGE_NAME
        val fileName =
            "/data/data/$inputPackageName/shared_prefs/${inputPackageName}_preferences.xml"
        val content = SuFileInputStream(SuFile(fileName)).use {
            it.bufferedReader().readText()
        }.let {

            var changed = it

            changed = if (changed.contains("<string name=\"additional_keyboard_theme\">"))
                changed.replace(
                    "<string name=\"additional_keyboard_theme\">.*</string>".toRegex(),
                    "<string name=\"additional_keyboard_theme\">system:$name</string>"
                )
            else
                changed.replace(
                    "<map>",
                    "<map><string name=\"additional_keyboard_theme\">system:$name</string>"
                )

            // Change enable_key_border value
            changed = if (changed.contains("<boolean name=\"enable_key_border\"")) {
                changed.replace(
                    "<boolean name=\"enable_key_border\" value=\".*\" />".toRegex(),
                    "<boolean name=\"enable_key_border\" value=\"$withBorders\" />"
                )
            } else {
                changed.replace(
                    "<map>",
                    "<map><boolean name=\"enable_key_border\" value=\"$withBorders\" />"
                )
            }

            return@let changed
        }
        SuFileOutputStream(File(fileName)).writer(Charset.defaultCharset())
            .use { outputStreamWriter ->
                outputStreamWriter.write(content)
            }

        return "am force-stop $inputPackageName".asCommand()
    }

    @SuppressLint("SdCardPath")
    fun getActiveTheme(): String {
        val inputPackageName = "com.google.android.inputmethod.latin"
        val fileLol =
            SuFile("/data/data/$inputPackageName/shared_prefs/${inputPackageName}_preferences.xml")
        return try {
            SuFileInputStream(fileLol).bufferedReader().readText()
                .split("<string name=\"additional_keyboard_theme\">")
                .let { if (it.size > 1) it[1].split("</string>")[0] else "" }.replace("system:", "")
                .replace(".zip", "")
        } catch (error: Exception) {
            ""
        }
    }

    fun shareThemes(context: Activity, themes: List<ThemeDataClass>) {
        val files = ArrayList<File>()
        themes.map { it.moveToCache(context) }.forEach {
            val image = File(it.path.removeSuffix(".zip"))
            files.add(File(it.path))
            if (image.exists()) files.add(image)
        }
        val zip = File(context.cacheDir, "themes.pack")
        zip.deleteOnExit()
        ZipHelper().zip(files.map { it.absolutePath }, zip.absolutePath)
        files.forEach { it.deleteOnExit() }
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName,
            zip
        )
        ShareCompat.IntentBuilder.from(context)
            .setStream(uri)
            .setType("application/pack")
            .intent
            .setAction(Intent.ACTION_SEND)
            .setDataAndType(uri, "application/pack")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).apply {
                context.startActivity(
                    Intent.createChooser(
                        this,
                        context.getString(R.string.share_themes)
                    )
                )
            }
    }

    fun getSoundsDirectory(): SuFile? {
        val productMedia = SuFile("/system/product/media/audio/ui/KeypressStandard.ogg")
        val systemMedia = SuFile("/system/media/audio/ui/KeypressStandard.ogg")
        return if (productMedia.exists() && productMedia.isFile) {
            SuFile("/system/product/media")
        } else if (systemMedia.exists() && systemMedia.isFile) {
            SuFile("/system/media")
        } else {
            null
        }
    }

    private fun writeSuFile(file: SuFile, content: String) {
        SuFileOutputStream(file).use {
            it.write(content.toByteArray(Charsets.UTF_8))
        }
    }
}
