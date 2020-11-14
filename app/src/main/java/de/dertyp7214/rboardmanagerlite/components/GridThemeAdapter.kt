package de.dertyp7214.rboardmanagerlite.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.dertyp7214.rboardmanagerlite.R
import de.dertyp7214.rboardmanagerlite.core.getBitmap
import de.dertyp7214.rboardmanagerlite.data.ThemeDataClass
import de.dertyp7214.rboardmanagerlite.helper.ColorHelper.dominantColor
import de.dertyp7214.rboardmanagerlite.helper.ColorHelper.isColorLight
import de.dertyp7214.rboardmanagerlite.helper.ThemeHelper
import de.dertyp7214.rboardmanagerlite.viewmodels.HomeViewModel

class GridThemeAdapter(
    private val context: FragmentActivity,
    private val list: ArrayList<ThemeDataClass>,
    private val homeViewModel: HomeViewModel,
    private val selectToggle: (selectOn: Boolean) -> Unit = {},
    private val addItemSelect: (theme: ThemeDataClass, index: Int) -> Unit = { _, _ -> },
    private val removeItemSelect: (theme: ThemeDataClass, index: Int) -> Unit = { _, _ -> }
) :
    RecyclerView.Adapter<GridThemeAdapter.ViewHolder>() {

    private var recyclerView: RecyclerView? = null
    private var lastPosition =
        recyclerView?.layoutManager?.let { (it as GridLayoutManager).findLastVisibleItemPosition() }
            ?: 0

    private var activeTheme = ""
    private val default = ContextCompat.getDrawable(
        context,
        R.drawable.ic_keyboard
    )!!.getBitmap()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        activeTheme = ThemeHelper.getActiveTheme()
    }

    fun removeItem(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    fun restoreItem(position: Int, item: ThemeDataClass) {
        list.add(position, item)
        notifyItemInserted(position)
    }

    fun getItem(position: Int): ThemeDataClass {
        return list[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.theme_item,
                parent,
                false
            )
        )
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val selection = list.map { it.selected }.contains(true)
        val dataClass = list[position]

        val color = dominantColor(dataClass.image ?: default)

        if (holder.gradient != null) {
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(color, Color.TRANSPARENT)
            )
            holder.gradient.background = gradient
        }

        holder.themeImage.setImageBitmap(dataClass.image ?: default)
        holder.themeImage.alpha = if (dataClass.image != null) 1F else .3F

        holder.themeName.text =
            "${
                dataClass.name.split("_").joinToString(" ") { it.capitalize() }
            } ${if (dataClass.name == activeTheme) "(applied)" else ""}"
        holder.themeNameSelect.text =
            "${
                dataClass.name.split("_").joinToString(" ") { it.capitalize() }
            } ${if (dataClass.name == activeTheme) "(applied)" else ""}"

        holder.themeName.setTextColor(if (isColorLight(color)) Color.BLACK else Color.WHITE)

        if (dataClass.selected)
            holder.selectOverlay.alpha = 1F
        else
            holder.selectOverlay.alpha = 0F

        holder.card.setCardBackgroundColor(color)

        holder.card.setOnClickListener {
            if (selection) {
                list[position].selected = !list[position].selected
                holder.selectOverlay.animate().alpha(1F - holder.selectOverlay.alpha)
                    .setDuration(200).withEndAction {
                        notifyDataSetChanged()
                        if (list[position].selected) addItemSelect(dataClass, position)
                        if (!list[position].selected) removeItemSelect(dataClass, position)
                        if (!list.map { it.selected }.contains(true)) selectToggle(false)
                    }.start()
            } else {
                SelectedThemeBottomSheet(dataClass, default, color, isColorLight(color)) {
                    homeViewModel.setRefetch(true)
                }.show(
                    context.supportFragmentManager,
                    ""
                )
            }
        }

        holder.card.setOnLongClickListener {
            list[position].selected = true
            holder.selectOverlay.animate().alpha(1F).setDuration(200).withEndAction {
                notifyDataSetChanged()
                selectToggle(true)
            }
            true
        }

        setAnimation(holder.card, position)
    }

    fun dataSetChanged() {
        lastPosition =
            recyclerView?.layoutManager?.let { (it as GridLayoutManager).findLastVisibleItemPosition() }
                ?: 0
        notifyDataSetChanged()
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation =
                AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val themeImage: ImageView = v.findViewById(R.id.theme_image)
        val themeName: TextView = v.findViewById(R.id.theme_name)
        val themeNameSelect: TextView = v.findViewById(R.id.theme_name_selected)
        val selectOverlay: ViewGroup = v.findViewById(R.id.select_overlay)
        val card: CardView = v.findViewById(R.id.card)
        val gradient: View? = try {
            v.findViewById(R.id.gradient)
        } catch (e: Exception) {
            null
        }
    }
}