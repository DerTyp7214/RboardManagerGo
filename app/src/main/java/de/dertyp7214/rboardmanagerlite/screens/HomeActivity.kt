package de.dertyp7214.rboardmanagerlite.screens

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import de.dertyp7214.rboardmanagerlite.R
import de.dertyp7214.rboardmanagerlite.helper.ThemeHelper

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ThemeHelper.loadThemes().forEach {
            Log.d("YEET", it.name)
        }
    }
}