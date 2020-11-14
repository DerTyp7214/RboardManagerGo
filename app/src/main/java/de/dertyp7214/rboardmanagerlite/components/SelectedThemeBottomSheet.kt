package de.dertyp7214.rboardmanagerlite.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.edit
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import com.topjohnwu.superuser.io.SuFile
import de.dertyp7214.rboardmanagerlite.R
import de.dertyp7214.rboardmanagerlite.core.setSwitchColor
import de.dertyp7214.rboardmanagerlite.data.ThemeDataClass
import de.dertyp7214.rboardmanagerlite.helper.ThemeHelper

class SelectedThemeBottomSheet(
    private val theme: ThemeDataClass,
    private val defaultImage: Bitmap,
    private val color: Int,
    private val isDark: Boolean,
    private val deleteCallback: () -> Unit = {}
) : RoundedBottomSheetDialogFragment() {
    @SuppressLint("DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.selected_theme_bottomsheet, container, false)

        val applyButton = v.findViewById<LinearLayout>(R.id.apply)
        val deleteButton = v.findViewById<LinearLayout>(R.id.delete)
        val enableBorderButton = v.findViewById<LinearLayout>(R.id.enable_border)
        val enableBorderSwitch = v.findViewById<SwitchMaterial>(R.id.enable_border_switch)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        var enableBorderSwitchSelected = sharedPreferences.getBoolean("enable_border", false)
        val themeName = v.findViewById<TextView>(R.id.theme_name)
        val themeIcon = v.findViewById<ImageView>(R.id.theme_image)
        val card: CardView = v.findViewById(R.id.card)
        val gradient: View? = try {
            v.findViewById(R.id.gradient)
        } catch (e: Exception) {
            null
        }

        if (gradient != null) {
            val g = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(color, Color.TRANSPARENT)
            )
            gradient.background = g
        }

        card.setCardBackgroundColor(color)
        if (resources.getBoolean(R.bool.darkmode)) enableBorderSwitch.setSwitchColor(resources.getColor(R.color.purple_200))

        themeName.text = theme.name.split("_").joinToString(" ") { it.capitalize() }
        themeName.setTextColor(if (!isDark) Color.WHITE else Color.BLACK)
        themeIcon.setImageBitmap(theme.image ?: defaultImage)

        deleteButton.setOnClickListener {
            MaterialDialog(requireContext()).show {
                cornerRadius(12F)
                message(res = R.string.delete_theme_confirm)
                positiveButton(res = R.string.yes) {
                    if (SuFile(theme.path).delete()) {
                        Toast.makeText(context, R.string.theme_deleted, Toast.LENGTH_LONG).show()
                        deleteCallback()
                    } else {
                        Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show()
                    }
                    it.dismiss()
                    this@SelectedThemeBottomSheet.dismiss()
                }
                negativeButton(res = R.string.no) {
                    it.dismiss()
                }
            }
        }

        applyButton.setOnClickListener {
            val response = ThemeHelper.applyTheme("${theme.name}.zip", enableBorderSwitchSelected)
            dismiss()
            if (response) Toast.makeText(
                context,
                getString(R.string.applied),
                Toast.LENGTH_SHORT
            ).show()
            else Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show()
        }

        enableBorderSwitch.isChecked = enableBorderSwitchSelected

        enableBorderButton.setOnClickListener {

            enableBorderSwitchSelected = !enableBorderSwitchSelected
            sharedPreferences.edit { putBoolean("enable_border", enableBorderSwitchSelected) }
            enableBorderSwitch.isChecked = enableBorderSwitchSelected
        }

        enableBorderSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit { putBoolean("enable_border", isChecked) }
            enableBorderSwitchSelected = isChecked
        }

        return v
    }
}