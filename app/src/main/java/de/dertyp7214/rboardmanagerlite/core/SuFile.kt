package de.dertyp7214.rboardmanagerlite.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileInputStream
import de.dertyp7214.rootutils.asCommand
import java.io.File
import java.io.IOException
import java.io.InputStream

fun SuFile.copy(newFile: File): Boolean {
    return "cp $absolutePath ${newFile.absolutePath}".asCommand()
}

fun SuFile.copyRecursively(newFile: File): Boolean {
    return "cp -a $absolutePath/. ${newFile.absolutePath}".asCommand()
}

fun SuFile.decodeBitmap(opts: BitmapFactory.Options? = null): Bitmap? {
    val pathName = absolutePath
    var bm: Bitmap? = null
    var stream: InputStream? = null
    try {
        stream = SuFileInputStream(pathName)
        bm = BitmapFactory.decodeStream(stream, null, opts)
    } catch (e: Exception) {
        /*  do nothing.
                If the exception happened on open, bm will be null.
            */
        Log.e("BitmapFactory", "Unable to decode stream: $e")
    } finally {
        if (stream != null) {
            try {
                stream.close()
            } catch (e: IOException) {
                // do nothing here
            }
        }
    }
    return bm
}