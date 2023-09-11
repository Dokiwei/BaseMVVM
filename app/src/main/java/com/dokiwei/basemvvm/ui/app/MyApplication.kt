package com.dokiwei.basemvvm.ui.app

import android.app.Application
import com.google.android.material.color.DynamicColors

/**
 * @author DokiWei
 * @date 2023/9/10 22:58
 */
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}