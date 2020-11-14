package de.dertyp7214.rboardmanagerlite

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.topjohnwu.superuser.Shell
import de.dertyp7214.rboardmanagerlite.Config.GBOARD_PACKAGE_NAME
import de.dertyp7214.rootutils.Magisk
import de.dertyp7214.rootutils.asCommand

class Application : Application() {
    var magisk: Magisk? = null
        private set

    companion object {
        var context: Context? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        Shell.Builder.create().setFlags(Shell.FLAG_MOUNT_MASTER)

        magisk = Magisk.getMagisk()
    }
}