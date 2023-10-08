package com.dokiwei.basemvvm.util

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton


/**
 * @author DokiWei
 * @date 2023/9/26 21:17
 */
// 自定义 Behavior 类
class MyBehavior(context: Context?, attrs: AttributeSet?) :
    FloatingActionButton.Behavior(context, attrs) {
    // 判断是否开始滑动
    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)||
                axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    // 滑动结束
    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, target: View, type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        if (child.visibility == View.GONE) {
            child.show()
        }
    }

    // 滑动过程中
    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        if (dyConsumed > 0 && child.visibility == View.VISIBLE) {
            child.hide()
        } else if (dyConsumed < 0 && child.visibility == View.GONE) {
            child.show()
        }
    }
}

