package com.dokiwei.basemvvm.ui.app

import android.Manifest
import android.app.UiModeManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.dokiwei.basemvvm.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val publicViewModel by lazy { ViewModelProvider(this)[PublicViewModel::class.java] }

    companion object {
        private val PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        @RequiresApi(Build.VERSION_CODES.P)
        private val PERMISSIONS_P = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.FOREGROUND_SERVICE
        )

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private val PERMISSIONS_TIRAMISU = arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE
        )

        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        private val PERMISSIONS_UPSIDE_DOWN_CAKE = arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
        )
        private const val REQUEST_EXTERNAL_STORAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
            initSystemBar()
            when (Build.VERSION.SDK_INT) {
                Build.VERSION_CODES.O -> ActivityCompat.requestPermissions(
                    this@MainActivity, PERMISSIONS, REQUEST_EXTERNAL_STORAGE
                )

                in Build.VERSION_CODES.P..Build.VERSION_CODES.S_V2 -> ActivityCompat.requestPermissions(
                    this@MainActivity, PERMISSIONS_P, REQUEST_EXTERNAL_STORAGE
                )
                Build.VERSION_CODES.TIRAMISU->ActivityCompat.requestPermissions(
                    this@MainActivity, PERMISSIONS_TIRAMISU, REQUEST_EXTERNAL_STORAGE
                )
                Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->ActivityCompat.requestPermissions(
                    this@MainActivity, PERMISSIONS_UPSIDE_DOWN_CAKE, REQUEST_EXTERNAL_STORAGE
                )
            }
        }
    }

    /**
     * 在这里获取动态权限回调
     *
     * @param requestCode requestPermissions(Activity, String[], int) 中传递的请求代码
     * @param permissions 请求的权限。绝不为空
     * @param grantResults 相应权限的授予结果，可以是 PackageManager
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                val havePermission =
                    grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                val s =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) "音频权限" else "存储权限"
                havePermission.let {
                    publicViewModel.havePermission.value = it
                    Toast.makeText(
                        this,
                        if (it) "${s}授权成功！" else "${s}授权被拒绝！",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    /**
     * 初始化沉浸式状态栏,以及获取状态栏导航栏高度
     *
     */
    private fun ActivityMainBinding.initSystemBar() {
        //设置状态栏沉浸,会导致状态栏下沉
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.apply {
            statusBarColor = Color.TRANSPARENT//设置状态栏透明
            navigationBarColor = Color.TRANSPARENT//设置导航栏透明
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                navigationBarDividerColor = Color.TRANSPARENT
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                isNavigationBarContrastEnforced = false
            }
            //不让输入法遮挡布局
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            val uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
            if (uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES) {
                isAppearanceLightNavigationBars = false
                isAppearanceLightStatusBars = false
            } else {
                isAppearanceLightNavigationBars = true
                isAppearanceLightStatusBars = true
            }
        }
        this.root.post {
            val windowInsetsCompat = ViewCompat.getRootWindowInsets(window.decorView)
            val statusBarsHeight =
                windowInsetsCompat?.getInsets(WindowInsetsCompat.Type.statusBars())?.top
            val navigationBarsHeight =
                windowInsetsCompat?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom
            statusBarsHeight.takeIf { it != null }
                ?.let { publicViewModel.statusBarsHeight.value = it }
            navigationBarsHeight.takeIf { it != null }
                ?.let { publicViewModel.navigationBarsHeight.value = it }
        }
    }
}