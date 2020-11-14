package de.dertyp7214.rboardmanagerlite.screens

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.button.MaterialButton
import de.dertyp7214.rboardmanagerlite.BuildConfig
import de.dertyp7214.rboardmanagerlite.Config
import de.dertyp7214.rboardmanagerlite.Config.THEME_LOCATION
import de.dertyp7214.rboardmanagerlite.R
import de.dertyp7214.rboardmanagerlite.helper.ThemeHelper
import de.dertyp7214.rootutils.asCommand
import de.dertyp7214.rootutils.rootAccess

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        if (rootAccess() || BuildConfig.DEBUG) {
            "rm -rf ${cacheDir.absolutePath}/*".asCommand()

            if (!checkGboardPermission()) requestGboardStorage()
            if (ThemeHelper.checkForExistingThemes()) ThemeHelper.getThemesPathFromProps()
                ?.apply { THEME_LOCATION = this }

            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            MaterialDialog(this).show {
                setContentView(R.layout.no_root)

                findViewById<MaterialButton>(R.id.button).setOnClickListener { finish() }
            }
        }
    }

    private fun checkGboardPermission(): Boolean {
        return packageManager.getPackageInfo(
            Config.GBOARD_PACKAGE_NAME,
            PackageManager.GET_PERMISSIONS
        )?.let {
            val perm = it.requestedPermissions?.filterIndexed { index, p ->
                p == "android.permission.READ_EXTERNAL_STORAGE" && ((it.requestedPermissionsFlags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0)
            }
            perm != null && perm.contains("android.permission.READ_EXTERNAL_STORAGE")
        } ?: false
    }

    private fun requestGboardStorage() {
        "pm grant ${Config.GBOARD_PACKAGE_NAME} android.permission.READ_EXTERNAL_STORAGE".asCommand()
    }
}