package org.openinit.multicloudfilemanager.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors
import org.openinit.multicloudfilemanager.R
import org.openinit.multicloudfilemanager.databinding.CustomuiCrumbviewBinding


class CrumbView : LinearLayout {

    private var binding = CustomuiCrumbviewBinding.inflate(LayoutInflater.from(context), this, true)

    private var mTitle = ""
    private var mPath = ""

    private var mShowArrow = false
    private var mIsActive = true


    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)  {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)  {}
    constructor(context: Context) : super(context) {}

    init {
        binding.icon.visibility = View.GONE
        updateActiveState()
    }

    fun setTitle(title: String) {
        mTitle = title
        binding.title.text = mTitle
        setActive(mIsActive)
    }

    fun setPath(path: String) {
        mPath = path
        setActive(mIsActive)
    }

    fun showArrow(showArrow: Boolean) {
        mShowArrow = showArrow
        if (mShowArrow) {
            binding.arrow.visibility = View.VISIBLE
        } else {
            binding.arrow.visibility = View.GONE
        }
    }

    fun showHome() {
        binding.icon.visibility = View.VISIBLE
        updateActiveState()
    }

    fun setActive(isActive: Boolean) {
        mIsActive = isActive
        updateActiveState()
    }

    fun getPath(): String {
        return mPath
    }


    private fun updateActiveState() {
        var textFieldPadding = 0

        if (mIsActive) {
            val activeColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnPrimaryContainer, ContextCompat.getColor(context, R.color.colorAccent))
            binding.root.setBackgroundResource(R.drawable.pill)
            binding.arrow.setColorFilter(activeColor)
            binding.icon.setColorFilter(activeColor)
            binding.title.setTextColor(activeColor)
            TooltipCompat.setTooltipText(binding.root, mPath)
            binding.title.maxWidth = getPixelFromDp(99999) // allow it as big as possible
            textFieldPadding = getPixelFromDp(8)
        } else {
            val inactiveColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnSurfaceVariant, ContextCompat.getColor(context, android.R.color.darker_gray))
            binding.root.background = null
            binding.arrow.setColorFilter(inactiveColor)
            binding.icon.setColorFilter(inactiveColor)
            binding.title.setTextColor(inactiveColor)
            TooltipCompat.setTooltipText(binding.root, null)
            binding.title.maxWidth = getPixelFromDp(90)
        }

        if (binding.icon.visibility == View.VISIBLE) {
            binding.title.setPadding(0, 0, textFieldPadding, 0)
        } else {
            binding.title.setPadding(textFieldPadding, 0, textFieldPadding, 0)
        }
    }

    private fun getPixelFromDp(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}