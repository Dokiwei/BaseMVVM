package com.dokiwei.basemvvm.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Thread.sleep
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess

/**
 * @author DokiWei
 * @date 2023/9/16 13:16
 */
class MyCrashHandler : Thread.UncaughtExceptionHandler {

    private val infoMap: MutableMap<String, String> = mutableMapOf()
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    companion object {
        val instance: MyCrashHandler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            MyCrashHandler()
        }
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        Log.e("异常捕捉", "Thread:${t.name} ${t.id} error:${e.message}")
        val countDownLatch = CountDownLatch(1)
        Thread {
            Looper.prepare()
            Toast.makeText(context, "系统崩溃了~\n错误信息:${e.message}\n正在保存日志", Toast.LENGTH_SHORT).show()
            collectBaseInfo()
            saveErrorInfo(t, e)
            sleep(1500)
            countDownLatch.countDown()
            Looper.loop()
        }.start()
        countDownLatch.await()
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(0)
    }

    private fun collectBaseInfo() {
        //获取包信息
        val packageManager = context?.packageManager
        packageManager?.let {
            try {
                val packageInfo =
                    it.getPackageInfo(context?.packageName ?: "", PackageManager.GET_ACTIVITIES)
                val versionName = packageInfo.versionName
                val versionCode =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode else packageInfo.versionCode
                infoMap["versionName"] = versionName
                infoMap["versionCode"] = versionCode.toString()
                //通过反射获取Build的全部参数
                val fields = Build::class.java.fields
                if (fields.isNotEmpty()) {
                    fields.forEach { field ->
                        field.isAccessible = true
                        field.get(null)?.let { field1 ->
                            infoMap[field.name] = field1.toString()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveErrorInfo(t: Thread, e: Throwable) {
        val stringBuffer = StringBuffer()
        infoMap.forEach { (key, value) ->
            stringBuffer.append("$key == $value\n")
        }
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        //获取到堆栈信息
        e.printStackTrace(printWriter)
        printWriter.close()
        //转换异常信息
        val errorStackInfo = stringWriter.toString()
        stringBuffer.append("Error:\n$errorStackInfo")
        val directoryPath = "${context?.filesDir?.absolutePath}/logs"
        File(directoryPath).takeUnless { it.exists() }
            ?.let { it.takeUnless { it.mkdirs() }?.let { Log.e("异常捕捉", "日志文件创建失败") } }
        val timestamp =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss"))
        val filename = "$directoryPath/$timestamp-error-log.txt"
        File(filename).writeText("Thread:\n${t.name}\nInfo:\n$stringBuffer")
    }


}