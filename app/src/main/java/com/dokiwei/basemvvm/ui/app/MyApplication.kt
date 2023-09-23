package com.dokiwei.basemvvm.ui.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.dokiwei.basemvvm.util.MyCrashHandler
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp

/**
 * @author DokiWei
 * @date 2023/9/10 22:58
 */
@HiltAndroidApp
class MyApplication : Application() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        DynamicColors.applyToActivitiesIfAvailable(this)
        MyCrashHandler.instance.init(this)
    }
}