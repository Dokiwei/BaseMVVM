package com.dokiwei.basemvvm.ui.app

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.dokiwei.basemvvm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val publicViewModel by lazy { ViewModelProvider(this)[PublicViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
            this.root.post {
                //获取屏幕的状态栏以及导航栏的高度
                //低版本可能需要用ViewCompat.getRootWindowInsets(findViewById(android.R.id.content))才能获取
                val windowInsetsCompat = ViewCompat.getRootWindowInsets(window.decorView)
                val statusBarsHeight =
                    windowInsetsCompat?.getInsets(WindowInsetsCompat.Type.statusBars())?.top
                val navigationBarsHeight =
                    windowInsetsCompat?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom
                //传递给PublicViewModel供全局使用
                if (statusBarsHeight != null) publicViewModel.statusBarsHeight.value = statusBarsHeight
                if (navigationBarsHeight != null) publicViewModel.navigationBarsHeight.value = navigationBarsHeight
            }
            //设置状态栏沉浸,会导致状态栏下沉
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.navigationBarDividerColor = Color.TRANSPARENT
            }
            //不让输入法遮挡布局
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
onBackPressed()
    }
}