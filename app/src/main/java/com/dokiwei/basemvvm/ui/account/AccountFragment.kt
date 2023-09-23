package com.dokiwei.basemvvm.ui.account

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.dokiwei.basemvvm.R
import com.dokiwei.basemvvm.base.BaseFragment
import com.dokiwei.basemvvm.component.MessageDialog
import com.dokiwei.basemvvm.content.broadcast.NotificationReceiver
import com.dokiwei.basemvvm.databinding.FragmentAccountBinding
import com.dokiwei.basemvvm.util.MyCoroutineExceptionHandler
import com.dokiwei.basemvvm.util.randomAvatar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

/**
 * @author DokiWei
 * @date 2023/9/10 23:20
 */
class AccountFragment : BaseFragment<FragmentAccountBinding, ViewModel>(
    FragmentAccountBinding::inflate, null
), View.OnClickListener {
    override fun initFragment(
        binding: FragmentAccountBinding, viewModel: ViewModel?, savedInstanceState: Bundle?
    ) {
        binding.avatar.setImageResource(randomAvatar())
        binding.navigationToCustomViewButton.setOnClickListener(this)
        binding.testExceptionHandler.setOnClickListener(this)
        binding.testCoroutineExceptionHandler.setOnClickListener(this)
        binding.testNotification.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        v?.id.let {
            when (it) {
                R.id.navigationToCustomView_button -> requireActivity().findNavController(R.id.app_nav)
                    .navigate(R.id.action_mainNavFragment_to_customViewFragment)

                R.id.test_exceptionHandler -> throw Throwable("自定义错误")
                R.id.test_coroutine_exceptionHandler -> {

                    val inflater = requireContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val dialogLayoutView = inflater.inflate(R.layout.dialog_message, null)

                    val dialog = MessageDialog.Build(
                        requireContext(),
                        R.style.custom_dialog,
                        dialogLayoutView
                    ).setCancelButton("取消") { dialog, _ ->
                        dialog.dismiss()
                    }.setConfirmButton("确认") { dialog, _ ->
                        dialog.dismiss()
                    }.setTitle("自定义协程异常").setMessage("协程异常记录:").create()
                    dialog.show()
                    lifecycleScope.launch(MyCoroutineExceptionHandler.handler) {
                        supervisorScope {
                            launch {
                                withContext(Dispatchers.Main) {
                                    dialog.addMessage("协程一开始:delay 2000ms")
                                }
                                delay(2000)
                                withContext(Dispatchers.Main) {
                                    dialog.addMessage("协程一发生异常")
                                }
                                throw Throwable("666")
                            }
                            launch {
                                withContext(Dispatchers.Main) {
                                    dialog.addMessage("协程二开始:delay 1000ms")
                                }
                                delay(1000)
                                withContext(Dispatchers.Main) {
                                    dialog.addMessage("协程二发生异常")
                                }
                                throw Throwable("555")
                            }
                        }
                    }
                }

                R.id.test_notification -> {
                    val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
                        putExtra(
                            "测试",
                            "点击了测试信息"
                        )
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        requireContext(), 0, intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    val notification = Notification.Builder(requireActivity(), "测试")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("测试")
                        .setContentText("这是一条测试通知")
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .build()
                    val notificationManager =
                        requireContext().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    val channel = NotificationChannel(
                        "测试",
                        "测试渠道",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    notificationManager.createNotificationChannel(channel)
                    notificationManager.notify(System.nanoTime().toInt(), notification)
                }

                else -> {}
            }
        }
    }
}