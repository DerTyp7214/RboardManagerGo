package de.dertyp7214.rboardmanagerlite.core

import android.app.Activity
import de.dertyp7214.rboardmanagerlite.Application

fun Activity.getApp(): Application? {
    return if (application is Application) application as Application else null
}