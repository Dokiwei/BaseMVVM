package com.dokiwei.basemvvm.util

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.AnimRes
import androidx.annotation.DrawableRes

/**
 * @author DokiWei
 * @date 2023/9/13 13:13
 */
fun View.setAnim(context: Context, @AnimRes id: Int) {
    this.animation = AnimationUtils.loadAnimation(context, id)
}

fun ImageButton.setImg(boolean: Boolean, @DrawableRes res1: Int, @DrawableRes res2: Int) {
    this.setImageResource(if (boolean) res1 else res2)
}

fun TextView.text(boolean: Boolean, s1: CharSequence, s2: CharSequence) {
    this.text = if (boolean) s1 else s2
}