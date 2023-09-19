package com.dokiwei.basemvvm.util

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler

/**
 * @author DokiWei
 * @date 2023/9/16 20:36
 */
object MyCoroutineExceptionHandler {
    val handler = CoroutineExceptionHandler { _, e->
        Log.e("协程异常捕捉", "error:${e.message}")
    }
}
