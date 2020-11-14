package de.dertyp7214.rboardmanagerlite.helper

import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileOutputStream
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ZipHelper {
    private val buffer = 80000
    fun zip(_files: List<String>, zipFileName: String) {
        try {
            var origin: BufferedInputStream?
            val dest = FileOutputStream(zipFileName)
            val out = ZipOutputStream(
                BufferedOutputStream(
                    dest
                )
            )
            val data = ByteArray(buffer)
            for (i in _files.indices) {
                val fi = FileInputStream(_files[i])
                origin = BufferedInputStream(fi, buffer)
                val entry =
                    ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1))
                out.putNextEntry(entry)
                var count: Int
                while (origin.read(data, 0, buffer).also { count = it } != -1) {
                    out.write(data, 0, count)
                }
                origin.close()
            }
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unpackZip(path: String, zipName: String): Boolean {
        ZipFile(zipName).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    SuFile(path).mkdirs()
                    SuFileOutputStream(SuFile(path, entry.name)).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
        return true
    }
}