package com.dokiwei.basemvvm.content.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.dokiwei.basemvvm.ui.app.MyApplication

/**
 * @author DokiWei
 * @date 2023/9/18 12:30
 */
class NotificationReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        val intentMessage = intent?.extras?.getString("测试")
        intentMessage?.let { Toast.makeText(MyApplication.context,it,Toast.LENGTH_LONG).show() }
    }
}